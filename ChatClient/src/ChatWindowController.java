import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;

public class ChatWindowController {

    @FXML private Button sendButton;
    @FXML private Button sendImageButton;
    @FXML private TextField textField;
    @FXML private TextFlow textFlow;
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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textFlow.getChildren().add(new Text(text + "\n"));
            }
        });
    }

    @FXML public void appendImage(BufferedImage img, String fromUser){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textFlow.getChildren().add(new Text(fromUser + ": "));
                textFlow.getChildren().add(new ImageView(SwingFXUtils.toFXImage(img, null)));
                textFlow.getChildren().add(new Text("\n"));
            }
        });
    }

    @FXML public void sendText() throws Exception{
        String msg = textField.getText();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textFlow.getChildren().add(new Text(userName + ": " + msg + "\n"));
            }
        });
        textField.clear();
        LoginWindowController.dOS.write(("&t&" + toUser + "$%^" + msg + "><|").getBytes(Charset.forName("UTF-8")));
    }

    @FXML public void sendImage() throws Exception{
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose new avatar");
        File file = chooser.showOpenDialog(sendImageButton.getScene().getWindow());
        System.out.println("File size: " + file.length());
        if (file != null) {
            String extension = file.getName().substring(file.getName().lastIndexOf(".")+1);
            if (extension.equals("jpg") || extension.equals("png") || extension.equals("gif")) {
                BufferedImage bimg = ImageIO.read(file);
                LoginWindowController.dOS.write(("$#*" + toUser + "$%^" + extension + "?|?").getBytes(Charset.forName("UTF-8")));
                ImageIO.write(bimg, extension, LoginWindowController.dOS);
                LoginWindowController.dOS.write("><|".getBytes(Charset.forName("UTF-8")));
                appendImage(bimg, userName);
            }
        }
    }
}
