/**
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.onehippo.gogreen.importer;

import java.io.File;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Simple plain wikipedia to jcr Node. 
 *
 * Note that this is <strong>not</strong> production code! Real code should use workflow instead of the low-level jcr calls in this class.
 * This just serves to import many wiki documents as efficient as possible
 *
 *
 */
public class NonWorkflowWikiImporterComponent extends BaseHstComponent {

    public static final Logger log = LoggerFactory.getLogger(NonWorkflowWikiImporterComponent.class);

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {
        request.setAttribute("message", request.getParameter("message"));
    }

    @Override
    public void doAction(HstRequest request, HstResponse response) throws HstComponentException {

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser;
        String numberStr = request.getParameter("number");
        int numberOfWikiDocs;
        try {
            numberOfWikiDocs = Integer.parseInt(numberStr);
        } catch (NumberFormatException e) {
            response.setRenderParameter("message", "number must be a number but was '"+numberStr+"'");
            return;
        }

        if(numberOfWikiDocs <= 0) {
            response.setRenderParameter("message", "number must be a number larger than 0 but was '"+numberStr+"'");
            return;
        }

        String offsetStr = request.getParameter("offset");
        int offset = 0;
        if (StringUtils.isNotBlank(offsetStr)) {
            try {
                offset = Integer.parseInt(offsetStr);
                if (offset < 0) {
                    offset = 0;
                }
            } catch (NumberFormatException e) {
                offset = 0;
            }
        }

        String wikiContentFileSystem = request.getParameter("filesystemLocation");

        if(StringUtils.isEmpty(wikiContentFileSystem)) {
            throw new  IllegalArgumentException("filesystemLocation should be correct");
        }

        String type = request.getParameter("type");
        if (type == null || !(type.equals("news") || type.equals("products"))) {
            response.setRenderParameter("message", "Invalid type. Must be 'news' or 'products' but was '"+type+"'");
            throw new IllegalArgumentException("type should be set");
        }

        try {
            parser = factory.newSAXParser();
            File f = new File(wikiContentFileSystem);
            WikiPediaToJCRHandler handler = null;
            long start = System.currentTimeMillis();
            try {

                Session writableSession = this.getPersistableSession(request);
                Node baseNode = writableSession.getNode(request.getRequestContext().getSiteContentBaseBean().getPath());

                Node wikiFolder;

                if(!baseNode.hasNode(type)) {
                    wikiFolder = baseNode.addNode(type, "hippostd:folder");
                    wikiFolder.addMixin("mix:referenceable");
                } else {
                    wikiFolder = baseNode.getNode(type);
                }
                handler = new WikiPediaToJCRHandler(wikiFolder, numberOfWikiDocs, offset, type);
                parser.parse(f, handler);

            }catch (ForcedStopException e) {
                // succesfull handler quits after numberOfWikiDocs has been achieved
            }catch (Exception e) {
                log.warn("Exception during importing wikipedia docs", e);
                response.setRenderParameter("message", "An exception happened. Did not import wiki docs. " + e.toString());
            }
            response.setRenderParameter("message", "Successfully imported '"+handler.count+"' wikipedia documents in '"+(System.currentTimeMillis() - start)+"' ms.");
        } catch (ParserConfigurationException | SAXException e) {
            response.setRenderParameter("message", "Did not import wiki: " + e.toString());
        }

    }

    class WikiPediaToJCRHandler extends DefaultHandler {

        private Node wikiFolder;
        private Node doc;
        private Node finishedDoc;
        private Node currentFolder;
        private Node currentSubFolder;
        private int numberOfSubFolders = 1;
        private String type;
        private int total;
        private int offset;
        private int offsetcount = 0;
        private StringBuilder fieldText = new StringBuilder();
        private boolean recording = false;
        int count = 0;
        int maxDocsPerFolder = 100;
        int maxSubFolders = 100;
        long startTime = 0;
        private final String[] users = {"ard","bard","arje","artur","reijn","berry","frank","mathijs","junaid","ate","tjeerd","verberg","simon","jannis"};
        private final String[] newsCaterogies = {"Solar",
                "Global Warming",
                "Energy",
                "Automotive",
                "Climate Change",
                "Electricity",
                "Environmental",
                "Architect",
                "Business",
                "Green",
                "Hydrogen",
                "Recycling",
                "Biology",
                "Biosphere Technology",
                "Conference",
                "Nano Technology",
                "Politics"};

        private final String[] productCategories = {"Food",
                "Solar",
                "Animals",
                "Garden",
                "Gadgets",
                "Health Care",
                "Clothing",
                "Transport"};

        private Random rand;

        private static final int NUMBER_OF_SECONDS_IN_TWO_YEARS = 63072000;

        public WikiPediaToJCRHandler(Node wikiFolder, int total, final int offset, String type) throws Exception {
            this.wikiFolder = wikiFolder;
            this.total = total;
            this.offset = offset;
            this.type = type;
            currentFolder = wikiFolder.addNode("wiki-" + type + "-" + System.currentTimeMillis(), "hippostd:folder");
            currentFolder.addMixin("mix:referenceable");
            currentSubFolder = currentFolder.addNode("wiki-" + System.currentTimeMillis(), "hippostd:folder");
            currentSubFolder.addMixin("mix:referenceable");
            rand = new Random(System.currentTimeMillis());
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
            if (qName.equals("page")) {
                if (offsetcount < offset) {
                    offsetcount++;
                    if ((offsetcount % maxDocsPerFolder) == 0) {
                        System.out.println("Offset '" + offset + "' not yet reached. Currently at '" + offsetcount
                                + "'");
                    }
                }

                if (offsetcount == offset) {
                    try {
                        count++;
                        if (count >= total) {
                            System.out.println(total);
                            wikiFolder.getSession().save();

                            System.out.println("Total added wiki docs = " + count + ". It took " + (System.currentTimeMillis() - startTime) + " ms.");
                            throw new ForcedStopException();
                        }
                        if ((count % maxDocsPerFolder) == 0) {
                            wikiFolder.getSession().save();
                            if(numberOfSubFolders >= maxSubFolders) {
                                currentFolder = wikiFolder.addNode("wiki-" + type + "-" + System.currentTimeMillis(), "hippostd:folder");
                                currentFolder.addMixin("mix:referenceable");
                                numberOfSubFolders = 0;
                            }
                            currentSubFolder = currentFolder.addNode("wiki-" + System.currentTimeMillis(), "hippostd:folder");
                            currentSubFolder.addMixin("mix:referenceable");
                            numberOfSubFolders++;
                            System.out.println("Counter = " + count);
                        }
                        Calendar cal = Calendar.getInstance();

                        int creationDateSecondsAgo = new Random().nextInt(NUMBER_OF_SECONDS_IN_TWO_YEARS);
                        // lastModifiedSecondsAgo = some random time after creationDateSecondsAgo
                        int lastModifiedSecondsAgo = new Random().nextInt(creationDateSecondsAgo);
                        // publicaionDateSecondsAgo = some random time after lastModifiedSecondsAgo
                        int publicaionDateSecondsAgo = new Random().nextInt(lastModifiedSecondsAgo);

                        final Calendar creationDate = Calendar.getInstance();
                        creationDate.add(Calendar.SECOND, (-1 * creationDateSecondsAgo));
                        final Calendar lastModificationDate = Calendar.getInstance();
                        lastModificationDate.add(Calendar.SECOND, (-1 * lastModifiedSecondsAgo));
                        final Calendar publicationDate = Calendar.getInstance();
                        publicationDate.add(Calendar.SECOND, (-1 * publicaionDateSecondsAgo));


                        String docName = "doc-" + cal.getTimeInMillis();
                        Node handle;

                        handle = currentSubFolder.addNode(docName, "hippo:handle");
                        handle.addMixin("mix:referenceable");
                        if ("news".equals(type)) {
                            doc = handle.addNode(docName, "hippogogreen:newsitem");
                            doc.setProperty("hippogogreen:categories", new String[]{newsCaterogies[rand.nextInt(newsCaterogies.length)],
                                    newsCaterogies[rand.nextInt(newsCaterogies.length)]});
                            doc.setProperty("hippogogreen:date", publicationDate);
                        } else {
                            doc = handle.addNode(docName, "hippogogreen:product");
                            doc.setProperty("hippogogreen:categories", new String[]{productCategories[rand.nextInt(productCategories.length)],
                                    productCategories[rand.nextInt(productCategories.length)]});
                            doc.setProperty("hippogogreen:rating", (double)rand.nextInt(6));
                            doc.setProperty("hippogogreen:price", (double) rand.nextInt(1000));
                        }

                        doc.addMixin("mix:referenceable");
                        String[] availability = {"live", "preview"};
                        doc.setProperty("hippo:availability", availability );
                        doc.setProperty("hippostd:stateSummary", "live");
                        doc.setProperty("hippostd:state", "published");
                        doc.setProperty("hippostdpubwf:lastModifiedBy", users[rand.nextInt(users.length)]);
                        doc.setProperty("hippostdpubwf:createdBy", users[rand.nextInt(users.length)]);
                        doc.setProperty("hippostdpubwf:lastModificationDate",  lastModificationDate);
                        doc.setProperty("hippostdpubwf:creationDate", creationDate);
                        doc.setProperty("hippostdpubwf:publicationDate", publicationDate);

                        doc.setProperty("hippotranslation:locale", "en");
                        doc.setProperty("hippotranslation:id", UUID.randomUUID().toString());


                    } catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (qName.equals("title")) {
                if (offsetcount == offset) {
                    recording = true;
                }
            }

            if (qName.equals("text")) {
                if (offsetcount == offset) {
                    recording = true;
                }
            }

            super.startElement(uri, localName, qName, attributes);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (offsetcount == offset) {
                if (qName.equals("page")) {
                    checkCorrectDoc();
                    finishedDoc = doc;
                } else if (qName.equals("title") && recording) {
                    checkCorrectDoc();
                    try {
                        doc.setProperty("hippogogreen:title", fieldText.toString().trim());
                        fieldText = new StringBuilder();
                    } catch (RepositoryException e) {
                        throw new SAXException(e);
                    }
                } else if (qName.equals("text") && recording) {
                    checkCorrectDoc();
                    try {
                        String text = fieldText.toString().trim();
                        if(text.length() > 100) {
                            doc.setProperty("hippogogreen:summary", text.substring(0,100));
                        } else {
                            doc.setProperty("hippogogreen:summary", text);
                        }
                        Node body = doc.addNode("hippogogreen:description","hippostd:html");
                        body.setProperty("hippostd:content", text);
                        fieldText = new StringBuilder();
                    } catch (RepositoryException e) {
                        throw new SAXException(e);
                    }
                }
            }
            super.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (recording) {
                fieldText.append(ch, start, length);
            }
        }

        private void checkCorrectDoc() throws SAXException {
            if (doc == finishedDoc) {
                throw new SAXException("Doc is same instance as finished doc. This should never happen");
            }
        }
    }

    class ForcedStopException extends RuntimeException {
        private static final long serialVersionUID = 1L;

    }

}
