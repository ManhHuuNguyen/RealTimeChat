import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.Array;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.net.SocketException;
import java.util.Arrays;

public class Server {

    public static MySqlCon database = null;
    public static ArrayList<User> activeUsers = new ArrayList<>(); // I might change this to a HashMap later
    private static final int BYTE_STREAM_LENGTH = 1000000;
    public static final String IMAGE_FOLDER_ROUTE = "/home/manh/Desktop/ChatServer/images/";
    public static final File SAVE_IMAGE_FOLDER = new File(IMAGE_FOLDER_ROUTE);

    public static void start() throws Exception {
        database = new MySqlCon("jdbc:mysql://localhost:3306/chatApp","root","root");
        System.out.println("Server starting...");
        ServerSocket listener = new ServerSocket(4310);
        while (true){
            Socket clientSocket = listener.accept();
            Thread messageThread = new Thread() {
                public void run() {
                    try {
                        DataInputStream dIS = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream dOS = new DataOutputStream(clientSocket.getOutputStream());
                        byte[] byteStream = new byte[BYTE_STREAM_LENGTH];
                        int index = 0;
                        String username = "";
                        String password = "";
                        long lastTimeSent = System.currentTimeMillis();
                        while (true) {
                            if (dIS.available() > 0) {
                                byte b = dIS.readByte();
                                if (b == (byte) ('|') && byteStream[index-1]==(byte)('<') && byteStream[index-2]==(byte)('>')) {
                                    String text = new String(byteStream, "UTF-8");
                                    String header = text.substring(0, 3);
                                    if (header.equals("&t&")){ // if text message
                                        String toUser = text.substring(3, text.indexOf("$%^"));
                                        String userMessage = text.substring(text.indexOf("$%^")+3, index-2);
                                        findUserByName(toUser).dOS.write
                                                (("#t#" + username + "$%^" + userMessage + "><|").
                                                getBytes(Charset.forName("UTF-8")));
                                        String chatRoom1 = username + toUser;
                                        String chatRoom2 = toUser + username;
                                        if (database.query(String.format("SHOW TABLES LIKE '%s'", filter(chatRoom1))).next()){
                                            database.update(String.format(
                                                    "INSERT INTO %s (writer, msg, image) VALUES ('%s', '%s', 'F');", filter(chatRoom1), filter(username), filter(userMessage)));
                                        }
                                        else if (database.query(String.format("SHOW TABLES LIKE '%s'", filter(chatRoom2))).next()){
                                            database.update(String.format(
                                                    "INSERT INTO %s (writer, msg, image) VALUES ('%s', '%s', 'F');", filter(chatRoom2), filter(username), filter(userMessage)));
                                        }
                                    }
                                    else if (header.equals("^o>")){
                                        // sending back the list of current online users
                                        String usrString = "";
                                        for (int i=0; i<activeUsers.size(); i++){
                                            usrString = usrString + activeUsers.get(i).userName + "###";
                                        }
                                        for (int i = 0; i < activeUsers.size(); i++) {
                                            try{
                                                activeUsers.get(i).dOS.write(("%s%" + usrString + "><|").getBytes(Charset.forName("UTF-8")));
                                            } catch (SocketException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    else if (header.equals(")*(")){
                                        // if user closes his FriendListWindow
                                        activeUsers.remove(findUserByName(text.substring(3, index-2)));
                                    }
                                    else if (header.equals("*u*")){// if username
                                        username = text.substring(3, index-2);
                                    }
                                    else if (header.equals("*p*")){// if password
                                        password = text.substring(3, index-2); // for login
                                        if (checkUserLogin(username, password) && findUserByName(username)==null){// if password and username match
                                            activeUsers.add(new User(dIS, dOS, username));
                                            System.out.println("Logged in successfully for user " + username);
                                            // send confirmation to client so that client opens to user
                                            dOS.write(("%s%" + "t" + "><|").getBytes(Charset.forName("UTF-8")));
                                            // for each active user, send this new username string full of names to them
                                            String usrString = "";
                                            for (int i=0; i<activeUsers.size(); i++){
                                                usrString = usrString + activeUsers.get(i).userName + "###";
                                            }
                                            for (int i=0; i<activeUsers.size(); i++){
                                                activeUsers.get(i).dOS.write(("%s%" + usrString + "><|").getBytes(Charset.forName("UTF-8")));
                                            }
                                        }
                                        else {
                                            // if password and username not match, send to client to print out error message
                                            dOS.write(("%s%" + "f" + "><|").getBytes(Charset.forName("UTF-8")));
                                        }
                                    }
                                    else if (header.equals(")-&")){
                                        // request past messages
                                        String toUser = text.substring(3, index-2);
                                        String chatRoom = toUser + username;
                                        String chatRoom2 = username + toUser;
                                        User u = findUserByName(username);
                                        if(database.query(String.format("SHOW TABLES LIKE '%s';", filter(chatRoom))).next()){
                                            ResultSet pastMsg = database.query(String.format("SELECT * from %s;", filter(chatRoom)));
                                            while (pastMsg.next()){
                                                String writer = pastMsg.getString("writer");
                                                String msg = pastMsg.getString("msg");
                                                String isImage = pastMsg.getString("image");
                                                if (isImage.equals("F")) {
                                                    u.dOS.write(("!t^" + toUser + "$%^" + writer + ": " + msg + "><|").getBytes(Charset.forName("UTF-8")));
                                                }
                                                else {
                                                    File file = new File(IMAGE_FOLDER_ROUTE + msg);
                                                    String extension = file.getName().substring(file.getName().lastIndexOf(".")+1);
                                                    BufferedImage bufferedImage = ImageIO.read(file);
                                                    u.dOS.write(("{;=" + toUser + "$%^" + writer + "/%\\" + extension + "$%^").getBytes(Charset.forName("UTF-8")));
                                                    ImageIO.write(bufferedImage, extension, u.dOS);
                                                    u.dOS.write("><|".getBytes(Charset.forName("UTF-8")));
                                                }
                                            }
                                        }
                                        else if (database.query(String.format("SHOW TABLES LIKE '%s';", filter(chatRoom2))).next()){
                                            ResultSet pastMsg = database.query(String.format("SELECT * from %s;", filter(chatRoom2)));
                                            while (pastMsg.next()){
                                                String writer = pastMsg.getString("writer");
                                                String msg = pastMsg.getString("msg");
                                                String isImage = pastMsg.getString("image");
                                                if (isImage.equals("F")) {
                                                    u.dOS.write(("!t^" + toUser + "$%^" + writer + ": " + msg + "><|").getBytes(Charset.forName("UTF-8")));
                                                }
                                                else {
                                                    File file = new File(IMAGE_FOLDER_ROUTE + msg);
                                                    String extension = file.getName().substring(file.getName().lastIndexOf(".")+1);
                                                    System.out.println(file.getAbsolutePath());
                                                    BufferedImage bufferedImage = ImageIO.read(file);
                                                    u.dOS.write(("{;=" + toUser + "$%^" + writer + "/%\\" + extension + "$%^").getBytes(Charset.forName("UTF-8")));
                                                    ImageIO.write(bufferedImage, extension, u.dOS);
                                                    u.dOS.write("><|".getBytes(Charset.forName("UTF-8")));
                                                }
                                            }
                                        }
                                        else { // if chat room doesn't exist, create new one
                                            database.update(String.format("CREATE TABLE %s (writer VARCHAR(255), msg TEXT, image VARCHAR(1));", filter(chatRoom)));
                                        }
                                    }
                                    else if (header.equals("^u^")){ // for sign up
                                        username = text.substring(3, index-2);
                                    }
                                    else if (header.equals("^p^")){
                                        password = text.substring(3, index-2);
                                        if (!checkUserSignUp(username)){
                                            // if user is not created
                                            // add new username and password to database
                                            database.update(String.format(
                                                    "INSERT INTO users (name, password) VALUES ('%s', '%s');", filter(username), filter(password)));
                                            activeUsers.add(new User(dIS, dOS, username));
                                            System.out.println("Sign up successfully for user " + username);
                                            // send confirmation to client so that clients open to user
                                            dOS.write(("%s%" + "t" + "><|").getBytes(Charset.forName("UTF-8")));
                                            // for each active user, send this new username string to them
                                            for (int i=0; i<activeUsers.size()-1; i++){
                                                // announce to each online user about the new arrival
                                                activeUsers.get(i).dOS.write(("%s%" + username + "><|").getBytes(Charset.forName("UTF-8")));
                                                // alert the user of every online user (excluding himself)
                                                dOS.write(("%s%" + activeUsers.get(i).userName + "><|").getBytes(Charset.forName("UTF-8")));
                                            }
                                        }
                                        else {
                                            // if user is already created
                                            dOS.write(("%s%" + "f" + "><|").getBytes(Charset.forName("UTF-8")));
                                        }
                                    }
                                    else if (header.equals("^o^")){
                                        String toUser = text.substring(3, index-2);
                                        findUserByName(toUser).dOS.write(("@s@" + username + "><|").getBytes(Charset.forName("UTF-8")));
                                    }
                                    else if (header.equals("$#*")){
                                        // get byte, recompose the image, save it to the folder then send it to the receiving client
                                        int firstIndexOf = text.indexOf("$%^");
                                        String toUser = text.substring(3, firstIndexOf);
                                        int secondIndexOf = text.indexOf("?|?");
                                        String extension = text.substring(firstIndexOf+3, secondIndexOf);
                                        System.out.println("Sending image of type "+ extension + " to " + toUser);
                                        byte[] imageInByte = Arrays.copyOfRange(byteStream, secondIndexOf+3, index-2);
                                        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageInByte));
                                        String newFileName = Integer.toString(SAVE_IMAGE_FOLDER.listFiles().length+1) + "." + extension;
                                        ImageIO.write(img, extension, new File(IMAGE_FOLDER_ROUTE + newFileName));
                                        User usr = findUserByName(toUser);
                                        usr.dOS.write((".?." + username + "$%^" + extension + "?|?").getBytes(Charset.forName("UTF-8")));
                                        usr.dOS.write(imageInByte);
                                        usr.dOS.write("><|".getBytes(Charset.forName("UTF-8")));
                                        String chatRoom1 = username + toUser;
                                        String chatRoom2 = toUser + username;
                                        if (database.query(String.format("SHOW TABLES LIKE '%s'", filter(chatRoom1))).next()){
                                            database.update(String.format(
                                                    "INSERT INTO %s (writer, msg, image) VALUES ('%s', '%s', 'T');", filter(chatRoom1), filter(username), filter(newFileName)));
                                        }
                                        else if (database.query(String.format("SHOW TABLES LIKE '%s'", filter(chatRoom2))).next()){
                                            database.update(String.format(
                                                    "INSERT INTO %s (writer, msg, image) VALUES ('%s', '%s', 'T');", filter(chatRoom2), filter(username), filter(newFileName)));
                                        }
                                    }
                                    else {

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

                            if (System.currentTimeMillis() - lastTimeSent > 1000){
                                lastTimeSent = System.currentTimeMillis();
                                try {
                                    dOS.write("^o>><|".getBytes(Charset.forName("UTF-8")));
                                } catch (SocketException e){
                                    activeUsers.remove(findUserByName(username));
                                    interrupt();
                                }
                            }
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            messageThread.start();
        }

    }

    public static User findUserByName(String name){
        for (int i=0; i<activeUsers.size(); i++){
            if (activeUsers.get(i).userName.equals(name)){
                return activeUsers.get(i);
            }
        }
        return null;
    }

    public static boolean checkUserLogin(String usn, String pw) throws Exception{
        ResultSet user = database.query(
                String.format("SELECT * FROM users WHERE name = '%s' AND password = '%s';", filter(usn), filter(pw)));
        return user.next();
    }
    public static boolean checkUserSignUp(String usn) throws Exception{
        ResultSet user = database.query(String.format("SELECT * FROM users WHERE name = '%s';", filter(usn)));
        return user.next();
    }

    public static String filter(String str){
        for (int i=0; i<str.length(); i++){
            if ((str.charAt(i)=='"') || (str.charAt(i)=='\'')){
                str = str.substring(0, i) + "\\" + str.substring(i);
                i+=1;
            }
        }
        return str;
    }
}
