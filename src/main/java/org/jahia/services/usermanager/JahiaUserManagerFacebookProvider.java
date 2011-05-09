package org.jahia.services.usermanager;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.User;
import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Charles Flond
 * Date: 23/01/11
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */
public class JahiaUserManagerFacebookProvider extends JahiaUserManagerProvider {

   // ------------------------------ FIELDS ------------------------------

    public static final String PROVIDER_NAME = "facebook";

    private Map<String, String> facebookProperties = null;

    private Map<String, String> mappedProperties = null;
    private Map<String, String> permissions = null;

    private JCRUserManagerProvider jcrUserManagerProvider;
    private CacheService cacheService = null;

    private static Logger logger = LoggerFactory.getLogger(JahiaUserManagerFacebookProvider.class);

     private Cache<String, Serializable> mUserCache;
     private Cache<String, JahiaUser> mProvidersUserCache;

     //Caches name
     public static final String LDAP_USER_CACHE = "FacebookUsersCache";
    public static final String PROVIDERS_USER_CACHE = "ProvidersUsersCache";


    // --------------------- GETTER / SETTER METHODS ---------------------

    public void setJcrUserManagerProvider(JCRUserManagerProvider jcrUserManagerProvider) {
        this.jcrUserManagerProvider = jcrUserManagerProvider;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setFacebookProperties(Map<String, String> facebookProperties) {

        this.facebookProperties = new HashMap<String, String>();
        this.mappedProperties = new HashMap<String, String>();
        this.permissions = new HashMap<String, String>();
       for (Object key : facebookProperties.keySet()) {
            String keyString = key.toString();
            String value = facebookProperties.get(keyString);
            this.facebookProperties.put(keyString, value);
            if (keyString.endsWith(".attribute.map") && value.equalsIgnoreCase("true")) {
                this.mappedProperties.put(StringUtils.substringBeforeLast(keyString,
                        ".attribute.map"), value);
            }
            else if(keyString.endsWith(".permission") && value.equalsIgnoreCase("true")) {
                String permKey = StringUtils.substringBeforeLast(keyString,".permission");
                this.permissions.put(permKey, value);
            }
       }
    }

    public Map<String, String> getPermissionsMap()
    {
        return this.permissions;
    }


    // -------------------------- OTHER METHODS --------------------------

    //Initialize the cache in the start method
    @Override
    public void start() throws JahiaInitializationException {
        mUserCache = cacheService.getCache(LDAP_USER_CACHE
                + (PROVIDER_NAME.equals(getKey()) ? "" : "-" + getKey()), true);
        mProvidersUserCache = cacheService.getCache(PROVIDERS_USER_CACHE, true);

        logger.debug("JahiaFacebookProvider Initialized");
    }

    //Nothing to do when stopping the provider
    @Override
    public void stop() throws JahiaException {

    }

    //We can't create user in facebook
    @Override
    public JahiaUser createUser(String name, String password, Properties properties) {
        return null;
    }

    //We can't delete user in facebook
    @Override
    public boolean deleteUser(JahiaUser user) {
        return false;
    }

    //We can't get the number of users in facebook :)
    @Override
    public int getNbUsers() {
        return -1;
    }

    //We can't get the user list in facebook
    @Override
    public List<String> getUserList() {
        return null;
    }

    //We can't get the username list in facebook
    @Override
    public List<String> getUsernameList() {
        return null;
    }

    //@todo : implement it
    @Override
    public boolean login(String userKey, String userPassword) {
        return true;
    }

    //Method called by the valve to get the JahiaFacebookUser - Provided paramas : facebook token
    public JahiaFacebookUser lookupUserByAccessToken(String fbToken, Integer expires) {

        //Get the Facebook Client by specifying the facebook token
        FacebookClient facebookClient  = new DefaultFacebookClient(fbToken);

        //Get the current facebook user
        User user = facebookClient.fetchObject("me", User.class);

        //Create a Jahia User Base on the Facebook User
        JahiaFacebookUser jfu =  facebookToJahiaUser(user,fbToken);

        return jfu;
    }

    //Method to transform a "business" facebook user in jahia facebook user
    private JahiaFacebookUser facebookToJahiaUser(User facebookUser, String facebookToken)
    {
        JahiaFacebookUser jfu = null;

        //Map facebook properties to jahia properties
        UserProperties userProps = new UserProperties();
        //if(mappedProperties.containsKey("access_token") && facebookToken!=null) userProps.setUserProperty("access_token", new UserProperty("access_token",facebookToken,true));
        if(mappedProperties.containsKey("j:facebookID") && facebookUser.getId()!=null) userProps.setUserProperty("j:facebookID", new UserProperty("j:facebookID",facebookUser.getId(),true));
        if(mappedProperties.containsKey("j:firstName") && facebookUser.getFirstName()!=null) userProps.setUserProperty("j:firstName", new UserProperty("j:firstName",facebookUser.getFirstName(),true));
        if(mappedProperties.containsKey("j:lastName") && facebookUser.getLastName()!=null) userProps.setUserProperty("j:lastName", new UserProperty("j:lastName",facebookUser.getLastName(),true));
        if(mappedProperties.containsKey("j:gender") && facebookUser.getGender()!=null) userProps.setUserProperty("j:gender", new UserProperty("j:gender",facebookUser.getGender(),true));
        if(mappedProperties.containsKey("preferredLanguage") && facebookUser.getLocale()!=null) userProps.setUserProperty("preferredLanguage",new UserProperty("preferredLanguage",facebookUser.getLocale(),true));
        if(mappedProperties.containsKey("j:email") && facebookUser.getEmail()!=null) userProps.setUserProperty("j:email", new UserProperty("j:email",facebookUser.getEmail(),true));
        if(mappedProperties.containsKey("j:about") && facebookUser.getAbout()!=null) userProps.setUserProperty("j:about",new UserProperty("j:about",facebookUser.getAbout(),true));
        if(mappedProperties.containsKey("j:birthDate") && facebookUser.getBirthday()!=null) userProps.setUserProperty("j:birthDate",new UserProperty("j:birthDate",facebookUser.getBirthday(),true));


        //@todo : map others facebook properties to "new" jahia properties

        //Create the jahia facebook user with the proper properties
        jfu = new JahiaFacebookUser(JahiaUserManagerFacebookProvider.PROVIDER_NAME,facebookUser.getId(),facebookUser.getId(),userProps);

        //Update cache
        if (jfu != null) {
            //new cache to populate : cross providers only based upon names...
            mProvidersUserCache.put("k" + jfu.getUserKey(), jfu);

            // name storage for speed
            mUserCache.put("n" + jfu.getUsername(), new JahiaUserWrapper(jfu));
            mProvidersUserCache.put("n" + jfu.getUsername(), jfu);
        }
        // use wrappers in local cache
        mUserCache.put("k" + jfu.getUserKey(), new JahiaUserWrapper(jfu));

        //Perform a lookup to check if we already have this user in the JCR
        JCRUser jcrUser = (JCRUser) jcrUserManagerProvider.lookupExternalUser(jfu);

        //If we don't have this user yet in the JCR, perform the deploy
        if (jcrUser == null) {

            try {
                //Deploy and then lookup to get the JCR User instance
                JCRStoreService.getInstance().deployExternalUser(jfu);
                jcrUser = (JCRUser) jcrUserManagerProvider.lookupExternalUser(jfu);
            } catch (RepositoryException e) {
                logger.error("Error deploying external user '" + jfu.getUsername() + "' for provider '" + this.PROVIDER_NAME
                        + "' into JCR repository. Cause: " + e.getMessage(), e);
            }
        }
        Properties myProperties = jcrUser.getProperties();
        jcrUser.setProperty("access_token",facebookToken);
        Properties myProperties2 = jcrUser.getProperties();
        return jfu;
    }

    //Method that allows to retrieve a jahia facebook user by userkey (prefix + username)
    @Override
    public JahiaUser lookupUserByKey(String userKey) {

        //Try to get the user from cache
        JahiaFacebookUser jahiaFacebookUser = null;
        jahiaFacebookUser = (JahiaFacebookUser)mProvidersUserCache.get("k" + userKey);

        //Else retrieve him by a classic lookup
        if(jahiaFacebookUser==null)
        {
            String name = removeKeyPrefix(userKey);
            jahiaFacebookUser = (JahiaFacebookUser)lookupUser(name);
        }
        return jahiaFacebookUser;
    }

    //Method that allows to retrieve a jahia facebook user by name (or username)
    //@todo: check if it works fine, not tested yet without cache
    @Override
    public JahiaUser lookupUser(String name) {

        JahiaFacebookUser jahiaFacebookUser = null;

        //Try to get the user from cache
        jahiaFacebookUser = (JahiaFacebookUser)mProvidersUserCache.get("n" + name);

        //Else do a lookup in the JCR to get the access token and then request facebook to get the user
         if(jahiaFacebookUser==null)
        {
            //Retrieve the user from the JCR
            JCRUser jcrUser = jcrUserManagerProvider.lookupExternalUser(name);

            if(jcrUser !=null)
            {
                //Get the access token
                String access_token = jcrUser.getProperty("access_token");

                if(access_token != null)
                {
                    //Get the Facebook Client based on the access token
                    FacebookClient facebookClient  = new DefaultFacebookClient(access_token);

                    //Get the corresponding facebook user
                    User user = facebookClient.fetchObject("me", User.class);

                    //Create a Jahia Facebook User based on the "business" Facebook User
                    JahiaFacebookUser jfu =  facebookToJahiaUser(user,access_token);

                    return jfu;
                }
                 else return null;
            }
            else return null;
        }

        return jahiaFacebookUser;
    }


     //We can't searh users directly in facebook
    @Override
    public Set<JahiaUser> searchUsers(Properties searchCriterias) {
        return null;
    }

    //Method to update the cache
    @Override
    public void updateCache(JahiaUser jahiaUser) {
        mUserCache.put("k" + jahiaUser.getUserKey(), new JahiaUserWrapper(jahiaUser));
        mProvidersUserCache.put("k" + jahiaUser.getUserKey(), jahiaUser);
        mUserCache.put("n" + jahiaUser.getUsername(), new JahiaUserWrapper(jahiaUser));
        mProvidersUserCache.put("n" + jahiaUser.getUsername(), jahiaUser);
    }

    //@todo: to implement
    @Override
    public boolean userExists(String name) {
        return false;
    }

    //Method to get the username base on the userkey
    private String removeKeyPrefix(String userKey) {
        if (userKey.startsWith("{" + getKey() + "}")) {
            return userKey.substring(getKey().length() + 2);
        } else {
            return userKey;
        }
    }

    //Class to wrapp the user in the internal cache
    public static class JahiaUserWrapper implements Serializable {

        private static final long serialVersionUID = -2955706620534674310L;

        // the internal user, only defined when creating object
        private JahiaUser user;

        /**
         * Constructor.
         *
         * @param ju JahiaUser, a user from a provider.
         */
        public JahiaUserWrapper(JahiaUser ju) {
            user = ju;
        }

        /**
         * Get the internal user.
         *
         * @return JahiaUser, the internal user.
         */
        public JahiaUser getUser() {
            return user;
        }
    }
}
