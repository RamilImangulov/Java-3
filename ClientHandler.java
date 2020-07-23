package ru.geekbrains.chat.server;

import com.sun.tools.javac.file.SymbolArchive;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String nick;

    public String getNick(){
        return nick;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream()); // socket.getInputStream() - это чтение потока данных
            this.out = new DataOutputStream(socket.getOutputStream());

// BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); - можно
// использовать для потока данных, но вот в каких случаях?

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // цикл авторизации
                        while (true) {
                            String str = in.readUTF(); // читаем сообщение от клиенат
                            if (str.startsWith("/auth")) { // /auth login pass
                                String[] tokens = str.split(" "); //разбиваем данные
                                String nickname = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]); //и запрашиваем по логину и паролю
                                if (nickname != null) {
                                    if (!server.isNickBusy(nickname)) {
                                        sendMsg("/authok");
                                        nick = nickname;
                                        server.subscribe(ClientHandler.this); //подписываем на рассылку
                                        ClientHandler.this.sendMsg(nick + " in the Chat");
                                        break;
                                    } else {
                                        ClientHandler.this.sendMsg("Nick is busy.");
                                    }
                                } else {
                                    ClientHandler.this.sendMsg("Check your Login/Password");
                                }
                            }
                        }
                        // цикл обзения с клиентом
                        while (true) {
                            String str = in.readUTF(); // читаем сообщение от клиенат
                            if (str.startsWith("/")) {
                                if (str.equals("/end")) {
                                    out.writeUTF("/serverclosed");
                                    break;
                                }
                                if (str.startsWith("/w ")) { // отправляем личные сообщения
                                    String[] tokens = str.split(" ", 3); //мы делим строку на три куска
                                    server.uniCast(ClientHandler.this, tokens[1], tokens[2]);
                                }
                            } else {
                                server.broadcastMsg(nick + ": " + str);
                                System.out.println("Client: " + str);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        server.unsubscribe(ClientHandler.this);
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
