package org.jahia.modules.facebook;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.User;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.usermanager.JahiaFacebookUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerFacebookProvider;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Charles Flond
 * Date: 17/04/11
 * Time: 19:09
 * To change this template use File | Settings | File Templates.
 */
public class FacebookUtil {

    private static Map<String, String> permissions = null;
    private static String permissionList = "";

     public static String getPermissionList() {

         if(permissions == null)
         {
             JahiaUserManagerFacebookProvider provider = (JahiaUserManagerFacebookProvider) SpringContextSingleton.getBean("JahiaUserManagerFacebookProvider");
             FacebookUtil.permissions = provider.getPermissionsMap();
         }

         if(!permissionList.isEmpty())
         {
             return permissionList;
         }
         else
         {

            Iterator entries = permissions.entrySet().iterator();
            while (entries.hasNext()) {
              Map.Entry thisEntry = (Map.Entry) entries.next();
              permissionList+=thisEntry.getKey();
              if(entries.hasNext()) permissionList+=",";
            }
            return permissionList;
         }
     }

    public static int getFriendsNumber(JahiaUser jahiaUser)
    {
        if(jahiaUser != null && jahiaUser instanceof JahiaFacebookUser)
        {
            String access_token = jahiaUser.getProperty("access_token");

            if(access_token != null)
            {
                 //Get the Facebook Client
                 FacebookClient facebookClient  = new DefaultFacebookClient(access_token);

                 //Get facebook user
                 User user = facebookClient.fetchObject("me", User.class);

                  //Get friends & feeds
                 Connection<User> myFriends = facebookClient.fetchConnection("me/friends", User.class);

                 if(myFriends!=null & myFriends.getData() !=null)  return myFriends.getData().size();
                 else return -1;
            }
        }

        return -1;
    }

    public static String getFacebookAppID()
    {
          return "149708825083446";
    }
}
