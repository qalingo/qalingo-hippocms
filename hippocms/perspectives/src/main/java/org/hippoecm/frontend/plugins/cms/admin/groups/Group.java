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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.util.ISO9075;
import org.apache.wicket.util.io.IClusterable;
import org.apache.jackrabbit.util.Text;
import org.apache.wicket.Session;
import org.hippoecm.frontend.plugins.cms.admin.domains.Domain;
import org.hippoecm.frontend.plugins.cms.admin.permissions.PermissionBean;
import org.hippoecm.frontend.plugins.cms.admin.users.DetachableUser;
import org.hippoecm.frontend.plugins.cms.admin.users.User;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.api.NodeNameCodec;
import org.onehippo.cms7.event.HippoEvent;
import org.onehippo.cms7.event.HippoEventConstants;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.cms7.services.eventbus.HippoEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Group implements Comparable<Group>, IClusterable {


    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(Group.class);

    private final static String PROP_DESCRIPTION = "hipposys:description";
    private final static String QUERY_ALL_LOCAL = "select * from hipposys:group where hipposys:securityprovider='internal' and (hipposys:system <> 'true' or hipposys:system IS NULL)";
    private final static String QUERY_ALL = "select * from hipposys:group";
    private final static String QUERY_ALL_ROLES = "select * from hipposys:role";
    private final static String QUERY_GROUP = "SELECT * FROM hipposys:group WHERE fn:name()='{}'";


    private String path;
    private String groupname;

    private String description;
    private boolean external = false;

    private transient Node node;

    public static QueryManager getQueryManager() throws RepositoryException {
        return UserSession.get().getQueryManager();
    }


    public static boolean exists(String groupname) {
        return getGroup(groupname) != null;
    }

    public static List<Group> getLocalGroups() {
        List<Group> groups = new ArrayList<Group>();
        NodeIterator iter;
        try {
            @SuppressWarnings({"deprecation"}) Query query = getQueryManager().createQuery(QUERY_ALL_LOCAL, Query.SQL);
            iter = query.execute().getNodes();
            while (iter.hasNext()) {
                Node node = iter.nextNode();
                if (node != null) {
                    try {
                        groups.add(new Group(node));
                    } catch (RepositoryException e) {
                        log.warn("Unable to add group to list", e);
                    }
                }
            }
        } catch (RepositoryException e) {
            log.error("Error while querying for a list of local groups", e);
        }
        // TODO: remove when query can sort on node names
        Collections.sort(groups);
        return groups;
    }


    public static List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<Group>();
        NodeIterator iter;
        try {
            @SuppressWarnings({"deprecation"}) Query query = getQueryManager().createQuery(QUERY_ALL, Query.SQL);
            iter = query.execute().getNodes();
            while (iter.hasNext()) {
                Node node = iter.nextNode();
                if (node != null) {
                    try {
                        groups.add(new Group(node));
                    } catch (RepositoryException e) {
                        log.warn("Unable to add group to list", e);
                    }
                }
            }
        } catch (RepositoryException e) {
            log.error("Error while querying for a list of local groups", e);
        }
        // TODO: remove when query can sort on node names
        Collections.sort(groups);
        return groups;
    }


    /**
     * Gets the Group with the specified name. If no Group with the specified name exists, null is returned.
     *
     * @param groupName the name of the Group to return
     * @return the Group with name groupName
     */
    public static Group getGroup(final String groupName) {
        final String escapedGroupName = Text.escapeIllegalJcr10Chars(ISO9075.encode(NodeNameCodec.encode(groupName, true)));
        final String queryString = QUERY_GROUP.replace("{}", escapedGroupName);
        try {
            @SuppressWarnings("deprecation") final Query query = getQueryManager().createQuery(queryString, Query.SQL);
            final QueryResult queryResult = query.execute();
            final NodeIterator iterator = queryResult.getNodes();
            if (!iterator.hasNext()) {
                return null;
            }
            return new Group(iterator.nextNode());
        } catch (RepositoryException e) {
            log.error("Unable to check if group '{}' exists, returning true", groupName, e);
            return null;
        }
    }

    /*
    * FIXME: should move to roles class or something the like when the admin perspective gets support for it
    *
    * @return A list of all roles defined in the system
    */
    public static List<String> getAllRoles() {
        List<String> roles = new ArrayList<String>();
        NodeIterator iter;
        try {
            @SuppressWarnings({"deprecation"}) Query query = getQueryManager().createQuery(QUERY_ALL_ROLES, Query.SQL);
            iter = query.execute().getNodes();
            while (iter.hasNext()) {
                Node node = iter.nextNode();
                if (node != null) {
                    try {
                        roles.add(node.getName());
                    } catch (RepositoryException e) {
                        log.warn("Unable to add group to list", e);
                    }
                }
            }
        } catch (RepositoryException e) {
            log.error("Error while querying for a list of local groups", e);
        }
        // TODO: remove when query can sort on node names
        Collections.sort(roles);
        return roles;
    }

    public boolean isExternal() {
        return external;
    }

    public String getGroupname() {
        return groupname;
    }

    @SuppressWarnings({"unused"})
    public void setGroupname(final String groupname) {
        this.groupname = groupname;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) throws RepositoryException {
        this.description = description;
    }


    //----------------------- constructors ---------//
    public Group() {
    }

    public Group(final Node node) throws RepositoryException {
        this.path = node.getPath().substring(1);
        this.groupname = NodeNameCodec.decode(node.getName());
        this.node = node;

        if (node.isNodeType(HippoNodeType.NT_EXTERNALGROUP)) {
            external = true;
        }

        if (node.hasProperty(PROP_DESCRIPTION)) {
            description = node.getProperty(PROP_DESCRIPTION).getString();
        } else if (node.hasProperty("description")) {
            description = node.getProperty("description").getString();
        }
    }

    /**
     * Returns all non-system users that are part of this group, including users that don't exist anymore
     * @return a list of {@link User}s
     * @throws RepositoryException
     */
    public List<String> getMembers() throws RepositoryException {
        return getMembers(false);
    }

    private List<String> getMembers(boolean includeSystemUsers) throws RepositoryException {
        final List<String> members = new ArrayList<String>();
        if (node.hasProperty(HippoNodeType.HIPPO_MEMBERS)) {
            final Value[] vals = node.getProperty(HippoNodeType.HIPPO_MEMBERS).getValues();
            for (Value val : vals) {
                final String username = val.getString();
                if (!includeSystemUsers && User.userExists(username)) {
                    final User user = new User(username);
                    if (user.isSystemUser()) {
                        continue;
                    }
                }
                members.add(username);
            }
        }
        Collections.sort(members);
        return members;

    }

    private List<String> getAllMembers() throws RepositoryException {
        return getMembers(true);
    }

    public List<DetachableUser> getMembersAsDetachableUsers() {
        List<String> usernames;
        try {
            usernames = getMembers();
        } catch (RepositoryException e) {
            throw new IllegalStateException("Cannot get members for this group", e);
        }

        List<DetachableUser> users = new ArrayList<DetachableUser>();
        for (String username : usernames) {
            if (!User.userExists(username)) {
                continue;
            }
            User user = new User(username);
            DetachableUser detachableUser = new DetachableUser(user);
            users.add(detachableUser);
        }

        return users;
    }

    //-------------------- persistence helpers ----------//

    /**
     * Wrapper needed for spi layer which doesn't know if a property exists or not
     *
     * @param node
     * @param name
     * @param value
     * @throws RepositoryException
     */
    private void setOrRemoveStringProperty(Node node, String name, String value) throws RepositoryException {
        if (value == null && !node.hasProperty(name)) {
            return;
        }
        node.setProperty(name, value);
    }

    /**
     * Create a new group
     *
     * @throws RepositoryException
     */
    public void create() throws RepositoryException {
        if (exists(getGroupname())) {
            throw new RepositoryException("Group already exists");
        }

        // FIXME: should be delegated to a groupmanager
        StringBuilder relPath = new StringBuilder();
        relPath.append(HippoNodeType.CONFIGURATION_PATH);
        relPath.append("/");
        relPath.append(HippoNodeType.GROUPS_PATH);
        relPath.append("/");
        relPath.append(NodeNameCodec.encode(getGroupname(), true));

        node = ((UserSession) Session.get()).getRootNode().addNode(relPath.toString(), HippoNodeType.NT_GROUP);
        setOrRemoveStringProperty(node, PROP_DESCRIPTION, getDescription());
        // save parent when adding a node
        node.getParent().getSession().save();
    }

    /**
     * save the current group
     *
     * @throws RepositoryException
     */
    public void save() throws RepositoryException {
        if (node.isNodeType(HippoNodeType.NT_GROUP)) {
            setOrRemoveStringProperty(node, PROP_DESCRIPTION, getDescription());
            node.getSession().save();
        } else {
            throw new RepositoryException("Only hipposys:group's can be edited.");
        }
    }

    /**
     * Delete the current group.
     *
     * @throws RepositoryException
     */
    public void delete() throws RepositoryException {
        removeAllPermissions();

        Node parent = node.getParent();
        node.remove();
        parent.getSession().save();

        HippoEventBus eventBus = HippoServiceRegistry.getService(HippoEventBus.class);
        if (eventBus != null) {
            final UserSession userSession = UserSession.get();
            HippoEvent event = new HippoEvent(userSession.getApplicationName())
                    .user(userSession.getJcrSession().getUserID())
                    .action("delete-group")
                    .category(HippoEventConstants.CATEGORY_GROUP_MANAGEMENT)
                    .message("deleted group " + groupname);
            eventBus.post(event);
        }
    }

    /**
     * Removes all permissions for this group
     *
     * @throws RepositoryException When a repository error occurs while removing a group reference on an AuthRole
     *                             object
     */
    public void removeAllPermissions() throws RepositoryException {
        List<PermissionBean> permissions = PermissionBean.forGroup(this);
        for (PermissionBean permission : permissions) {
            permission.getAuthRole().removeGroup(groupname);
        }
    }

    public void removeMembership(String user) throws RepositoryException {
        List<String> members = getAllMembers();
        members.remove(user);
        node.setProperty(HippoNodeType.HIPPO_MEMBERS, members.toArray(new String[members.size()]));
        node.getSession().save();
    }

    public void addMembership(String user) throws RepositoryException {
        List<String> members = getAllMembers();
        members.add(user);
        node.setProperty(HippoNodeType.HIPPO_MEMBERS, members.toArray(new String[members.size()]));
        node.getSession().save();
    }

    /**
     * Get the roles for this group on the passed Domain.
     *
     * @param domain the {@link Domain} to get the roles for
     * @return the roles
     */
    public List<Domain.AuthRole> getLinkedAuthenticatedRoles(final Domain domain) {
        Map<String, Domain.AuthRole> authRoles = domain.getAuthRoles();
        List<Domain.AuthRole> roles = new ArrayList<Domain.AuthRole>();
        for (Map.Entry<String, Domain.AuthRole> entry : authRoles.entrySet()) {
            Domain.AuthRole authenticationRole = entry.getValue();
            final boolean groupHasRole = authenticationRole.getGroupnames().contains(getGroupname());
            if (groupHasRole) {
                roles.add(authenticationRole);
            }
        }
        return roles;
    }

    /**
     * Returns all domain - authrole combinations for this group
     *
     * @return a {@link List} of {@link PermissionBean}s
     */
    public List<PermissionBean> getPermissions() {
        return PermissionBean.forGroup(this);
    }

    //--------------------- default object -------------------//

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || (obj.getClass() != this.getClass())) {
            return false;
        }
        Group other = (Group) obj;
        return other.getPath().equals(getPath());
    }

    public int hashCode() {
        return (null == path ? 0 : path.hashCode());
    }

    public int compareTo(Group o) {
        return groupname.compareTo(o.getGroupname());
    }
}
