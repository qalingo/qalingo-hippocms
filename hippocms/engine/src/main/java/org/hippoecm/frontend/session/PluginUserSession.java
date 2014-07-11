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
package org.hippoecm.frontend.session;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.common.util.StringUtils;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hippoecm.frontend.Home;
import org.hippoecm.frontend.Main;
import org.hippoecm.frontend.NoRepositoryAvailablePage;
import org.hippoecm.frontend.PluginApplication;
import org.hippoecm.frontend.model.JcrSessionModel;
import org.hippoecm.frontend.model.UserCredentials;
import org.hippoecm.frontend.observation.FacetRootsObserver;
import org.hippoecm.frontend.observation.JcrObservationManager;
import org.hippoecm.frontend.plugin.IPlugin;
import org.hippoecm.frontend.plugin.config.IPluginConfigService;
import org.hippoecm.frontend.plugin.config.impl.IApplicationFactory;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.api.HippoNode;
import org.hippoecm.repository.api.HippoSession;
import org.hippoecm.repository.api.HippoWorkspace;
import org.hippoecm.repository.api.WorkflowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Wicket {@link org.apache.wicket.Session} that maintains a reference to a JCR {@link javax.jcr.Session}.  It is
 * available to plugins as a threadlocal variable during request processing.
 * <p/>
 * When the Wicket session is no longer referenced, the JCR session model is detached.
 */
public class PluginUserSession extends UserSession {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(UserSession.class);

    private static UserCredentials fallbackCredentials;
    private static final Map<UserSession, JcrSessionReference> jcrSessions = new WeakHashMap<UserSession, JcrSessionReference>();

    private transient Session fallbackSession;
    private final IModel<ClassLoader> classLoader;
    private final IModel<WorkflowManager> workflowManager;
    private transient FacetRootsObserver facetRootsObserver;
    private UserCredentials credentials;
    private Map<String, Integer> pluginComponentCounters = new HashMap<String, Integer>();

    @SuppressWarnings("unused")
    private String sessionId;
    private int pageId;


    public UserCredentials getUserCredentials() {
        return credentials;
    }

    public static void setCredentials(UserCredentials credentials) throws RepositoryException {
        fallbackCredentials = credentials;
    }

    public static PluginUserSession get() {
        return (PluginUserSession) UserSession.get();
    }

    public PluginUserSession(Request request) {
        super(request);

        classLoader = new LoadableDetachableModel<ClassLoader>() {
            private static final long serialVersionUID = 1L;

            @Override
            protected ClassLoader load() {
                Session session = getJcrSessionInternal();
                if (session != null) {
                    try {
                        return ((HippoSession) session).getSessionClassLoader();
                    } catch (RepositoryException ex) {
                        log.error(ex.getClass().getName() + ": " + ex.getMessage(), ex);
                    }
                }
                return null;
            }

        };

        workflowManager = new LoadableDetachableModel<WorkflowManager>() {
            private static final long serialVersionUID = 1L;

            @Override
            protected WorkflowManager load() {
                Session jcrSession = getJcrSessionInternal();
                if (jcrSession != null) {
                    try {
                        HippoWorkspace workspace = (HippoWorkspace) jcrSession.getWorkspace();
                        return workspace.getWorkflowManager();
                    } catch (RepositoryException ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
            }

        };

        //Calling the dirty() method causes this wicket session to be reset in the http session
        //so that it knows that the wicket session has changed (we've just added the jcr session model etc.)
        dirty();
    }

    @Deprecated
    public PluginUserSession(Request request, LoadableDetachableModel<Session> jcrSessionModel) {
        super(request);
        classLoader = new LoadableDetachableModel<ClassLoader>() {
            private static final long serialVersionUID = 1L;

            @Override
            protected ClassLoader load() {
                return null;
            }
        };
        workflowManager = new LoadableDetachableModel<WorkflowManager>() {
            private static final long serialVersionUID = 1L;

            @Override
            protected WorkflowManager load() {
                return null;
            }
        };

        try {
            login(null, jcrSessionModel);
        } catch (LoginException ignore) {
        }

        //Calling the dirty() method causes this wicket session to be reset in the http session
        //so that it knows that the wicket session has changed (we've just added the jcr session model etc.)
        dirty();
    }

    private IModel<Session> getJcrSessionModel() {
        synchronized (jcrSessions) {
            JcrSessionReference ref = jcrSessions.get(this);
            if (ref != null) {
                return ref.jcrSession;
            }
            return null;
        }
    }

    /**
     * Retrieve the JCR {@link javax.jcr.Session} that is bound to the Wicket {@link org.apache.wicket.Session}. This
     * method will throw a RestartResponseException when no JCR session is available.
     */
    public Session getJcrSession() {
        Session session = getJcrSessionInternal();
        if (session == null) {
            Main main = (Main) Application.get();
            if (fallbackCredentials == null) {
                try {
                    main.getRepository(); // side effect of reinitializing fallback credentials
                } catch (RepositoryException ignored) {
                }
            }
            if (fallbackSession == null) {
                try {
                    fallbackSession = JcrSessionModel.login(fallbackCredentials);
                } catch (RepositoryException e) {
                    log.warn("Cannot login fallback session: " + e.getMessage());
                }
            }
            session = fallbackSession;
            if (session == null) {
                main.resetConnection();
                throw new RestartResponseException(NoRepositoryAvailablePage.class);
            }
        } else if (fallbackSession != null) {
            fallbackSession.logout();
            fallbackSession = null;
        }
        return session;
    }

    private Session getJcrSessionInternal() {
        IModel<Session> sessionModel = getJcrSessionModel();
        if (sessionModel != null) {
            Session result = sessionModel.getObject();
            if (result != null && result.isLive()) {
                return result;
            }
        }
        return null;
    }

    /**
     * Release the JCR {@link javax.jcr.Session} that is bound to the Wicket session.  The session model will take care
     * of saving any pending changes.  Event listeners will remain registered and will reregister with a new session.
     */
    public void releaseJcrSession() {
        IModel<Session> sessionModel = getJcrSessionModel();
        if (sessionModel != null) {
            sessionModel.detach();
        }
        classLoader.detach();
        workflowManager.detach();
        facetRootsObserver = null;
    }

    public void login() {
        try {
            UserCredentials userCreds = getUserCredentialsFromRequestAttribute();
            login(userCreds, null);
        } catch (LoginException ignore) {}
    }

    /**
     * {@link #login()} method invokes this method if there's any <code>UserCredentials</code> object from the request.
     * For example, Web SSO Agent can set a UserCredentials for the user as request attribute.
     * @return
     */
    protected UserCredentials getUserCredentialsFromRequestAttribute() {
        HttpServletRequest request = ((HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest());
        return (UserCredentials) request.getAttribute(UserCredentials.class.getName());
    }

    @Override
    public void login(final String username, final String password) throws LoginException {
        UserCredentials credentials = new UserCredentials(username, password);
        this.login(credentials);
    }

    public void login(UserCredentials credentials) throws LoginException {
        login(credentials, null);

        IApplicationFactory factory = getApplicationFactory();
        final IPluginConfigService application = factory.getApplication(getApplicationName());
        if (application != null && !application.isSaveOnExitEnabled()) {
            IModel<Session> sessionModel = getJcrSessionModel();
            if (sessionModel instanceof JcrSessionModel) {
                ((JcrSessionModel) sessionModel).setSaveOnExit(false);
            }
        }
    }

    private void handleLoginException(javax.jcr.LoginException le) throws LoginException {
        final Throwable rootCause = le.getCause();

        if (rootCause instanceof FailedLoginException) {
            throw new LoginException(LoginException.CAUSE.INCORRECT_CREDENTIALS);
        } else if (rootCause instanceof CredentialExpiredException) {
            throw new LoginException(LoginException.CAUSE.PASSWORD_EXPIRED);
        } else if (rootCause instanceof AccountExpiredException) {
            throw new LoginException(LoginException.CAUSE.ACCOUNT_EXPIRED);
        }
    }

    @Deprecated
    public void login(UserCredentials credentials, LoadableDetachableModel<Session> sessionModel) throws LoginException {
        if (sessionModel == null) {
            sessionModel = new JcrSessionModel(credentials);
        }
        classLoader.detach();
        workflowManager.detach();
        facetRootsObserver = null;
        IModel<Session> oldModel = null;
        synchronized (jcrSessions) {
            JcrSessionReference sessionRef = jcrSessions.get(this);
            if (sessionRef != null) {
                oldModel = sessionRef.jcrSession;
            } else {
                sessionRef = new JcrSessionReference(this);
                jcrSessions.put(this, sessionRef);
            }
            sessionRef.jcrSession = sessionModel;
        }

        this.credentials = credentials;
        if (oldModel != null) {
            oldModel.detach();
        }

        if (sessionModel instanceof JcrSessionModel) {
            try {
                ((JcrSessionModel) sessionModel).getSessionObject();
            } catch (javax.jcr.LoginException ex) {
                handleLoginException(ex);
            }
        } else {
            sessionModel.getObject();
        }

        if (credentials == null) {
            pageId = 0;
        } else {
            pageId = 1;
        }
    }

    public void logout() {
        classLoader.detach();
        workflowManager.detach();
        facetRootsObserver = null;

        IModel<Session> oldModel = null;
        synchronized (jcrSessions) {
            JcrSessionReference sessionRef = jcrSessions.get(this);
            if (sessionRef != null) {
                oldModel = sessionRef.jcrSession;
                jcrSessions.remove(this);
            }
        }
        if (oldModel != null) {
            oldModel.detach();
        }
        JcrObservationManager.getInstance().cleanupListeners(this);

        pageId = 0;

        invalidate();
        dirty();
        if (WebApplication.exists()) {
            throw new RestartResponseException(WebApplication.get().getHomePage());
        }
    }

    public Credentials getCredentials() {
        return credentials.getJcrCredentials();
    }

    public int getPageId() {
        return pageId;
    }

    /**
     * Retrieve the JCR session classloader, if available, or null when this is not the case.
     */
    public ClassLoader getClassLoader() {
        return classLoader.getObject();
    }

    /**
     * Retrieve the Hippo workflow manager, if one is available.  When none is, null is returned.
     */
    public WorkflowManager getWorkflowManager() {
        return workflowManager.getObject();
    }

    /**
     * Retrieve the JCR query manager, when one is available.  When none is, null is returned.
     */
    public QueryManager getQueryManager() {
        Session jcrSession = getJcrSessionInternal();
        if (jcrSession != null) {
            try {
                return jcrSession.getWorkspace().getQueryManager();
            } catch (RepositoryException e) {
                log.error(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public ObservationManager getObservationManager() {
        return JcrObservationManager.getInstance();
    }

    public IApplicationFactory getApplicationFactory() {
        return ((Main) Main.get()).getApplicationFactory(getJcrSession());
    }

    /**
     * Retrieve the JCR root node.  Null is returned when no session is available or the root node cannot be obtained
     * from it.
     */
    public HippoNode getRootNode() {
        HippoNode result = null;
        try {
            Session jcrSession = getJcrSessionInternal();
            if (jcrSession != null) {
                result = (HippoNode) jcrSession.getRootNode();
            }
        } catch (RepositoryException e) {
            log.error(e.getMessage());
        }
        return result;
    }

    @Override
    public void detach() {
        if (fallbackSession != null) {
            fallbackSession.logout();
            fallbackSession = null;
        }

        JcrSessionReference.cleanup();
        super.detach();
    }

    public void flush() {
        JcrObservationManager.getInstance().cleanupListeners(this);
        clear();
    }

    @Override
    public void onInvalidate() {
        releaseJcrSession();

        JcrObservationManager.getInstance().cleanupListeners(this);
        JcrSessionReference.cleanup();
    }

    /**
     * THIS METHOD IS NOT PART OF THE PUBLIC API AND SHOULD NOT BE INVOKED BY PLUGINS
     */
    public FacetRootsObserver getFacetRootsObserver() {
        if (facetRootsObserver == null) {
            facetRootsObserver = new FacetRootsObserver();
        }
        return facetRootsObserver;
    }

    // Do not add the @Override annotation on this
    public Object getMarkupId(Component component) {
        String markupId = null;
        for (Component ancestor = component.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
            if (ancestor instanceof IPlugin || ancestor instanceof Home) {
                markupId = ancestor.getMarkupId(true);
                break;
            }
        }
        if (markupId == null) {
            return "root";
        }
        int componentNum = 0;
        if (pluginComponentCounters.containsKey(markupId)) {
            componentNum = pluginComponentCounters.get(markupId);
        }
        ++componentNum;
        pluginComponentCounters.put(markupId, componentNum);
        return markupId + "_" + componentNum;
    }

    public String getApplicationName() {
        String applicationName;
        Session session = getJcrSession();
        String userID = session.getUserID();

        if (StringUtils.isEmpty(userID) || userID.equalsIgnoreCase("anonymous")) {
            applicationName = "login";
        } else {
            applicationName = PluginApplication.get().getPluginApplicationName();
        }

        return applicationName;
    }

    @Override
    public HippoRepository getHippoRepository() throws RepositoryException {
        Main main = (Main) Application.get();
        return main.getRepository();
    }

}
