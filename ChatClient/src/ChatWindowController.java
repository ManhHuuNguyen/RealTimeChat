import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.nio.charset.Charset;

public class ChatWindowController {

    @FXML private Button sendButton;
    @FXML private TextField textField;
    @FXML private TextArea textArea;

    @FXML public void appendText(String text){
        textArea.appendText(text + "\n");
    }

    @FXML public void sendText() throws Exception{
        String msg = textField.getText();
        textField.clear();
        textArea.appendText(msg + "\n");
        LoginWindowController.dOS.write(("&t&" + msg + "|").getBytes(Charset.forName("UTF-8")));
    }

}
