<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html >
<html dir="ltr" lang="${pageContext.response.locale.language}">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>ID-porten</title>
    <link rel="stylesheet" type="text/css" href="<spring:url value='/css/open-sans/open-sans.css?v=2'/>" />
    <link rel="stylesheet" type="text/css" href="<spring:url value='/css/style.min.css?v=2'/>" />
    <link rel="stylesheet" type="text/css" href="<spring:url value='/css/font-awesome.min.css?v=2'/>" />
    <meta name="description" content='<spring:message code="eidas-cidp-proxy.htmlheader.description"/>' />
    <meta name="keywords" content='<spring:message code="eidas-cidp-proxy.htmlheader.keywords"/>' />
    <meta name="language" content='<spring:message code="eidas-cidp-proxy.htmlheader.lang"/>' />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
</head>

<c:choose>
<c:when test="${not empty requestScope['class']}">
<body class="${requestScope['class']}">
</c:when>
<c:otherwise>
<body>
</c:otherwise>
</c:choose>

