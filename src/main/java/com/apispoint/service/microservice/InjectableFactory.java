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
package com.apispoint.service.microservice;

import java.util.HashMap;
import java.util.Map;

import com.apispoint.service.microservice.injectors.Injectable;

public class InjectableFactory {

    private static InjectableFactory instance = new InjectableFactory();
    static {
//        instance.registerInjectable("aws-storage-s3",       AwsStorageS3.class.getName());
//        instance.registerInjectable("aws-storage-kms",      AwsStorageKms.class.getName());
//        instance.registerInjectable("aws-database-dynamo",  AwsDatabaseDynamo.class.getName());
//        instance.registerInjectable("aws-sqlquery-athena",  AwsSQLQueryAthena.class.getName());
//        instance.registerInjectable("aws-notification-ses", AwsNotificationSES.class.getName());
//        instance.registerInjectable("aws-notification-sns", AwsNotificationSNS.class.getName());
    }

    private Map<String, String> map = new HashMap<>();

    private InjectableFactory() {}

    public static InjectableFactory getInstance() {
        return instance;
    }

    public void registerInjectable(String key, String klassName) {
        if(key != null && klassName != null)
            map.put(key, klassName);
    }

    public void removeInjectable(String key) {
        map.remove(key);
    }

    public Injectable getInjectable(String key) {
        Injectable inj = null;

        try {
            inj = (Injectable) Class.forName(map.get(key)).getConstructor().newInstance();
        } catch(SecurityException se) {
            System.err.println("Injectable security violation, exiting...");
            System.exit(-100);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return inj;
    }

}
