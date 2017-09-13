import java.io.DataInputStream;
import java.io.DataOutputStream;

public class User {
    public DataInputStream dIS;
    public DataOutputStream dOS;
    public String userName;

    public User(DataInputStream input, DataOutputStream output, String user){
        dIS = input;
        dOS = output;
        userName = user;
    }

    public String getName(){
        return userName;
    }
}
