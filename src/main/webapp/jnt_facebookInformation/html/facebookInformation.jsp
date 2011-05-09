<%@ page import="com.restfb.FacebookClient" %>
<%@ page import="com.restfb.DefaultFacebookClient" %>
<%@ page import="com.restfb.types.User" %>
<%@ page import="com.restfb.types.Page" %>
<%@ page import="org.jahia.services.usermanager.JahiaFacebookUser" %>
<%@ page import="org.jahia.services.content.JCRSessionFactory" %>
<%@ page import="com.restfb.Connection" %>
<%@ page import="com.restfb.types.Post" %>
<%@ page import="com.restfb.json.JsonObject" %>
<%@ page import="com.restfb.Parameter" %>

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

<div style="width: 850px; border: medium dotted; padding: 10px;">

    Friends Number : ${fbUtil:getFriendsNumber(renderContext.user)}

    <%--
    <c:choose>
        <c:when test="${!empty sessionScope.currentFacebookUser}">

            <%
                    //Retrieve the Jahia Facebook Client from the session
                    JahiaFacebookUser jfu = (JahiaFacebookUser)session.getAttribute("currentFacebookUser");

                    //Get the Facebook Client
                    FacebookClient facebookClient  = new DefaultFacebookClient(jfu.getAccess_token());

                    //Get current user
                    User user = facebookClient.fetchObject("me", User.class);

                     //Get friends & feeds
                    Connection<User> myFriends = facebookClient.fetchConnection("me/friends", User.class);
                    Connection<Post> myFeed = facebookClient.fetchConnection("me/feed", Post.class);

                     //Get photos and save the first photo
                    JsonObject photosConnection = facebookClient.fetchObject("me/photos", JsonObject.class);
                    String firstPhotoUrl="" ;
                    if(photosConnection.getJsonArray("data").length() > 0)
                    {
                        firstPhotoUrl = photosConnection.getJsonArray("data").getJsonObject(0).getString("source");
                    }

            %>


                <img src="https://graph.facebook.com/${sessionScope.currentFacebookUser.id}/picture" style="float:left;"/>
                <h2 style="padding-left: 80px;padding-bottom:30px;padding-top:5px;">${sessionScope.currentFacebookUser.name}</h2>
                <h3>Birthday : <%=user.getBirthday()%></h3>
                <h3>Email : <%=user.getEmail()%></h3>
                <h3>Number of friends : <%=myFriends.getData().size()%></h3><br/>
                <h3>A random picture of your profile : </h3><img src="<%=firstPhotoUrl%>"/><br/>

        </c:when>
        <c:otherwise>
            You should be connected to view your facebook informations.
        </c:otherwise>
    </c:choose>  --%>
</div>