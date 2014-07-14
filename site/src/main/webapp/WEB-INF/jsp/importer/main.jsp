<%--
  Copyright 2014 Hippo B.V. (http://www.onehippo.com)

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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://www.hippoecm.org/jsp/hst/core" prefix='hst'%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<hst:headContribution keyHint="title"><title>ADD </title></hst:headContribution>

<div id="yui-u">
  <c:if test="${not empty message}">
    <h2>${message}</h2>
  </c:if>
  <h1>Add Wikipedia Documents</h1>
  <hr/>
  <br/>

  <form action="<hst:actionURL/>" method="get">
    <table>
      <tr>
        <td>Number: </td>
        <td><input type="text" name="number"/></td>
      </tr>
      <tr>
        <td>offset
        </td>
        <td><input type="text" name="offset"/></td>
      </tr>
      <tr>
        <td>Wiki location on filesystem:</td>
        <td><input type="text" name="filesystemLocation"/></td>
      </tr>
      <tr>
      <td>Type</td>
      <td>
        <input type="radio" name="type" value="news"/> News<br/>
        <input type="radio" name="type" value="products"/> Products
      </td>
    </tr>
      <tr>
        <td></td>
        <td><input type="submit" value="Add X wikipedia docs"/></td>
      </tr>
    </table>
  </form>
</div>