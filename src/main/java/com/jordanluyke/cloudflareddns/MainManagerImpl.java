package com.jordanluyke.cloudflareddns;

import com.google.inject.Inject;
import com.jordanluyke.cloudflareddns.model.DnsRecord;
import com.jordanluyke.cloudflareddns.util.NettyHttpClient;
import com.jordanluyke.cloudflareddns.util.NodeUtil;
import io.reactivex.rxjava3.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class MainManagerImpl implements MainManager {
    private static final Logger logger = LogManager.getLogger(MainManager.class);
    private static final long updateInterval = 5;
    private static final TimeUnit updateUnit = TimeUnit.MINUTES;
    private static final long retryInterval = 10;
    private static final TimeUnit retryUnit = TimeUnit.SECONDS;

    @Inject private Config config;
    @Inject private Cloudflare cloudflare;

    private boolean connected = false;
    private Optional<String> deviceIp = Optional.empty();
    private Optional<String> dnsRecordIp = Optional.empty();

    @Override
    public Completable start() {
        return config.load()
                .doOnComplete(() -> {
                    Observable.interval(0, updateInterval, updateUnit)
                            .flatMapMaybe(Void -> update()
                                    .onErrorResumeWith(Maybe.empty()))
                            .blockingSubscribe();
                });
    }

    private Maybe<DnsRecord> update() {
        return getIp()
                .doOnSuccess(ip -> {
                    if(!connected) {
                        logger.info("Connected");
                        connected = true;
                    }
                    if(!deviceIp.isPresent() || !deviceIp.get().equals(ip))
                        logger.info("Acquired IP: {}", ip);
                    deviceIp = Optional.of(ip);
                })
                .doOnError(err -> {
                    if(connected) {
                        if(err.getCause() instanceof UnknownHostException || err.getCause() instanceof SocketException)
                            logger.error("Disconnected");
                        connected = false;
                    }
                })
                .retryWhen(errors -> errors
                        .zipWith(Flowable.defer(() -> {
                            long updateIntervalSeconds = updateUnit.toSeconds(updateInterval);
                            long retryIntervalSeconds = retryUnit.toSeconds(retryInterval);
                            int count = (int) (updateIntervalSeconds / retryIntervalSeconds);
                            if(updateIntervalSeconds % retryIntervalSeconds == 0 && count > 1)
                                count--;
                            return Flowable.range(1, count);
                        }), (e, i) -> i)
                        .flatMap(i -> Flowable.timer(retryInterval, retryUnit)))
                .flatMapMaybe(ip -> {
                    if(deviceIp.equals(dnsRecordIp))
                        return Maybe.empty();
                    return cloudflare.getZones()
                            .filter(zone -> zone.getName().equals(config.getDomain()))
                            .firstOrError()
                            .onErrorResumeNext(err -> {
                                if(err instanceof NoSuchElementException)
                                    logger.error("No domain found with name: {}", config.getDomain());
                                return Single.error(err);
                            })
                            .toMaybe();
                })
                .flatMapSingle(zone -> cloudflare.getDnsRecords(zone.getId())
                        .filter(record -> record.getName().equals(config.getRecordName()) || record.getName().equals(config.getRecordName() + "." + config.getDomain()))
                        .firstOrError()
                        .onErrorResumeNext(err -> {
                            if(err instanceof NoSuchElementException)
                                logger.error("No DNS record found with name: {}", config.getRecordName());
                            return Single.error(err);
                        }))
                .flatMap(record -> {
                    if(deviceIp.get().equals(record.getContent())) {
                        logger.info("DNS IP up to date");
                        dnsRecordIp = deviceIp;
                        return Maybe.empty();
                    }
                    return cloudflare.updateDnsRecord(record.getZoneId(), record.getId(), record.getType(), record.getName(), deviceIp.get())
                            .doOnSuccess(r -> {
                                logger.info("Updated DNS record: {} to {}", r.getName(), r.getContent());
                                dnsRecordIp = deviceIp;
                            })
                            .toMaybe();
                });
    }

    private Single<String> getIp() {
        return NettyHttpClient.get("https://api.ipify.org?format=json")
                .map(res -> NodeUtil.getOrThrow("ip", res.getBodyJson()));
    }
}
