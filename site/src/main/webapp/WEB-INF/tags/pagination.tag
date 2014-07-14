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

<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="hst" uri="http://www.hippoecm.org/jsp/hst/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ tag trimDirectiveWhitespaces="true" %>
<%@ attribute name="pageableResult" required="true" type="com.onehippo.gogreen.utils.PageableCollection"
              rtexprvalue="true" %>
<%@ attribute name="queryName" required="false" type="java.lang.String" rtexprvalue="true" %>
<%@ attribute name="queryValue" required="false" type="java.lang.String" rtexprvalue="true" %>


<%--set various pagination visible pages--%>
<c:set var="currentpage" value="${pageableResult.currentPage}"/>

<c:if test="${pageableResult.totalPages <= 7}">
    <c:set var="listSize" value="7"/>
</c:if>
<c:if test="${pageableResult.totalPages > 7}">
    <c:set var="listSize" value="5"/>
</c:if>
<c:if test="${pageableResult.currentPage > 3 and pageableResult.currentPage < (pageableResult.totalPages - 2)}">
    <c:set var="listSize" value="2"/>
</c:if>

<c:choose>
    <c:when test="${currentpage < listSize -1 || empty currentpage || (currentpage - listSize < 1) }">
        <c:set var="start" value="1"/>
    </c:when>
    <c:otherwise>
        <c:set var="start" value="${currentpage - (listSize-1)}"/>
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${pageableResult.totalPages < (currentpage + listSize - 1)}">
        <c:set var="end" value="${pageableResult.totalPages}"/>
    </c:when>
    <c:otherwise>
        <c:set var="end" value="${(currentpage + listSize -1)}"/>
    </c:otherwise>
</c:choose>

<c:if test="${not empty pageableResult.currentRange}">
    <ul id="page-nav">
        <c:choose>
            <c:when test="${pageableResult.previous}">
                <li class="prev">
                    <c:url var="prevLink" value="">
                        <c:param name="${queryName}" value="${fn:escapeXml(queryValue)}"/>
                        <c:param name="pageNumber" value="${pageableResult.previousPage}"/>
                    </c:url>
                    <a href="${fn:escapeXml(prevLink)}"><fmt:message key="pagination.previous"/></a>
                </li>
            </c:when>
            <c:otherwise>
                <li class="prev disabled"><fmt:message key="pagination.previous"/></li>
            </c:otherwise>
        </c:choose>

        <c:if test="${pageableResult.currentPage > 3 and pageableResult.totalPages > 7}">
            <li>
                <c:url var="pageLink" value="">
                    <c:param name="${queryName}" value="${fn:escapeXml(queryValue)}"/>
                    <c:param name="pageNumber" value="1"/>
                </c:url>
                <a href="${fn:escapeXml(pageLink)}"><c:out value="1"/></a>
            </li>
            <li><c:out value="..."/></li>
        </c:if>

        <c:forEach var="page" begin="${start}" end="${end}">
            <c:choose>
                <c:when test="${page == pageableResult.currentPage}">
                    <li class="active"><c:out value="${page}"/></li>
                </c:when>
                <c:otherwise>
                    <li>
                        <c:url var="pageLink" value="">
                            <c:param name="${queryName}" value="${fn:escapeXml(queryValue)}"/>
                            <c:param name="pageNumber" value="${page}"/>
                        </c:url>
                        <a href="${fn:escapeXml(pageLink)}"><c:out value="${page}"/></a>
                    </li>
                </c:otherwise>
            </c:choose>
        </c:forEach>

        <c:if test="${pageableResult.currentPage < (pageableResult.totalPages - 2) and pageableResult.totalPages > 7}">
            <li><c:out value="..."/></li>
            <li>
                <c:url var="pageLink" value="">
                    <c:param name="${queryName}" value="${fn:escapeXml(queryValue)}"/>
                    <c:param name="pageNumber" value="${pageableResult.totalPages}"/>
                </c:url>
                <a href="${fn:escapeXml(pageLink)}"><c:out value="${pageableResult.totalPages}"/></a>
            </li>
        </c:if>

        <c:choose>
            <c:when test="${pageableResult.next}">
                <li class="next">
                    <c:url var="nextLink" value="">
                        <c:param name="${queryName}" value="${fn:escapeXml(queryValue)}"/>
                        <c:param name="pageNumber" value="${pageableResult.nextPage}"/>
                    </c:url>
                    <a href="${fn:escapeXml(nextLink)}"><fmt:message key="pagination.next"/></a>
                </li>
            </c:when>
            <c:otherwise>
                <li class="next disabled"><fmt:message key="pagination.next"/></li>
            </c:otherwise>
        </c:choose>
    </ul>
</c:if>
