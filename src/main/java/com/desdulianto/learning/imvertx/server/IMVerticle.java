package com.desdulianto.learning.imvertx.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
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
        // event bus untuk berkomunikasi antar instance verticle
        EventBus eventBus = getVertx().eventBus();

        //subscribe to broadcast eventbus
        MessageConsumer<Object> consumer = eventBus.consumer("broadcast");
        MessageProducer<Object> publisher = eventBus.publisher("broadcast");

        // handler untuk menghandle event data jaringan
        imServer.connectHandler(socket -> {
            processMessage(consumer, socket);

            socket.handler(buffer ->
                receiveMessage(buffer, publisher)
            );

            socket.closeHandler(buffer ->
                System.out.println("bye")
            );
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

    /**
     * receive message handler, send received message to eventbus
     *
     * @param buffer received message from network
     * @param producer eventbus producer to publish ke message to eventbus
     */
    private void receiveMessage(Buffer buffer, MessageProducer<Object> producer) {
        producer.write(buffer);
    }

    /**
     * process message in the eventbus by sending it to client through netsocket
     * @param consumer eventbus message consumer, to get and handle the message
     * @param socket netsocket to send the message to client
     */
    private void processMessage(MessageConsumer<Object> consumer, NetSocket socket) {
        consumer.handler(message ->
            socket.write(new JsonObject(message.body().toString()).toBuffer())
        );
    }
}
