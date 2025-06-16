<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Calculator</title>
    <style>
        body { font-family: sans-serif; margin: 20px; }
        .calculator-container { max-width: 400px; }
        h1 {
            font-family: 'Times New Roman', Times, serif;
            font-size: 2.5em;
            margin-bottom: 20px;
        }
        .input-fields input[type="number"] {
            padding: 8px;
            margin-right: 10px;
            border: 1px solid #ccc;
            width: 100px;
            box-sizing: border-box;
        }
        .operation-buttons {
            margin-top: 15px;
            margin-bottom: 15px;
        }
        .operation-buttons button {
            padding: 8px 10px;
            margin-right: 5px;
            margin-bottom: 5px;
            background-color: #f0f0f0;
            border: 1px solid #ababab;
            border-radius: 3px;
            cursor: pointer;
            min-width: 110px;
            text-align: center;
        }
        .operation-buttons button:hover {
            background-color: #e0e0e0;
        }
        .result-display {
            margin-top: 20px;
            font-size: 1.2em;
        }
        .error-message {
            margin-top: 15px;
            color: red;
            font-weight: bold;
        }
    </style>
</head>
<body>
<div class="calculator-container">
    <h1>Caculator</h1>

    <form action="${pageContext.request.contextPath}/calculate" method="post">
        <div class="input-fields">
            <input type="number" name="number1" value="${empty number1 ? '' : number1}" step="any" placeholder="Number 1" required>
            <input type="number" name="number2" value="${empty number2 ? '' : number2}" step="any" placeholder="Number 2" required>
        </div>

        <div class="operation-buttons">
            <button type="submit" name="operation" value="addition">Addition(+)</button>
            <button type="submit" name="operation" value="subtraction">Subtraction(-)</button>
            <br>
            <button type="submit" name="operation" value="multiplication">Multiplication(X)</button>
            <button type="submit" name="operation" value="division">Division(/)</button>
        </div>
    </form>

    <c:if test="${not empty error}">
        <p class="error-message"><c:out value="${error}"/></p>
    </c:if>

    <c:if test="${not empty result and empty error}">
        <p class="result-display">Result ${operationName} : <c:out value="${result}"/></p>
    </c:if>
</div>
</body>
</html>