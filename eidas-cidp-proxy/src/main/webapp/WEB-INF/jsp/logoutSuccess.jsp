<%@ page session="false" language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ include file="common/header.jsp" %>
<%@ include file="common/toppanel_disabled.jsp" %>

<main>
	<section class="Box Box-noBorder">
		<div class="Box_main">
			<div id="main" class="notification with-Icon">
				<h1><spring:message code="eidas-cidp-proxy.logoutsuccess.header" text=""/></h1>
				<p><spring:message code="eidas-cidp-proxy.logoutsuccess.info" text=""/></p>
			</div>
		</div>
	</section>
</main>

<jsp:include page="common/footer.jsp" />
