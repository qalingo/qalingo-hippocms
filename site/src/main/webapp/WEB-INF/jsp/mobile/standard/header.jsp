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

<%@ page language="java" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.hippoecm.org/jsp/hst/core" prefix='hst' %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<div id="hd">
  <!-- language navigation -->
  <hst:include ref="langnavigation"/>

  <div id="hd-main">

    <!-- logo -->
    <c:set var="lang" value="${pageContext.request.locale.language}"/>

    <hst:include ref="logo"/>

    <!-- navigation -->
    <ul id="nav">
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
            <li class="active">
              <a href="${link}"><c:out value="${item.name}"/></a>
              <ul>
                <c:choose>
                  <c:when test="${detailPage}">
                    <li class="back">
                      <a href="${link}" onclick="javascript:history.back(); return false;"><c:out value="${item.name}"/></a>
                    </li>
                  </c:when>
                  <c:otherwise>
                    <c:forEach items="${item.childMenuItems}" var="childItem">
                      <c:choose>
                        <c:when test="${empty childItem.externalLink}">
                          <hst:link var="childLink" link="${childItem.hstLink}"/>
                        </c:when>
                        <c:otherwise>
                          <c:set var="childLink" value="${fn:escapeXml(childItem.externalLink)}"/>
                        </c:otherwise>
                      </c:choose>
                      <c:choose>
                        <c:when test="${childItem.expanded or (menu.deepestExpandedItem == item and childItem == item.childMenuItems[0])}">
                          <li class="active">
                            <a href="${childLink}"><c:out value="${childItem.name}"/></a>
                          </li>
                        </c:when>
                        <c:otherwise>
                          <li>
                            <a href="${childLink}"><c:out value="${childItem.name}"/></a>
                          </li>
                        </c:otherwise>
                      </c:choose>
                    </c:forEach>
                  </c:otherwise>
                </c:choose>
              </ul>
            </li>
          </c:when>
          <c:otherwise>
            <li>
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
  </div>
</div>
