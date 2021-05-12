package com.jordanluyke.cloudflareddns;

import io.reactivex.rxjava3.core.Completable;

public interface MainManager {
    Completable start();
}
