package com.mgtriffid.games.cotta.experimental.guice.modules;

import com.google.inject.AbstractModule;

public class ChildModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SomeBean.class).to(SomeBeanImpl.class);
    }
}
