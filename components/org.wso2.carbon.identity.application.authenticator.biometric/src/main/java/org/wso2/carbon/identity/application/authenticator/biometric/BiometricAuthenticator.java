/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.identity.application.authenticator.biometric;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authenticator.biometric.dao.impl.DeviceDAOImpl;
import org.wso2.carbon.identity.application.authenticator.biometric.notification.handler.impl.FirebasePushNotificationSenderImpl;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authenticator.biometric.BiometricAuthenticatorConstants.BIOMETRIC_AUTH_CHALLENGE;

/**
 * Biometric Authenticator class.
 */
public class BiometricAuthenticator extends AbstractApplicationAuthenticator
        implements FederatedApplicationAuthenticator {

    private static final Log log = LogFactory.getLog(BiometricAuthenticator.class);

    @Override
    public String getFriendlyName() {

        return BiometricAuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    public boolean canHandle(HttpServletRequest request) {
        log.info("Signed challenge from the form submitted: " + request.getParameter(BiometricAuthenticatorConstants.SIGNED_CHALLENGE));
        return request.getParameter(BiometricAuthenticatorConstants.SIGNED_CHALLENGE) != null;
    }

    @Override
    public String getContextIdentifier(javax.servlet.http.HttpServletRequest request) {

        log.info("session data key at canhandle method: "+ request.getParameter(BiometricAuthenticatorConstants.SESSION_DATA_KEY) );
        return request.getParameter(BiometricAuthenticatorConstants.SESSION_DATA_KEY);
    }

    @Override
    public String getName() {

        return BiometricAuthenticatorConstants.AUTHENTICATOR_NAME;
    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {

        AuthenticatedUser user = context.getSequenceConfig().getStepMap().get(context.getCurrentStep() - 1).getAuthenticatedUser();
        String username = user.getUserName();

        Map<String, String> authenticatorProperties = context.getAuthenticatorProperties();
        String serverKey = authenticatorProperties.get(BiometricAuthenticatorConstants.SERVER_KEY);
        String fcmUrl = authenticatorProperties.get(BiometricAuthenticatorConstants.FCM_URL);

        String hostname = IdentityUtil.getHostName();
        String serviceProviderName = context.getServiceProviderName();
        String message = username + " is trying to log into " + serviceProviderName + " from " + hostname;
        // TODO: 2019-11-20  support localization.
        String sessionDataKey = request.getParameter(BiometricAuthenticatorConstants.SESSION_DATA_KEY);

        UUID challenge = UUID.randomUUID();
        String randomChallenge = challenge.toString();
        context.setProperty(BIOMETRIC_AUTH_CHALLENGE, randomChallenge);

        DeviceDAOImpl biometricDAO = DeviceDAOImpl.getInstance();
        String deviceID = biometricDAO.getDeviceID(username);
        log.info("challenge is: " + challenge);
        log.info("SDK is : " + sessionDataKey );

        FirebasePushNotificationSenderImpl pushNotificationSender = FirebasePushNotificationSenderImpl.getInstance();
        pushNotificationSender.sendPushNotification(deviceID, serverKey, message, randomChallenge, sessionDataKey);
        try {
            String pollingEndpoint = "https://biometricauthenticator.private.wso2.com:9443/" +
                    "authenticationendpoint/wait.jsp?sessionDataKey=";
            // TODO: 2019-11-20 refer other used cases.see login.jsp and edit accordingly.
            String waitPage = pollingEndpoint + sessionDataKey;
            response.sendRedirect(waitPage);
        } catch (IOException e) {
            log.error("Error when trying to redirect to wait.jsp page", e);
        }
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest httpServletRequest, HttpServletResponse
            httpServletResponse, AuthenticationContext authenticationContext) throws AuthenticationFailedException {

        String randomChallenge = (String) authenticationContext.getProperty(BIOMETRIC_AUTH_CHALLENGE);
        log.info("now in process auth method.");
        if (randomChallenge.equals(httpServletRequest.getParameter(BiometricAuthenticatorConstants.SIGNED_CHALLENGE))) {
            AuthenticatedUser user = authenticationContext.getSequenceConfig().
                    getStepMap().get(authenticationContext.getCurrentStep() - 1).getAuthenticatedUser();
            authenticationContext.setSubject(user);
        }else { authenticationContext.setProperty(BiometricAuthenticatorConstants.AUTHENTICATION_STATUS, true);
            throw new AuthenticationFailedException("Authentication failed! Sent and received challenges are not the same.");
        }
    }

    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<>();

        String firebaseServerKey = "Firebase Server Key";
        Property serverKeyProperty = new Property();
        serverKeyProperty.setName(BiometricAuthenticatorConstants.SERVER_KEY);
        serverKeyProperty.setDisplayName(firebaseServerKey);
        serverKeyProperty.setDescription("Enter the firebase server key ");
        serverKeyProperty.setDisplayOrder(0);
        serverKeyProperty.setRequired(false);
        serverKeyProperty.setConfidential(true);
        configProperties.add(serverKeyProperty);
        // TODO: 2019-11-20 add firebase url and make it optional

        String fcmUrl = "Firebase url";
        Property fcmUrlProperty = new Property();
        fcmUrlProperty.setName(BiometricAuthenticatorConstants.FCM_URL);
        fcmUrlProperty.setDisplayName(fcmUrl);
        fcmUrlProperty.setDescription("Enter the url of firebase endpoint ");
        fcmUrlProperty.setDisplayOrder(1);
        fcmUrlProperty.setConfidential(true);
        configProperties.add(fcmUrlProperty);
        return configProperties;
    }
}
