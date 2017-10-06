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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

public class LoginWindowController {

    @FXML private Button loginButton;
    @FXML private Button signUpButton;
    @FXML private TextField name;
    @FXML private PasswordField password;
    @FXML private TextField IP;
    @FXML private Label label;
    public static Socket clientSocket = null;
    public static DataInputStream dIS = null;
    public static DataOutputStream dOS = null;
    public static ArrayList<String> onlineUsers = new ArrayList<>();
    public FriendListController friendListController;
    private final int BYTE_STREAM_LENGTH = 1000000;

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
                    byte[] byteStream = new byte[BYTE_STREAM_LENGTH];
                    int index = 0;
                    while (true) {
                        if (dIS.available() > 0) {
                            byte b = dIS.readByte();
                            if (b == (byte) ('|') && byteStream[index-1]==(byte)('<') && byteStream[index-2]==(byte)('>')) {
                                String text = new String(byteStream, "UTF-8");
                                String header = text.substring(0, 3);
                                if (header.equals("#t#")){
                                    // if text message
                                    int indexOfSeparator = text.indexOf("$%^");
                                    String fromUser = text.substring(3, indexOfSeparator);
                                    FriendListController.chatWindowList.get(fromUser).appendText(
                                            fromUser + ": " + text.substring(indexOfSeparator+3, index-2));
                                }
                                else if (header.equals("!t^")){
                                    // retrieve past messages
                                    int firstIndex = text.indexOf("$%^");
                                    String windowName = text.substring(3, firstIndex);
                                    while(FriendListController.chatWindowList.get(windowName)==null){
                                        sleep(10); // bad practice...
                                    }
                                    FriendListController.chatWindowList.get(windowName).appendText(
                                            text.substring(firstIndex+3, index-2));
                                }
                                else if (header.equals(".?.")){
                                    // if image message
                                    int firstIndexOf = text.indexOf("$%^");
                                    String windowName = text.substring(3, firstIndexOf);
                                    int secondIndexOf = text.indexOf("?|?");
                                    String extension = text.substring(firstIndexOf+3, secondIndexOf);
                                    byte[] imageInByte = Arrays.copyOfRange(byteStream, secondIndexOf+3, index-2);
                                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageInByte));
                                    ChatWindowController window = FriendListController.chatWindowList.get(windowName);
                                    window.appendImage(img, windowName);
                                }
                                else if (header.equals("{;=")){
                                    // retrieve past image message
                                    int firstIndexOf = text.indexOf("$%^");
                                    int secondIndexOf = text.indexOf("/%\\");
                                    int lastIndexOf = text.lastIndexOf("$%^");
                                    String windowName = text.substring(3, firstIndexOf);
                                    String writer = text.substring(firstIndexOf+3, secondIndexOf);
                                    String extension = text.substring(secondIndexOf+3, lastIndexOf);
                                    byte[] imageInByte = Arrays.copyOfRange(byteStream, lastIndexOf+3, index-2);
                                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageInByte));
                                    while(FriendListController.chatWindowList.get(windowName)==null){
                                        sleep(10); // bad practice...
                                    }
                                    FriendListController.chatWindowList.get(windowName).appendImage(img, writer);

                                }
                                else if (header.equals("^o>")){
                                    // check if user is still online
                                    dOS.write("^o>><|".getBytes(Charset.forName("UTF-8")));
                                }
                                else if (header.equals("%s%")){
                                    // if message is from server about login/signup
                                    if (text.substring(3, index-2).equals("t")){
                                        // if true, log user into another window
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    openFriendListWindow(name.getText());
                                                } catch (Exception e){
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                    else if (text.substring(3, index-2).equals("f")){
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
                                        // if this is a list of name
                                        onlineUsers = new ArrayList<String>(Arrays.asList(text.substring(3, index-2).split("###")));
                                        onlineUsers.remove(name.getText());
                                        final int s = index-2;
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                friendListController.changeNum(Integer.toString(onlineUsers.size()));
                                                friendListController.updateOnlineUser(onlineUsers);
                                            }
                                        });
                                    }
                                }
                                else if (header.equals("@s@")){
                                    // if message is about requesting new window
                                    final int s = index-2; // to allow it to be used in platform.runlater
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                String fromUser = text.substring(3, s);
                                                if (friendListController.openChatWindow(fromUser)){
                                                    // if window is newly opened, request past messages
                                                    dOS.write((")-&" + fromUser + "><|").getBytes(Charset.forName("UTF-8")));
                                                }
                                            } catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                                else {
                                    //
                                }
                                byteStream = new byte[BYTE_STREAM_LENGTH];
                                index = 0;
                            }
                            else {
                                byteStream[index] = b;
                                index += 1;
                            }
                        }
                        else {
                            sleep(10);
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
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
                    byte[] byteStream = new byte[2000];
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
                                                    e.printStackTrace();
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
                                byteStream = new byte[BYTE_STREAM_LENGTH];
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
                    e.printStackTrace();
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
        friendListController.initialize(stage, userName, Integer.toString(onlineUsers.size()));
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
            dOS.write(("^u^" + userName + "><|").getBytes(Charset.forName("UTF-8"))); // to differentiate between username, password, and comment
            dOS.write(("^p^" + pass + "><|").getBytes(Charset.forName("UTF-8")));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendLoginData(){
        String userName = name.getText();
        String pass = password.getText();
        try {
            dOS.write(("*u*" + userName + "><|").getBytes(Charset.forName("UTF-8"))); // to differentiate between username, password, and comment
            dOS.write(("*p*" + pass + "><|").getBytes(Charset.forName("UTF-8")));
        } catch (Exception e){
            e.printStackTrace();
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
