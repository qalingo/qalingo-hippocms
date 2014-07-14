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

package com.onehippo.gogreen.components;

import com.onehippo.gogreen.utils.FeedFetcher;
import com.sun.syndication.feed.synd.SyndFeed;

import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.parameters.ParametersInfo;

@ParametersInfo(type = FeedParamsInfo.class)
public class Feed extends BaseComponent {

    private static final FeedFetcher feedFetcher = new FeedFetcher();

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) {
        super.doBeforeRender(request, response);

        // get parameters
        FeedParamsInfo paramsInfo = getComponentParametersInfo(request);
        request.setAttribute("title", paramsInfo.getTitle());
        request.setAttribute("numberOfItems", paramsInfo.getNumberOfItems());

        String feedUrl = paramsInfo.getFeedUrl();
        int updateInterval = paramsInfo.getUpdateInterval();
        int connectTimeout = paramsInfo.getConnectTimeout();
        int readTimeout = paramsInfo.getReadTimeout();

        // get feed
        SyndFeed feed = feedFetcher.retrieveFeed(feedUrl, updateInterval, connectTimeout, readTimeout);
        request.setAttribute("feed", feed);
        request.setAttribute("feedUrl", feedUrl);
    }

}
