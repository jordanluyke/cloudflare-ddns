package com.jordanluyke.cloudflareddns;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.jordanluyke.cloudflareddns.model.DnsRecord;
import com.jordanluyke.cloudflareddns.model.Zone;
import com.jordanluyke.cloudflareddns.util.NettyHttpClient;
import com.jordanluyke.cloudflareddns.util.NodeUtil;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class CloudflareApi {
    private static final Logger logger = LogManager.getLogger(CloudflareApi.class);
    private final static String baseUrl = "https://api.cloudflare.com/client/v4";

    private Config config;
    private NettyHttpClient httpClient;

    public Observable<Zone> getZones() {
        return request("/zones", HttpMethod.GET)
                .flatMapObservable(Observable::fromIterable)
                .map(zone -> NodeUtil.parseNodeInto(Zone.class, zone));
    }

    public Observable<DnsRecord> getDnsRecords(String zoneId) {
        return request("/zones/" + zoneId + "/dns_records", HttpMethod.GET)
                .flatMapObservable(Observable::fromIterable)
                .map(record -> NodeUtil.parseNodeInto(DnsRecord.class, record));
    }

    public Single<DnsRecord> updateDnsRecord(String zoneId, String recordId, String type, String name, String content) {
        Map<String, Object> body = new HashMap<>();
        body.put("type", type);
        body.put("name", name);
        body.put("content", content);
        return request("/zones/" + zoneId + "/dns_records/" + recordId, HttpMethod.PUT, body)
                .map(r -> NodeUtil.parseNodeInto(DnsRecord.class, r));
    }

    private Single<JsonNode> request(String path, HttpMethod method) {
        return request(path, method, Collections.emptyMap());
    }

    private Single<JsonNode> request(String path, HttpMethod method, Map<String, Object> body) {
        return httpClient.request(baseUrl + path, method, body, getHeaders())
                .flatMap(res -> {
                    if(res.getStatusCode() != 200 || !NodeUtil.getBoolean("success", res.getJsonBody()).orElse(true)) {
                        logger.error("{}", res);
                        return Single.error(new RuntimeException("Bad response"));
                    }
                    return Single.just(res.getJsonBody().get("result"));
                });
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.getApiToken());
        return headers;
    }
}
