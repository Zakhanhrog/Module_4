<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>JSP - Hello World</title>
</head>
<body>
<h1><%= "SandwichCondiment" %>
</h1>
<br/>
<a href="${pageContext.request.contextPath}/condimentForm">Click here to Sandwich</a>
</body>
</html>