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

package org.jahia.services.usermanager.facebook;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerProvider;
import org.jahia.services.usermanager.UserProperties;
import org.jahia.services.usermanager.UserProperty;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.User;

/**
 * @author Charles Flond Date: 23/01/11 Time: 15:26
 */
public class JahiaUserManagerFacebookProvider extends JahiaUserManagerProvider {

    private class FacebookPropertiesMapping {

        private User fbUser;
        private UserProperties props;

        private FacebookPropertiesMapping(User fbUser, String token) {
            super();
            this.fbUser = fbUser;
            this.props = new UserProperties();
            props.setUserProperty("access_token", new UserProperty("access_token", token, true));
            for (String key : mappedProperties.keySet()) {
                add(key);
            }
        }

        void add(String key) {
            if (Boolean.valueOf(mappedProperties.get(key))) {
                String value = null;
                if ("j:facebookID".equals(key)) {
                    value = fbUser.getId();
                } else if ("j:firstName".equals(key)) {
                    value = fbUser.getFirstName();
                } else if ("j:lastName".equals(key)) {
                    value = fbUser.getLastName();
                } else if ("j:gender".equals(key)) {
                    value = fbUser.getGender();
                } else if ("preferredLanguage".equals(key)) {
                    value = fbUser.getLocale();
                } else if ("j:email".equals(key)) {
                    value = fbUser.getEmail();
                } else if ("j:about".equals(key)) {
                    value = fbUser.getAbout();
                } else if ("j:birthDate".equals(key)) {
                    Date birthdayAsDate = fbUser.getBirthdayAsDate();
                    if (birthdayAsDate != null) {
                        GregorianCalendar c = new GregorianCalendar();
                        c.setTimeInMillis(birthdayAsDate.getTime());
                        value = ISO8601.format(c);
                    }
                } else if ("name".equals(key)) {
                    value = fbUser.getName();
                } else if ("bio".equals(key)) {
                    value = fbUser.getBio();
                } else if ("education".equals(key)) {
                    value = fbUser.getEducation().toString();
                } else if ("hometown".equals(key)) {
                    value = fbUser.getHometownName();
                } else if ("interested_in".equals(key)) {
                    value = fbUser.getInterestedIn().toString();
                } else if ("link".equals(key)) {
                    value = fbUser.getLink().toString();
                } else if ("significant_other".equals(key)) {
                    value = fbUser.getSignificantOther() != null ? fbUser.getSignificantOther()
                            .toString() : null;
                } else if ("political".equals(key)) {
                    value = fbUser.getPolitical();
                } else if ("religion".equals(key)) {
                    value = fbUser.getReligion();
                } else if ("relationship_status".equals(key)) {
                    value = fbUser.getRelationshipStatus();
                } else if ("website".equals(key)) {
                    value = fbUser.getWebsite();
                } else if ("work".equals(key)) {
                    value = fbUser.getWork().toString();
                }

                if (value != null) {
                    props.setUserProperty(key, new UserProperty(key, value, true));
                }
            }
        }

        UserProperties getUserProperties() {
            return props;
        }

    }

    // Class to wrapp the user in the internal cache
    public static class JahiaUserWrapper implements Serializable {

        private static final long serialVersionUID = -2955706620534674310L;

        // the internal user, only defined when creating object
        private JahiaUser user;

        /**
         * Constructor.
         * 
         * @param ju
         *            JahiaUser, a user from a provider.
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

    // Caches name
    public static final String LDAP_USER_CACHE = "FacebookUsersCache";

    private static Logger logger = LoggerFactory.getLogger(JahiaUserManagerFacebookProvider.class);
    public static final String PROVIDER_NAME = "facebook";

    public static final String PROVIDERS_USER_CACHE = "ProvidersUsersCache";
    private CacheService cacheService = null;

    private Map<String, String> facebookProperties = null;

    private JCRUserManagerProvider jcrUserManagerProvider;
    private Map<String, String> mappedProperties = null;

    private Cache<String, JahiaUser> mProvidersUserCache;
    private Cache<String, Serializable> mUserCache;

    private Map<String, String> permissions = null;

    // We can't create user in facebook
    @Override
    public JahiaUser createUser(String name, String password, Properties properties) {
        return null;
    }

    // We can't delete user in facebook
    @Override
    public boolean deleteUser(JahiaUser user) {
        return false;
    }

    // Method to transform a "business" facebook user in jahia facebook user
    private JahiaFacebookUser facebookToJahiaUser(User facebookUser, String facebookToken) {
        JahiaFacebookUser jfu = null;

        // Map facebook properties to jahia properties
        UserProperties userProps = new FacebookPropertiesMapping(facebookUser, facebookToken)
                .getUserProperties();

        // Create the jahia facebook user with the proper properties
        jfu = new JahiaFacebookUser(JahiaUserManagerFacebookProvider.PROVIDER_NAME,
                facebookUser.getId(), facebookUser.getId(), userProps);

        // Update cache
        if (jfu != null) {
            // new cache to populate : cross providers only based upon names...
            mProvidersUserCache.put("k" + jfu.getUserKey(), jfu);

            // name storage for speed
            mUserCache.put("n" + jfu.getUsername(), new JahiaUserWrapper(jfu));
            mProvidersUserCache.put("n" + jfu.getUsername(), jfu);
        }
        // use wrappers in local cache
        mUserCache.put("k" + jfu.getUserKey(), new JahiaUserWrapper(jfu));

        // Perform a lookup to check if we already have this user in the JCR
        JCRUser jcrUser = (JCRUser) jcrUserManagerProvider.lookupExternalUser(jfu);

        // If we don't have this user yet in the JCR, perform the deploy
        if (jcrUser == null) {

            try {
                // Deploy and then lookup to get the JCR User instance
                JCRStoreService.getInstance().deployExternalUser(jfu);
                jcrUser = (JCRUser) jcrUserManagerProvider.lookupExternalUser(jfu);
            } catch (RepositoryException e) {
                logger.error(
                        "Error deploying external user '" + jfu.getUsername() + "' for provider '"
                                + PROVIDER_NAME + "' into JCR repository. Cause: " + e.getMessage(),
                        e);
            }
        }
        return jfu;
    }

    // We can't get the number of users in facebook :)
    @Override
    public int getNbUsers() {
        return -1;
    }

    public Map<String, String> getPermissionsMap() {
        return this.permissions;
    }

    // We can't get the user list in facebook
    @Override
    public List<String> getUserList() {
        return Collections.emptyList();
    }

    // We can't get the username list in facebook
    @Override
    public List<String> getUsernameList() {
        return Collections.emptyList();
    }

    // @todo : implement it
    @Override
    public boolean login(String userKey, String userPassword) {
        return true;
    }

    // Method that allows to retrieve a jahia facebook user by name (or username)
    // @todo: check if it works fine, not tested yet without cache
    @Override
    public JahiaUser lookupUser(String name) {

        JahiaFacebookUser jahiaFacebookUser = null;

        // Try to get the user from cache
        jahiaFacebookUser = (JahiaFacebookUser) mProvidersUserCache.get("n" + name);

        // Else do a lookup in the JCR to get the access token and then request facebook to get the user
        if (jahiaFacebookUser == null) {
            // Retrieve the user from the JCR
            JCRUser jcrUser = jcrUserManagerProvider.lookupExternalUser(name);

            if (jcrUser != null) {
                // Get the access token
                String access_token = jcrUser.getProperty("access_token");

                if (access_token != null) {
                    // Get the Facebook Client based on the access token
                    FacebookClient facebookClient = new DefaultFacebookClient(access_token);

                    // Get the corresponding facebook user
                    User user = facebookClient.fetchObject("me", User.class);

                    // Create a Jahia Facebook User based on the "business" Facebook User
                    JahiaFacebookUser jfu = facebookToJahiaUser(user, access_token);

                    return jfu;
                } else
                    return null;
            } else
                return null;
        }

        return jahiaFacebookUser;
    }

    // Method called by the valve to get the JahiaFacebookUser - Provided paramas : facebook token
    public JahiaFacebookUser lookupUserByAccessToken(String fbToken) {

        // Get the Facebook Client by specifying the facebook token
        FacebookClient facebookClient = new DefaultFacebookClient(fbToken);

        // Get the current facebook user
        User user = facebookClient.fetchObject("me", User.class);

        // Create a Jahia User Base on the Facebook User
        JahiaFacebookUser jfu = facebookToJahiaUser(user, fbToken);

        return jfu;
    }

    // Method that allows to retrieve a jahia facebook user by userkey (prefix + username)
    @Override
    public JahiaUser lookupUserByKey(String userKey) {

        // Try to get the user from cache
        JahiaFacebookUser jahiaFacebookUser = null;
        jahiaFacebookUser = (JahiaFacebookUser) mProvidersUserCache.get("k" + userKey);

        // Else retrieve him by a classic lookup
        if (jahiaFacebookUser == null) {
            String name = removeKeyPrefix(userKey);
            jahiaFacebookUser = (JahiaFacebookUser) lookupUser(name);
        }
        return jahiaFacebookUser;
    }

    // Method to get the username base on the userkey
    private String removeKeyPrefix(String userKey) {
        if (userKey.startsWith("{" + getKey() + "}")) {
            return userKey.substring(getKey().length() + 2);
        } else {
            return userKey;
        }
    }

    // We can't search users directly in facebook
    @Override
    public Set<JahiaUser> searchUsers(Properties searchCriterias) {
        return Collections.emptySet();
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
                this.mappedProperties.put(
                        StringUtils.substringBeforeLast(keyString, ".attribute.map"), value);
            } else if (keyString.endsWith(".permission") && value.equalsIgnoreCase("true")) {
                String permKey = StringUtils.substringBeforeLast(keyString, ".permission");
                this.permissions.put(permKey, value);
            }
        }
    }

    public void setJcrUserManagerProvider(JCRUserManagerProvider jcrUserManagerProvider) {
        this.jcrUserManagerProvider = jcrUserManagerProvider;
    }

    // Initialize the cache in the start method
    @Override
    public void start() throws JahiaInitializationException {
        mUserCache = cacheService.getCache(LDAP_USER_CACHE
                + (PROVIDER_NAME.equals(getKey()) ? "" : "-" + getKey()), true);
        mProvidersUserCache = cacheService.getCache(PROVIDERS_USER_CACHE, true);

        logger.debug("JahiaFacebookProvider Initialized");
    }

    // Nothing to do when stopping the provider
    @Override
    public void stop() throws JahiaException {

    }

    // Method to update the cache
    @Override
    public void updateCache(JahiaUser jahiaUser) {
        mUserCache.put("k" + jahiaUser.getUserKey(), new JahiaUserWrapper(jahiaUser));
        mProvidersUserCache.put("k" + jahiaUser.getUserKey(), jahiaUser);
        mUserCache.put("n" + jahiaUser.getUsername(), new JahiaUserWrapper(jahiaUser));
        mProvidersUserCache.put("n" + jahiaUser.getUsername(), jahiaUser);
    }

    // @todo: to implement
    @Override
    public boolean userExists(String name) {
        return false;
    }
}
