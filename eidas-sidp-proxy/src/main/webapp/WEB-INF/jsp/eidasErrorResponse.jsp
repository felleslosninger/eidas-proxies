<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="inc/header.jsp" />
<header class="h-Main">
</header>

<main>
    <section class="Box">
        <div class="Box_header">
            <h1 class="Box_header-title with-logo logo-eid-gray"><spring:message code="eidas-proxy.header"/></h1>
            <div class="Box_header-provider"><img src="<spring:url value='images/eu.png' />" alt="Logo"/></div>
        </div>
        <div class="Box_main">
            <div class="notification notification-error with-Icon icon-error">
                <h1><spring:message code="eidas-proxy.login_failed.info"/></h1>
                <p>${eidasErrorResponse.message}</p>
                <p><spring:message code="eidas-proxy.login_failed.help"/></p>
            </div>
            <div class="fm-Controls with-Action">
                <a id="ok" name="ok" role="button" class="btn btn-Action" href="<spring:url value='cancel' />" onKeyPress="handleButtonKeyPress(event)">
                    <span><spring:message code='eidas-proxy.button.ok'/></span>
                </a>
            </div>
        </div>
    </section>
</main>

<jsp:include page="inc/footer.jsp" />
