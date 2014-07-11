/*
 *  Copyright 2008-2013 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.plugins.yui.datetime;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.util.MappingException;
import org.hippoecm.frontend.util.PluginConfigMapper;

public class DateFieldWidget extends Panel {

    private static final long serialVersionUID = 1L;

    public DateFieldWidget(String id, IModel<Date> model, IPluginContext context, IPluginConfig config) {
        super(id, model);
        YuiDatePickerSettings settings = new YuiDatePickerSettings();
        if (config.containsKey("datepicker")) {
            try {
                PluginConfigMapper.populate(settings, config.getPluginConfig("datepicker"));
            } catch (MappingException e) {
                throw new RuntimeException(e);
            }
        }
        YuiDateTimeField dateTimeField = new YuiDateTimeField("widget", model, settings);
        boolean todayLinkVisible = config.getAsBoolean("show.today.button", true);
        dateTimeField.setTodayLinkVisible(todayLinkVisible);
        add(dateTimeField);
    }
}
