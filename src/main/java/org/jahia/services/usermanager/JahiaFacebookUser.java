package org.jahia.services.usermanager;

/**
 * Created by IntelliJ IDEA.
 * User: Charles Flond
 * Date: 23/01/11
 * Time: 15:24
 * To change this template use File | Settings | File Templates.
 */
public class JahiaFacebookUser extends BaseJahiaExternalUser {


    //Constructor
    protected JahiaFacebookUser(String providerKey, String username, String userKey, UserProperties userProperties)
    {
          super(providerKey, username, "{"+providerKey+"}" + userKey, userProperties);
    }

    @Override
    protected boolean removePropertyExternal(String key) {
        //Not supported by Facebook
        return false;
    }

    @Override
    protected boolean setPropertyExternal(String key, String value) {
        //Not supported by Facebook
        return false;
    }

    public boolean setPassword(String password) {
        //Not supported by Facebook
        return false;
    }
}
