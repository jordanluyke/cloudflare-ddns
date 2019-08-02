package com.jordanluyke.cloudflareddns.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Zone {
    private String id;
    private String name;
    private String status;
    private boolean paused;
    private String type;
    @JsonProperty("development_mode") private int developmentMode;
    @JsonProperty("name_servers") private String[] nameServers;
    @JsonProperty("original_name_servers") private String[] originalNameServers;
    @JsonProperty("original_registrar") private String originalRegistrar;
    @JsonProperty("original_dnshost") private String originalDnshost;
    @JsonProperty("modified_on") private String modifiedOn;
    @JsonProperty("created_on") private String createdOn;
    @JsonProperty("activated_on") private String activatedOn;
    private JsonNode meta;
    private JsonNode owner;
    private JsonNode account;
    private String[] permissions;
    private JsonNode plan;

    @Override
    public String toString() {
        return "Zone{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", paused=" + paused +
                ", type='" + type + '\'' +
                ", developmentMode=" + developmentMode +
                ", nameServers=" + Arrays.toString(nameServers) +
                ", originalNameServers=" + Arrays.toString(originalNameServers) +
                ", originalRegistrar='" + originalRegistrar + '\'' +
                ", originalDnshost='" + originalDnshost + '\'' +
                ", modifiedOn='" + modifiedOn + '\'' +
                ", createdOn='" + createdOn + '\'' +
                ", activatedOn='" + activatedOn + '\'' +
                ", meta=" + meta +
                ", owner=" + owner +
                ", account=" + account +
                ", permissions=" + Arrays.toString(permissions) +
                ", plan=" + plan +
                '}';
    }
}
