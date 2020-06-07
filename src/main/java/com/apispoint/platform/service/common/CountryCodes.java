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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.apispoint.platform.service.JsonUtils;
import com.apispoint.service.Service;

public class CountryCodes extends Service {

    private static class CountryEntry {
        public CountryEntry(String name, String nameLocale, String iso) {
            this.name       = name;
            this.nameLocale = nameLocale;
            this.iso        = iso;
        }
        public String name;
        public String nameLocale;
        public String iso;
    }

    private String   jsonIso;
    private String[] jsonNames = new String[2];

    private Set<String> setName       = new HashSet<String>();
    private Set<String> setNameLocale = new HashSet<String>();

    private Map<String, String> mapCodes = new HashMap<String, String>();
    private Map<String, String> mapNames = new HashMap<String, String>();

    private void setLocale(Locale locale, boolean setLocale) throws Exception {
        String iso        = locale.getCountry();
        String name       = locale.getDisplayCountry();
        String nameLocale = locale.getDisplayCountry(locale);

        String json = JsonUtils.toJson(
                new CountryEntry(
                        name,
                        setLocale ? nameLocale : null,
                        iso));

        //
        // Set : names[Locale]
        //
        setName.add(name);
        if(setLocale)
            setNameLocale.add(nameLocale);

        //
        // Map :   ISO->CE <ISO-Code,   CountryEntry>
        // Map : Names->CE <Name,       CountryEntry>
        // Map : Names->CE <NameLocale, CountryEntry>
        //
        mapCodes.put(iso, json);
        mapNames.put(name, json);
        if(setLocale)
            mapNames.put(nameLocale, json);

    }

    public boolean initialize(Map<String, Object> map) {
        boolean ok = false;

        try {
            List<String> isos = Arrays.asList(Locale.getISOCountries());
            jsonIso = JsonUtils.toJson(isos);

            Locale[] locales = Locale.getAvailableLocales();

            for(Locale l : locales)
                if(l.getDisplayCountry() != null && "".equals(l.getDisplayCountry()) == false && isos.contains(l.getCountry()))
                    setLocale(l, true);

            for(String iso : isos)
                if(mapCodes.containsKey(iso) == false && isos.contains(iso))
                    setLocale(new Locale("", iso), false);

            jsonNames[0] = JsonUtils.toJson(setName);

            setNameLocale.addAll(setName);
            jsonNames[1] = JsonUtils.toJson(setNameLocale);

            ok = true;
        } catch(Exception e) {}

        return ok;
    }

    public void get(Map<String, String> params, Event evt) {
        boolean lcl = params.containsKey("locale");
        boolean iso = params.containsKey("iso");
        boolean nam = params.containsKey("name");
        boolean cde = params.containsKey("code");

             if(iso) evt.status.body = jsonIso;
        else if(lcl) evt.status.body = jsonNames[1];
        else if(cde) evt.status.body = mapCodes.get(getDecodedParam(params, "code"));
        else if(nam) evt.status.body = mapNames.get(getDecodedParam(params, "name"));
        else         evt.status.body = jsonNames[0];

        evt.status.code = 200;
        evt.end();
    }

}
