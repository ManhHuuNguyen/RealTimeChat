import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

public class FriendListController {

    @FXML private Button addFriendButton;
    @FXML private ImageView avatar;
    @FXML private Label name;
    @FXML private Label onlineUserNum;
    @FXML private ListView<String> onlineUserList;
    public static HashMap<String, ChatWindowController> chatWindowList = new HashMap();
    public Stage stage;
    private String username = null;

    public void initialize(Stage stage, String username, String onlineUser){
        this.stage = stage;
        this.username = username;
        changeName(this.username);
        changeNum(onlineUser);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    System.out.println("Closing down the app...");
                    LoginWindowController.dOS.write((")*(" + username + "><|").getBytes(Charset.forName("UTF-8")));
                    for (ChatWindowController controller: chatWindowList.values()){
                        controller.stage.close();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return username;
    }

    public void changeName(String string){
        name.setText(string);
    }

    public void changeNum(String string){
        onlineUserNum.setText(string);
    }

    public void addUser(String string){
        onlineUserList.getItems().add(string);
    }



    @FXML public void changeAvatar(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose new avatar");
        File file = chooser.showOpenDialog(addFriendButton.getScene().getWindow());
        if (file != null) {
            Image img = new Image(file.getAbsolutePath());
            avatar.setImage(img);
        }
    }

    @FXML public void addFriend(){
        Stage stage = new Stage();
        Pane root = new Pane();
        root.setPrefWidth(300.0);
        root.setPrefHeight(100.0);
        root.getChildren().add(new Label("Dude, you have no friend. Get real!!!"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML public void handleMouseClick(MouseEvent arg) throws Exception{
        String chosen = onlineUserList.getSelectionModel().getSelectedItem();
        if (openChatWindow(chosen)) {
            // if chat window is opened successfully
            LoginWindowController.dOS.write(("^o^" + chosen + "><|").getBytes(Charset.forName("UTF-8")));
            LoginWindowController.dOS.write((")-&" + chosen + "><|").getBytes(Charset.forName("UTF-8")));
        }
    }

    public void updateOnlineUser(ArrayList<String> onlineUsers){
        onlineUserList.getItems().clear();
        onlineUserList.getItems().addAll(onlineUsers);
    }

    public synchronized boolean openChatWindow(String toUser) throws Exception {
        if (toUser != null && !chatWindowList.containsKey(toUser)) {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/ChatWindow.fxml"));
            Pane root = loader.load();
            ChatWindowController controller = loader.getController();
            controller.initialize(stage, toUser, username);
            chatWindowList.put(toUser, controller);
            Scene scene = new Scene(root, 500, 500);
            stage.setTitle(toUser);
            stage.setScene(scene);
            stage.show();
            return  true;
        }
        return false;
    }

}
