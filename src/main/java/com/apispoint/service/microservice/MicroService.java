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

import com.apispoint.service.Service;
import com.apispoint.service.microservice.injectors.Database;
import com.apispoint.service.microservice.injectors.Injectable;
import com.apispoint.service.microservice.injectors.Storage;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public abstract class MicroService extends Service {

    protected Database collection;
    protected Storage  persistent;

    protected Map<String, Injectable> injectable = new HashMap<>();

    public boolean initialize(Map<String, Object> map) {
        boolean success = true;
        try {
            if(map.containsKey("injectable")) {
                JsonArray injectables = (JsonArray) map.get("injectable");
                JsonObject injectable;
                Injectable inj;
                for(int i = 0; i < injectables.size(); i++) {
                    injectable = injectables.getJsonObject(i);
                    inj = InjectableFactory.getInstance().getInjectable(injectable.getString("injectablekey"));

                    this.injectable.put(injectable.getString("mapkey"), inj) ;

                    //
                    // Injectable assignment shortcut
                    //
                         if("collection".equalsIgnoreCase(injectable.getString("mapkey"))) collection = (Database) inj;
                    else if("persistent".equalsIgnoreCase(injectable.getString("mapkey"))) persistent = (Storage) inj;

                    success &= inj.init(
                            injectable.containsKey("init")
                            ? injectable.getJsonObject("init").getMap()
                            : new HashMap<>());
                    if(success == false) break;
                }
            }
        } catch(Exception e) {
            success = false;
            e.printStackTrace();
            System.err.println("MicroService initialization failure");
        }

        return success;
    }

}
