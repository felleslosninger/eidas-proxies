<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<header class="h-Main">
    <div class="h-Main_Content">
        <div class="h-Main_Content_Placeholder">
            <span class="h-Main_Content_Provider"><spring:message code="eidas-cidp-proxy.header"/></span>
        </div>
        <a href="" role="button" class="h-Menu_Trigger-mobile"><span class="fa fa-bars"></span></a>
        <div class="h-Menu_Container" id="js-menues">
            <nav class="h-Menu h-Menu-preOpened">
                <a href="" role="button" aria-haspopup="true" aria-expanded="false" class="h-Menu_Trigger"><span>Språk</span><span class="fa fa-angle-down fa-lg" aria-hidden="true"></span></a>
                <ul>
                  <li><a href="<spring:url value="/showAttributeConsent?locale=nb" />" class="h-Main_Menu_Element"><span>Bokmål</span></a></li>
                  <li><a href="<spring:url value="/showAttributeConsent?locale=nn" />" class="h-Main_Menu_Element"><span>Nynorsk</span></a></li>
                  <li><a href="<spring:url value="/showAttributeConsent?locale=en" />" class="h-Main_Menu_Element"><span>Engelsk</span></a></li>
                  <li><a href="<spring:url value="/showAttributeConsent?locale=se" />" class="h-Main_Menu_Element js-last"><span>Sámigiella</span></a></li>
                </ul>
            </nav>
        </div>
    </div>
</header>
