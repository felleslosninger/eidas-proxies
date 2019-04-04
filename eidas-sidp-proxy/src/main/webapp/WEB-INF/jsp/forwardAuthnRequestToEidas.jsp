<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <script type="text/javascript" src="<spring:url value='/js/jquery-3.2.1.min.js'/>"></script>
    <script>
        $(function() {
            $('#eidasNodeAuthnRequest').submit();
        });
    </script>
</head>
<body>
<form id="eidasNodeAuthnRequest" action="${eidasNodeUrl}" method="post">
    <input type="hidden" name="SAMLRequest" value="${authnRequest}" />
    <input type="hidden" name="country" value="${country}" />
</form>
</body>
</html>
