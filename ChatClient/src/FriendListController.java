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

import java.io.File;
import java.util.ArrayList;

public class FriendListController {

    @FXML private Button addFriendButton;
    @FXML private ImageView avatar;
    @FXML private Label name;
    @FXML private Label onlineUserNum;
    @FXML private ListView<String> onlineUserList;

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
        System.out.println(file.getAbsolutePath());
        Image img = new Image(file.getAbsolutePath());
        avatar.setImage(img);

    }

    @FXML public void addFriend(){
        System.out.println("You have no friend. Get real!!!");
    }

    @FXML public void handleMouseClick(MouseEvent arg) throws Exception{
        System.out.println("clicked on " + onlineUserList.getSelectionModel().getSelectedItem());
        openChatWindow(onlineUserList.getSelectionModel().getSelectedItem());
    }

    public synchronized void openChatWindow(String name) throws Exception{
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/ChatWindow.fxml"));
        Pane root = loader.load();
        ChatWindowController controller = loader.getController();
        Scene scene = new Scene(root, 500, 500);
        stage.setTitle(name);
        stage.setScene(scene);
        stage.show();

    }

}
