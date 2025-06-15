<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>Từ Điển Anh - Việt Đơn Giản</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 20px; background-color: #f8f9fa; }
    .container {
      background-color: #ffffff;
      padding: 25px;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
      max-width: 600px;
      margin: 40px auto;
    }
    h1 { text-align: center; color: #343a40; margin-bottom: 20px; }
    label { display: block; margin-bottom: 8px; color: #495057; font-weight: bold; }
    input[type="text"] {
      width: calc(100% - 22px);
      padding: 10px;
      margin-bottom: 20px;
      border: 1px solid #ced4da;
      border-radius: 4px;
      box-sizing: border-box;
    }
    input[type="submit"] {
      background-color: #007bff;
      color: white;
      padding: 10px 20px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 16px;
      transition: background-color 0.2s;
    }
    input[type="submit"]:hover { background-color: #0056b3; }
    .result-area { margin-top: 25px; }
    .meaning {
      padding: 15px;
      background-color: #d4edda;
      border: 1px solid #c3e6cb;
      color: #155724;
      border-radius: 4px;
    }
    .meaning strong { font-size: 1.1em; }
    .error-message {
      padding: 15px;
      background-color: #f8d7da;
      border: 1px solid #f5c6cb;
      color: #721c24;
      border-radius: 4px;
    }
  </style>
</head>
<body>
<div class="container">
  <h1>Từ Điển Anh - Việt</h1>

  <form action="${pageContext.request.contextPath}/dictionary/lookup" method="post">
    <div>
      <label for="englishWordId">Nhập từ tiếng Anh cần tra:</label>
      <input type="text" id="englishWordId" name="word"
             value="<c:out value='${searchedWord}'/>" placeholder="Ví dụ: hello, world, book...">
    </div>
    <div>
      <input type="submit" value="Tra Cứu">
    </div>
  </form>

  <div class="result-area">
    <c:if test="${not empty errorMessage}">
      <div class="error-message">
        <p><c:out value="${errorMessage}" /></p>
      </div>
    </c:if>

    <c:if test="${not empty vietnameseMeaning}">
      <div class="meaning">
        <p>
          Từ "<strong><c:out value="${searchedWord}"/></strong>" có nghĩa là:
          <strong><c:out value="${vietnameseMeaning}"/></strong>
        </p>
      </div>
    </c:if>
  </div>
</div>
</body>
</html>