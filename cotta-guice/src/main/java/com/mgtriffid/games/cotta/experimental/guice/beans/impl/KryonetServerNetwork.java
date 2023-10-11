package com.mgtriffid.games.cotta.experimental.guice.beans.impl;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mgtriffid.games.cotta.experimental.guice.beans.ServerNetwork;
import com.mgtriffid.games.cotta.experimental.guice.data.ClientInputDto;
import com.mgtriffid.games.cotta.experimental.guice.data.EnterGameIntent;
import com.mgtriffid.games.cotta.experimental.guice.data.InputFromClient;
import com.mgtriffid.games.cotta.experimental.guice.dto.EnterTheGameDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KryonetServerNetwork implements ServerNetwork {
    private Server server;
    private final ConcurrentLinkedQueue<EnterGameIntent> enterGameIntents = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<InputFromClient> clientsInputs = new ConcurrentLinkedQueue<>();

    @Override
    public void initialize() throws IOException {
        server = new Server();
//        registerClasses(server.getKryo());
        configureListener();
        server.bind(16001, 16002);
        server.start();
    }

    @Override
    public List<EnterGameIntent> drainEnterGameIntents() {
        List<EnterGameIntent> ret = new ArrayList<>();
        Iterator<EnterGameIntent> iterator = enterGameIntents.iterator();
        while (iterator.hasNext()) {
            ret.add(iterator.next());
            iterator.remove();
        }
        return ret;
    }

    private void configureListener() {
        Listener listener = new ServerListener();
        server.addListener(listener);
    }

    class ServerListener implements Listener {
        @Override
        public void received(Connection connection, Object object) {
            if (object instanceof EnterTheGameDto) {
                EnterTheGameDto etgDto = (EnterTheGameDto) object;
                enterGameIntents.add(new EnterGameIntent(
                        connection.getID(),
                        new HashMap<>(etgDto.params)
                ));
            }
            if (object instanceof ClientInputDto) {
                clientsInputs.add(new InputFromClient(
                        connection.getID(),
                        (ClientInputDto) object
                ));
            }
        }
    }
}
