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
    <c:when test="${!renderContext.loggedIn  || (currentAliasUser.username eq 'guest')}">
        <div>
            <a href="https://graph.facebook.com/oauth/authorize?client_id=149708825083446&display=page&redirect_uri=${url.server}${url.base}${renderContext.mainResource.node.path}.html&scope=${fbUtil:getPermissionList()}" >
                <img src="${url.currentModule}/img/fbconnect.gif" title="Facebook Connect" />
            </a>
            <c:if test="${!empty param.error}">
                <span style="color:red;">${param.error} - ${param.error_reason} : ${error_description}</span>
            </c:if>
        </div>
    </c:when>
    <c:when test="${renderContext.loggedIn && renderContext.user.providerName eq 'facebook'}">
                You are now connected with your facebook account<br/>
                <a href="<c:url value="${url.logout}"/>">Click here to logout</a>
    </c:when>
    <c:when test="${renderContext.loggedIn && !renderContext.liveMode }">
             <img src="${url.currentModule}/img/fbconnect.gif" title="Facebook Connect" />
    </c:when>
</c:choose>

