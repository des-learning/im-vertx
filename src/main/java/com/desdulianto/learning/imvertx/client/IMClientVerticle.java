package com.desdulianto.learning.imvertx.client;

import com.desdulianto.learning.imvertx.server.Message;
import com.desdulianto.learning.imvertx.server.TextMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;


// verticle client
public class IMClientVerticle extends AbstractVerticle {
    private NetClient client;
    private EventBus eventBus;
    private NetSocket socket;

    // start client
    @Override
    public void start() {
        // connect ke server
        client = getVertx().createNetClient();
        client.connect(1286, "localhost", res -> {
            if (res.succeeded()) {
                socket = res.result();
                handleMessage(socket);
                eventBus = getVertx().eventBus();
                subcribe();
                // info bahwa sudah bisa start interaksi
                eventBus.publish("system", "start");
            } else {
                System.out.println("Unable to connect");
                getVertx().close();
            }
        });
    }

    // mengirimkan pesan ke server
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
            // jalankan verticle interactive untuk interaksi user
            if (objectMessage.body().toString().equals("start")) {
                System.out.println("Connected");
                Verticle interactive = new IMInteractiveVerticle(socket);
                // verticle interactive akan blocking karena membaca input dari keyboard
                // oleh karena itu verticle ini dijalankan pada thread terpisah `setWorker(true)`
                getVertx().deployVerticle(interactive, new DeploymentOptions().setWorker(true));
                // matikan verticle apabila sudah selesai
            } else if (objectMessage.body().equals("stop")) {
                System.out.println("Bye-bye");
                getVertx().close();
            }
        });
    }
}
