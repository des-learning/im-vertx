package com.desdulianto.learning.imvertx.server;

import com.desdulianto.learning.imvertx.model.User;
import com.desdulianto.learning.imvertx.packet.ChatMessage;
import com.desdulianto.learning.imvertx.packet.ConversationMessage;
import com.desdulianto.learning.imvertx.packet.ListOnlineUsersMessage;
import com.desdulianto.learning.imvertx.packet.LoginMessage;
import com.desdulianto.learning.imvertx.packet.LoginNotification;
import com.desdulianto.learning.imvertx.packet.LogoutNotification;
import com.desdulianto.learning.imvertx.packet.OnlineUsers;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.core.shareddata.LocalMap;

import java.util.Collection;

public class ConnectionHandler {
    private final Vertx vertx;
    private final NetSocket socket;
    private User user;
    private MessageConsumer<Object> broadcastConsumer;

    public ConnectionHandler(Vertx vertx, NetSocket socket) {
        this.vertx = vertx;
        this.socket = socket;
        this.user = null;
        this.broadcastConsumer = vertx.eventBus().consumer("broadcast");

        // read message from network
        this.socket.handler(this::receiveMessage);

        // close connection handler
        this.socket.closeHandler(this::close);
    }

    private boolean isOnline() {
        return user != null;
    }

    /**
     * process message from the network
     * @param buffer incoming mesage from the network
     */
    private void receiveMessage(Buffer buffer) {
        ChatMessage message;

        try {
            message = buffer.toJsonObject().mapTo(ChatMessage.class);
        } catch (Exception e) {
            System.err.println("Error decoding message " + buffer.toString());
            return;
        }

        if (message instanceof LoginMessage) {
            LoginMessage m = (LoginMessage) message;
            // login user
            if (! login(m)) {
                System.out.println(user.getUsername() + " already online");
            } else {
                System.out.println(user.getUsername() + " becomes online");
            }
        } else if (message instanceof ConversationMessage) {
            // change from to current login user
            handleConversation((ConversationMessage) message);
        } else if (message instanceof ListOnlineUsersMessage) {
            sendOnlineUsers();
        } else {
            System.out.println("Unknown message");
        }
    }

    private boolean login(LoginMessage m) {
        LocalMap<String, User> map = vertx.sharedData().getLocalMap("users");
        User user = new User(m.getUsername());
        if (map.putIfAbsent(m.getUsername(), user) == null) {
            this.user = user;

            // register handler to user/<username> to send the message to network client
            vertx.eventBus().consumer("user/" + m.getUsername()).handler(this::incomingMessage);
            // consume broadcast
            broadcastConsumer.handler(this::incomingMessage);

            // broadcast login
            broadcastMessage(new LoginNotification(this.user));
        }

        return this.user != null;
    }

    private boolean logout() {
        if (isOnline()) {
            // shared data of online users
            LocalMap<String, User> map = vertx.sharedData().getLocalMap("users");
            map.remove(user.getUsername());

            // broadcast logout
            broadcastMessage(new LogoutNotification(this.user));
            return true;
        }
        return false;
    }

    /**
     * send message through the network to client
     * @param message message to send to client
     */
    private void incomingMessage(Message<Object> message) {
        socket.write(JsonObject.mapFrom(message.body()).toBuffer());
    }

    /**
     * handle message
     * @param message message
     */
    private void handleConversation(ConversationMessage message) {
        // only handle message when online and the sender is valid
        if (isOnline() && message.getFrom().equals(user.getUsername())) {
            sendMessage(new User(message.getTo()), message);
        }
    }

    private void sendOnlineUsers() {
        if (isOnline()) {
            LocalMap<String, User> map = vertx.sharedData().getLocalMap("users");
            Collection<User> users = map.values();
            users.remove(this.user);
            sendMessageToSelf(new OnlineUsers(users));
        }
    }

    private void sendMessage(User user, ChatMessage message) {
        vertx.eventBus().publish("user/" + user.getUsername(), JsonObject.mapFrom(message));
    }

    private void sendMessageToSelf(ChatMessage message) {
        sendMessage(this.user, message);
    }

    private void broadcastMessage(ChatMessage message) {
        vertx.eventBus().publish("broadcast", JsonObject.mapFrom(message));
    }

    private void close(Void aVoid) {
        if (logout()) {
            System.out.println(user.getUsername() + " becomes offline");
        }
        System.out.println("client disconnect from " + socket.remoteAddress());
    }
}
