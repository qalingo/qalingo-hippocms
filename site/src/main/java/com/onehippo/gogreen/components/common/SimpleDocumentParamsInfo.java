/*
 * Copyright 2010-2013 Hippo B.V. (http://www.onehippo.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.onehippo.gogreen.components.common;

import org.hippoecm.hst.core.parameters.DocumentLink;
import org.hippoecm.hst.core.parameters.Parameter;

/**
 * HST Component Parameters Info class for SimpleDocument. Used by PageComposer.
 */
public interface SimpleDocumentParamsInfo {
    String PARAM_DOCUMENTLOCATION = "documentlocation";

    @Parameter(name = PARAM_DOCUMENTLOCATION, required = true, displayName = "Document")
    @DocumentLink(docType = "hippogogreen:simpledocument", allowCreation = true, docLocation = "common/simpledocuments")
    String getDocumentLocation();
}
