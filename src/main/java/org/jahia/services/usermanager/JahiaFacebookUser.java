package org.jahia.services.usermanager;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Charles Flond
 * Date: 23/01/11
 * Time: 15:24
 * To change this template use File | Settings | File Templates.
 */
public class JahiaFacebookUser implements JahiaUser {

    //Logger
    private static Logger logger = LoggerFactory.getLogger(JahiaFacebookUser.class);

    //User properties
    private UserProperties mProperties = new UserProperties ();

    private String username;
    private String path = null;

    /** User home page property * */
    private static final String mHOMEPAGE_PROP = "user_homepage";

    //Constructor
    public JahiaFacebookUser(String username, UserProperties userProperties)
    {
          this.username=username;
          this.mProperties=userProperties;
    }

    //Equals method
     public boolean equals (Object another) {

        if (this == another) return true;

        if (another != null && this.getClass() == another.getClass()) {
            return (getUserKey().equals(((JahiaUser) another).getUserKey()));
        }
        return false;
    }


    //Return the username
    public String getUsername() {
        return username;
    }

    //Return the name
    public String getName() {
        return username;
    }

    //Return the user key
    public String getUserKey() {
        return "{facebook}"+getUsername();
    }

    /**
    * @deprecated use getUserProperties() instead
    * @return Properties the properties returned here should NEVER be modified,
    * only modifications through setProperty() are supported and serialized.
    */
    public Properties getProperties() {
         if (mProperties != null) {
            return mProperties.getProperties();
        } else {
            return null;
        }
    }

    /**
    * The properties here should not be modified, as the modifications will
    * not be serialized. Use the setProperty() method to modify a user's
    * properties.
    * @return UserProperties
    */
    public UserProperties getUserProperties() {
        /*if (!propLoaded ) {
            getFacebookProvider().mapFacebookToJahiaProperties(mProperties, this);
            propLoaded = true;
        }*/
        return mProperties;
    }


    public String getProperty(String key) {
        if (key != null) {
            if (mProperties.getProperty(key) != null) {
                return mProperties.getProperty(key);
            }
            if (getUserProperties() != null) {
                return getUserProperties().getProperty(key);
            }
        }
        return null;
    }

    public UserProperty getUserProperty(String key) {
         if ((getUserProperties() != null) && (key != null)) {
            return getUserProperties().getUserProperty (key);
        }
        return null;
    }

    /**
     * Remove the specified property from the properties list.
     *
     * @param key Property's name.
     *
     * @return Return true on success or false on any failure.
     * @By Charles : assume that his one is also deprecated
     */
    public boolean removeProperty(String key) {
        boolean result = false;
        UserProperties mProperties = getUserProperties();
        if (mProperties == null) {
            return result;
        }

        if ((key != null) && (key.length () > 0) && (!mProperties.isReadOnly(key))) {
            JCRUserManagerProvider userManager = (JCRUserManagerProvider) SpringContextSingleton.getBean("JCRUserManagerProvider");
            JCRUser jcrUser = (JCRUser) userManager.lookupExternalUser(this);
            if(jcrUser!=null) {
                jcrUser.removeProperty(key);
                result = true;
            }

        }
        //Predrag
        if (result) {
            mProperties.removeUserProperty (key);
        }
        //Predrag
        return result;
    }

    /**
     * Change the user's password.
     *
     * @param password New user's password
     *
     * @return Return true id the old password is the same as the current one and
     *         the new password is valid. Return false on any failure.
     *
     * @todo FIXME : not supported in this read-only facebook implementation
     */
    public boolean setPassword (String password) {
        return false;
    }

    public boolean setProperty(String key, String value) {
        boolean result = false;
        UserProperties mProperties = getUserProperties();
        if (mProperties == null) {
            return result;
        }

        if ((key != null) && (value != null) && (!mProperties.isReadOnly(key))) {

            JCRUserManagerProvider userManager = (JCRUserManagerProvider) SpringContextSingleton.getBean("JCRUserManagerProvider");
            JCRUser jcrUser = (JCRUser) userManager.lookupExternalUser(this);
            if (jcrUser == null) {
                // deploy
                try {
                    JCRStoreService.getInstance().deployExternalUser(this);
                    jcrUser = (JCRUser) userManager.lookupExternalUser(this);
                } catch (RepositoryException e) {
                    logger.error("Error deploying external user '" + getName() + "' for provider '" + getProviderName()
                            + "' into JCR repository. Cause: " + e.getMessage(), e);
                }
            }
            if(jcrUser!=null) {
                result = jcrUser.setProperty(key, value);
            }

            // End remove --------------------
            if (result) {
                try {
                    mProperties.setProperty(key, value);
                } catch (UserPropertyReadOnlyException uproe) {
                    logger.warn("Cannot set read-only property " + key);
                }
                ServicesRegistry.getInstance().getJahiaUserManagerService().updateCache(this);
            }
        }
        return result;
    }


    //-------------------------------------------------------------------------
    /**
     * Returns the user's home page id.
     * -1 : undefined
     *
     * @return int The user homepage id.
     */
    public int getHomepageID() {
        if (getUserProperties() != null) {

            try {
                // Get the home page from the Jahia DB.
                // By default an external user is represented with a -1 user ID.
                String value = mProperties.getProperty (mHOMEPAGE_PROP);
                if (value == null) {
                    return -1;
                }
                return Integer.parseInt (value);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return -1;
    }

    //-------------------------------------------------------------------------
    /**
     * Set the home page id.
     *
     * @param id The user homepage id.
     *
     * @return false on error
     */
     public boolean setHomepageID(int id) {
         // Set the home page into the Jahia DB.
        // By default an external user is represented with a -1 user ID.
        return setProperty (mHOMEPAGE_PROP, String.valueOf (id));
    }


    public boolean isMemberOfGroup(int siteID, String name) {
         // Get the services registry
        ServicesRegistry servicesRegistry = ServicesRegistry.getInstance ();
        if (servicesRegistry != null) {

            // get the group management service
            JahiaGroupManagerService groupService =
                    servicesRegistry.getJahiaGroupManagerService ();

            // lookup the requested group
            JahiaGroup group = groupService.lookupGroup (siteID, name);
            if (group != null) {
                return group.isMember (this);
            }
        }
        return false;
    }

    //@TODO : implement it
    public boolean isAdminMember(int siteID) {
        return false;
    }

    //A facebook user will never be the ROOT user
    public boolean isRoot() {
        return false;
    }

    //We can't check the password for a facebook user. Facebook do it for us
    public boolean verifyPassword(String password) {
        return false;
    }

    //Return the provider name
    public String getProviderName() {
        return JahiaUserManagerFacebookProvider.PROVIDER_NAME;
    }

    //Return the path where the user is stored in the JCR
     public String getLocalPath() {
        if (path == null) {
            path = ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule().getPathForUsername(getUsername());
        }
        return path;
    }

    //Return the Facebook User provider
    private JahiaUserManagerFacebookProvider getFacebookProvider() {
        return ((JahiaUserManagerFacebookProvider) SpringContextSingleton.getModuleBean("JahiaUserManagerFacebookProvider"));
    }




}
