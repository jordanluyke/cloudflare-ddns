package com.jordanluyke.cloudflareddns.util;

import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Stream;

public class ErrorHandlingCompletableObserver implements CompletableObserver {
    private static final Logger logger = LogManager.getLogger(ErrorHandlingCompletableObserver.class);

    @Override
    public void onComplete() {
    }

    @Override
    public void onError(Throwable e) {
        Stream.of(e.getStackTrace())
            .forEach(trace -> logger.error("{}", trace));
        logger.error("{}: {}", e.getClass().getSimpleName(), e.getMessage());
    }

    @Override
    public void onSubscribe(Disposable disposable) {
    }
}