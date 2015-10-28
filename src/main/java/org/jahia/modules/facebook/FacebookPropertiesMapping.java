package org.jahia.modules.facebook;

import com.restfb.types.User;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import java.util.*;

/**
 * Created by amidani on 19/10/2015.
 */
public class FacebookPropertiesMapping {

    private Map<String, String> facebookProperties = null;
    private Map<String, String> mappedProperties = null;
    private Map<String, String> permissions = null;

    private User fbUser;
    private Properties props;

    public void mapProperties(JCRUserNode jahiaUser, Properties props) throws RepositoryException {
        for (Object key : props.keySet()) {
            jahiaUser.setProperty(key.toString(), (String)props.get(key));
        }
    }

    public Properties mapProperties(User fbUser, String token) {
        this.fbUser = fbUser;
        this.props = new Properties();
        props.setProperty("access_token", token);
        for (String key : this.mappedProperties.keySet()) {
            add(key);
        }

        return props;
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
            } else if ("picture".equals(key)) {
                value = fbUser.getPicture().getUrl();
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
            } /*else if ("education".equals(key)) {
                value = fbUser.getEducation().toString();
            } */else if ("hometown".equals(key)) {
                value = fbUser.getHometownName();
            } else if ("interested_in".equals(key)) {
                value = StringUtils.join(fbUser.getInterestedIn(), ";");
            } else if ("link".equals(key)) {
                value = fbUser.getLink() != null ? fbUser.getLink()
                        .toString() : null;
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
                value = StringUtils.join(fbUser.getWork(), ";");
            }

            if (value != null) {
                props.setProperty(key, value);
            }
        }
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

}
