package com.jordanluyke.cloudflareddns;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.cloudflareddns.util.NodeUtil;
import io.reactivex.rxjava3.core.Completable;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Singleton
public class Config {
    private static final Logger logger = LogManager.getLogger(Config.class);
    public static final Path configPath = Paths.get(System.getProperty("user.home"), "cloudflare-ddns-config.json");

    private String apiToken;
    private String domain;
    private String recordName;

    public Completable load() {
        try {
            if(!Files.exists(configPath))
                return setup();
            byte[] bytes = Files.readAllBytes(configPath);
            JsonNode body = NodeUtil.getJsonNode(bytes);
            apiToken = NodeUtil.getString("apiToken", body).orElseThrow(() -> new RuntimeException("Field required: apiToken"));
            domain = NodeUtil.getString("domain", body).orElseThrow(() -> new RuntimeException("Field required: domain"));
            recordName = NodeUtil.getString("recordName", body).orElseThrow(() -> new RuntimeException("Field required: recordName"));
            logger.info("Config loaded");
            return Completable.complete();
        } catch(IOException e) {
            return Completable.error(new RuntimeException(e.getMessage()));
        }
    }

    public Completable save() {
        ObjectNode node = NodeUtil.mapper.createObjectNode();
        node.put("apiToken", apiToken);
        node.put("domain", domain);
        node.put("recordName", recordName);
        try {
            Files.write(configPath, NodeUtil.mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(node));
            logger.info("Config saved");
            return Completable.complete();
        } catch(IOException e) {
            return Completable.error(new RuntimeException("IOException"));
        }
    }

    private Completable setup() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("API token (Permissions: Zone.Zone, Zone.DNS | Resources: All zones): ");
        apiToken = scanner.nextLine().trim();
        System.out.print("Domain (e.g. example.com): ");
        domain = scanner.nextLine().trim();
        System.out.print("DNS record name (e.g. home): ");
        recordName = scanner.nextLine().trim();

        return save();
    }
}
