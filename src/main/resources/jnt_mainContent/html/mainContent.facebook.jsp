<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fbUtil" uri="http://www.jahia.org/tags/facebook" %>
<%@taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>

<jcr:nodeProperty node="${currentNode}" name="image" var="image"/>
<c:set var="pageUrl" value="${url.server}${url.base}${renderContext.mainResource.node.path}.html"/>
<div class="maincontent">
    <h3 class="title"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h3>
        <c:if test="${!empty image}">
            <div class="imagefloat${currentNode.properties.align.string}">
            			<img src="${image.node.url}" alt="${image.node.url}"/>
                        </div>
        </c:if>
		 ${currentNode.properties.body.string}
    <c:set var="imgUrl" value="${url.server}${image.node.url}"/>
    <%-- trick for using external URLs for image if available - otherwise images are not shown from localhost --%>
    <c:set var="imgMeta" value="${not empty image.node.properties['jcr:description'] ? image.node.properties['jcr:description'].string : ''}"/>
    <c:if test="${fn:startsWith(imgMeta, 'http://')}">
        <c:set var="imgUrl" value="${imgMeta}"/>
    </c:if>
    <c:set var="fbTitle" value="${functions:escapeJavaScript(currentNode.displayableName)}"/>
    <c:set var="fbDescr" value="${functions:escapeJavaScript(functions:removeHtmlTags(currentNode.properties.body.string))}"/>
    <span style="float: right"><a href="#share" onclick='fbPostToFeed("${functions:escapeJavaScript(pageUrl)}", "${functions:escapeJavaScript(imgUrl)}", "${fbTitle}", "www.jahia.com", "${fbDescr}"); return false;' title="Share on Facebook"><img src="${url.currentModule}/images/fb.png" alt=" " width="25" height="25"/><img src="${url.currentModule}/images/post-button.jpg" alt="Share on Facebook" title="Share on Facebook"/></a></span>
</div>
<br class="clear"/>
<template:addResources type="javascript" resources="jquery.min.js,http://connect.facebook.net/en_US/all.js"/>
<template:addResources>
<script type="text/javascript">
$(document).ready(function() {
	FB.init({appId: "${fbUtil:getFacebookAppID()}", status: true, cookie: true});
});
function fbPostToFeed(lnk, pict, title, capt, descr) {
    var obj = {
      method: 'feed',
      link: lnk,
      picture: pict,
      name: title,
      caption: capt,
      description: descr,
      redirect_uri: '${pageUrl}'
    };
    FB.ui(obj, null);
  }
</script>
</template:addResources>
<c:if test="${not requestScope.jahiaFacebookPostIncluded}">
<c:set var="jahiaFacebookPostIncluded" value="true" scope="request"/>
<div id="fb-root"></div>
</c:if>