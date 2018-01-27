package com.desdulianto.learning.imvertx.client;

import com.desdulianto.learning.imvertx.client.IMClientVerticle;
import io.vertx.core.Vertx;

public class IMClient {
    public static void main(String []args) {
        Vertx vertx = Vertx.vertx();

        IMClientVerticle imVerticle = new IMClientVerticle();
        vertx.deployVerticle(imVerticle);
    }
}
