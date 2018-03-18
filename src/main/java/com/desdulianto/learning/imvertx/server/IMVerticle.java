package com.desdulianto.learning.imvertx.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.NetServer;
import io.vertx.ext.web.Router;

public class IMVerticle extends AbstractVerticle {
    // start server
    @Override
    public void start() throws Exception {
        // network server
        NetServer imServer = getVertx().createNetServer();

        // handling connection from client
        imServer.connectHandler(socket -> new NetConnectionHandler(getVertx(), socket));

        // listen ke jaringan
        imServer.listen(1286, "localhost", res -> {
            if (res.succeeded()) {
                System.out.println("listening");
            } else {
                System.out.println("failed to bind");
                getVertx().close();
            }
        });

        HttpServer httpServer = getVertx().createHttpServer();
        Router router = Router.router(getVertx());

        router.get("/").handler(context -> {
            context.response().sendFile("hello.html");
        });

        httpServer.requestHandler(router::accept);
        httpServer.websocketHandler(socket -> new WSConnectionHandler(getVertx(), socket));
        httpServer.listen(8080);
    }
}
