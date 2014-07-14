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

package com.onehippo.gogreen.components.events;

import com.onehippo.gogreen.beans.EventDocument;
import com.onehippo.gogreen.components.BaseComponent;
import com.onehippo.gogreen.components.ComponentUtil;
import com.onehippo.gogreen.utils.Constants;
import com.onehippo.gogreen.utils.GoGreenUtil;
import com.onehippo.gogreen.utils.PageableCollection;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.query.filter.Filter;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoDocumentIterator;
import org.hippoecm.hst.content.beans.standard.HippoFacetChildNavigationBean;
import org.hippoecm.hst.content.beans.standard.HippoFacetNavigationBean;
import org.hippoecm.hst.content.beans.standard.HippoResultSetBean;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.util.ContentBeanUtils;
import org.hippoecm.hst.util.SearchInputParsingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Overview extends BaseComponent {

    private static final Logger log = LoggerFactory.getLogger(Overview.class);
    private static final String PARAM_PAGE_SIZE = "pageSize";
    private static final int DEFAULT_PAGE_SIZE = 5;
    private static final String PARAM_CURRENT_PAGE = "pageNumber";
    private static final int DEFAULT_CURRENT_PAGE = 1;
    private static final String PARAM_ORDER_BY = "orderBy";
    private static final String DEFAULT_ORDER_BY = "hippogogreen:date";
    private static final String PARAM_SORT_ORDER = "sortOrder";
    private static final String DEFAULT_SORT_ORDER = "ascending";
    private static final int DEFAULT_SHOW_MORE = 25;

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) {
        super.doBeforeRender(request, response);
        final HstRequestContext ctx = request.getRequestContext();
        HippoBean currentBean = ctx.getContentBean();
        if (currentBean == null) {
            return;
        }

        request.setAttribute("defaultShowMore", DEFAULT_SHOW_MORE);
        
        String pageSizeParam = getPublicRequestParameter(request, PARAM_PAGE_SIZE);
        if (pageSizeParam == null || "".equals(pageSizeParam)) {
            pageSizeParam = getComponentParameter(PARAM_PAGE_SIZE);
        }
        int pageSize = ComponentUtil.parseIntParameter(PARAM_PAGE_SIZE, pageSizeParam, DEFAULT_PAGE_SIZE, log);
        request.setAttribute("pageSize", pageSize);

        String currentPageParam = getPublicRequestParameter(request, PARAM_CURRENT_PAGE);
        int currentPage = ComponentUtil.parseIntParameter(PARAM_CURRENT_PAGE, currentPageParam, DEFAULT_CURRENT_PAGE, log);

        String orderBy = getComponentParameter(PARAM_ORDER_BY);
        if (orderBy == null || "".equals(orderBy)) {
            orderBy = DEFAULT_ORDER_BY;
        }

        String sortOrder = getComponentParameter(PARAM_SORT_ORDER);
        if (sortOrder == null || "".equals(sortOrder)) {
            sortOrder = DEFAULT_SORT_ORDER;
        }

        String query = this.getPublicRequestParameter(request, "query");
        query = SearchInputParsingUtils.parse(query, false);
        
        request.setAttribute("query", StringEscapeUtils.escapeHtml(query));

        String order = this.getPublicRequestParameter(request, "order");
        request.setAttribute("order", StringEscapeUtils.escapeHtml(order));
        
        String from = this.getPublicRequestParameter(request, "from");
        String jsEnabled = getPublicRequestParameter(request, "jsEnabled");
        
        try {

            HstQuery hstQuery = ctx.getQueryManager().createQuery(ctx.getSiteContentBaseBean(), EventDocument.class);
            if(!StringUtils.isEmpty(query)) {
                Filter f = hstQuery.createFilter();
                Filter f1 = hstQuery.createFilter();
                f1.addContains(".", query);
                Filter f2 = hstQuery.createFilter();
                f2.addContains("hippogogreen:title", query);
                f.addOrFilter(f1);
                f.addOrFilter(f2);
                hstQuery.setFilter(f);
            }
            if (!StringUtils.isEmpty(order) && !"relevance".equals(order)) {
                if ("-lastModificationDate".equals(order)) {
                    hstQuery.addOrderByDescending("hippostdpubwf:lastModificationDate");
                }
                else if (order.startsWith("-")) {
                    hstQuery.addOrderByDescending("hippogogreen:" + order.substring(1));
                }
                else {
                    hstQuery.addOrderByAscending("hippogogreen:" + order);
                }
            }
            else if ("descending".equals(sortOrder)){
                hstQuery.addOrderByDescending(orderBy);
            }
            else {
                hstQuery.addOrderByAscending(orderBy);
            }
            if (from != null && Boolean.parseBoolean(jsEnabled)) {
                hstQuery.setOffset(Integer.valueOf(from));
            }
            
            if (!(currentBean instanceof HippoFacetChildNavigationBean)) {

                HstQueryResult result = hstQuery.execute();
                request.setAttribute("docs", new PageableCollection<HippoBean>(
                        result.getHippoBeans(), pageSize, currentPage));
                request.setAttribute("count", result.getSize());
            } else {
                
                HippoFacetNavigationBean facNavBean = ContentBeanUtils.getFacetNavigationBean(hstQuery);
    
                HippoResultSetBean resultSet = facNavBean.getResultSet();
                // when there are 0 results, the resultset is null
                if(resultSet == null) {
                    return;
                }
                HippoDocumentIterator<EventDocument> beans = resultSet.getDocumentIterator(EventDocument.class);
                if(hstQuery.getOffset()>0){
                    beans.skip(hstQuery.getOffset());
                }
                
                PageableCollection collection = 
                    new PageableCollection(beans, 
                            facNavBean.getCount().intValue(), 
                            GoGreenUtil.getIntConfigurationParameter(request, 
                                Constants.PAGE_SIZE, pageSize), currentPage); 
                request.setAttribute("docs", collection);
                request.setAttribute("count", resultSet.getCount());
            }

        } catch (QueryException qe) {
            log.error("Error while getting the documents " + qe.getMessage(), qe);
        }

    }

}
