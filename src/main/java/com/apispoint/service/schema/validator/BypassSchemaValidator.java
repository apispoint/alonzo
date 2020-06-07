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

import java.util.Collection;

public class BypassSchemaValidator implements SchemaValidator {

    public boolean init(byte[] schema)                             { return true; }
    public boolean isValid(byte[] data, Collection<String> errors) { return true; }

    public String getFormat() { return null; }
    public String getSchema() { return null; }

}
