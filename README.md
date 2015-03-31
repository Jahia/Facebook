Facebook
========

This module integrate with the RESTFB library, to provide Facebook Connect (SSO), Graph API (display data), ...

Prerequisite
-------------
This module is designed to be deployed on Jahia Digital Factory 7.0.0.1 and higher.

Usage
-----
See this video on how to use the module. Although it was produced for Jahia 6.6, it is almost exactly the same
procedure to use it on Jahia 7.0.0.1

https://www.youtube.com/watch?v=s1CSWkK9iOo

In order to make this module work on your website you need to put your facebook app id and secret in the spring file
(src/main/resources/META-INF/spring/mod-facebook.xml) as FacebookAuthValve bean properties.

```
<bean id="FacebookAuthValve" class="org.jahia.params.valves.facebook.FacebookAuthValveImpl">
        <property name="appId" value="APP_ID"/>
        <property name="appSecret">
            <bean class="org.jahia.utils.EncryptionUtils$EncryptedPasswordFactoryBean">
                <property name="password" value="ENCRYPTED_APP_SECRET" />
            </bean>
        </property>
        ...
```

Where APP_ID is your app id and ENCRYPTED_APP_SECRET is your app secret after encryption.
You can then deploy the module on your Jahia Server.

Refer to the sections below to know how to create a facebook app and how to encrypt your app secret.

Create a Facebook app
---------------------
Please refer to the following page if you don't know how to create a Facebook app :
https://developers.facebook.com/docs/web/tutorials/scrumptious/register-facebook-application

Once your app is created you can go to its dashboard, there you will find the app id and app secret.
You will also need to add your Jahia server URL in the app authorized domains.


Encrypt your app secret
-----------------------
You will have to encrypt your app secret before putting it in the module spring file.

Go to the Jahia JCR Console : http://localhost:8081/jahiace/tools/jcrConsole.jsp and paste the following script :
```
String secret ="APP_SECRET";
org.jasypt.encryption.StringEncryptor encryptor = org.jahia.utils.EncryptionUtils.getStringEncryptor();
System.out.println("### Encrypted secret : "+encryptor.encrypt(secret)+" ###");
```

Replace APP_SECRET by your app secret. Then click on the Execute button and go to your Digital factory server console
to see your encrypted app secret displayed.

Known issues
------------
- When redeploying the module, you need to also restart Jahia otherwise you'll have strange issues such as class loading
or auth issues.

Using on 7.0.0.0 CE
-------------------
As this module is designed to work with Jahia 7.0.0.1 and higher, you will have to modify its source code a little bit
to use it on Digital Factory 7.0.0.0 CE.

Open the module spring file (src/main/resources/META-INF/spring/mod-facebook.xml) and add the following bean
declaration :

    <bean class="org.jahia.params.valves.AuthPipelineInitializer">
        <property name="authPipeline" ref="authPipeline"/>
    </bean>

You will now be able to safely deploy the module.