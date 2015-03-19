<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fbUtil" uri="http://www.jahia.org/tags/facebook" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="currentAliasUser" type="org.jahia.services.usermanager.JahiaUser"--%>

<template:addResources type="css" resources="loginForm.css"/>

<c:if test="${!renderContext.loggedIn || currentAliasUser.username == 'guest'}">
    <script type="text/javascript">
        document.onkeydown = function (e) { if ((e || window.event).keyCode == 13) document.loginForm.submit(); };
    </script>
    <span style="float: right;">
    <c:url var="connectUrl"
        value="https://graph.facebook.com/oauth/authorize">
        <c:param name="client_id" value="${fbUtil:getFacebookAppID()}" />
        <c:param name="display" value="page" />
        <c:param name="redirect_uri"
            value="${url.server}${url.base}${renderContext.mainResource.node.path}.html" />
        <c:param name="scope" value="${fbUtil:getPermissionList()}" />
    </c:url>
    <a href="${connectUrl}">
        <img src="${url.currentModule}/images/fbconnect.gif" title="Login with Facebook" alt="Login with Facebook"/>
    </a>
    </span>
    <ui:loginArea class="loginForm">
        <h3 class="loginicon">${fn:escapeXml(currentNode.displayableName)}</h3>
        <ui:isLoginError var="loginResult">
            <span class="error"><fmt:message bundle="JahiaInternalResources" key="${loginResult == 'account_locked' ? 'message.accountLocked' : 'message.invalidUsernamePassword'}"/></span>
        </ui:isLoginError>

        <p>
            <label class="left" for="username"><fmt:message key="label.username"/></label>
            <input class="full" type="text" value="" tabindex="1" maxlength="250" name="username" id="username"/>
        </p>

        <p>
            <label class="left" for="password"><fmt:message key="label.password"/></label>
            <input class="full" type="password" tabindex="2" maxlength="250" name="password" id="password"/>
        </p>

        <p>
            <input type="checkbox" id="rememberme" name="useCookie"/>
            <label for="rememberme" class="rememberLabel"><fmt:message key="loginForm.rememberMe.label"/></label>

        </p>

        <div class="divButton">
            <input type="submit" name="search" class="button" value="<fmt:message key='loginForm.loginbutton.label'/>"/>
        </div>
    </ui:loginArea>

</c:if>
<c:if test="${renderContext.loggedIn && currentAliasUser.username != 'guest'}">
    <c:if test="${renderContext.user.providerName == 'facebook'}">
        <div class="loginForm">
            <div class='image'>
                <div class='itemImage itemImageRight'>
                    <img src="https://graph.facebook.com/${renderContext.user.username}/picture" alt="" border="0"/>
                </div>
            </div>
            <h3 class="logouticon"><fmt:message key="label.logout"/></h3>
            <p><fmt:message key="label.loggedAs"/>&nbsp;
            <img src="${url.currentModule}/images/facebook-icon.jpg" alt=" " />&nbsp;
            <c:if test="${not empty renderContext.user.properties['link']}" var="linkAvailable">
            <a href="${renderContext.user.properties['link']}" target="_blank">${renderContext.user.properties["j:firstName"]}&nbsp;${renderContext.user.properties["j:lastName"]}<c:if test="${!empty currentAliasUser}"> (as ${currentAliasUser.username})</c:if></a>
            </c:if>
            <c:if test="${not linkAvailable}">
            ${renderContext.user.properties["j:firstName"]}&nbsp;${renderContext.user.properties["j:lastName"]}<c:if test="${!empty currentAliasUser}"> (as ${currentAliasUser.username})</c:if>
            </c:if>
            </p>
            <p>
                Friends:&nbsp;${fbUtil:getFriendsNumber(renderContext.user)}<br/>
                Email:&nbsp;${renderContext.user.properties["j:email"]}
            </p>
            <p>
            </p>
            <p><a class="aButton"
                  href='<c:url value="${url.logout}"/>'><span><fmt:message key="label.logout"/></span></a></p>
        </div>
    </c:if>
    <c:if test="${renderContext.user.providerName != 'facebook'}">
        <jcr:node var="user" path="${renderContext.user.localPath}"/>
        <jcr:nodeProperty node="${user}" name="j:publicProperties" var="publicProperties" />
        <c:set var="publicPropertiesAsString" value=""/>
    
        <c:forEach items="${publicProperties}" var="value">
            <c:set var="publicPropertiesAsString" value="${value.string} ${publicPropertiesAsString}"/>
        </c:forEach>
    
        <div class="loginForm">
            <div class='image'>
                <div class='itemImage itemImageRight'>
                    <c:choose>
                        <c:when test="${fn:contains(publicPropertiesAsString, 'j:picture')}">
                            <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>
                            <c:if test="${not empty picture}">
                                <img src="${picture.node.thumbnailUrls['avatar_120']}" alt="${fn:escapeXml(person)}"/>
                            </c:if>
                            <c:if test="${empty picture}">
                                <img src="<c:url value='/modules/default/images/userbig.png'/>" alt="" border="0"/>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <img src="<c:url value='/modules/default/images/userbig.png'/>" alt="" border="0"/>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <h3 class="logouticon"><fmt:message key="label.logout"/></h3>
            <p><fmt:message key="label.loggedAs"/>&nbsp;${renderContext.user.username}<c:if test="${!empty currentAliasUser}"> (as ${currentAliasUser.username})</c:if>
            </p>
            <p><a class="aButton"
                  href='<c:url value="${url.logout}"/>'><span><fmt:message key="label.logout"/></span></a></p>
        </div>
    </c:if>
</c:if>
