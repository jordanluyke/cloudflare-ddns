package com.jordanluyke.cloudflareddns;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jordanluyke.cloudflareddns.util.NodeUtil;
import io.reactivex.Completable;
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
            apiToken = NodeUtil.getOrThrow("apiToken", body);
            domain = NodeUtil.getOrThrow("domain", body);
            recordName = NodeUtil.getOrThrow("recordName", body);
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

        System.out.print("API token: ");
        apiToken = scanner.nextLine().trim();
        System.out.print("Domain: ");
        domain = scanner.nextLine().trim();
        System.out.print("Record name: ");
        recordName = scanner.nextLine().trim();

        return save();
    }
}
