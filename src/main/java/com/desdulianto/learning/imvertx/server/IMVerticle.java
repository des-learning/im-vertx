package com.desdulianto.learning.imvertx.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

public class IMVerticle extends AbstractVerticle {
    private NetServer imServer;
    private EventBus eventBus;

    @Override
    public void start() throws Exception {
        imServer = getVertx().createNetServer();
        eventBus = getVertx().eventBus();

        imServer.connectHandler(socket -> {
            socket.handler(buffer -> {
                // subscribe to event bus
                subscribe(socket);
                // handle message
                handleMassage(buffer.toJsonObject().mapTo(Message.class));
            });

            socket.closeHandler(buffer -> {
                System.out.println("bye");
            });
        });

        imServer.listen(1286, "localhost", res -> {
            if (res.succeeded()) {
                System.out.println("listening");
            } else {
                System.out.println("failed to bind");
                getVertx().close();
            }
        });
    }

    private void subscribe(NetSocket socket) {
        // subscribe to broadcast channel
        eventBus.consumer("broadcast", message -> {
            JsonObject json = new JsonObject(message.body().toString());
            socket.write(json.toBuffer());
        });
    }

    private void handleMassage(Message message) {
        eventBus.publish("broadcast", JsonObject.mapFrom(message));
    }
}
