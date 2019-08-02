# Cloudflare DDNS

Updates a Cloudflare DNS record with current IP.

### Setup

1. Create API token

    ![](https://i.imgur.com/Y0Wowli.png)
    
1. Build and run jar

    ```
    mvn clean compile assembly:single
    java -jar target/cloudflare-ddns-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```

1. Enter API token, domain, and record name when prompted

    - `cloudflare-ddns-config.json` is saved to `user.home` directory.
