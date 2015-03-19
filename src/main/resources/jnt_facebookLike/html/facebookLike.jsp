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

<%--
<div id="fb-root"></div>
<script>(function(d, s, id) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) {return;}
  js = d.createElement(s); js.id = id;
  js.src = "//connect.facebook.net/en_US/all.js#xfbml=1";
  fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));</script>

<div class="fb-like" data-send="false" data-width="450" data-show-faces="true" data-font="arial"></div>
--%>

<c:url value="http://www.facebook.com/plugins/like.php" var="pageUrl">
    <c:param name="app_id" value="${fbUtil:getFacebookAppID()}"/>
    <c:param name="" value=""/>
    <c:param name="href" value="${url.server}${renderContext.mainResource.node.url}"/>
    <c:param name="send" value="false"/>
    <c:param name="layout" value="${currentNode.properties.layout.string}"/>
    <c:param name="width" value="${currentNode.properties.width.string}"/>
    <c:param name="show_faces" value="${currentNode.properties.show_faces.string}"/>
    <c:param name="action" value="${currentNode.properties.action.string}"/>
    <c:param name="colorscheme" value="${currentNode.properties.colorscheme.string}"/>
    <c:param name="font" value="${currentNode.properties.font.string}"/>
    <c:param name="height" value="80"/>
</c:url>


<iframe src="${pageUrl}"
        scrolling="no" frameborder="0" style="border:none; overflow:hidden; width:${currentNode.properties.width.string}px; height:80px;" allowTransparency="true"></iframe>