<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Chi tiết Sản phẩm - <c:out value="${productDetail.name}"/></title>
    <style>
        body { font-family: sans-serif; }
        .container {
            width: 50%;
            margin: 20px auto;
            padding: 20px;
            border: 1px solid #ccc;
            border-radius: 5px;
            background-color: #f9f9f9;
        }
        h1 { color: #333; }
        p { margin-bottom: 10px; }
        strong { color: #555; }
        a { text-decoration: none; color: blue; }
        a:hover { text-decoration: underline; }
    </style>
</head>
<body>
<div class="container">
    <h1>Chi tiết Sản phẩm</h1>

    <c:if test="${not empty productDetail}">
        <p><strong>ID:</strong> <c:out value="${productDetail.id}" /></p>
        <p><strong>Tên Sản phẩm:</strong> <c:out value="${productDetail.name}" /></p>
        <p><strong>Giá:</strong> <c:out value="${String.format('%,.0f VNĐ', productDetail.price)}" /></p>
    </c:if>

    <c:if test="${empty productDetail}">
        <p style="color:red;">Không tìm thấy thông tin sản phẩm.</p>
    </c:if>

    <p><a href="${pageContext.request.contextPath}/products">Quay lại danh sách sản phẩm</a></p>
</div>
</body>
</html>