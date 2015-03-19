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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.jahia.params.ProcessingContext;
import org.jahia.params.valves.AuthValveContext;
import org.jahia.params.valves.AutoRegisteredBaseAuthValve;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.notification.HttpClientService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.facebook.JahiaUserManagerFacebookProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Charles Flond Date: 31/01/11 Time: 11:50
 */
public class FacebookAuthValveImpl extends AutoRegisteredBaseAuthValve {

    private static final transient Logger logger = LoggerFactory
            .getLogger(FacebookAuthValveImpl.class);

    private String appId;

    private String appSecret;

    private HttpClientService httpClientService;

    private String getAccessToken(String fbCode, HttpServletRequest request)
            throws IOException {
        // Build the URL to request the access token
        StringBuilder tokenUrl = new StringBuilder();
        tokenUrl.append("https://graph.facebook.com/oauth/access_token?redirect_uri=")
                .append(URLEncoder.encode(request.getRequestURL().toString(), "UTF-8"))
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
        AuthValveContext authContext = (AuthValveContext) context;
        HttpServletRequest request = authContext.getRequest();
        String fbCode = request.getParameter("code");

        // If we have the code from Facebook, we can start to work
        if (fbCode != null) {
            try {
                JahiaUser jfu = null;

                String fbToken = getAccessToken(fbCode, request);
                if (fbToken != null) {
                    JahiaUserManagerFacebookProvider provider = (JahiaUserManagerFacebookProvider) ServicesRegistry
                            .getInstance().getJahiaUserManagerService().getProvider("facebook");
                    jfu = provider.lookupUserByAccessToken(fbToken);
                }

                if (jfu != null) {
                    authContext.getSessionFactory().setCurrentUser(jfu);
                    request.getSession().setAttribute(ProcessingContext.SESSION_USER, jfu);
                } else {
                    throw new RuntimeException("Cannot retrieve user from access token");
                }
                return;
            } catch (Exception e) {
                logger.warn("Error authenticating the user via Facebook", e);
            }
        }
        valveContext.invokeNext(context);
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
