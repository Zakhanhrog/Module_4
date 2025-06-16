<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Sandwich Condiments</title>
    <style>
        body { font-family: sans-serif; margin: 20px; }
        h1, h2 { color: #333; }
        .condiment-group label { margin-right: 15px; }
        .save-button {
            margin-top: 20px;
            padding: 8px 15px;
            background-color: #f0f0f0;
            border: 1px solid #ccc;
            border-radius: 4px;
            cursor: pointer;
        }
        .save-button:hover {
            background-color: #e0e0e0;
        }
    </style>
</head>
<body>
<h1>Sandwich Condiments</h1>
<hr>
<form:form modelAttribute="sandwich" action="save" method="post">
    <h2>Choose your condiments:</h2>
    <div class="condiment-group">
        <c:forEach var="condiment" items="${allCondiments}">
            <form:checkbox path="condiments" value="${condiment}" label="${condiment}" />
        </c:forEach>
    </div>
    <br>
    <input type="submit" value="Save" class="save-button"/>
</form:form>
</body>
</html>