package com.desdulianto.learning.imvertx.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

public class IMVerticle extends AbstractVerticle {
    // start server
    @Override
    public void start() throws Exception {
        // socket server, beberapa verticle dapat listen ke port dan ip yang sama dikarenakan
        // vertx sebenarnya hanya listen 1 kali pada jaringan dan kemudian masing-masing instance verticle
        // yang listen ke ip dan port yang sama hanya mendapatkan reference ke instance server
        NetServer imServer = getVertx().createNetServer();

        imServer.connectHandler(socket -> {
            // process broadcast message
            processMessage(getVertx().eventBus().consumer("broadcast"), socket);

            // receive message
            socket.handler(buffer -> {
                receiveMessage(getVertx(), buffer);
            });

            socket.closeHandler(aVoid -> {
                System.out.println(String.format("disconnected from %s", socket.remoteAddress()));
            });
        });

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

    private void processMessage(MessageConsumer<Object> consumer, NetSocket socket) {
        consumer.handler(message -> {
            socket.write(new JsonObject(message.body().toString()).toBuffer());
        });
    }

    private void receiveMessage(Vertx vertx, Buffer buffer) {
        vertx.eventBus().publish("broadcast", buffer);
    }
}
