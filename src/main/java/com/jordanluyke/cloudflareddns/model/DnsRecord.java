package com.jordanluyke.cloudflareddns.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @Override
    public String toString() {
        return "DnsRecord{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                ", proxiable=" + proxiable +
                ", proxied=" + proxied +
                ", ttl=" + ttl +
                ", locked=" + locked +
                ", zoneId='" + zoneId + '\'' +
                ", zoneName='" + zoneName + '\'' +
                ", modifiedOn='" + modifiedOn + '\'' +
                ", createdOn='" + createdOn + '\'' +
                ", meta=" + meta +
                '}';
    }
}
