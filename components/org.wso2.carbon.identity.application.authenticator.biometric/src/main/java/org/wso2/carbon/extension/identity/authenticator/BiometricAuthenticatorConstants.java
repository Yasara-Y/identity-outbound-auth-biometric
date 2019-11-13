/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.extension.identity.authenticator;

/**
 * Utils class for Biometric Authenticator Constants.
 */
public class BiometricAuthenticatorConstants {
    public static final String AUTHENTICATOR_NAME = "biometric";
    public static final String AUTHENTICATOR_FRIENDLY_NAME = "biometricAuthenticator";
    public static final String SESSION_DATA_KEY = "sessionDataKey";
    //biometric authorize endpoint URL
    public static final String BIOMETRIC_OAUTH_ENDPOINT = "";
    //biometric token  endpoint URL
    public static final String BIOMETRIC_TOKEN_ENDPOINT = "";
    //biometric user info endpoint URL
    public static final String BIOMETRIC_USERINFO_ENDPOINT = "";

    public static final String USERNAME = "Username";

    public static final String SERVER_KEY = "ServerKey";
    public static final String T = "&t=";
    public static final String SDK_WEB = "&SDKWeb=";
    public static final String SIGNED_CHALLENGE = "signedChallenge";
    public static final String AUTHENTICATED_USER = "authenticatedUser";


}