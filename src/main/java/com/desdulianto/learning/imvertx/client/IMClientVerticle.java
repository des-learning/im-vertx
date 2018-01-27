package com.desdulianto.learning.imvertx.client;

import com.desdulianto.learning.imvertx.server.Message;
import com.desdulianto.learning.imvertx.server.TextMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.util.Scanner;

public class IMClientVerticle extends AbstractVerticle {
    private NetClient client;
    private EventBus eventBus;
    private NetSocket socket;

    @Override
    public void start() {
        client = getVertx().createNetClient();
        client.connect(1286, "localhost", res -> {
            if (res.succeeded()) {
                socket = res.result();
                handleMessage(socket);
                eventBus = getVertx().eventBus();
                subcribe();
                eventBus.publish("system", "start");
            } else {
                System.out.println("Unable to connect");
                getVertx().close();
            }
        });
    }

    private void handleMessage(NetSocket socket) {
        socket.handler(buffer -> {
            Message message = buffer.toJsonObject().mapTo(Message.class);
            if (message instanceof TextMessage) {
                TextMessage tm = (TextMessage) message;
                System.out.println("From Server: " + tm.getMessage());
            }
        });
    }

    private void subcribe() {
        eventBus.consumer("system").handler(objectMessage -> {
            if (objectMessage.body().toString().equals("start")) {
                System.out.println("Connected");
                Verticle interactive = new IMInteractiveVerticle(socket);
                getVertx().deployVerticle(interactive, new DeploymentOptions().setWorker(true));
            } else if (objectMessage.body().equals("stop")) {
                System.out.println("Bye-bye");
                getVertx().close();
            }
        });
    }
}
