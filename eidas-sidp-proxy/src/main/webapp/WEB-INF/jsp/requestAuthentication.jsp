<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
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
            <form:form method="post" action="authRedir" autocomplete="off">
                <fieldset>
                    <div class="fm-Fields">
                        <p class="warning"><spring:message code="eidas-proxy.select_country.legal"/></p>
                        <p class="legend-Style"><spring:message code="eidas-proxy.select_country.info"/></p>

                        <div class="cs-InputContainer">
                            <div class="cs-Input fm-Field" id="js-country">
                                <div class="cs-Input_Placeholder">
                                    <label for="country" class="visuallyHidden"><spring:message code="eidas-proxy.select_country.form.label"/></label>
                                    <input type="text" id="country" placeholder="<spring:message code='eidas-proxy.select_country.form.country_placeholder' />" autocomplete="off" aria-haspopup="true" aria-expanded="false" />
                                    <button class="cs-Button fa fa-angle-down" id="js-trigger" aria-haspopup="true" aria-expanded="false"></button>
                                </div>
                                <input type="hidden" name="countrycode" value=""/>
                            </div>
                            <span class="fm-Message"><spring:message code="eidas-proxy.select_country.form.error" /></span>
                            <ul role="listbox" class="cs-List" id="js-country-list">
                                <c:forEach items="${countries}" var="country">
                                    <li role="option"><a data-id="${country.countryCode()}" href="">${country.countryName()}</a></li>
                                </c:forEach>
                            </ul>
                        </div>
                    </div>
                </fieldset>

                <div class="fm-Controls with-Normal with-Action">
                    <button id="next" class="btn btn-Action" type="submit">
                        <span><spring:message code='eidas-proxy.select_country.form.submit' /></span>
                    </button>
                    <a id="cancel" role="button" class="btn btn-Normal" href="<spring:url value='cancel' />" onKeyPress="handleButtonKeyPress(event)">
                        <span><spring:message code="eidas-proxy.select_country.form.cancel" /></span>
                    </a>
                </div>

            </form:form>
        </div>
    </section>
</main>

<jsp:include page="inc/footer.jsp" />
