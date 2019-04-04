<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ include file="common/header.jsp" %>
<%@ include file="common/toppanel_disabled.jsp" %>

<main>
	<section class="Box">
		<div class="Box_Section Box_Section-ServiceProvider">
			<div class="Box_Section_Title"><spring:message code="eidas-cidp-proxy.header"/></div>
			<img src="<spring:url value="/images/eu.png" />" alt="Logo" />
		</div>
		<div class="Box_header">
			<h1 class="Box_header-title with-logo logo-eid-gray"><spring:message code="eidas-cidp-proxy.error.attributes.header"/></h1>
		</div>
		<form class="fm-Form">
			<div class="Box_main">

				<div class="fm-Fields">
					<div class="notification with-Icon">
						<p>
							<spring:message code="eidas-cidp-proxy.error.attributes.info_part1" />
							<span>(</span>
							<c:forEach var="entry" items="${attributeMap}" varStatus="status">
								<span class="title"><spring:message code="${entry}" text="" /></span>
								<c:if test="${! status.last}">,</c:if>
							</c:forEach>
							<span>).</span>
						</p>
						<p>
							<spring:message code="eidas-cidp-proxy.error.attributes.info_part2" />
						</p>
					</div>
				</div>
				<div class="fm-Controls with-Normal">
					<a class="btn btn-Normal" href="<spring:url value="/saml/logout" />">
						<span><spring:message	code="eidas-cidp-proxy.error.attributes.btn_logout" /></span>
					</a>
				</div>
			</div>
		</form>
	</section>
	<div class="mi-Provider"><img src="<spring:url value="/images/eu.png" />" alt="Logo" /></div>
</main>

<%@ include file="common/footer.jsp" %>
