/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.params.valves;

import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerFacebookProvider;
import org.jahia.services.usermanager.JahiaUserManagerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Charles Flond
 * Date: 31/01/11
 * Time: 11:50
 * To change this template use File | Settings | File Templates.
 */
public class FacebookAuthValveImpl extends BaseAuthValve {

    //Log4j
    private static final transient Logger logger = LoggerFactory.getLogger(FacebookAuthValveImpl.class);

    private static String app_id = "149708825083446";//JahiaResourceBundle.getString("Facebook", "app_id",new Locale("secret"), "Facebook");
    private static String app_secret = "9e834945988546f279e9fd19f94508f5";//JahiaResourceBundle.getString("Facebook", "app_secret",new Locale("secret"), "Facebook");

    //Invoke method
    public void invoke(Object context, ValveContext valveContext) throws PipelineException
    {
        //Retrieve the context, the current request and the get parameter (code)
        AuthValveContext authContext = (AuthValveContext) context;
        HttpServletRequest request = authContext.getRequest();
        String fbCode = request.getParameter("code");

        //If we have the code from Facebook, we can start to work
        if (fbCode != null) {
            try {

                //Build the URL to request the access token
                String token_url = "https://graph.facebook.com/oauth/access_token?redirect_uri="
                       + request.getRequestURL()
                       + "&client_id="
                       + app_id  + "&client_secret="
                       + app_secret + "&code=" + fbCode;

                //Connection and reading of the access token page
                URL facebook_auth_url = new URL(token_url);
                String result = readURL (facebook_auth_url);

                //Parsing of the access token page to extract the token and the expiration
                String fbToken = null;
                Integer expires = null;
                String[] pairs = result.split("&");
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    if (kv.length != 2) {
                        throw new RuntimeException("Unexpected auth response");
                    } else {
                        if (kv[0].equals("access_token")) {
                            fbToken = kv[1];
                        }
                        if (kv[0].equals("expires")) {
                            expires = Integer.valueOf(kv[1]);
                        }
                    }
                }

                //Iterate over the provider list to find the JahiaUserManagerFacebookProvider
                List<? extends JahiaUserManagerProvider> v = ServicesRegistry.getInstance().getJahiaUserManagerService().getProviderList();
                for (Iterator<? extends JahiaUserManagerProvider> iterator = v.iterator(); iterator.hasNext();) {
                    JahiaUserManagerProvider userManagerProviderBean = iterator.next();
                    if (userManagerProviderBean.getClass().getName().equals(JahiaUserManagerFacebookProvider.class.getName())) {

                        //Cast the userManagerProvider to a userManagerFacebookProvider
                        JahiaUserManagerFacebookProvider jahiaUserManagerFacebookProvider = (JahiaUserManagerFacebookProvider)userManagerProviderBean;

                        //Send the token to the jahiaUserManagerFacebookProvider
                        //And get a jahia user as a response
                        JahiaUser jfu = jahiaUserManagerFacebookProvider.lookupUserByAccessToken(fbToken,expires);

                        if (jfu != null) {
                               authContext.getSessionFactory().setCurrentUser(jfu);
                               request.getSession().setAttribute(ProcessingContext.SESSION_USER, jfu);
                        }
                        else throw new RuntimeException("Cannot retrieve user from access token");
                        return;
                    }
                }

                /*TO DELETE*/
                /*    //Facebook Client
                    FacebookClient facebookClient  = new DefaultFacebookClient(fbToken);

                    //Get current user
                    User user = facebookClient.fetchObject("me", User.class);

                    //Create a Jahia User Base on the Facebook User
                    JahiaFacebookUser jfu = new JahiaFacebookUser(fbToken,user.getId(),user.getName());
                    authContext.getSessionFactory().setCurrentUser(jfu);  */


            } catch (Exception e) {
                logger.debug("Exception thrown",e);
            }
        }
        else {
            logger.debug("No facebook code provided");
        }
        valveContext.invokeNext(context);
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
