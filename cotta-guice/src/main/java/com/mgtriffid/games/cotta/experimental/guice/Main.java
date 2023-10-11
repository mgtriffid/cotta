package com.mgtriffid.games.cotta.experimental.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.mgtriffid.games.cotta.experimental.guice.api.JCottaGame;
import com.mgtriffid.games.cotta.experimental.guice.api.JCottaGameConfiguration;
import com.mgtriffid.games.cotta.experimental.guice.beans.JCottaServerGameInstance;
import com.mgtriffid.games.cotta.experimental.guice.beans.state.JCottaState;
import com.mgtriffid.games.cotta.experimental.guice.modules.CottaServerModule;
import com.mgtriffid.games.cotta.experimental.guice.scopes.GameInstanceScope;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.google.inject.name.Names.named;

public class Main {
    public static void main(String[] args) {
        JCottaGame game = getGame();

        Injector injector = Guice.createInjector(
                new CottaServerModule(game)
        );
        GameInstanceScope scope = injector.getInstance(Key.get(GameInstanceScope.class).withAnnotation(named("gameInstanceScope")));
        scope.enter();
        try {


            injector.getInstance(JCottaServerGameInstance.class).run();
        } finally {
            scope.exit();
        }
    }

    @NotNull
    private static JCottaGame getGame() {
        return new JCottaGame() {
            @Override
            public JCottaGameConfiguration getConfig() {
                return new JCottaGameConfiguration() {
                    @Override
                    public void print() {
                        System.out.println("Hello from config");
                    }

                    @Override
                    public int getHistoryLength() {
                        return 8;
                    }

                    @Override
                    public long getTickLengthMs() {
                        return 20;
                    }
                };
            }

            @Override
            public List<Class> getComponentClasses() {
                return new ArrayList<>();
            }

            @Override
            public List<Class> getMetaEntitiesInputComponents() {
                return new ArrayList<>();
            }

            @Override
            public List<Class> getServerSystems() {
                return new ArrayList<>();
            }

            @Override
            public void initializeServerState(JCottaState state) {
                // no-op stub
            }
        };
    }
}
