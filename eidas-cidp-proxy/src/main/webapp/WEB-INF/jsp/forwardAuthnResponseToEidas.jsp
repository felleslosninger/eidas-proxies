<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<head>
  <script type="text/javascript" src="<spring:url value="js/jquery-3.2.1.min.js"/>"></script>
  <script>
    $(function() {
      $('#eidasSubmission').submit();
    });
  </script>
</head>
<body>
<form id="eidasSubmission" action="${assertionConsumerUrl}" method="post">
  <input type="hidden" name="SAMLResponse" value="${SAMLResponseToEidas}"/>
  <input type="hidden" name="username" value="${userName}"/>
</form>
</body>
</html>
