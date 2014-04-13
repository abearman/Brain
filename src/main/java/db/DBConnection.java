package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


/** 
 * db.DBConnection class handles creating and closing the connection.
 */
public class DBConnection {
	

	private Statement stmt;
	private Connection con;
	
	/** 
	 * Constructor connects to the database.
	 */
	public DBConnection() throws SQLException {
		try{
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			con = DriverManager.getConnection("jdbc:mysql://" + MyDBInfo.MYSQL_DATABASE_SERVER + "/" + MyDBInfo.MYSQL_DATABASE_NAME, MyDBInfo.MYSQL_USERNAME, MyDBInfo.MYSQL_PASSWORD);
			stmt = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Returns a statement so that product catalog can do querying.
	 */
	public Statement getStatement(){
		return stmt;
	}
	
	/** 
	 * Closes the connection.
	 */
	public void closeConnection(){
		try{
			con.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
}


