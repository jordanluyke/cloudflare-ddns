package com.jordanluyke.cloudflareddns;

import com.google.inject.AbstractModule;

public class MainModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MainManager.class).to(MainManagerImpl.class);
    }
}
