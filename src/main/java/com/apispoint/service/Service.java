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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.Security;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.apispoint.platform.service.JWTUtils;
import com.apispoint.platform.service.JsonUtils;
import com.apispoint.service.resource.ResourceLoader;
import com.apispoint.service.schema.validator.BypassSchemaValidator;
import com.apispoint.service.schema.validator.JsonSchemaValidator;
import com.apispoint.service.schema.validator.SchemaValidator;
import com.auth0.jwt.interfaces.Claim;

public abstract class Service extends AbstractVerticle {

    private static final Set<String> METRIC_FIELDS    = new HashSet<String>();
    private static final Set<String> ALL_HTTP_METHODS = new HashSet<String>();

    static {
        ALL_HTTP_METHODS.add("GET");
        ALL_HTTP_METHODS.add("DELETE");
        ALL_HTTP_METHODS.add("POST");
        ALL_HTTP_METHODS.add("PUT");
        ALL_HTTP_METHODS.add("OPTIONS");
        ALL_HTTP_METHODS.add("CONNECT");
        ALL_HTTP_METHODS.add("TRACE");
        ALL_HTTP_METHODS.add("PATCH");
        ALL_HTTP_METHODS.add("HEAD");

        METRIC_FIELDS.add("OPEN-CONNECTIONS");
        METRIC_FIELDS.add("OPEN-WEBSOCKETS");
        METRIC_FIELDS.add("OPEN-NETSOCKETS");
    }

    private Set<String> methodsSet = new HashSet<String>();
    private Set<String> rmMetricSet = new HashSet<String>();

    private SchemaValidator schemaValidator = null;

    private static AtomicBoolean infoDisplayed = new AtomicBoolean(false);

    private static class CORS {
        public String          origin  = "*";
        public Set<String>     headers = cors_headers;
        public Set<HttpMethod> methods = cors_methods;
        public boolean         credentials = false;

        private static Set<String>     cors_headers = new HashSet<String>();
        private static Set<HttpMethod> cors_methods = new HashSet<HttpMethod>();

        static {
            cors_headers.add("Origin");
            cors_headers.add("X-Requested-With");
            cors_headers.add("Content-Type");
            cors_headers.add("Accept");
            cors_headers.add("Authorization");
        }
    }

    public static class Event {
        public static interface IChunk {
            public void chunk(String chunk);
        }

        private Boolean ended = false;
        private HttpServerResponse event;

        private Event(HttpServerResponse event) {
            this.event = event;
        }

        public Status status = new Status();

        public void setChunked(boolean b) {
            event.setChunked(b);
        }
        public void writeChunked(String chunk) {
            event.write(chunk);
        }
        public void endChunked() {
            if(event.isChunked()) {
                event.end();
                ended = true;
            }
        }

        public void end() {
            if(event.isChunked() == false) {
                event.headers().set("Content-Type", status.mime);

                event.setStatusCode(status.code);
                if(status.mesg != null)
                    event.setStatusMessage(status.mesg);

                if(status.data != null) event.end(Buffer.buffer(status.data));
                else if(status.body != null) event.end(status.body);
                else                         event.end();

                ended = true;
            }
        }
    }

    public static class Status {
        public Status() {}
        public Status(int code) { this.code = code; }

        public int    code = 400;
        public byte[] data = null;
        public String body = null;
        public String mesg = null;
        public String mime = "application/json; charset=utf-8";
    }

    protected boolean authPassOk = false;
    protected boolean authTimeOk = false;
    protected String  authPasswd = null;
    protected String  authIssuer = null;
    protected long    authPeriod = 0;
    private   String  authStatus = "Off";

    private Set<String> authorization = new HashSet<String>();

    public static String getDecodedParam(Map<String, String> params, String p) {
        String decoded = null;
        String value = params.get(p);
        if(value != null)
            try {
                decoded = java.net.URLDecoder.decode(value, "UTF-8");
            } catch(Exception e) {}

        return decoded;
    }

    private static String getSchemaErrMsg(Collection<String> errs) {
        JsonObject msg = new JsonObject();
        msg.put("message", "Schema validation failed");

        JsonArray reasons = new JsonArray();
        for(String err : errs)
            reasons.add(err);
        msg.put("reasons", reasons);

        return msg.toString();
    }
    private static String normalizeUrl(String s, String base) {
        return ("/" + base + "/" + s).replaceAll("/{2,}", "/");
    }
    private static String normalizeMethod(String method) {
        return method.trim().toUpperCase();
    }
    private static void normalizeMethodCollection(Set<String> methods)
    {
        String norm;
        String[] list = methods.toArray(new String[methods.size()]);
        methods.clear();

        for(String method : list) {
            norm = normalizeMethod(method);
            if(ALL_HTTP_METHODS.contains(norm))
                methods.add(norm);
        }
    }
    protected static Map<String, Set<String>> normalizeMethodMap(Map<String, Object> methodMap, String base, boolean list) {
        String norm;
        Set<String> paths;
        List<String> tmpPaths;
        Map<String, Set<String>> normMap = new HashMap<>();

        for(Entry<String, Object> entry : methodMap.entrySet()) {
            norm = normalizeMethod(entry.getKey());
            if(ALL_HTTP_METHODS.contains(norm)) {
                if(list) tmpPaths = (List<String>) entry.getValue();
                else     tmpPaths = (List<String>) ((JsonArray) entry.getValue()).getList();

                paths = new HashSet<>();
                for(String path : tmpPaths)
                    paths.add(normalizeUrl(path, base));
                normMap.put(norm, paths);
            }
        }

        return normMap;
    }

    private boolean grant(String method, MultiMap headers, MultiMap params, Status sts) {
        boolean methodRequiresAuth = false;
        for(String m : authorization)
            methodRequiresAuth |= method.equalsIgnoreCase(m);

        if(authorization.isEmpty() || methodRequiresAuth == false)
            return true;

        String _tkn = getToken(headers);
        String _sub = authenticToken(_tkn);
        if(_sub == null) {
            sts.code = 401;
            return false;
        }
        params.add("_sub", _sub);
        params.add("_tkn", _tkn);

        return true;
    }
    private String getToken(MultiMap headers) {
        String headerValue = headers.get("Authorization");
        String[] split = headerValue != null ? headerValue.split(" ") : null;

        if(split != null && split.length == 2 && split[0].equals("Bearer") && split[1].trim().length() > 0)
            return split[1].trim();

        return null;
    }
    private String authenticToken(String token) {
        String _sub = null;

        Map<String, Claim> map = JWTUtils.verify(authPasswd.getBytes(), token);
        if(map != null && map.containsKey("sub"))
            _sub = map.get("sub").asString();

        return _sub;
    }
    private Map<String, String> seedParams(MultiMap params, MultiMap headers, HttpServerRequest req) {
        Map<String, String> map = new HashMap<String, String>();

        map.put("_path",  req.path());
        map.put("_query", req.query());
        map.put("_uri",   req.uri());
        for(Entry<String, String> entry : params.entries())
            map.put(entry.getKey(), entry.getValue());
        for(Entry<String, String> entry : headers.entries())
            map.put(entry.getKey(), entry.getValue());

        return map;
    }
    private ByteBuffer seedBuffer(Buffer buffer) {
        ByteBuffer bbuffer = ByteBuffer.allocate(buffer != null ? buffer.length() : 0);

        if(buffer != null) {
            bbuffer.put(buffer.getBytes());
            bbuffer.rewind();
        }

        return bbuffer;
    }

    private void route(Router router, Map<String, Set<String>> methodMap) {
        Service service = this;
        String key;
        HttpMethod httpMethod = null;

        for(Entry<String, Set<String>> entry : methodMap.entrySet()) {
            key = entry.getKey();

            switch(key) {
                case "DELETE": httpMethod = HttpMethod.DELETE; break;
                case    "GET": httpMethod = HttpMethod.GET;    break;
                case   "POST": httpMethod = HttpMethod.POST;   break;
                case    "PUT": httpMethod = HttpMethod.PUT;    break;
                default:       httpMethod = null;
            }

            if(httpMethod != null) {
                methodsSet.add(key);

                for(String path : entry.getValue()) {
                    //
                    // Add Body Handler for non-get verbs
                    //
                    if(httpMethod != HttpMethod.GET)
                        router.route(httpMethod, path).handler(BodyHandler.create());

                    //
                    // Add Service Handler for verb methods
                    //
                    router.route(httpMethod, path).blockingHandler(event -> {
                        Event evt = new Event(event.response());
                        try {
                            Buffer buffer = event.getBody();
                            HttpMethod reqMethod = event.request().method();
                            MultiMap params = event.request().params();
                            MultiMap header = event.request().headers();

                            if(grant(reqMethod.toString(), header, params, evt.status)) {
                                Collection<String> schemaErrs = new ArrayList<>();
                                boolean schemaValid = schemaValidator.isValid((buffer != null ? buffer : Buffer.buffer()).getBytes(), schemaErrs);
                                if(schemaValid || HttpMethod.GET.equals(reqMethod)) {
                                    Class<?>[] types = new Class<?>[(HttpMethod.GET.equals(reqMethod) ? 2 : 3)];
                                    Object[] args = new Object[types.length];

                                    types[0] = Map.class;
                                    args[0] = seedParams(params, header, event.request());
                                    types[1] = Event.class;
                                    args[1] = evt;
                                    if(types.length == 3) {
                                        types[2] = ByteBuffer.class;
                                        args[2] = seedBuffer(buffer);
                                    }

                                    Method method = service.getClass().getMethod(
                                            reqMethod.toString().toLowerCase(),
                                            types);
                                    method.invoke(service, args);
                                }
                                else {
                                    evt.status.code = 405;
                                    evt.status.body = getSchemaErrMsg(schemaErrs);
                                }
                            }
                        } catch(Exception e) {
                            e.printStackTrace();
                            evt.status.code = 500;
                        }
                        finally {
                            if(evt.ended == false)
                                evt.end();
                        }
                    }, false);
                }
            }
        }
    }

    public void start(Promise<Void> future) {
        JsonObject config = config();

        String host = config.getString("host");
        int    port = config.getInteger("port");
        String base = config.getString("base");
        String schm = config.getString("schema");

        if(base == null)
            base = "";

        String stspth, schpth;
        stspth = normalizeUrl("/status", base);
        schpth = normalizeUrl("/schema", base);

        try {
            authorization.addAll(config.getJsonArray("authorization", new JsonArray()).getList());
            normalizeMethodCollection(authorization);

            authPasswd = ResourceLoader.getInstance().getResource(config.getString("authPasswd", "")).toString();
            authPeriod = config.getLong("authPeriod", 0L);
            authIssuer = config.getString("authIssuer", "n/a");
            if(authPasswd != null) {
                authStatus = "Err";
                authPassOk = authPasswd.length() > 0;
                authTimeOk = authPeriod > 0;

                     if(authPassOk && authTimeOk)          authStatus = "Pass/TTL";
                else if(authPassOk && authTimeOk == false) authStatus = "Pass/---";
            }
        } catch(Exception e) {
            future.fail("Authorization initialization failed");
        }

        //
        // Critical Initialization
        //

        // Schema
        boolean schemaInitialized = false;
        try {
            byte[] schema = null;

            if(schm != null) {
                schema = ResourceLoader.getInstance().getResource(schm).getBytes();
                schemaValidator = new JsonSchemaValidator();
            }
            else
                schemaValidator = new BypassSchemaValidator();

            schemaInitialized = schemaValidator.init(schema);
        } catch(Exception e) {}
        if(!schemaInitialized) {
            future.fail("Schema initialization failed");
        }

        // Service
        boolean serviceInitialized = false;
        try {
            JsonObject service_init = config.getJsonObject("service_init", new JsonObject());
            serviceInitialized = initialize(service_init.getMap());
        } catch(Exception e) {}
        if(!serviceInitialized) {
            future.fail("Service initialization failed");
        }

        //
        // Soft Configuration
        //
        JsonObject service_config = config.getJsonObject("service_config", new JsonObject());
        try {
            configure(service_config.getMap());
        } catch(Exception e) {
            future.fail("Service configuration failed");
        }

        Map<String, Set<String>> methodMap = normalizeMethodMap(config.getJsonObject("methods", new JsonObject()).getMap(), base, false);

        Set<String> paths = new HashSet<>();
        for(Set<String> list : methodMap.values())
            paths.addAll(list);

        String vf = null;
        String vp = null;
        try {
            vf = ResourceLoader.getInstance().getResource(config.getString("vaultFile")).toString();
            vp = ResourceLoader.getInstance().getResource(config.getString("vaultPass")).toString();
        } catch(Exception e) {
            future.fail("Vault initialization failed");
        }

        boolean ssl = (vf != null && vp != null);
        HttpServerOptions httpopts = new HttpServerOptions();

        httpopts.setCompressionSupported(config.getBoolean("compressionSupported", Boolean.TRUE));
        httpopts.setTcpKeepAlive(config.getBoolean("keepAlive", Boolean.FALSE));

        if(ssl) {
            httpopts.setSsl(ssl);
            String storeType = config.getString("ksFormat", "p12");

            if("jks".equals(storeType)) httpopts.setKeyStoreOptions((new JksOptions()).setPath(vf).setPassword(vp));
            else                        httpopts.setPfxKeyCertOptions((new PfxOptions()).setPath(vf).setPassword(vp));
        }

        HttpServer server = vertx.createHttpServer(httpopts);

        Router router = Router.router(vertx);

        //
        // CORS Handler
        //
        CORS cors = null;
        if(config.containsKey("cors")) {
            try {
                JsonObject jo = config.getJsonObject("cors");

                // Default CORS
                if(jo.isEmpty()) {
                    cors = new CORS();
                    for(String method : methodMap.keySet())
                        cors.methods.add(HttpMethod.valueOf(method.toUpperCase()));
                }
                else {
                    cors = JsonUtils.fromJson(config.getJsonObject("cors").toString(), CORS.class);
                }

                router.route().handler(
                    CorsHandler
                        .create((String)                  cors.origin)
                        .allowedHeaders((Set<String>)     cors.headers)
                        .allowedMethods((Set<HttpMethod>) cors.methods)
                        .allowCredentials(                cors.credentials)
                );
            } catch (Exception e) {
                future.fail("CORS initialization failed");
            }
        }

        //
        // Route GET for Service Status
        //
        MetricsService metricsService = MetricsService.create(vertx);

        router.get(stspth).handler(routingContext -> {
            Duration d = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
            long  day = d.toDays();
            long  hrs = d.minusDays(day).toHours();
            long  mns = d.minusDays(day).minusHours(hrs).toMinutes();
            float sec = d.minusDays(day).minusHours(hrs).minusMinutes(mns).toMillis() / 1000.0f;

            JsonObject metrics = metricsService.getMetricsSnapshot(server);
            metrics.put("uptime", String.format("%s%d:%d:%2.3f",  (day > 0 ? (day + "-") : ""), hrs, mns, sec));

            // Remove unused verbs from status response
            Iterator<Entry<String, Object>> it = metrics.iterator();
            Entry<String, Object> entry;
            while(it.hasNext()) {
                entry = it.next();
                for(String rmMetric : rmMetricSet)
                    if(entry.getKey().toUpperCase().startsWith(rmMetric)) {
                        it.remove();
                        break;
                    }
            }

            routingContext.response().end(metrics.toString());
        });

        //
        // Route GET for Service Schema
        //
        router.get(schpth).handler(routingContext -> {
            JsonObject schema = new JsonObject();
            schema.put("format", schemaValidator.getFormat());
            schema.put("schema", schemaValidator.getSchema());

            routingContext.response().end(schema.toString());
        });

        //
        // Route User Methods
        //
        route(router, methodMap);

        rmMetricSet.addAll(ALL_HTTP_METHODS);
        rmMetricSet.addAll(METRIC_FIELDS);
        rmMetricSet.removeAll(methodsSet);

        server.requestHandler(router).listen(port, host);

        if(infoDisplayed.compareAndSet(false, true)) {
            String serviceClassName = this.getClass().getSimpleName();

            System.out.printf("%nAPI Endpoint Methods: %s /ON %s", methodMap.keySet().toString(), methodsSet.toString());

            System.out.printf("%n%nAPI Schema Validation: %s", ((schemaValidator instanceof BypassSchemaValidator) ? "Off" : "On"));

            System.out.printf("%n%nAPI Authorization: %s",           (authorization.isEmpty() ? "Off" : "On"));
            System.out.printf("%nAPI Authorization Methods: %s",     authorization.toString());
            System.out.printf("%nAPI Authorization Credentials: %s", authStatus);

            System.out.printf("%n%nDefault CryptoProvider: %s", Security.getProviders()[0]);
            System.out.printf("%n%nTLS Enabled: %s",           (ssl ? "On" : "Off"));

            System.out.printf("%n%nCORS Enabled: %s", (cors != null ? "On" : "Off"));
            System.out.printf("%n\torigin:      %s",  (cors != null ? cors.origin             : "n/a"));
            System.out.printf("%n\tcredentials: %s",  (cors != null ? cors.credentials        : "n/a"));
            System.out.printf("%n\theaders:     %s",  (cors != null ? cors.headers.toString() : "n/a"));
            System.out.printf("%n\tmethods:     %s",  (cors != null ? cors.methods.toString() : "n/a"));

            System.out.printf("%n%n%s> http%s://%s:%d%s", serviceClassName, (ssl ? "s" : ""), host, port, stspth);
            System.out.printf("%n%s> http%s://%s:%d%s%n", serviceClassName, (ssl ? "s" : ""), host, port, schpth);

            String key;
            for(Entry<String, Set<String>> entry : methodMap.entrySet()) {
                key = entry.getKey();
                for(String path : entry.getValue())
                    System.out.printf("%n%s> (%-6s) http%s://%s:%d%s", serviceClassName, key, (ssl ? "s" : ""), host, port, path);
            }

            System.out.println("\n");
        }

        if(future.future().isComplete() == false)
            future.complete();
    }

    /**
     * Critical initialization is invoked before configure and is designed
     * to signal a required prerequisite completion status.
     *
     * If the return is true  the service proceeds with execution
     * If the return is false the service fails-fast and execution terminates
     *
     * Returns:
     *    true  if critical initialization succeeded
     *    false if critical initialization failed
     */
    public boolean initialize(Map<String, Object> map) {
        return true;
    }

    /**
     * Soft configuration is invoked after service_init and is designed to
     * pass configuration options to a successfully initialized service.
     * This method should gracefully handle any exceptions internally.
     */
    public void configure(Map<String, Object> map) {}

    public void   post(Map<String, String> params, Event evt, ByteBuffer body) {}
    public void    get(Map<String, String> params, Event evt)                  {}
    public void    put(Map<String, String> params, Event evt, ByteBuffer body) {}
    public void delete(Map<String, String> params, Event evt, ByteBuffer body) {}

}
