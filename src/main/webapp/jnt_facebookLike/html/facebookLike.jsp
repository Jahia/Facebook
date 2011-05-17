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

 <c:url value="${url.server}${renderContext.mainResource.node.url}" var="pageUrl" />
 <%--
 <iframe src="http://www.facebook.com/plugins/like.php?href=${pageUrl}" scrolling="no" frameborder="0" style="border:none; width:450px; height:80px"></iframe>
 --%>
<iframe src="http://www.facebook.com/plugins/like.php?app_id=${fbUtil:getFacebookAppID()}&amp;href=${pageUrl}&amp;send=${currentNode.properties.displaySendButton.string}&amp;layout=${currentNode.properties.layout.string}&amp;width=${currentNode.properties.width.string}&amp;show_faces=${currentNode.properties.show_faces.string}&amp;action=${currentNode.properties.action.string}&amp;colorscheme=${currentNode.properties.colorscheme.string}&amp;font=${currentNode.properties.font.string}&amp;height=80"
        scrolling="no" frameborder="0" style="border:none; overflow:hidden; width:${currentNode.properties.width.string}px; height:80px;" allowTransparency="true"></iframe>