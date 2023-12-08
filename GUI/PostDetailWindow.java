import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostDetailWindow extends JFrame {
    private JLabel lblMemberID, lblName, lblContact, lblEmail, lblRegistrationDate;
    private JTextField txtMemberID, txtName, txtContact, txtEmail, txtRegistrationDate;
    private JButton btnEdit, btnDelete, btnClose;
    private DB_Conn_Query dbConnection;

    public PostDetailWindow(DB_Conn_Query dbConnection, int memberID) {
        this.dbConnection = dbConnection;

        setTitle("회원 상세 정보");
        setSize(400, 300);
        setLayout(null);

        initComponents(memberID);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents(int memberID) {
        lblMemberID = new JLabel("회원 ID:");
        lblName = new JLabel("이름:");
        lblContact = new JLabel("연락처:");
        lblEmail = new JLabel("이메일:");
        lblRegistrationDate = new JLabel("등록일:");

        txtMemberID = new JTextField();
        txtName = new JTextField();
        txtContact = new JTextField();
        txtEmail = new JTextField();
        txtRegistrationDate = new JTextField();

        btnEdit = new JButton("수정");
        btnDelete = new JButton("삭제");
        btnClose = new JButton("닫기");

        lblMemberID.setBounds(20, 20, 80, 20);
        lblName.setBounds(20, 50, 80, 20);
        lblContact.setBounds(20, 80, 80, 20);
        lblEmail.setBounds(20, 110, 80, 20);
        lblRegistrationDate.setBounds(20, 140, 80, 20);

        txtMemberID.setBounds(120, 20, 200, 20);
        txtName.setBounds(120, 50, 200, 20);
        txtContact.setBounds(120, 80, 200, 20);
        txtEmail.setBounds(120, 110, 200, 20);
        txtRegistrationDate.setBounds(120, 140, 200, 20);

        btnEdit.setBounds(20, 180, 80, 30);
        btnDelete.setBounds(120, 180, 80, 30);
        btnClose.setBounds(220, 180, 80, 30);

        add(lblMemberID);
        add(lblName);
        add(lblContact);
        add(lblEmail);
        add(lblRegistrationDate);

        add(txtMemberID);
        add(txtName);
        add(txtContact);
        add(txtEmail);
        add(txtRegistrationDate);

        add(btnEdit);
        add(btnDelete);
        add(btnClose);

        // 더블클릭한 회원 정보를 가져와서 텍스트 필드에 표시
        loadMemberInfo(memberID);

        btnEdit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 수정 기능 구현
            }
        });

        btnDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 삭제 기능 구현
            }
        });

        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();  // 창 닫기
            }
        });
        
        // 창을 화면 가운데로 위치시키는 코드
        setLocationRelativeTo(null);
    }

    // 회원 정보를 받아와 JTextField에 넣는 메서드
    private void loadMemberInfo(int memberID) {
        try {
            String query = "SELECT * FROM 회원 WHERE 회원ID = ?";
            PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query);
            pstmt.setInt(1, memberID);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                txtMemberID.setText(String.valueOf(rs.getInt("회원ID")));
                txtName.setText(rs.getString("이름"));
                txtContact.setText(rs.getString("연락처"));
                txtEmail.setText(rs.getString("이메일"));
                txtRegistrationDate.setText(rs.getString("등록일"));
            }

            rs.close();
            pstmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

