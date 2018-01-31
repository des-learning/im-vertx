package com.desdulianto.learning.imvertx.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;

import java.util.Optional;

public class IMVerticle extends AbstractVerticle {
    // start server
    @Override
    public void start() throws Exception {
        // network server
        NetServer imServer = getVertx().createNetServer();

        // handling connection from client
        imServer.connectHandler(socket -> new ConnectionHandler(getVertx(), socket));

        // listen ke jaringan
        imServer.listen(1286, "localhost", res -> {
            if (res.succeeded()) {
                System.out.println("listening");
            } else {
                System.out.println("failed to bind");
                getVertx().close();
            }
        });
    }
}
