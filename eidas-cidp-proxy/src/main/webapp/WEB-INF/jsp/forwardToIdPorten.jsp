<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%--@elvariable id="authnRequest" type="java.lang.String"--%>
<%--@elvariable id="consumerServiceUrl" type="java.lang.String"--%>
<html>
<head>
  <script type="text/javascript" src="<spring:url value="js/jquery-3.2.1.min.js"/>"></script>
  <script>
    $(function() {
      $('#idPortenSubmission').submit();
    });
  </script>
</head>
<body>
<form id="idPortenSubmission" action="${consumerServiceUrl}" method="post">
  <input type="hidden" name="SAMLRequest" value="${authnRequest}" />
  <input type="hidden" name="RelayState" value="" />
</form>
</body>
</html>
