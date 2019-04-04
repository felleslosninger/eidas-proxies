<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<% request.setAttribute("class", "hide-mobileMenu popup"); %>

<jsp:include page="common/header.jsp"/>
<jsp:include page="common/toppanel_disabled.jsp"/>
<main>
    <section class="Box">
        <div class="Box_header">
            <h1 class="Box_header-title with-close-button"><spring:message code="eidas-cidp-proxy.help.header" text=""/></h1>
            <span class="Box_header-close">
                <a href="<spring:url value="/showAttributeConsent" />"><span class="fa fa-close"><span class="visuallyHidden"><spring:message code="eidas-cidp-proxy.btn_close" text=""/></span></span></a>
            </span>
        </div>
        <div class="Box_main">
            <div class='fm-Fields'>
                <p><spring:message code="eidas-cidp-proxy.help.info_1" text=""/></p>
                <p><spring:message code="eidas-cidp-proxy.help.info_2" text=""/></p>
                <p class="with-Link"><a target="_blank" href="<spring:message code="eidas-cidp-proxy.help.link_url" text="" />"><span><spring:message code="eidas-cidp-proxy.help.link" text=""/></span></a></p>
            </div>
            <div class="fm-Controls with-Normal">
                <a class="btn btn-Normal" href="<spring:url value="/showAttributeConsent" />">
                    <span><spring:message code="eidas-cidp-proxy.btn_close" text=""/></span>
                </a>
            </div>
        </div>
    </section>
</main>

<jsp:include page="common/footer.jsp"/>