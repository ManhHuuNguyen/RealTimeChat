import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class LoginWindowController {

    @FXML private Button loginButton;
    @FXML private Button signUpButton;
    @FXML private TextField name;
    @FXML private PasswordField password;
    @FXML private TextField IP;
    @FXML private Label label;
    static Socket clientSocket = null;
    static DataInputStream dIS = null;
    static DataOutputStream dOS = null;
    static ArrayList<String> onlineUsers = new ArrayList<>();

    FriendListController friendListController;

    @FXML public void login() throws Exception{
        label.setVisible(false);
        if (clientSocket == null){
            clientSocket = new Socket(IP.getText(), 4310);
            dIS = new DataInputStream(clientSocket.getInputStream());
            dOS = new DataOutputStream(clientSocket.getOutputStream());
        }

        Thread th = new Thread(){
            public void run(){
                try {
                    byte[] byteStream = new byte[8096];
                    int index = 0;
                    while (true) {
                        if (dIS.available() > 0) {
                            byte b = dIS.readByte();
                            if (b == (byte) ('|')) {
                                String text = new String(byteStream, "UTF-8");
                                if (text.substring(0, 3).equals("#t#")){
                                    // if text message
                                    String fromUser = text.substring(3, text.indexOf("$%^"));
                                    System.out.println(fromUser);
                                    System.out.println("Key set: " + FriendListController.chatWindowList.keySet());
                                    System.out.println(text.substring(text.indexOf("$%^")+3, index));
                                    FriendListController.chatWindowList.get(fromUser).appendText(
                                            fromUser + ":" + text.substring(text.indexOf("$%^")+3, index));
                                }
                                else if (text.substring(0, 3).equals("%s%")){
                                    // if message is from server about login/signup
                                    if (text.substring(3, index).equals("t")){
                                        // if true, log user into another window
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    openFriendListWindow(name.getText());
                                                } catch (Exception e){
                                                    System.out.println(e);
                                                }
                                            }
                                        });
                                    }
                                    else if (text.substring(3, index).equals("f")){
                                        // if false, print error message and clear input fields
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                changeWarningText("Wrong username or password");
                                                clearInputField();
                                            }
                                        });
                                    }
                                    else {
                                        // if this is a name
                                        onlineUsers.add(text.substring(3, index));
                                        final int s = index;
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                friendListController.changeNum(Integer.toString(onlineUsers.size()));
                                                friendListController.addUser(text.substring(3, s));
                                            }
                                        });
                                    }
                                }
                                else if (text.substring(0, 3).equals("@s@")){
                                    // if message is about requesting new window
                                    final int s = index; // to allow it to be used in platform.runlater
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                friendListController.openChatWindow(text.substring(3, s));

                                            } catch (Exception e){
                                                System.out.println(e);
                                            }
                                        }
                                    });
                                }
                                else {
                                    // if message is from other users
                                }
                                byteStream = new byte[8096];
                                index = 0;
                            } else {
                                byteStream[index] = b;
                                index += 1;
                            }
                        } else {
                            sleep(10);
                        }
                    }
                } catch (Exception e){
                    System.out.println(e);
                }
            }
        };
        th.start();
        sendLoginData();
    }

    @FXML public void signUp() throws Exception{
        label.setVisible(false);
        if (clientSocket == null) {
            clientSocket = new Socket(IP.getText(), 4310);
            dIS = new DataInputStream(clientSocket.getInputStream());
            dOS = new DataOutputStream(clientSocket.getOutputStream());
        }

        Thread th = new Thread(){
            public void run(){
                try {
                    byte[] byteStream = new byte[8096];
                    int index = 0;
                    while (true) {
                        if (dIS.available() > 0) {
                            byte b = dIS.readByte();
                            if (b == (byte) ('|')) {
                                String text = new String(byteStream, "UTF-8");
                                if (text.substring(0, 3).equals("%s%")){
                                    // if message is from server
                                    if (text.substring(3, index).equals("t")){
                                        // if true, log user into another window
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    openFriendListWindow(name.getText());
                                                } catch (Exception e){
                                                    System.out.println(e);
                                                }
                                            }
                                        });
                                    }
                                    else if (text.substring(3, index).equals("f")){
                                        // if false, print error message and clear input fields
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                changeWarningText("Username or password already in use");
                                                clearInputField();
                                            }
                                        });
                                    }
                                    else {
                                        // if this is a name
                                        onlineUsers.add(text.substring(3, index));
                                        final int s = index;
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                friendListController.changeNum(Integer.toString(onlineUsers.size()));
                                                friendListController.addUser(text.substring(3, s));
                                            }
                                        });
                                    }
                                }
                                else {
                                    // if message is from other users
                                }
                                byteStream = new byte[8096];
                                index = 0;
                            } else {
                                byteStream[index] = b;
                                index += 1;
                            }
                        } else {
                            sleep(10);
                        }
                    }
                } catch (Exception e){
                    System.out.println(e);
                }
            }
        };
        th.start();
        sendSignUpData();
    }

    @FXML public void close(){
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }

    public synchronized void openFriendListWindow(String userName) throws Exception{
        System.out.println("This is the client of user: " + userName);
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/FriendList.fxml"));
        Pane root = loader.load();
        friendListController= loader.getController();
        friendListController.setUsername(userName);
        friendListController.changeName(friendListController.getUsername());
        friendListController.changeNum(Integer.toString(onlineUsers.size()));
        Scene scene = new Scene(root, 300, 700);
        stage.setTitle("Friend List");
        stage.setScene(scene);
        stage.show();
        // close the login window
        close();
    }

    public void sendSignUpData(){
        String userName = name.getText();
        String pass = password.getText();
        try {
            dOS.write(("^u^" + userName + "|").getBytes(Charset.forName("UTF-8"))); // to differentiate between username, password, and comment
            dOS.write(("^p^" + pass + "|").getBytes(Charset.forName("UTF-8")));
        } catch (Exception e){
            System.out.println(e.toString());
        }
    }

    public void sendLoginData(){
        String userName = name.getText();
        String pass = password.getText();
        try {
            dOS.write(("*u*" + userName + "|").getBytes(Charset.forName("UTF-8"))); // to differentiate between username, password, and comment
            dOS.write(("*p*" + pass + "|").getBytes(Charset.forName("UTF-8")));
        } catch (Exception e){
            System.out.println(e.toString());
        }
    }

    public void clearInputField(){
        name.setText("");
        password.setText("");
    }

    public void changeWarningText(String text){
        label.setText(text);
        label.setVisible(true);
    }
}
