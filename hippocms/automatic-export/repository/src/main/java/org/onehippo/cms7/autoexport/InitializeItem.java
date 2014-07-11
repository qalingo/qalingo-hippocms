/*
 *  Copyright 2011-2013 Hippo B.V. (http://www.onehippo.com)
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.cms7.autoexport;

import java.io.File;
import java.io.IOException;

import javax.jcr.observation.Event;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static org.onehippo.cms7.autoexport.AutoExportModule.log;
import static org.onehippo.cms7.autoexport.Constants.DELTA_URI;
import static org.onehippo.cms7.autoexport.Constants.FILE;
import static org.onehippo.cms7.autoexport.Constants.MERGE;
import static org.onehippo.cms7.autoexport.Constants.NAME;
import static org.onehippo.cms7.autoexport.Constants.QFILE;
import static org.onehippo.cms7.autoexport.Constants.QMERGE;
import static org.onehippo.cms7.autoexport.Constants.QNAME;
import static org.onehippo.cms7.autoexport.Constants.QNODE;
import static org.onehippo.cms7.autoexport.Constants.SV_URI;

final class InitializeItem {
    
    private final String name;
    private final Double sequence;
    private final String contentResource;
    private final String contentRoot;
    private final String nodeTypesResource;
    private final String namespace;
    private final File exportDir;
    private final Module module;
    
    private String contextPath;
    private Boolean isDeltaXML;
    private Boolean enabled;
    private DeltaXML deltaXML;
    
    private boolean contextNodeRemoved = false;
    
    private String stringValue;
    
    InitializeItem(String name, Double sequence, 
            String contentResource, String contentRoot, 
            String contextPath, String nodeTypesResource, 
            String namespace, File exportDir,
            Module module) {
        this.name = name;
        this.sequence = sequence;
        this.contentResource = contentResource;
        this.contentRoot = contentRoot;
        this.contextPath = contextPath;
        this.nodeTypesResource = nodeTypesResource;
        this.namespace = namespace;
        this.exportDir = exportDir;
        this.module = module;
        if (contentResource != null && contentResource.endsWith(".zip")) {
            enabled = false;
        }
    }

    // Constructor for testing
    InitializeItem(String name, Boolean enabled, Module module) {
        this(name, -1d, null, null, null, null, null, null, module);
        this.enabled = enabled;
    }
    
    String getName() {
        return name;
    }

    Double getSequence() {
        return sequence;
    }
    
    String getContentResource() {
        return contentResource;
    }
    
    String getContentRoot() {
        return contentRoot;
    }
    
    String getNodeTypesResource() {
        return nodeTypesResource;
    }
    
    String getNamespace() {
        return namespace;
    }
    
    String getContextPath() {
        if (contextPath == null && contentResource != null) {
            initContentResourceValues();
        }
        return contextPath;
    }
    
    String getContextNodeName() {
        String contextPath = getContextPath();
        if (contextPath != null) {
            int offset = contextPath.lastIndexOf('/');
            return contextPath.substring(offset+1);
        }
        return null;
    }
    
    Module getModule() {
        return module;
    }
    
    void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
    
    boolean isDeltaXML() {
        if (isDeltaXML == null && contentResource != null) {
            initContentResourceValues();
        }
        return isDeltaXML == null ? false : isDeltaXML;
    }
    
    boolean isEnabled() {
        if (enabled == null && contentResource != null) {
            initContentResourceValues();
        }
        if (enabled == null) {
            enabled = true;
        }
        return enabled;
    }
    
    DeltaXML getDeltaXML() {
        return deltaXML;
    }
    
    void handleEvent(ExportEvent event) {
        if (getContentResource() == null) {
            return;
        }
        if (!isEnabled()) {
            return;
        }
        if (isDeltaXML == null) {
            isDeltaXML = !event.getPath().equals(contextPath);
        }
        if (isDeltaXML) {
            if (deltaXML == null) {
                deltaXML = new DeltaXML(contextPath);
            }
            deltaXML.handleEvent(event);
        }
        if (event.getType() == Event.NODE_REMOVED && contextPath.startsWith(event.getPath())) {
            contextNodeRemoved = true;
        }
        else if (event.getType() == Event.NODE_ADDED && contextPath.equals(event.getPath())) {
            contextNodeRemoved = false;
        }
    }
    
    boolean processEvents() {
        if (!isEnabled()) {
            return false;
        }
        if (isDeltaXML()) {
            return deltaXML.processEvents();
        }
        return true;
    }
    
    
    boolean isEmpty() {
        if (isDeltaXML()) {
            return deltaXML == null || deltaXML.isEmpty();
        }
        return contextNodeRemoved;
    }
    
    private void initContentResourceValues() {
        if (contentResource.endsWith(".zip")) {
            return;
        }
        File file = new File(exportDir, contentResource);
        if (!file.exists()) {
            return;
        }
        // context must be read from file, it is the contentroot plus
        // name of the root node in the content xml file
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            Document document = builder.parse(file);

            String contextNodeName = document.getDocumentElement().getAttribute(QNAME);
            contextPath = contentRoot.equals("/") ? "/" + contextNodeName : contentRoot + "/" + contextNodeName;
            
            String directive = document.getDocumentElement().getAttribute(QMERGE);
            isDeltaXML = directive != null && !directive.equals("");
            
            if (isDeltaXML) {
                deltaXML = parseDeltaXML(document);
                if (deltaXML == null) {
                    log.info("Content resource " + contentResource + " uses delta xml semantics that are not supported " +
                            "by automatic export. Changes to the context " + contextPath + " must be exported manually.");
                    enabled = false;
                }
            }
            if (enabled != Boolean.FALSE && containsFileReferenceValues(document)) {
                log.info("Content resource " + contentResource + " uses external file reference values. " +
                        "This is not supported by automatic export. Changes to the context " + contextPath
                        + " must be exported manually.");
                enabled = false;
            }
        } catch (ParserConfigurationException e) {
            log.error("Failed to read content resource " + contentResource + " as xml.", e);
        } catch (SAXException e) {
            log.error("Failed to read content resource " + contentResource + " as xml.", e);
        } catch (IOException e) {
            log.error("Failed to read content resource " + contentResource + " as xml.", e);
        }
    }
    
    private DeltaXML parseDeltaXML(Document document) {
        DeltaXMLInstruction instruction = parseInstructionElement(document.getDocumentElement(), null);
        if (instruction == null) {
            return null;
        }
        return new DeltaXML(contextPath, instruction);
        
    }
    
    private DeltaXMLInstruction parseInstructionElement(Element element, DeltaXMLInstruction parent) {
        
        boolean isNode = element.getTagName().equals(QNODE);
        String name = element.getAttribute(QNAME);
        String directive = element.getAttribute(QMERGE);

        DeltaXMLInstruction instruction;
        if (parent == null) {
            instruction = new DeltaXMLInstruction(isNode, name, directive, contextPath);
        } else {
            instruction = new DeltaXMLInstruction(isNode, name, directive, parent);
        }
        if (instruction.isCombineDirective()) {
            final NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node item = childNodes.item(i);
                if (item instanceof Element) {
                    DeltaXMLInstruction child = parseInstructionElement((Element) item, instruction);
                    if (child == null) {
                        return null;
                    }
                    instruction.addInstruction(child);
                }
            }
        }
        if (instruction.isUnsupportedDirective()) {
            return null;
        }
        return instruction;
    }

    private boolean containsFileReferenceValues(Document document) {
        return containsFileReferenceValues(document.getDocumentElement());
    }

    private boolean containsFileReferenceValues(final Element element) {
        if (element.hasAttribute(QFILE)) {
            return true;
        }
        final NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node item = childNodes.item(i);
            if (item instanceof Element) {
                if (containsFileReferenceValues((Element) item)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (stringValue == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("InitializeItem [name = ");
            sb.append(name);
            sb.append(", sequence = ");
            sb.append(sequence);
            sb.append(", contentResource = ");
            sb.append(contentResource);
            sb.append(", contentRoot = ");
            sb.append(contentRoot);
            sb.append(", nodeTypesResource = ");
            sb.append(nodeTypesResource);
            sb.append(", contextPath = ");
            sb.append(contextPath);
            sb.append(", namespace = ");
            sb.append(namespace);
            sb.append(", isDeltaXML = ");
            sb.append(isDeltaXML);
            sb.append("]");
            stringValue = sb.toString();
        }
        return stringValue;
    }
    
}
