<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Selected Condiments</title>
    <style>
        body { font-family: sans-serif; margin: 20px; }
        h1 { color: #333; }
        ul { list-style-type: disc; margin-left: 20px; }
    </style>
</head>
<body>
<h1>Selected Condiments</h1>
<hr>
<c:choose>
    <c:when test="${not empty selectedCondiments}">
        <p>You have selected the following condiments:</p>
        <ul>
            <c:forEach var="condiment" items="${selectedCondiments}">
                <li><c:out value="${condiment}" /></li>
            </c:forEach>
        </ul>
    </c:when>
    <c:otherwise>
        <p><c:out value="${message}" /></p>
    </c:otherwise>
</c:choose>
<br>
<a href="${pageContext.request.contextPath}/">Back to selection</a>
</body>
</html>