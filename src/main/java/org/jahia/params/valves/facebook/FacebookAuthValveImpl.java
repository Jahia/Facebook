/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.params.valves.facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.User;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.jahia.api.Constants;
import org.jahia.modules.facebook.FacebookPropertiesMapping;
import org.jahia.modules.facebook.FacebookUtil;
import org.jahia.params.valves.*;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.notification.HttpClientService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Properties;

/**
 * @author Charles Flond Date: 31/01/11 Time: 11:50
 */
public class FacebookAuthValveImpl extends AutoRegisteredBaseAuthValve {

    private static final transient Logger logger = LoggerFactory
            .getLogger(FacebookAuthValveImpl.class);

    private String appId;

    private String appSecret;

    private HttpClientService httpClientService;

    private FacebookPropertiesMapping facebookPropertiesMapping;

    private JahiaUserManagerService userService;

    private CookieAuthConfig cookieAuthConfig;

    public static final String USE_COOKIE = "useCookie";

    public static final String VALVE_RESULT = "login_valve_result";

    public class LoginEvent extends BaseLoginEvent {
        private static final long serialVersionUID = -7356560804745397662L;

        public LoginEvent(Object source, JahiaUser jahiaUser, AuthValveContext authValveContext) {
            super(source, jahiaUser, authValveContext);
        }
    }

    private String getAccessToken(String fbCode, String site, HttpServletRequest request)
            throws IOException {
        // Build the URL to request the access token
        StringBuilder tokenUrl = new StringBuilder();
        tokenUrl.append("https://graph.facebook.com/oauth/access_token?redirect_uri=")
                .append(URLEncoder.encode(request.getRequestURL().toString()+"?site="+site, "UTF-8"))
                .append("&client_id=").append(appId).append("&client_secret=").append(appSecret)
                .append("&code=").append(fbCode);

        // Connection and reading of the access token page
        String result = httpClientService.executeGet(tokenUrl.toString());
        if (result == null) {
            return null;
        }

        // Parsing of the access token page to extract the token and the expiration
        String fbToken = null;
        String[] pairs = result.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length != 2) {
                throw new RuntimeException("Unexpected auth response");
            } else {
                if (kv[0].equals("access_token")) {
                    fbToken = kv[1];
                    break;
                }
            }
        }
        return fbToken;
    }

    public String getAppId() {
        return appId;
    }

    // Invoke method
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {

        // Retrieve the context, the current request and the get parameter (code)
        final AuthValveContext authContext = (AuthValveContext) context;
        final HttpServletRequest request = authContext.getRequest();
        if(authContext.getSessionFactory().getCurrentUser() != null){
            return;
        }
        String fbCode = request.getParameter("code");
        String site = request.getParameter("site");

        // If we have the code from Facebook, we can start to work
        if (fbCode != null) {
            try {

                String fbToken = getAccessToken(fbCode, site, request);

                if (fbToken != null) {
                    // Get the Facebook Client based on the access token
                    FacebookClient facebookClient = new DefaultFacebookClient(fbToken, FacebookUtil.FB_VERSION);

                    // Get the corresponding facebook user
                    final User user = facebookClient.fetchObject("me", User.class, Parameter.with("fields", "id, name, email, locale, first_name, last_name, picture"));
                    Locale userLocale = Locale.getDefault();
                    String[] localeParams =  user.getLocale()!=null? user.getLocale().split("_") : new String[]{};
                    if (localeParams.length > 1) {
                        userLocale = new Locale(localeParams[0], localeParams[1]);
                    }

                    // Create a Jahia Facebook User based on the "business" Facebook User
                    final Properties props = facebookPropertiesMapping.mapProperties(user, fbToken);
                    JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser((JahiaUser) null, "default", userLocale, new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

                            //Look for previous connection's user
                            JCRUserNode userNode = userService.lookupUser(user.getEmail(), session);
                            if(userNode == null){
                                //Create the user in JCR
                                userNode = userService.createUser(user.getEmail(), "SHA-1:*", props, session);
                                if (userNode == null) {
                                    throw new RuntimeException("Cannot create user from access token");
                                }
                            }else{
                                facebookPropertiesMapping.mapProperties(userNode, props);
                            }
                            try {
                                URL urlPicture = new URL(user.getPicture().getUrl());
                                InputStream input = urlPicture.openStream();
                                JCRNodeWrapper filesFolder = userNode.hasNode("files")?userNode.getNode("files"):userNode.addNode("files", "jnt:folder");
                                JCRNodeWrapper pictureNode = filesFolder.uploadFile(user.getId(), input, ContentTypes.IMAGE_JPEG);

                                if(pictureNode != null ){

                                    pictureNode.addMixin("jmix:autoPublish");
                                    userNode.setProperty("j:picture", pictureNode);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            session.save();
                            loginFacebookUser(authContext, request, userNode, session.getLocale());
                            return null;
                        }
                    });
                    return;
                }
            } catch (Exception e) {
                logger.warn("Error authenticating the user via Facebook", e);
            }
        }
        valveContext.invokeNext(context);
    }

    private void loginFacebookUser(AuthValveContext authContext, HttpServletRequest httpServletRequest, JCRUserNode theUser, Locale userLocale){
        if (logger.isDebugEnabled()) {
            logger.debug("User " + theUser + " logged in with facebook.");
        }

        httpServletRequest.getSession().setAttribute(Constants.SESSION_USER, theUser.getJahiaUser());
        httpServletRequest.setAttribute(VALVE_RESULT, "ok");
        authContext.getSessionFactory().setCurrentUser(theUser.getJahiaUser());

        // do a switch to the user's preferred language
        if (userLocale != null) {
            httpServletRequest.getSession().setAttribute(Constants.SESSION_LOCALE, userLocale);
        }

        String useCookie = httpServletRequest.getParameter(USE_COOKIE);
        if ((useCookie != null) && ("on".equals(useCookie))) {
            // the user has indicated he wants to use cookie authentication
            CookieAuthValveImpl.createAndSendCookie(authContext, theUser, cookieAuthConfig);
        }

        SpringContextSingleton.getInstance().publishEvent(new FacebookAuthValveImpl.LoginEvent(this, theUser.getJahiaUser(), authContext));
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public void setHttpClientService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }
    public void setFacebookPropertiesMapping(FacebookPropertiesMapping facebookPropertiesMapping) {
        this.facebookPropertiesMapping = facebookPropertiesMapping;
    }

    public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
        this.cookieAuthConfig = cookieAuthConfig;
    }

    public void setUserService(JahiaUserManagerService userService) {
        this.userService = userService;
    }
    
    private String readURL(URL url) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = url.openStream();
        int r;

        while ((r = is.read()) != -1) {
            baos.write(r);
        }

        return new String(baos.toByteArray());
    }
}
