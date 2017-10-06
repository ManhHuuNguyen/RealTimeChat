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
import java.util.ArrayList;
import java.util.HashMap;

public class ChatWindowController {

    @FXML private Button sendButton;
    @FXML private Button sendImageButton;
    @FXML private TextField textField;
    @FXML private TextFlow textFlow;
    public String toUser = null;
    public String userName = null;
    public Stage stage;
    public static HashMap<String, Image> emoticons;

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
        emoticons = new HashMap(){{
            put("/:*", new Image("/images/emoticons/airkissing.png"));
            put("/**", new Image("/images/emoticons/angry.png"));
            put("/--", new Image("/images/emoticons/cocky.png"));
            put("/TT", new Image("/images/emoticons/crying.png"));
            put("/:O", new Image("/images/emoticons/shock.png"));
            put("/:)", new Image("images/emoticons/happy.png"));
            put("/:D", new Image("images/emoticons/laughing.png"));
            put("/|>", new Image("images/emoticons/lolling.png"));
            put("/<3", new Image("images/emoticons/heart.png"));
            put("/:(", new Image("images/emoticons/sad.png"));
            put("/:z", new Image("images/emoticons/scared.png"));
            put("/:|", new Image("images/emoticons/disbelieve.png"));
            put("/:[", new Image("images/emoticons/sick.png"));
            put("/:>", new Image("images/emoticons/whimsy.png"));
            put("/<>", new Image("images/emoticons/broken_heart.png"));
            put("/@@", new Image("images/emoticons/horrified.png"));
            put("/~~", new Image("images/emoticons/orgasming.png"));
            put("/.o", new Image("images/emoticons/winking.png"));
        }};
    }

    @FXML public void appendText(String text){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int start = 0;
                for (int i=0; i<text.length()-2; i++){
                    String currentChars = "" + text.charAt(i) + text.charAt(i+1) + text.charAt(i+2);
                    if (emoticons.containsKey(currentChars)){
                        textFlow.getChildren().add(new Text(text.substring(start, i)));
                        i += 3;
                        start = i;
                        textFlow.getChildren().add(new ImageView(emoticons.get(currentChars)));
                    }
                }
                textFlow.getChildren().add(new Text(text.substring(start) + "\n"));
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
        appendText(userName + ": " + msg + "\n");
        textField.clear();
        LoginWindowController.dOS.write(("&t&" + toUser + "$%^" + msg + "><|").getBytes(Charset.forName("UTF-8")));
    }

    @FXML public void sendImage() throws Exception{
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose new avatar");
        File file = chooser.showOpenDialog(sendImageButton.getScene().getWindow());
        if (file != null) {
            System.out.println("File size: " + file.length());
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
