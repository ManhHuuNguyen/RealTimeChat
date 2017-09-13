import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.util.ArrayList;

public class Server {

    public static MySqlCon database = null;
    public static ArrayList<User> activeUsers = new ArrayList<>();

    public static void start() throws Exception {
        database = new MySqlCon("jdbc:mysql://localhost:3306/chatApp","root","root");
        System.out.println("Server starting...");
        ServerSocket listener = new ServerSocket(4310);
        while (true){
            Socket clientSocket = listener.accept();
            System.out.println("Connection accepted!!!");
            Thread th = new Thread() {
                public void run() {
                    try {
                        DataInputStream dIS = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream dOS = new DataOutputStream(clientSocket.getOutputStream());
                        byte[] byteStream = new byte[8096];
                        int index = 0;
                        String username = "";
                        String password = "";
                        while (true) {
                            if (dIS.available() > 0) {
                                byte b = dIS.readByte();
                                if (b == (byte) ('|')) {
                                    String text = new String(byteStream, "UTF-8");
                                    if (text.substring(0, 3).equals("*u*")){
                                        // if username
                                        username = text.substring(3, index);
                                    }
                                    else if (text.substring(0, 3).equals("*p*")){
                                        // if password
                                        password = text.substring(3, index);
                                        if (checkLogin(username, password)){
                                            // if password and username match
                                            activeUsers.add(new User(dIS, dOS, username));
                                            System.out.println("Logged in successfully for user " + username);
                                            // send confirmation to client so that client opens to user
                                            dOS.write(("%s%" + "t" + "|").getBytes(Charset.forName("UTF-8")));
                                            // for each active user, send this new username string to them
                                            for (User onlineUser: activeUsers){
                                                onlineUser.dOS.write(("%s%" + username + "|").getBytes(Charset.forName("UTF-8")));
                                            }
                                        }
                                        else {
                                            // if password and username not match, send to client to print out error message
                                            dOS.write(("%s%" + "f" + "|").getBytes(Charset.forName("UTF-8")));
                                        }
                                    }
                                    else {
                                        // if text message
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
                        }
                    } catch (Exception e){
                        System.out.println(e);
                    }
                }
            };
            th.start();
        }
    }

    public static boolean checkLogin(String usn, String pw) throws Exception{
        ResultSet user = database.query(
                String.format("SELECT * FROM users WHERE name = '%s' AND password = '%s'", usn, pw));
        return user.next();
    }
}
