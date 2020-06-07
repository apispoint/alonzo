/*
 * Copyright APIS Point, LLC or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.apispoint.platform.service.provision;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import com.apispoint.platform.service.JWTUtils;
import com.apispoint.platform.service.JsonUtils;
import com.apispoint.platform.service.ServiceUtils;
import com.apispoint.service.microservice.MicroService;

public class Signin extends MicroService {

    private static SecureRandom rng = new SecureRandom();

    private static final Logger LOGGER = Logger.getGlobal();

    private static Base64.Decoder d64 = Base64.getDecoder();
    private static Base64.Encoder e64 = Base64.getEncoder();

    public static enum CREATION_MODE { ACTIVE, DENY, PENDING, SUSPENDED };

    public static enum ACCOUNT_STATE { NEW, PENDING, ACTIVE, EXPIRED, DELAYED, SUSPENDED };

    private static final String AUTHN_ERR_MSG = "{\"message\":\"Invalid user or password\", \"state\":\"INVALID_CREDENTIALS\"}";
    private static final String AUTHN_REQ_MSG = "{\"message\":\"Requesting MFA code\", \"state\":\"PENDING_CREDENTIALS\"}";
    private static final String ACCNT_LCK_MSG = "{\"message\":\"Account suspended\", \"state\":\"SUSPENDED_ACCOUNT\"}";
    private static final String AUTHN_LCK_MSG = "{\"message\":\"Authorization only\", \"state\":\"AUTHORIZATION_ONLY\"}";

    private String  algorithm     = null;
    private long    attemptWindow = -1;
    private int     attemptLimit  = -1;
    private long    permitDelay   = -1;
    private boolean allowClaims   = false;

    private CREATION_MODE createMode = CREATION_MODE.ACTIVE;

    protected static class Credentials {
        public String user;
        public String password;
        public Map<String, Object> claims;
    }

    private interface Factor {}

    private static class PWD implements Factor {
        public String                password;
        public Map<String, Password> passwords = new HashMap<String, Password>();
    }

    private static class Password {
        public String salt;
        public String hash;
        public String algo;
        public long   createTime;
    }

    private static class OTP implements Factor {
        public transient byte[] qrcode = null;
        public transient byte[] key    = null;

        public String secret;
        public long   createTime = System.currentTimeMillis();
    }

    private static class User {
        public String uuid;
        public String user;

        public long attemptPoll;
        public long attemptTime;

        public long createTime;
        public long accessTime;
        public long permitTime;

        public ACCOUNT_STATE state = ACCOUNT_STATE.NEW;
        public Map<String, Object> claims;

        public PWD pwd = new PWD();
        public OTP otp = null;
    }

    public String generateUniqueIndexValue() {
        return ServiceUtils.generateCustomerID();
    }

    public long generateExpiry() {
        return (LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(authPeriod)).toEpochSecond(ZoneOffset.UTC);
    }

    public boolean initialize(Map<String, Object> map) {
        boolean failedAuth = authPassOk == false && authTimeOk == false;

        algorithm     = map.containsKey("algorithm")     ? (String)  map.get("algorithm")     : "SHA3-512";
        attemptWindow = map.containsKey("attemptWindow") ? (long)    map.get("attemptWindow") : 900000;
        attemptLimit  = map.containsKey("attemptLimit")  ? (int)     map.get("attemptWindow") : 3;
        permitDelay   = map.containsKey("permitDelay")   ? (long)    map.get("permitDelay")   : 1800000;
        allowClaims   = map.containsKey("allowClaims")   ? (boolean) map.get("allowClaims")   : false;

        String cMode = map.containsKey("createMode") ? (String) map.get("createMode") : CREATION_MODE.ACTIVE.toString();
        createMode = CREATION_MODE.valueOf(cMode);

        if(map.containsKey("algorithm")) {
            String algo = (String) map.get("algorithm");
            try {
                MessageDigest.getInstance(algo);
                algorithm = algo;
            } catch(Exception e) {
                LOGGER.warning(
                        String.format("Invalid algorithm = %s; defaulting to %s",
                                algo,
                                algorithm));
            }
        }

        if(failedAuth)
            LOGGER.severe("Invalid authorization configuration, exiting....");

        return super.initialize(map) && failedAuth == false;
    }

    public void configure(Map<String, Object> map) {
    }

    protected byte[] getDigest(byte[] pass, byte[] salt, String algo) {
        byte[] digest = null;

        try {
            MessageDigest md = MessageDigest.getInstance(algo);

            byte[] saltpass = new byte[salt.length + pass.length];
            System.arraycopy(salt, 0, saltpass, 0, salt.length);
            System.arraycopy(pass, 0, saltpass, salt.length, pass.length);

            digest = md.digest(saltpass);
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }

        return digest;
    }
    protected boolean checkSaltAndDigest(Credentials cred, User user) {
        boolean ret = false;

        try {
            Password usrPwd = user.pwd.passwords.get(user.pwd.password);

            byte[] _pass = cred.password.getBytes("UTF-8");
            byte[] _salt = d64.decode(usrPwd.salt);
            String _hash = e64.encodeToString(getDigest(_pass, _salt, usrPwd.algo));

            ret = usrPwd.hash.equalsIgnoreCase(_hash);
        } catch(UnsupportedEncodingException e) { e.printStackTrace(); }

        return ret;
    }
    protected boolean checkAccessType(User user) {
        return
                user.state == ACCOUNT_STATE.NEW ||
                user.state == ACCOUNT_STATE.ACTIVE;
    }
    protected boolean newSaltAndDigest(String password, User user) {
        boolean ret = false;

        try {
            byte[] _salt = new byte[16];
            rng.nextBytes(_salt);
            byte[] _pass = password.getBytes("UTF-8");

            Password pwd = new Password();

            pwd.algo       = algorithm;
            pwd.salt       = e64.encodeToString(_salt);
            pwd.hash       = e64.encodeToString(getDigest(_pass, _salt, pwd.algo));
            pwd.createTime = System.currentTimeMillis();

            user.pwd.password = Long.toString(pwd.createTime);
            user.pwd.passwords.put(user.pwd.password, pwd);

            ret = true;
        } catch(UnsupportedEncodingException e) { e.printStackTrace(); }

        return ret;
    }

    private Credentials parseCredentials(String json) {
        Credentials cred = null;

        try {
            cred = JsonUtils.fromJson(json, Credentials.class);
            if(
                    cred.user     == null
                 || cred.password == null
                 || cred.password.isEmpty()
                 || (cred.user = cred.user.trim()).isEmpty()
              )
                cred = null;
            else
                cred.user = ServiceUtils.normalizeUser(cred.user);
        } catch(Exception e) { e.printStackTrace(); }

        return cred;
    }
    private User getUser(Credentials cred) {
        if(cred == null) return null;

        User user = null;
        try {
            Iterator<String> iter = collection.queryCollectionDocument("user", "user", cred.user).iterator();
            String json;
            if(iter.hasNext()) {
                json = iter.next().replaceFirst(collection.index, "uuid");
                user = JsonUtils.fromJson(json, User.class);
            }
        } catch(Exception e) { e.printStackTrace(); }

        return user;
    }

    public void post(Map<String, String> params, Event evt, ByteBuffer body) {
        boolean auth = params.containsKey("auth");
        Credentials cred = parseCredentials(new String(body.array()));
        User user = getUser(cred);

        process(cred, user, evt, auth);
        evt.end();
    }
    public void put(Map<String, String> params, Event evt, ByteBuffer body) {
    }

    private void process(Credentials cred, User user, Event evt, boolean auth) {
        evt.status.body = AUTHN_ERR_MSG;
        evt.status.code = 403;

        // Credentials cannot be null, EVER!
        if(cred == null) return;

             if(user == null && auth == false) createUser(cred, evt.status);
        else if(user != null && auth == true)  authorize(cred, user, evt.status);
    }
    private void authorize(Credentials cred, User user, Status sts) {
        try {
            // If the account is SUSPENDED fail-fast authorization
            if(user.state == ACCOUNT_STATE.SUSPENDED) {
                sts.body = ACCNT_LCK_MSG;
                return;
            }

            if(System.currentTimeMillis() - user.attemptTime > attemptWindow)                       user.attemptPoll = 0;
            if(System.currentTimeMillis() > user.permitTime && user.state == ACCOUNT_STATE.DELAYED) user.state = ACCOUNT_STATE.ACTIVE;

            boolean checkedTYP = checkAccessType(user);
            boolean checkedPWD = checkSaltAndDigest(cred, user);

            if(checkedTYP && checkedPWD) {
                // Indicate MFA is required, IFF type and usr/pwd are valid
                sts.body = AUTHN_REQ_MSG;
                sts.code = 401;
            }
            else if(checkedTYP && checkedPWD) {
                user.attemptPoll = 0;
                user.accessTime  = System.currentTimeMillis();
                user.state       = ACCOUNT_STATE.ACTIVE;

                Map<String, Object> claims = new HashMap<String, Object>();

                if(user.claims != null && user.claims.isEmpty() == false)
                    claims.putAll(user.claims);

                // Standard claims supersede user claims
                claims.put("iss", authIssuer);
                claims.put("sub", user.uuid);
                claims.put("exp", Date.from(Instant.ofEpochSecond(generateExpiry())));

                Map<String, Object> tokenMap = new HashMap<>();
                tokenMap.put("token", JWTUtils.sign(authPasswd.getBytes(), claims));

                if(user.otp != null && user.otp.qrcode != null) {
                    Map<String, String> map = new HashMap<>();
                    map.put("qrcode", new String(user.otp.qrcode));
                    map.put("secret", new String(user.otp.key));
                    tokenMap.put("otp", map);
                }

                sts.body = JsonUtils.toJson(tokenMap);
                sts.code = 200;
            }
            else {
                user.attemptPoll++;
                user.attemptTime = System.currentTimeMillis();

                if(user.attemptPoll > attemptLimit) {
                    user.state = ACCOUNT_STATE.DELAYED;
                    user.permitTime = System.currentTimeMillis() + permitDelay;
                }
            }
        } catch(Exception e) { e.printStackTrace(); } finally {
            try {
                collection.putCollectionDocument(JsonUtils.toJson(user).replace("uuid", collection.index));
            } catch(Exception e) { e.printStackTrace(); }
        }
    }
    protected void createUser(Credentials cred, Status sts) {
        if(createMode == CREATION_MODE.DENY) {
            sts.body = AUTHN_LCK_MSG;
            return;
        }

        try {
            User user = new User();

            user.uuid = generateUniqueIndexValue();
            user.user = cred.user;
            user.createTime = System.currentTimeMillis();
            user.state = ACCOUNT_STATE.NEW;

            if(allowClaims && cred.claims != null && cred.claims.isEmpty() == false)
                user.claims = cred.claims;

            if(newSaltAndDigest(cred.password, user))
                authorize(cred, user, sts);

            sts.code = 200;
        } catch(Exception e) { e.printStackTrace(); }
    }

}
