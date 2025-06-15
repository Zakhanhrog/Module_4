<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>Danh sách Sản phẩm </title>
    <style>
        table { border-collapse: collapse; width: 60%; margin-top: 20px; }
        th, td { border: 1px solid #ddd; text-align: left; padding: 8px; }
        th { background-color: #f0f0f0; }
    </style>
</head>
<body>
<h1>Danh sách Sản phẩm </h1>

<c:if test="${not empty productListAttribute}">
    <table>
        <thead>
        <tr>
            <th>ID</th>
            <th>Tên Sản phẩm</th>
            <th>Giá (VNĐ)</th>
            <th>Chi tiết sản phẩm</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${productListAttribute}" var="product">
            <tr>
                <td><c:out value="${product.id}" /></td>
                <td><c:out value="${product.name}" /></td>
                <td><c:out value="${String.format('%,.0f', product.price)}" /></td>
                <td><a href="${pageContext.request.contextPath}/products/detail/${product.id}">Xem chi tiết</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</c:if>

<c:if test="${empty productListAttribute}">
    <p>Không có sản phẩm nào để hiển thị.</p>
</c:if>
<p><a href="${pageContext.request.contextPath}/">Về trang chủ (redirects to products)</a></p>
</body>
</html>