import java.sql.*;

public class MySqlCon {

    private Connection con = null;

    public MySqlCon(String link, String name, String password) throws Exception{
        Class.forName("com.mysql.jdbc.Driver");
        con = DriverManager.getConnection(link, name, password);
    }

    public Connection getConnection(){
        return con;
    }

    public void update(String sqlCommand) throws Exception{
        con.createStatement().executeUpdate(sqlCommand);
    }

    public ResultSet query(String sqlCommand) throws Exception{
        return con.createStatement().executeQuery(sqlCommand);
    }
}
