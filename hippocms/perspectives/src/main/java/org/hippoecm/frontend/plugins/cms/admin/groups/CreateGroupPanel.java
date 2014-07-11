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
package org.hippoecm.frontend.plugins.cms.admin.groups;

import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;
import org.hippoecm.frontend.plugins.cms.admin.AdminBreadCrumbPanel;
import org.hippoecm.frontend.session.UserSession;
import org.onehippo.cms7.event.HippoEvent;
import org.onehippo.cms7.event.HippoEventConstants;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.cms7.services.eventbus.HippoEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateGroupPanel extends AdminBreadCrumbPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(CreateGroupPanel.class);

    private final Form form;

    private DetachableGroup groupModel = new DetachableGroup();

    public CreateGroupPanel(final String id, final IBreadCrumbModel breadCrumbModel) {
        super(id, breadCrumbModel);
        setOutputMarkupId(true);
        
        // add form with markup id setter so it can be updated via ajax
        form = new Form("form", new CompoundPropertyModel(groupModel));
        form.setOutputMarkupId(true);
        add(form);

        FormComponent fc;
        fc = new RequiredTextField("groupname");
        fc.add(StringValidator.minimumLength(2));
        fc.add(new GroupValidator());
        form.add(fc);
        
        form.add(new TextField("description"));

        form.add(new AjaxButton("create-button", form) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                Group group = groupModel.getGroup();
                String groupname = group.getGroupname();
                try {
                    group.create();
                    HippoEventBus eventBus = HippoServiceRegistry.getService(HippoEventBus.class);
                    if (eventBus != null) {
                        UserSession userSession = UserSession.get();
                        HippoEvent event = new HippoEvent(userSession.getApplicationName())
                                .user(userSession.getJcrSession().getUserID())
                                .action("create-group")
                                .category(HippoEventConstants.CATEGORY_GROUP_MANAGEMENT)
                                .message("added group " + groupname);
                        eventBus.post(event);
                    }
                    Session.get().info(getString("group-created", groupModel));
                    // one up
                    List<IBreadCrumbParticipant> l = breadCrumbModel.allBreadCrumbParticipants();
                    breadCrumbModel.setActive(l.get(l.size() -2));
                } catch (RepositoryException e) {
                    Session.get().warn(getString("group-create-failed", groupModel));
                    log.error("Unable to create group '" + groupname + "' : ", e);
                }
            }
            @Override
            protected void onError(AjaxRequestTarget target, Form form) {
                // make sure the feedback panel is shown
                target.add(CreateGroupPanel.this);
            }
        });

        // add a button that can be used to submit the form via ajax
        form.add(new AjaxButton("cancel-button") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                // one up
                List<IBreadCrumbParticipant> l = breadCrumbModel.allBreadCrumbParticipants();
                breadCrumbModel.setActive(l.get(l.size() -2));
            }
        }.setDefaultFormProcessing(false));
    }

    class GroupValidator extends StringValidator {
        private static final long serialVersionUID = 1L;

        @Override
        public void validate(IValidatable validatable) {
            super.validate(validatable);

            String groupname = (String) validatable.getValue();
            if (Group.exists(groupname)) {
                validatable.error(new ValidationError(this, "GroupnameValidator.exists"));
            }
        }
    }

    public IModel<String> getTitle(Component component) {
        return new StringResourceModel("group-create", component, null);
    }

}
