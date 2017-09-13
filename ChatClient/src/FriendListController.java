import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;

public class FriendListController {

    @FXML private Button addFriendButton;
    @FXML private ImageView avatar;
    @FXML private Label name;
    @FXML private Label onlineUserNum;

    public void changeName(String string){
        name.setText(string);
    }

    public void changeNum(String string){
        onlineUserNum.setText(string);
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


}
