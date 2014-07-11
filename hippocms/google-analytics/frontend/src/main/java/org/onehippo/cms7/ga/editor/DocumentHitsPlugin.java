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
package org.onehippo.cms7.ga.editor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.cms7.services.googleanalytics.GoogleAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DocumentHitsPlugin extends RenderPlugin<Node> {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(DocumentHitsPlugin.class);    
    private static final double RANGE = 62.0;

    private enum Period {

        DAYS("ga:nthDay"),
        WEEKS("ga:nthWeek"),
        MONTHS("ga:nthMonth");
        
        private final String dimension;
        
        private Period(String dimension) {
            this.dimension = dimension;
        }
        
        private String getDimension() {
            return dimension;
        }
        
        private static Period fromString(String s) {
            if (s.equalsIgnoreCase("days")) {
                return DAYS;
            }
            if (s.equalsIgnoreCase("weeks")) {
                return WEEKS;
            }
            if (s.equalsIgnoreCase("months")) {
                return MONTHS;
            }
            log.warn("Invalid configuration value for property period: '" + s + "'. Defaulting to period 'days'.");
            return DAYS;
        }
        
        private static String getI18CaptionKey(Period period) {
            switch(period) {
            case DAYS: return "period.days.caption";
            case WEEKS: return "period.weeks.caption";
            case MONTHS: return "period.months.caption";
            }
            return null;
        }
        
        private static String getI18InfoKey(Period period) {
            switch(period){
            case DAYS: return "period.days.info";
            case WEEKS: return "period.weeks.info";
            case MONTHS: return "period.months.info";
            }
            return null;
        }
                
    }
    
    // configuration
    private Long numberofintervals;
    private Period period;

    // variables for internal processing
    private List<Long> pageViewsList;
    private Long maxPageViews = -1L;
    private Long mostRecentPageViews = -1L;
    private boolean loaded = false;
    private boolean success = true;
    private String errorMessage;
    
    public DocumentHitsPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);
        
        this.numberofintervals = config.getAsLong("numberofintervals", 10);
        this.period = Period.fromString(config.getString("period", "weeks"));
        
        // We lazy load the google analytics widget using ajax
        AjaxLazyLoadPanel panel = new AjaxLazyLoadPanel("ajax-lazy-load-panel") {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getLazyLoadComponent(String markupId) {
                Fragment fragment = new Fragment(markupId, "lazy-load-document-hits-container", DocumentHitsPlugin.this);
                
                Image image = new Image("document-hits-image", getGraphUrl());
                image.add(new AttributeModifier("title", getGraphInfo()));
                fragment.add(image);
                Label label = new Label("document-hits-label", getGraphLabel());
                fragment.add(label);
                return fragment;
            }
        };
        
        add(panel);

    }
    
    private String getGraphLabel() {
        if (!loaded) {
            loadGraphData();
        }
        
        if (!success) {
            return errorMessage;
        }

        return getTranslation(Period.getI18CaptionKey(period), new Model<String[]>(new String[] { mostRecentPageViews.toString() }));
    }
    
    private String getGraphInfo() {
        return getTranslation(Period.getI18InfoKey(period), new Model<String[]>(new String[]{numberofintervals.toString()}));
    }

    private String getGraphUrl() {

        if (!loaded) {
            loadGraphData();
        }
        
        if (!success) {
            return "empty.gif";
        }
        
        StringBuilder graphData = new StringBuilder();
        // the graph has a range of 62: A-Z, a-z, and 0-9 (26+26+10=62) where 'A' represents the y axis minimum
        // and '9' its maximum. We need space for the number of page views plus one to accommodate zero page views
        double value = (RANGE / (maxPageViews+1));
        
        for (Long pageViews : pageViewsList) {
            // yPos is a number between 0 and 61 that locates the number of page views
            // on the scale
            double yPos = Math.floor(pageViews*value);
            // now represent this number as a char
            char nextChar;
            // 0-9
            if (yPos > 51) {
                nextChar = (char) (yPos - 52 + (int)'0');
            }
            // a-z
            else if (yPos > 25) {
                nextChar = (char) (yPos - 26 + (int)'a');
            }
            // A-Z
            else {
                nextChar = (char) (yPos + (int)'A');
            }
            graphData.append(nextChar);
        }
        
        
        return getScheme() + "://chart.googleapis.com/chart?cht=ls&amp;chs=75x18&amp;chco=0077cc&amp;chm=B%2Ce6f2fa%2C0%2C0.0%2C0.0&amp;chd=s%3A" + graphData.toString();
    }
    
    private void loadGraphData() {
        loaded = true;
        success = false;
        try {
            // Create Google analytics query
            DataQuery query = new DataQuery(new URL("https://www.google.com/analytics/feeds/data"));
            query.setIds(getTableId());
            query.setDimensions(period.getDimension());
            query.setMetrics("ga:pageviews");
            query.setFilters("ga:pagePath==" + getParentNodePath());
            query.setStartDate(getStartDate());
            query.setEndDate(getEndDate());
            
            if (log.isDebugEnabled()) {
                log.debug("Querying google analytics for feed with url: " + query.getUrl());
            }
            
            // Execute the query
            DataFeed dataFeed = getAnalyticsService().getFeed(query.getUrl(), DataFeed.class);
            
            // Gather the results
            pageViewsList = new LinkedList<Long>();
            for (DataEntry entry : dataFeed.getEntries()) {
                Long pageViews = entry.longValueOf("ga:pageviews");
                pageViewsList.add(pageViews);
                // remember the maximum number of page views in the data set
                if (maxPageViews < pageViews) {
                    maxPageViews = pageViews;
                }
                // remember the last number of page views in the data set
                mostRecentPageViews = pageViews;
            }
            success = true;
        } catch (MalformedURLException e) {
            errorMessage = e.getMessage();
            if (log.isDebugEnabled()) {
                log.warn("Failed to load google analytics graph data", e);
            } else {
                log.warn("Failed to load google analytics graph data: " + e.getClass().getName() + ": " + e.getMessage());
            }
        } catch (RepositoryException e) {
            errorMessage = e.getMessage();
            if (log.isDebugEnabled()) {
                log.warn("Failed to load google analytics graph data", e);
            } else {
                log.warn("Failed to load google analytics graph data: " + e.getClass().getName() + ": " + e.getMessage());
            }
        } catch (AuthenticationException e) {
            errorMessage = e.getMessage();
            if (log.isDebugEnabled()) {
                log.warn("Failed to load google analytics graph data", e);
            } else {
                log.warn("Failed to load google analytics graph data: " + e.getClass().getName() + ": " + e.getMessage());
            }
        } catch (IOException e) {
            errorMessage = e.getMessage();
            if (log.isDebugEnabled()) {
                log.warn("Failed to load google analytics graph data", e);
            } else {
                log.warn("Failed to load google analytics graph data: " + e.getClass().getName() + ": " + e.getMessage());
            }
        } catch (ServiceException e) {
            errorMessage = e.getMessage();
            if (log.isDebugEnabled()) {
                log.warn("Failed to load google analytics graph data", e);
            } else {
                log.warn("Failed to load google analytics graph data: " + e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }
    
    private String getStartDate() {
        Calendar cal = Calendar.getInstance();
        if (period == Period.DAYS) {
            // google analytics data availability has a latency of
            // 24 hours, therefore we shift the number of days
            cal.add(Calendar.DATE, (int) -numberofintervals -1);
        }
        else if (period == Period.WEEKS) {
            // when querying for page views per week, we must make sure
            // the first week is a whole week (starting on Sunday)
            cal.add(Calendar.DATE, (int) -(numberofintervals*7));
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        }
        else {
            // when querying for page views per month, we must make sure
            // the first month is a whole month (starting on the first)
            cal.add(Calendar.MONTH, (int) -numberofintervals);
            cal.set(Calendar.DAY_OF_MONTH, 1);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(cal.getTime());
    }

    private String getEndDate() {
        Calendar cal = Calendar.getInstance();
        // Because google analytics data availability has a latency of
        // 24 hours our end date is yesterday
        cal.add(Calendar.DATE, -1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(cal.getTime());
    }
    
    private AnalyticsService getAnalyticsService() throws AuthenticationException {
        AnalyticsService analyticsService = new AnalyticsService("hippocms7_reporting_v1");
        // Client Login Authorization.
        analyticsService.setUserCredentials(getUserName(), getPassword());
        return analyticsService;
    }

    private String getTableId() {
        final GoogleAnalyticsService service = HippoServiceRegistry.getService(GoogleAnalyticsService.class);
        return service != null ? service.getTableId() : null;
    }

    private String getUserName() {
        final GoogleAnalyticsService service = HippoServiceRegistry.getService(GoogleAnalyticsService.class);
        return service != null ? service.getUserName() : null;
    }

    private String getPassword() {
        final GoogleAnalyticsService service = HippoServiceRegistry.getService(GoogleAnalyticsService.class);
        return service != null ? service.getPassword() : null;
    }

    private String getParentNodePath() throws RepositoryException {
        Node node = getModelObject();
        return node.getParent().getPath();
    }
    
    private String getScheme() {
        return ((ServletWebRequest) getRequest()).getContainerRequest().getScheme();
    }
    
    private String getTranslation(String key, Model<String[]> model) {
        return new StringResourceModel(key, this, model, "").getObject();
    }
}
