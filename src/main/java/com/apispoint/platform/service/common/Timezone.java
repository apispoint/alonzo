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
package com.apispoint.platform.service.common;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.apispoint.platform.service.JsonUtils;
import com.apispoint.service.Service;

public class Timezone extends Service {

    private static class TzEntry {
        public TzEntry(String name, String id, double dst, double offset) {
            this.name = name;
            this.id = id;
            this.dst = dst;
            this.offset = offset;
        }
        public String name;
        public String id;
        public double dst;
        public double offset;
    }

    private Map<String, String> tz_map = new HashMap<String, String>();
    private String tz_ids;

    public boolean initialize(Map<String, Object> map) {
        boolean ok = false;

        try {
            String[] ids = TimeZone.getAvailableIDs();
            tz_ids = JsonUtils.toJson(ids);
            TimeZone tz;
            for(String id : ids) {
                tz = TimeZone.getTimeZone(id);
                tz_map.put(id, JsonUtils.toJson(new TzEntry(
                    tz.getDisplayName(),
                    id,
                    (tz.getDSTSavings() != 0 ? ((double) tz.getDSTSavings()) / 1000 / 60 / 60 : 0),
                    ((double) tz.getRawOffset()) / 1000 / 60 / 60)));
            }
            ok = true;
        } catch(Exception e) {}

        return ok;
    }

    public void get(Map<String, String> params, Event evt) {
        String id = getDecodedParam(params, "id");

        evt.status.body = id != null ? tz_map.get(id) : tz_ids;
        evt.status.code = 200;
        evt.end();
    }

}
