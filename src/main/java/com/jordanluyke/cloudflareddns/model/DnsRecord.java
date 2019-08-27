package com.jordanluyke.cloudflareddns.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DnsRecord {
    private String id;
    private String type;
    private String name;
    private String content;
    private boolean proxiable;
    private boolean proxied;
    private int ttl;
    private boolean locked;
    @JsonProperty("zone_id") private String zoneId;
    @JsonProperty("zone_name") private String zoneName;
    @JsonProperty("modified_on") private String modifiedOn;
    @JsonProperty("created_on") private String createdOn;
    private JsonNode meta;
}
