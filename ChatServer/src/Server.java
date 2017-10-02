import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.net.SocketException;

public class Server {

    public static MySqlCon database = null;
    public static ArrayList<User> activeUsers = new ArrayList<>(); // I might change this to a HashMap later

    public static void start() throws Exception {
        database = new MySqlCon("jdbc:mysql://localhost:3306/chatApp","root","root");
        System.out.println("Server starting...");
        ServerSocket listener = new ServerSocket(4310);
        while (true){
            Socket clientSocket = listener.accept();
            System.out.println("Connection accepted!!!");
            Thread messageThread = new Thread() {
                public void run() {
                    try {
                        DataInputStream dIS = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream dOS = new DataOutputStream(clientSocket.getOutputStream());
                        byte[] byteStream = new byte[8096];
                        int index = 0;
                        String username = "";
                        String password = "";
                        long lastTimeSent = System.currentTimeMillis();
                        while (true) {
                            if (dIS.available() > 0) {
                                byte b = dIS.readByte();
                                if (b == (byte) ('|')) {
                                    String text = new String(byteStream, "UTF-8");
                                    String header = text.substring(0, 3);
                                    if (header.equals("&t&")){ // if text message
                                        String toUser = text.substring(3, text.indexOf("$%^"));
                                        String userMessage = text.substring(text.indexOf("$%^")+3, index);
                                        findUserByName(toUser).dOS.write
                                                (("#t#" + username + "$%^" + userMessage + "|").
                                                getBytes(Charset.forName("UTF-8")));
                                        String chatRoom1 = username + toUser;
                                        String chatRoom2 = toUser + username;
                                        if (database.query(String.format("SHOW TABLES LIKE '%s'", filter(chatRoom1))).next()){
                                            database.update(String.format(
                                                    "INSERT INTO %s (writer, msg) VALUES ('%s', '%s');", filter(chatRoom1), filter(username), filter(userMessage)));
                                        }
                                        else if (database.query(String.format("SHOW TABLES LIKE '%s'", filter(chatRoom2))).next()){
                                            database.update(String.format(
                                                    "INSERT INTO %s (writer, msg) VALUES ('%s', '%s');", filter(chatRoom2), filter(username), filter(userMessage)));
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
                                                activeUsers.get(i).dOS.write(("%s%" + usrString + "|").getBytes(Charset.forName("UTF-8")));
                                            } catch (SocketException e){
                                                e.printStackTrace();
                                            }
                                        }

                                    }
                                    else if (header.equals("*u*")){// if username
                                        username = text.substring(3, index);
                                    }
                                    else if (header.equals("*p*")){// if password
                                        password = text.substring(3, index); // for login
                                        if (checkUserLogin(username, password)){// if password and username match
                                            activeUsers.add(new User(dIS, dOS, username));
                                            System.out.println("Logged in successfully for user " + username);
                                            // send confirmation to client so that client opens to user
                                            dOS.write(("%s%" + "t" + "|").getBytes(Charset.forName("UTF-8")));
                                            // for each active user, send this new username string full of names to them
                                            String usrString = "";
                                            for (int i=0; i<activeUsers.size(); i++){
                                                usrString = usrString + activeUsers.get(i).userName + "###";
                                            }
                                            for (int i=0; i<activeUsers.size(); i++){
                                                activeUsers.get(i).dOS.write(("%s%" + usrString + "|").getBytes(Charset.forName("UTF-8")));
                                            }
                                        }
                                        else {
                                            // if password and username not match, send to client to print out error message
                                            dOS.write(("%s%" + "f" + "|").getBytes(Charset.forName("UTF-8")));
                                        }
                                    }
                                    else if (header.equals("^u^")){ // for sign up
                                        username = text.substring(3, index);
                                    }
                                    else if (header.equals("^p^")){
                                        password = text.substring(3, index);
                                        if (!checkUserSignUp(username)){
                                            // if user is not created
                                            // add new username and password to database
                                            database.update(String.format(
                                                    "INSERT INTO users (name, password) VALUES ('%s', '%s');", filter(username), filter(password)));
                                            activeUsers.add(new User(dIS, dOS, username));
                                            System.out.println("Sign up successfully for user " + username);
                                            // send confirmation to client so that clients open to user
                                            dOS.write(("%s%" + "t" + "|").getBytes(Charset.forName("UTF-8")));
                                            // for each active user, send this new username string to them
                                            for (int i=0; i<activeUsers.size()-1; i++){
                                                // announce to each online user about the new arrival
                                                activeUsers.get(i).dOS.write(("%s%" + username + "|").getBytes(Charset.forName("UTF-8")));
                                                // alert the user of every online user (excluding himself)
                                                dOS.write(("%s%" + activeUsers.get(i).userName + "|").getBytes(Charset.forName("UTF-8")));
                                            }
                                        }
                                        else {
                                            // if user is already created
                                            dOS.write(("%s%" + "f" + "|").getBytes(Charset.forName("UTF-8")));
                                        }
                                    }
                                    else if (header.equals("^o^")){
                                        String toUser = text.substring(3, index);
                                        findUserByName(toUser).dOS.write(("@s@" + username + "|").getBytes(Charset.forName("UTF-8")));
                                        String chatRoom = toUser + username;
                                        String chatRoom2 = username + toUser;
                                        if(database.query(String.format("SHOW TABLES LIKE '%s';", filter(chatRoom))).next()){
                                            ResultSet pastMsg = database.query(String.format("SELECT * from %s;", filter(chatRoom)));
                                            System.out.println(chatRoom + " exists");
                                            while (pastMsg.next()){
                                                String writer = pastMsg.getString("writer");
                                                String msg = pastMsg.getString("msg");
                                                findUserByName(username).dOS.write(("!t^" + toUser + "$%^" + writer + ":" + msg + "|").getBytes(Charset.forName("UTF-8")));
                                                findUserByName(toUser).dOS.write(("!t^" + username + "$%^" + writer + ":" + msg + "|").getBytes(Charset.forName("UTF-8")));
                                            }
                                        }
                                        else if (database.query(String.format("SHOW TABLES LIKE '%s';", filter(chatRoom2))).next()){
                                            ResultSet pastMsg = database.query(String.format("SELECT * from %s;", filter(chatRoom2)));
                                            System.out.println(chatRoom2 + " exists");
                                            while (pastMsg.next()){
                                                String writer = pastMsg.getString("writer");
                                                String msg = pastMsg.getString("msg");
                                                findUserByName(username).dOS.write(("!t^" + toUser + "$%^" + writer + ":" + msg + "|").getBytes(Charset.forName("UTF-8")));
                                                findUserByName(toUser).dOS.write(("!t^" + username + "$%^" + writer + ":" + msg + "|").getBytes(Charset.forName("UTF-8")));

                                            }
                                        }
                                        else { // if chat room doesn't exist, create new one
                                            database.update(String.format("CREATE TABLE %s (writer VARCHAR(255), msg TEXT);", filter(chatRoom)));
                                        }
                                    }
                                    else {

                                    }
                                    byteStream = new byte[8096];
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
                                    dOS.write("^o>|".getBytes(Charset.forName("UTF-8")));
                                } catch (SocketException e){
                                    System.out.println("remove user...");
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
