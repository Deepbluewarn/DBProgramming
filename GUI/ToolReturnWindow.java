import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ToolReturnWindow extends JFrame {
    private DB_Conn_Query dbConnection;
    private JLabel lblMemberName, lblMemberContact;
    private JTextField txtMemberName, txtMemberContact;
    private JButton btnSearch, btnReturn, btnClose;
    private JTable rentalTable;
    private DefaultTableModel rentalTableModel;

    public ToolReturnWindow(DB_Conn_Query dbConnection) {
        this.dbConnection = dbConnection;

        setTitle("공구 반납");
        setSize(1000, 700);
        setLayout(null);

        initComponents();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        // 레이블 생성 및 위치 지정
        lblMemberName = new JLabel("이름");
        lblMemberContact = new JLabel("전화번호");

        lblMemberName.setBounds(10, 10, 40, 20);
        lblMemberContact.setBounds(170, 10, 60, 20);

        add(lblMemberName);
        add(lblMemberContact);

        // 텍스트 필드 생성 및 위치 지정
        txtMemberName = new JTextField();
        txtMemberContact = new JTextField();

        txtMemberName.setBounds(50, 10, 100, 20);
        txtMemberContact.setBounds(230, 10, 100, 20);

        add(txtMemberName);
        add(txtMemberContact);

        // 검색, 반납, 닫기 버튼 생성 및 위치 지정
        btnSearch = new JButton("검색");
        btnReturn = new JButton("반납");
        btnClose = new JButton("닫기");

        btnSearch.setBounds(340, 10, 80, 20);
        btnReturn.setBounds(800, 600, 80, 30);
        btnClose.setBounds(900, 600, 80, 30);

        add(btnSearch);
        add(btnReturn);
        add(btnClose);

        // 검색 버튼에 액션 리스너 추가
        btnSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchRentalHistory();
            }
        });

        // 반납 버튼에 액션 리스너 추가
        btnReturn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                returnTool();
            }
        });

        // 닫기 버튼에 액션 리스너 추가
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // 창 닫기
            }
        });

        // 대여 기록을 표시할 테이블 생성
        rentalTableModel = new DefaultTableModel(new String[]{"대여ID", "대여시작일", "반납일", "반납예정일", "공구ID", "항목ID", "관리자ID"}, 0);
        rentalTable = new JTable(rentalTableModel);
        JScrollPane rentalScrollPane = new JScrollPane(rentalTable);
        rentalScrollPane.setBounds(10, 50, 960, 500);
        add(rentalScrollPane);

        // 화면을 화면 중앙으로 위치시킴
        setLocationRelativeTo(null);
    }

    private void searchRentalHistory() {
        String memberName = txtMemberName.getText().trim();
        String memberContact = txtMemberContact.getText().trim();

        if (memberName.isEmpty() || memberContact.isEmpty()) {
            JOptionPane.showMessageDialog(null, "회원 이름과 연락처를 입력하세요.");
            return;
        }

        clearTable(rentalTableModel);

        try {
            // 회원 이름과 연락처를 이용하여 대여 기록을 가져옴
            String query = "SELECT * FROM 대여기록 WHERE 회원ID = (SELECT 회원ID FROM 회원 WHERE 이름 = ? AND 연락처 = ?)";
            try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
                pstmt.setString(1, memberName);
                pstmt.setString(2, memberContact);

                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Object[] row = new Object[7];
                    for (int i = 1; i <= 7; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    rentalTableModel.addRow(row);
                }

                rs.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void returnTool() {
        int selectedRow = rentalTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "대여 기록을 선택하세요.");
            return;
        }

        try {
            int rentID = (int) rentalTableModel.getValueAt(selectedRow, 0);
            int toolID = (int) rentalTableModel.getValueAt(selectedRow, 4);

            // Update the return date in the database
            String updateReturnDateQuery = "UPDATE 대여기록 SET 반납일 = ? WHERE 대여ID = ?";
            try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(updateReturnDateQuery)) {
                java.util.Date currentDate = new java.util.Date();
                java.sql.Date sqlReturnDate = new java.sql.Date(currentDate.getTime());

                pstmt.setDate(1, sqlReturnDate);
                pstmt.setInt(2, rentID);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Notify the return
                    JOptionPane.showMessageDialog(null, "공구가 성공적으로 반납되었습니다.");
                    // Optionally, update the table to reflect the changes
                    rentalTableModel.setValueAt(sqlReturnDate, selectedRow, 2);
                } else {
                    JOptionPane.showMessageDialog(null, "반납에 실패했습니다.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void clearTable(DefaultTableModel tableModel) {
        int rowCount = tableModel.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            tableModel.removeRow(i);
        }
    }
}