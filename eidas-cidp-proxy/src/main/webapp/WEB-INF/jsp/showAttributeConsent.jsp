<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ include file="common/header.jsp" %>
<%@ include file="common/toppanel.jsp" %>

<main>
    <section class="Box">
        <div class="Box_Section Box_Section-ServiceProvider">
            <div class="Box_Section_Title"><spring:message code="eidas-cidp-proxy.header"/></div>
            <img src="<spring:url value="/images/eu.png" />" alt="Logo" />
        </div>
        <div class="Box_header">
            <h1 class="Box_header-title with-logo logo-eid-gray"><spring:message code="eidas-cidp-proxy.box_header"/></h1>
        </div>

        <form class="fm-Form">
            <div class="Box_main">

                <div class="fm-Fields">
                    <p class="ingress with-Link">
                        <spring:message code="eidas-cidp-proxy.box_text"/>
                    </p>
                    <div class="notification with-Link with-Icon">
                        <p><spring:message code="eidas-cidp-proxy.info_text"/>
                            <c:forEach var="attrName" items="${attributes}" varStatus="status">
                                <spring:message code="${attrName}" text="" var="attribute" />
                                <spring:message code="eidas-cidp-proxy.word.and" text="" var="andWord" />
                                <c:choose>
                                    <c:when test="${status.first}">
                                        ${fn:toUpperCase(fn:substring(attribute, 0, 1))}${fn:toLowerCase(fn:substring(attribute, 1, fn:length(attribute)))},
                                    </c:when>
                                    <c:when test="${status.last}">
                                        ${fn:toLowerCase(andWord)} ${fn:toLowerCase(attribute)}.
                                    </c:when>
                                    <c:otherwise>
                                        ${fn:toLowerCase(attribute)},
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </p>
                        <a href="<spring:url value="/showAttributeDetails" />"><span><spring:message code="eidas-cidp-proxy.info_link"/></span></a>
                    </div>
                </div>
                <div class="fm-Controls with-Normal with-Action">
                    <a class="btn btn-Action" role="button" href="<spring:url value="/acceptConsent" />" onkeypress="handleButtonKeyPress(event)"><span><spring:message code="eidas-cidp-proxy.btn_consent" /></span></a>
                    <a class="btn btn-Normal" role="button" href="<spring:url value="/rejectConsent" />" onkeypress="handleButtonKeyPress(event)"><span><spring:message code="eidas-cidp-proxy.btn_cancel" /></span></a>
                </div>
            </div>
        </form>
        <div class="Box_footer">
            <div class="Box_footer-links"></div>
            <div class="Box_footer-help">
                <a href="<spring:url value="/showAttributeHelp" />"><span class="fa fa-question-circle"></span><span class="visuallyHidden"><spring:message code="eidas-cidp-proxy.btn_help" /></span></a>
            </div>
        </div>
    </section>
    <div class="mi-Provider"><img src="<spring:url value="/images/eu.png" />" alt="Logo" /></div>
</main>

<%@ include file="common/footer.jsp" %>

