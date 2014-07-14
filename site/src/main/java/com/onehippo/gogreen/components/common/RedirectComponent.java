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

import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onehippo.gogreen.components.BaseComponent;

public class RedirectComponent extends BaseComponent {

    private static final String REDIRECT_PARAM = "redirect";
    private static final Logger log = LoggerFactory.getLogger(RedirectComponent.class);

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) {

        final String redirect = getComponentParameter(REDIRECT_PARAM);
        log.debug("redirect param is " + redirect);
        if (StringUtils.isEmpty(redirect)) {
            throw new HstComponentException("Parameter '" + REDIRECT_PARAM + "' is required for " + this.getClass().getName());
        }
        this.sendRedirect(redirect, request, response);
    }

}
