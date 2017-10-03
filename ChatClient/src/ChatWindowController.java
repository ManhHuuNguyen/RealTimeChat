import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.nio.charset.Charset;

public class ChatWindowController {

    @FXML private Button sendButton;
    @FXML private TextField textField;
    @FXML private TextArea textArea;
    public String toUser = null;
    public String userName = null;
    public Stage stage;

    public void initialize(Stage stage, String toUser, String userName){
        this.stage = stage;
        this.toUser = toUser;
        this.userName = userName;
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                FriendListController.chatWindowList.remove(toUser);
            }
        });
    }

    @FXML public void appendText(String text){
        textArea.appendText(text + "\n");
    }

    @FXML public void sendText() throws Exception{
        String msg = textField.getText();
        textField.clear();
        textArea.appendText(userName + ": " + msg + "\n");
        LoginWindowController.dOS.write(("&t&" + toUser + "$%^" + msg + "|").getBytes(Charset.forName("UTF-8")));
    }

}
