package ru.geekbrains.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;

    public Server() {
        // Vector это как ArrayLisr, но только для многопоточности, так как он синхронизированный
        clients = new Vector<>();
        ServerSocket server = null; // иницилизация локальной перемннной, так что пишу нолик
        Socket socket = null;
        try {
            AuthService.connect();
            // создали сервер и выюрали ему порт
            server = new ServerSocket(6666);
            System.out.println("Server is working...");
            while (true) {
                socket = server.accept(); // точка подлючения со строны сервера или розетка (информация - кто подключился с какого IP, port)
                System.out.println("New client");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void uniCast(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nickTo)) {
                o.sendMsg("from " + from.getNick() + ": " + msg);
                from.sendMsg("to " + nickTo + " you sent: " + msg);
                return;
            }
        }
        from.sendMsg("Nick: " + nickTo + " was not found in the chat.");
    }

    public void broadcastMsg(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }


    public boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    //лист клиентов
    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientslist ");
        for (ClientHandler o : clients) {
            sb.append(o.getNick() + " ");
        }
        String out = sb.toString();
        for (ClientHandler o : clients) {
            o.sendMsg(out);
        }
    }

    //подписываем клиента, который вышел из сети
    public void subscribe(ClientHandler client) {
        clients.add((client));
        broadcastClientsList();

    }
    //отписываем клиента, который вышел из сети
    public void unsubscribe(ClientHandler client) {
        clients.remove((client));
        broadcastClientsList();
    }
}
