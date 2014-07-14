/**
 * Copyright 2010-2013 Hippo B.V. (http://www.onehippo.com)
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

package com.onehippo.gogreen.components.products;

import java.util.ArrayList;
import java.util.List;

import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.parameters.ParametersInfo;
import org.hippoecm.hst.core.request.HstRequestContext;

import com.onehippo.gogreen.beans.Product;
import com.onehippo.gogreen.components.BaseComponent;

@ParametersInfo(type = FeaturedProductsParamInfo.class)
public class FeaturedProducts extends BaseComponent {

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) {
        super.doBeforeRender(request, response);
        final HstRequestContext ctx = request.getRequestContext();
        FeaturedProductsParamInfo paramInfo = getComponentParametersInfo(request);
        List<Product> products = new ArrayList<Product>();
        final HippoBean siteContentBaseBean = ctx.getSiteContentBaseBean();
        products.add(siteContentBaseBean.<Product>getBean(paramInfo.getProduct1()));
        products.add(siteContentBaseBean.<Product>getBean(paramInfo.getProduct2()));
        products.add(siteContentBaseBean.<Product>getBean(paramInfo.getProduct3()));
        request.setAttribute("products", products);
        Boolean isReseller = request.isUserInRole("reseller");
        request.setAttribute("reseller", isReseller);
    }
}

