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
package com.apispoint.platform.service;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

public final class JWTUtils {

    private JWTUtils() {}

    public static String sign(byte[] secret, Map<String, Object> claims) {
        Builder jwt = JWT.create();

        for(Entry<String, Object> claim : claims.entrySet()) {
                 if(claim.getValue() instanceof Boolean) jwt.withClaim(claim.getKey(), (Boolean) claim.getValue());
            else if(claim.getValue() instanceof Date)    jwt.withClaim(claim.getKey(), (Date)    claim.getValue());
            else if(claim.getValue() instanceof Double)  jwt.withClaim(claim.getKey(), (Double)  claim.getValue());
            else if(claim.getValue() instanceof Integer) jwt.withClaim(claim.getKey(), (Integer) claim.getValue());
            else if(claim.getValue() instanceof Long)    jwt.withClaim(claim.getKey(), (Long)    claim.getValue());
            else if(claim.getValue() instanceof String)  jwt.withClaim(claim.getKey(), (String)  claim.getValue());
        }

        return jwt.sign(Algorithm.HMAC256(secret));
    }

    public static Map<String, Claim> verify(byte[] secret, String token) {
        Map<String, Claim> claims = null;

        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
            DecodedJWT jwt = verifier.verify(token);
            claims = jwt.getClaims();
        } catch(Exception e) {}

        return claims;
    }

}
