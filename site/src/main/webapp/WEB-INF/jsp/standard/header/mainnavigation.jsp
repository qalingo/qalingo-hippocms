<%--

    Copyright 2010-2013 Hippo B.V. (http://www.onehippo.com)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>

<%@include file="../../includes/tags.jspf" %>


<!-- main navigation -->
<ul id="main-nav">
  <c:forEach var="item" items="${menu.siteMenuItems}">
    <c:choose>
      <c:when test="${empty item.externalLink}">
        <hst:link var="link" link="${item.hstLink}"/>
      </c:when>
      <c:otherwise>
        <c:set var="link" value="${fn:escapeXml(item.externalLink)}"/>
      </c:otherwise>
    </c:choose>
    <c:choose>
      <c:when test="${item.expanded}">
        <li class="menu-item active">
          <a href="${link}"><c:out value="${item.name}"/></a>
        </li>
      </c:when>
      <c:otherwise>
        <li class="menu-item">
          <a href="${link}"><c:out value="${item.name}"/></a>
        </li>
      </c:otherwise>
    </c:choose>

  </c:forEach>

  <hst:defineObjects/>
  <c:if test="${hstRequest.requestContext.preview}">
    <li class="edit-menu-button">
      <hst:cmseditmenu menu="${menu}"/>
    </li>
  </c:if>
</ul>
