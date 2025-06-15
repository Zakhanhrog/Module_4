<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %> <

<html>
<head>
    <title>Chuyển đổi Tiền tệ USD sang VNĐ</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f4f4f4; }
        .container {
            background-color: #fff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            max-width: 500px;
            margin: auto;
        }
        h1 { text-align: center; color: #333; }
        label { display: block; margin-bottom: 5px; color: #555; }
        input[type="number"], input[type="text"] {
            width: calc(100% - 22px);
            padding: 10px;
            margin-bottom: 15px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
        }
        input[type="submit"] {
            background-color: #007bff;
            color: white;
            padding: 10px 15px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
        }
        input[type="submit"]:hover { background-color: #0056b3; }
        .result {
            margin-top: 20px;
            padding: 15px;
            background-color: #e9ecef;
            border: 1px solid #ced4da;
            border-radius: 4px;
        }
        .result p { margin: 5px 0; }
        .error-message {
            color: red;
            background-color: #f8d7da;
            border: 1px solid #f5c6cb;
            padding: 10px;
            border-radius: 4px;
            margin-bottom: 15px;
        }
        .success-message {
            color: green;
            background-color: #d4edda;
            border: 1px solid #c3e6cb;
            padding: 10px;
            border-radius: 4px;
            margin-bottom: 15px;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>Công cụ Chuyển đổi USD sang VNĐ</h1>

    <c:if test="${not empty errorMessage}">
        <div class="error-message">
            <p><c:out value="${errorMessage}" /></p>
        </div>
    </c:if>

    <c:if test="${not empty successMessage}">
        <div class="success-message">
            <p><c:out value="${successMessage}" /></p>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/converter/submit" method="post">
        <div>
            <label for="exchangeRate">Tỷ giá (1 USD = ? VNĐ):</label>
            <input type="text" id="exchangeRate" name="exchangeRate" value="<c:out value='${exchangeRate}'/>" placeholder="Ví dụ: 25000">
        </div>
        <div>
            <label for="usdAmount">Số tiền USD cần chuyển đổi:</label>
            <input type="text" id="usdAmount" name="usdAmount" value="<c:out value='${usdAmount}'/>" placeholder="Ví dụ: 100">
        </div>
        <div>
            <input type="submit" value="Chuyển đổi">
        </div>
    </form>

    <c:if test="${not empty vndAmount}">
        <div class="result">
            <h3>Kết quả chuyển đổi:</h3>
            <p><strong>Tỷ giá đã nhập:</strong>
                <fmt:formatNumber value="${exchangeRate}" type="number" groupingUsed="true" /> VNĐ/USD
            </p>
            <p><strong>Số tiền USD:</strong>
                <fmt:formatNumber value="${usdAmount}" type="number" groupingUsed="true" /> USD
            </p>
            <p><strong>Số tiền VNĐ tương ứng:</strong>
                <fmt:formatNumber value="${vndAmount}" type="currency" currencySymbol="VNĐ" groupingUsed="true" maxFractionDigits="0"/>
            </p>
        </div>
    </c:if>
</div>
</body>
</html>