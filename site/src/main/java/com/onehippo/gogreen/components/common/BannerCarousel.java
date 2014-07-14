/*
 * Copyright 2011-2013 Hippo B.V. (http://www.onehippo.com)
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

import com.onehippo.gogreen.beans.Banner;
import com.onehippo.gogreen.components.BaseComponent;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.parameters.ParametersInfo;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Banner Carousel component for Home Page
 */
@ParametersInfo(type = BannerCarouselParamsInfo.class)
public class BannerCarousel extends BaseComponent {

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) {
        super.doBeforeRender(request, response);
        final HstRequestContext ctx = request.getRequestContext();
        BannerCarouselParamsInfo paramsInfo = getComponentParametersInfo(request);
        List<Banner> banners = new ArrayList<Banner>();
        HippoBean siteContentBaseBean = ctx.getSiteContentBaseBean();
        
        Banner banner = null;
        if(!StringUtils.isEmpty(paramsInfo.getBanner1())) {
            banner = siteContentBaseBean.getBean(paramsInfo.getBanner1());
            if(banner != null) {
                banners.add(banner);
            }
        }
        if(!StringUtils.isEmpty(paramsInfo.getBanner2())) {
            banner = siteContentBaseBean.getBean(paramsInfo.getBanner2());
            if(banner != null) {
                banners.add(banner);
            }
        }
        if(!StringUtils.isEmpty(paramsInfo.getBanner3())) {
            banner = siteContentBaseBean.getBean(paramsInfo.getBanner3());
            if(banner != null) {
                banners.add(banner);
            }
        }
        if(!StringUtils.isEmpty(paramsInfo.getBanner4())) {
            banner = siteContentBaseBean.getBean(paramsInfo.getBanner4());
            if(banner != null) {
                banners.add(banner);
            }
        }
        if(!StringUtils.isEmpty(paramsInfo.getBanner5())) {
            banner = siteContentBaseBean.getBean(paramsInfo.getBanner5());
            if(banner != null) {
                banners.add(banner);
            }
        }

        request.setAttribute("banners", banners);
    }
}
