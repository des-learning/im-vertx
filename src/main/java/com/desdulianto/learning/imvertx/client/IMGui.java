package com.desdulianto.learning.imvertx.client;

import com.desdulianto.learning.imvertx.packet.ChatMessage;
import com.desdulianto.learning.imvertx.packet.TextMessage;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class IMGui extends Application implements Initializable {
    @FXML
    private TextArea areaChat;
    @FXML
    private TextField txtChat;
    @FXML
    private Button btnSend;
    private IMClientVerticle verticle;

    private Vertx vertx;

    private Logger logger;

    public IMGui() {
        logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ChatWindow.fxml"));

        stage.setScene(new Scene(root));
        stage.show();

        stage.setOnCloseRequest(windowEvent -> {
            if (vertx != null) {
                vertx.close();
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startVerticle();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    private void startVerticle() {
        vertx = Vertx.vertx();

        verticle = new IMClientVerticle();
        vertx.deployVerticle(verticle, stringAsyncResult -> {
            if (stringAsyncResult.succeeded()) {
                logger.info("Success starting verticle");

                // consume incoming message
                verticle.incomingMessageHandler(message -> {
                    String m = message.mapTo(TextMessage.class).getMessage();
                    areaChat.appendText(m + "\n");
                });

                // send outgoing message
                btnSend.setOnAction(event -> {
                    ChatMessage message = new TextMessage(txtChat.getText());
                    verticle.outgoingMessageHandler(JsonObject.mapFrom(message));
                    txtChat.clear();
                    txtChat.requestFocus();
                    //System.out.println(JsonObject.mapFrom(message).encodePrettily());
                });
            } else {
                logger.warning("Failed starting verticle");
                logger.severe(Arrays.toString(stringAsyncResult.cause().getStackTrace()));
            }
        });
    }
}
