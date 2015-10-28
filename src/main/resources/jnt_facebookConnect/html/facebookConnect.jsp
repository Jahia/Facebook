<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
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

<%--Display the link Facebook Connect if the user is not yet connected--%>
<c:choose>
    <c:when test="${!renderContext.loggedIn || renderContext.editMode || currentAliasUser.username eq 'guest'}">
        <div>
            <c:url var="connectUrl" value="https://graph.facebook.com/oauth/authorize">
                <c:param name="client_id" value="${fbUtil:getFacebookAppID()}"/>
                <c:param name="scope" value="public_profile,email"/>
                <c:param name="display" value="page"/>
                <c:param name="redirect_uri" value="${url.server}${url.context}${url.base}${renderContext.mainResource.node.path}.html?site=${renderContext.site.name}"/>
            </c:url>
            <a href="${connectUrl}" >
                <img src="${url.currentModule}/images/fbconnect.gif" title="Login with Facebook" />
            </a>
            <c:if test="${!empty param.error}">
                <span style="color:red;">${param.error} - ${param.error_reason} : ${error_description}</span>
            </c:if>
        </div>
    </c:when>
    <c:when test="${renderContext.loggedIn && currentAliasUser.username ne 'guest'}">
        <c:url var="logoutUrl" value="${url.logout}">
            <c:param name="action" value="logout"/>
        </c:url>
        <c:url var="pictureUrl" value="${renderContext.user.properties['picture']}"/>
        <img src="${pictureUrl}" title="${renderContext.user.properties['name']}"
                height="30px" width="30px" style="border-radius: 3px;"/><b>&nbsp${renderContext.user.properties['name']}</b>
                (<a href="${logoutUrl}">Logout)</a>
    </c:when>
    <c:when test="${!renderContext.liveMode}">
        <img src="${url.currentModule}/images/fbconnect.gif" title="Facebook Connect" />
    </c:when>
</c:choose>

