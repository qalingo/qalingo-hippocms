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

<%@ page language="java"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.hippoecm.org/jsp/hst/core" prefix='hst'%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<hst:headContribution keyHint="customform" category="jsInternal">
    <hst:link path="/js/custom-form-elements.js" var="customform"/>
    <script type="text/javascript" src="${customform}"></script>
</hst:headContribution>

<c:if test="${not empty pollDocument}">
  <c:choose>
  <c:when test="${voteSuccess eq 'true'}">
    <div class="box-general box-poll" id="home-poll">
      <ul>
        <c:if test="${not empty pollDocument.poll.text}">
          <li class="title"><c:out value="${pollDocument.poll.text}"/></li>
        </c:if>
        <c:forEach var="curOption" items="${pollVotes.options}">
          <li class="result"><c:out value="${curOption.value}"/>
            <span class="wrapper"><span class="poll-bar" style="width: ${curOption.votesPercentage}%;"><span class="poll-percentage"> ${curOption.votesPercentage}%</span></span></span>
          </li>
          <li class="text"><c:out value="${curOption.label}"/></li>
        </c:forEach>
      </ul>
      <c:if test="${pollVotes.totalVotesPercentage != 100}">
        <div class="message">Due to rounding the percentages don't add up to 100%</div>
      </c:if>
    </div>
  </c:when>
  <c:otherwise>
    <form id="form-poll" method="post" action="<hst:actionURL />">
      <ul class="box-general box-poll">
        <c:if test="${not empty pollDocument.poll.text}">
          <li class="title"><c:out value="${pollDocument.poll.text}"/></li>
        </c:if>
        <c:if test="${not empty pollDocument.poll.introduction}">
          <li class="introduction"><c:out value="${pollDocument.poll.introduction}"/></li>
        </c:if>
        <c:choose>
        <c:when test="${option eq 'pollNoCookies'}">
          <span><fmt:message key="poll.nocookies"/></span>
        </c:when>
        <c:otherwise>
          <li><input type="hidden" name="path" value="${fn:escapeXml(path)}"/></li>
          <c:forEach var="curOption" items="${pollDocument.poll.options}">
            <li class="option"><input type="radio" name="option" class="styled" value="${curOption.value}" /><span class="option-letter"><c:out value="${curOption.value}"/></span></li>
            <li class="text"><c:out value="${curOption.label}"/></li>
          </c:forEach>
          <li class="more"><input type="submit" value="<fmt:message key="poll.submit.label"/>" /></li>
          <c:if test="${voteSuccess eq 'false'}">
           <li><span><fmt:message key="poll.votingfailed"/></span></li>
          </c:if>
        </c:otherwise>
        </c:choose>
      </ul>
    </form>
  </c:otherwise>
  </c:choose>
</c:if>
