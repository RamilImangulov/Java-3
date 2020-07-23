package ru.geekbrains.chat.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    @FXML
    TextField msgField;

    @FXML
    TextArea textArea;

    @FXML
    HBox upperPanel;

    @FXML
    HBox bottomPanel;

    @FXML
    TextField tfLogin;

    @FXML
    PasswordField pfPassword;

    @FXML
    ListView<String> clientsList;


    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    final String IP_ADDRESS = "localhost";
    final int PORT = 6666;

    private boolean isAuthorized;


    // когда окно запускается все лишние
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false);
    }

    //для удобства, сетторы хорошо работают с полями
    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
        if (!isAuthorized) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            clientsList.setVisible(false);
            clientsList.setManaged(false);
        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            clientsList.setVisible(true);
            clientsList.setManaged(true);
        }
    }

    public void connect(){
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            setAuthorized(false);

            // В этом потоке слушаем сервер
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/authok")) {
                                Controller.this.setAuthorized(true);
                                break; //выходим из цикла авторизации
                            } else {
                                textArea.appendText(str + "\n");
                            }
                        }
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/")) {
                                if (str.equals("/serverclosed")) break; //для закрытия сокетов на стороне клиента
                                if (str.startsWith("/clientslist ")) {
                                    String[] tokens = str.split(" ");
                                    //для изменения интерфейса через этот поток
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            clientsList.getItems().clear();
                                            for (int i = 1; i < tokens.length; i++) {
                                                clientsList.getItems().add(tokens[i]);
                                            }
                                        }
                                    });
                                }
                            } else {
                                textArea.appendText(str + "\n");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Controller.this.setAuthorized(false);
                        textArea.clear();
                    }
                }
            });
            thread.setDaemon(true);//daemon поток начинается в самом начале и работает, если есть хоть один работающий поток
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(){
        if(socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF("/auth " + tfLogin.getText() + " " + pfPassword.getText());
            tfLogin.clear();
            pfPassword.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addUser() {
        System.out.println("Registration");
    }
}
