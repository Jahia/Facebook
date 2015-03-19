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

<c:if test="${renderContext.loggedIn}">
<c:if test="${!renderContext.editMode && renderContext.user.providerName eq 'facebook'}">
<template:addResources type="css" resources="jquery.neosmart.fb.wall.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.neosmart.fb.wall.js"/>
<template:addResources>
    <script type="text/javascript">
        $(document).ready(function() {
        	$('#jahia-facebook-wall').fbWall({ id:"${renderContext.user.properties['j:facebookID']}",accessToken:"${renderContext.user.properties['access_token']}",
        		max: 10});
       	});
    </script>
</template:addResources>
<div id="jahia-facebook-wall"></div>
</c:if>
<c:if test="${renderContext.editMode}">
    <fieldset>
        <legend>Facebook Wall</legend>
    </fieldset>
</c:if>
</c:if>


