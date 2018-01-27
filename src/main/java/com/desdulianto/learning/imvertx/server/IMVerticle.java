package com.desdulianto.learning.imvertx.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

public class IMVerticle extends AbstractVerticle {
    private NetServer imServer;
    private EventBus eventBus;

    // start server
    @Override
    public void start() throws Exception {
        // socket server, beberapa verticle dapat listen ke port dan ip yang sama dikarenakan
        // vertx sebenarnya hanya listen 1 kali pada jaringan dan kemudian masing-masing instance verticle
        // yang listen ke ip dan port yang sama hanya mendapatkan reference ke instance server
        imServer = getVertx().createNetServer();
        // event bus untuk berkomunikasi antar instance verticle
        eventBus = getVertx().eventBus();

        // handler untuk menghandle event data jaringan
        imServer.connectHandler(socket -> {
            socket.handler(buffer -> {
                // subscribe to event bus
                subscribe(socket);
                // handle message yang diterima dari jaringan
                handleMassage(buffer.toJsonObject().mapTo(Message.class));
            });

            socket.closeHandler(buffer -> {
                System.out.println("bye");
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

    // subcribe ke eventbus pada alamat broadcast untuk menerima semua
    // pesan pada alamat tersebut
    private void subscribe(NetSocket socket) {
        // subscribe to broadcast channel
        eventBus.consumer("broadcast", message -> {
            JsonObject json = new JsonObject(message.body().toString());
            // kirimkan ke client
            socket.write(json.toBuffer());
        });
    }

    // mengirimkan pesan ke alamat eventbus broadcast
    private void handleMassage(Message message) {
        eventBus.publish("broadcast", JsonObject.mapFrom(message));
    }
}
