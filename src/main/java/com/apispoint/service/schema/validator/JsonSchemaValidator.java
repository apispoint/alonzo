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
package com.apispoint.service.schema.validator;

import mjson.Json;
import mjson.Json.Schema;

import java.util.Collection;

public class JsonSchemaValidator implements SchemaValidator {

    private Schema sch = null;
    private String txt = null;

    public boolean init(byte[] schema) {
        sch = Json.schema(Json.read(new String(schema)));
        txt = sch.toJson().toString();
        return true;
    }

    public boolean isValid(byte[] data, Collection<String> errors) {
        Json status = sch.validate(Json.read(new String(data)));
        boolean errored = status.has("errors");

        if(errored)
            for(Json e: status.at("errors").asJsonList())
                errors.add(e.asString());

        return !errored;
    }

    public String getFormat() { return "json"; }
    public String getSchema() { return txt; }

}
