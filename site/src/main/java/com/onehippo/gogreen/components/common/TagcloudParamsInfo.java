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

import org.hippoecm.hst.core.parameters.Parameter;

/**
 * HST Component Parameters Info class for the Tagcloud. Used by the PageComposer.
 */
public interface TagcloudParamsInfo {
    String PARAM_TAGCLOUDLOCATION = "tagCloudLocation";

    @Parameter(name = PARAM_TAGCLOUDLOCATION, required = true, displayName = "Cloud location", defaultValue = "")
    String getTagcloudLocation();
}
