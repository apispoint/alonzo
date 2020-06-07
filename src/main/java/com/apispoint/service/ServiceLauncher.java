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
package com.apispoint.service;

import java.io.File;
import java.nio.file.Files;
import java.security.Security;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.fips.FipsStatus;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;

public final class ServiceLauncher {

    static {
        System.setProperty("org.bouncycastle.fips.approved_only", "true");
        Security.insertProviderAt(new BouncyCastleFipsProvider(), 1);

        boolean fips_sw_ok = FipsStatus.isReady();
        System.out.println("FIPS_CRYPTO_MODULE_INTEGRITY_OK: " + fips_sw_ok);
        if(fips_sw_ok == false) {
            System.out.println("panic (fips crypto module integrity compromised) exiting...\n\n");
            System.exit(-1000);
        }

        boolean fips_am_ok = CryptoServicesRegistrar.isInApprovedOnlyMode();
        System.out.println("FIPS_CRYPTO_APPROVED_MODE_OK: " + fips_am_ok);
        if(fips_am_ok == false) {
            System.out.println("panic (fips crypto module in unapproved mode) exiting...\n\n");
            System.exit(-1001);
        }
    }

    public static void main(String... args) {
        try {
            String file = args[0];
            JsonObject config = new JsonObject(
                    new String(Files.readAllBytes(
                            (new File(file)).toPath())));

            String base = config.getString("base");

            if(base == null)
                base = "";

            Map<String, Set<String>> methodMap = Service.normalizeMethodMap(config.getJsonObject("methods", new JsonObject()).getMap(), base, true);

            Set<String> paths = new HashSet<>();
            for(Set<String> list : methodMap.values())
                paths.addAll(list);

            DropwizardMetricsOptions drpopts = new DropwizardMetricsOptions();
            drpopts.setEnabled(true);

            for(String path : paths)
                drpopts.addMonitoredHttpServerUri(new Match().setValue(path));

            VertxOptions vopts = new VertxOptions().setMetricsOptions(drpopts);
            Vertx vertx = Vertx.vertx(vopts);

            int instances = config.getInteger(
                    "ap.bls:instances",
                    args.length == 1 ? Runtime.getRuntime().availableProcessors() : Integer.parseInt(args[1]));

            DeploymentOptions dopts = new DeploymentOptions().setConfig(config).setInstances(instances).setWorker(true);
            vertx.deployVerticle(config.getString("ap.bls:bse"), dopts, res -> {
                if (res.succeeded()) {
                    System.out.println("Deployment complete; instances up: " + instances);
                } else {
                    System.out.println(args[0] + " service deployment failed\n" + res.cause().toString() + "\nExiting...");
                    System.exit(-1);
                }
            });
        } catch(Exception e) {
            System.err.println("Exception in ServiceLauncher:\n");
            e.printStackTrace();
            System.exit(-2);
        }
    }

}
