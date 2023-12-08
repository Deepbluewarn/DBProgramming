import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB_Conn_Query {
	
 Connection con = null;

 public DB_Conn_Query() {
	 
     // 데이터베이스 연결 정보
     String url = "jdbc:oracle:thin:@localhost:1521:XE";
     String id = "TOOL";
     String password = "1234";

     try {
         Class.forName("oracle.jdbc.driver.OracleDriver");
         System.out.println("드라이버 적재 성공");

         // 데이터베이스 연결
         con = DriverManager.getConnection(url, id, password);
         System.out.println("DB 연결 성공");
     } catch (ClassNotFoundException e) {
         System.out.println("드라이버를 찾을 수 없습니다.");
     } catch (SQLException e) {
         System.out.println("DB 연결 실패");
     }
 }

 // 데이터베이스 연결 종료 메서드
 public void closeConnection() {
     try {
         if (con != null && !con.isClosed()) {
             con.close();
             System.out.println("DB 연결 종료");
         }
     } catch (SQLException e) {
         e.printStackTrace();
     }
 }

 // 데이터베이스 연결 객체 반환 메서드
 public Connection getConnection() {
     return con;
 }
}
