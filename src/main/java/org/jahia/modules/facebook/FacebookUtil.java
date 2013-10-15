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

package org.jahia.modules.facebook;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jahia.params.valves.FacebookAuthValveImpl;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.usermanager.JahiaFacebookUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerFacebookProvider;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.User;

/**
 * @author Charles Flond Date: 17/04/11 Time: 19:09
 */
public class FacebookUtil {

    private static String permissionList = "";
    private static Map<String, String> permissions = null;

    public static String getFacebookAppID() {
        return ((FacebookAuthValveImpl) SpringContextSingleton.getModuleBean("FacebookAuthValve"))
                .getAppId();
    }

    public static int getFriendsNumber(JahiaUser jahiaUser) {
        if (jahiaUser != null && jahiaUser instanceof JahiaFacebookUser) {
            String access_token = jahiaUser.getProperty("access_token");

            if (access_token != null) {
                // Get the Facebook Client
                FacebookClient facebookClient = new DefaultFacebookClient(access_token);

                // Get friends & feeds
                Connection<User> myFriends = facebookClient.fetchConnection("me/friends",
                        User.class);

                if (myFriends != null & myFriends.getData() != null)
                    return myFriends.getData().size();
                else
                    return -1;
            }
        }

        return -1;
    }

    public static String getPermissionList() {

        if (permissions == null) {
            JahiaUserManagerFacebookProvider provider = (JahiaUserManagerFacebookProvider) SpringContextSingleton
                    .getModuleBean("JahiaUserManagerFacebookProvider");
            FacebookUtil.permissions = provider.getPermissionsMap();
        }

        if (permissionList.length() > 0) {
            return permissionList;
        } else {
            Iterator<Entry<String, String>> entries = permissions.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, String> thisEntry = entries.next();
                permissionList += thisEntry.getKey();
                if (entries.hasNext()) {
                    permissionList += ",";
                }
            }
            return permissionList;
        }
    }
}
