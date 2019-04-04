<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

		<footer class="f-Main">
			<div class="f-Main_Content f-Main_Content-singleLine">
				<div class="f-Main_Logo" aria-hidden="true"></div>
				<div class="f-Main_Info">
					<p><spring:message code="eidas-proxy.htmlfooter.copy" /></p>
				</div>
			</div>
			<div id="version" style="display:none">${eidasProxyVersion}</div>
		</footer>
		<script type="text/javascript" src="<spring:url value='/js/jquery-3.2.1.min.js'/>"></script>
		<script type="text/javascript" src="<spring:url value='/js/handleButtonKeyPress.js?v=1'/>"></script>
		<script type="text/javascript" src="<spring:url value='/js/country-selector.js?v=2'/>"></script>
	</body>
</html>
