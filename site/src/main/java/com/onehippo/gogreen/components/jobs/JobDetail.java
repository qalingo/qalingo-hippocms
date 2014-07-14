/*
 * Copyright 2013 Hippo B.V. (http://www.onehippo.com)
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
package com.onehippo.gogreen.components.jobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.onehippo.gogreen.components.Detail;

import org.hippoecm.hst.content.beans.standard.facetnavigation.HippoFacetNavigation;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobDetail extends Detail {

    public static final Logger log = LoggerFactory.getLogger(JobDetail.class);

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) {

        super.doBeforeRender(request, response);
        HippoFacetNavigation facnav = ((HippoFacetNavigation)request.getRequestContext().getSiteContentBaseBean().getBean("jobfacets"));
        List<String> facets = new ArrayList<String>();
        String[] facetArray = facnav.getProperty("hippofacnav:facets");
        Collections.addAll(facets, facetArray);
        int employerIndex = facets.indexOf("hippogogreen:employer");

        if(employerIndex >= 0 && employerIndex < ((String[])facnav.getProperty("hippofacnav:facetnodenames")).length) {
            request.setAttribute("employer", ((String[])facnav.getProperty("hippofacnav:facetnodenames"))[employerIndex]);
        }
        else {
            log.warn("Error occurred during fetching of employer facet translation");
        }

    }
}
