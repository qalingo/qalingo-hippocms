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


<c:set var="style">event-item<c:if test="${preview}"> editable</c:if></c:set>
<c:forEach items="${docs.items}" var="item">
    <ul class="${style}">
        <li class="full-link"><a href="<hst:link hippobean="${item}"/>"></a></li>
        <li class="calendar"><img src="<hst:link path="/images/mobile/bg-calendar.png"/>" alt="" />
            <span class="month"><fmt:formatDate value="${item.date.time}" pattern="MMM"/></span>
            <span class="day"><fmt:formatDate value="${item.date.time}" pattern="dd"/></span>
        </li>
        <c:url var="linkUrl" value="http://maps.google.com/?q=${item.location.street} ${item.location.number}, ${item.location.city} ${item.location.postalCode} ${item.location.province}"/>
        <c:url var="imageUrl" value="http://maps.google.com/maps/api/staticmap?zoom=10&size=150x100&maptype=roadmap&markers=color:green|${item.location.street} ${item.location.number}, ${item.location.city} ${item.location.postalCode} ${item.location.province}&sensor=true"/>
        <li class="gmaps"><a href="${fn:escapeXml(linkUrl)}">
            <img src="${fn:escapeXml(imageUrl)}" alt="Google Maps" />
        </a></li>
        <li class="title"><a href="<hst:link hippobean="${item}"/>"><c:out value="${item.title}"/></a></li>
        <li><hst:cmseditlink hippobean="${item}" /></li>
    </ul>
</c:forEach>
