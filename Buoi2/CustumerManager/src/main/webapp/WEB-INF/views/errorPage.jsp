<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title>Lỗi</title>
  <style>
    body { font-family: sans-serif; text-align: center; padding-top: 50px; }
    .error-container {
      width: 60%;
      margin: 0 auto;
      padding: 20px;
      border: 1px solid #ff0000;
      background-color: #ffebeb;
      border-radius: 5px;
    }
    h1 { color: #cc0000; }
    a { text-decoration: none; color: blue; }
    a:hover { text-decoration: underline; }
  </style>
</head>
<body>
<div class="error-container">
  <h1>Đã có lỗi xảy ra</h1>
  <c:if test="${not empty errorMessage}">
    <p><c:out value="${errorMessage}"/></p>
  </c:if>
  <c:if test="${empty errorMessage}">
    <p>Không tìm thấy trang hoặc tài nguyên yêu cầu.</p>
  </c:if>
  <p><a href="${pageContext.request.contextPath}/products">Quay lại danh sách sản phẩm</a></p>
</div>
</body>
</html>