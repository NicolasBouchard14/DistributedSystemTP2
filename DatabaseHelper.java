import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class DatabaseHelper {

    private static String database = "TP2_output";
    private static String user = "root";
    private static String password = "";
    private static String hostname = "localhost";

    private static Connection CreateConnection(){

        Connection conn = null;

        try {

            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+database+"?user="+user+"&password="+password);

        }
        catch (SQLException ex) {

            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());

        }
        catch(ClassNotFoundException ex){

            System.out.println("Class not found error: " + ex.getMessage());

        }

        return conn;
    }

    public static boolean InsertText(String en_text, String fr_text){

        Connection conn = CreateConnection();

        try {

            Statement stmt = conn.createStatement();

            String sql = "INSERT INTO texte VALUES (null, \""+en_text+"\", \""+fr_text+"\")";
            stmt.executeUpdate(sql);

        }
        catch(SQLException ex){

            System.out.println(ex.getMessage());

            return false;

        }

        return true;
    }

    public static boolean InsertImage(String[] blobs){

        Connection conn = CreateConnection();

        byte[] orig = Base64.decode(blobs[0]);
        byte[] f1 = Base64.decode(blobs[1]);
        byte[] f2 = Base64.decode(blobs[2]);

        try {

            String sql = "INSERT INTO image VALUES (?, ?, ?)";
            
            PreparedStatement stm = conn.prepareStatement(sql);
            
	    stm.setBytes(1,orig);
	    stm.setBytes(2,f1);
	    stm.setBytes(3,f2);

	    stm.executeUpdate();
	    stm.close();

        }
        catch(SQLException ex){

            System.out.println(ex.getMessage());

            return false;

        }

        return true;
    }
}
