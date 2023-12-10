import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class RentToolWindow extends JFrame {
    private JTable toolTable, memberTable;
    private DefaultTableModel toolTableModel, memberTableModel;
    private JButton btnRent, btnClose;
    private DB_Conn_Query dbConnection;

    public RentToolWindow(DB_Conn_Query dbConnection) {
        this.dbConnection = dbConnection;

        setTitle("공구 대여");
        setSize(1000, 700);
        setLayout(null);

        initComponents();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        toolTableModel = new DefaultTableModel(new String[]{"공구 ID", "공구이름", "제조사", "종류", "보유수량", "대여수량"}, 0);
        toolTable = new JTable(toolTableModel);
        JScrollPane toolScrollPane = new JScrollPane(toolTable);
        toolScrollPane.setBounds(10, 50, 450, 500);
        add(toolScrollPane);

        memberTableModel = new DefaultTableModel(new String[]{"회원 ID", "이름", "연락처", "이메일", "등록일"}, 0);
        memberTable = new JTable(memberTableModel);
        JScrollPane memberScrollPane = new JScrollPane(memberTable);
        memberScrollPane.setBounds(470, 50, 450, 500);
        add(memberScrollPane);

        btnRent = new JButton("대여");
        btnClose = new JButton("닫기");

        btnRent.setBounds(700, 560, 120, 30);
        btnClose.setBounds(830, 560, 120, 30);

        add(btnRent);
        add(btnClose);

        btnRent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 대여 기능 구현
                rentTool();
                dispose();  // 창 닫기
            }
        });

        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();  // 창 닫기
            }
        });

        loadToolData();
        loadMemberData();
        
        // 창을 화면 가운데로 위치시키는 코드
        setLocationRelativeTo(null);
    }

    // JTABLE에 DB에서 공구값을 넣는 메서드
    private void loadToolData() {
        clearTable(toolTableModel);
        try {
            Statement stmt = dbConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("select * from 공구");

            while (rs.next()) {
                Object[] row = new Object[6];
                for (int i = 1; i <= 6; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                toolTableModel.addRow(row);
            }

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // JTABLE에 DB에서 회원값을 넣는 메서드
    private void loadMemberData() {
        clearTable(memberTableModel);
        try {
            Statement stmt = dbConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("select * from 회원");

            while (rs.next()) {
                Object[] row = new Object[5];
                for (int i = 1; i <= 5; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                memberTableModel.addRow(row);
            }

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    // JTABLE 갱신 메서드
    // JTABLE의 모든 행 초기화
    private void clearTable(DefaultTableModel tableModel) {
        int rowCount = tableModel.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            tableModel.removeRow(i);
        }
    }
    
    private void rentTool() {
        int selectedToolRow = toolTable.getSelectedRow();
        int selectedMemberRow = memberTable.getSelectedRow();

        if (selectedToolRow == -1 || selectedMemberRow == -1) {
            JOptionPane.showMessageDialog(null, "공구와 회원을 선택하세요.");
            return;
        }

        int toolID = ((BigDecimal) toolTableModel.getValueAt(selectedToolRow, 0)).intValue();
        int memberID = ((BigDecimal) memberTableModel.getValueAt(selectedMemberRow, 0)).intValue();

        try {
            int availableQuantity = getAvailableQuantity(toolID);

            if (availableQuantity <= 0) {
                JOptionPane.showMessageDialog(null, "해당 공구의 보유수량이 없어 대여할 수 없습니다.");
                return;
            }

            Date currentDate = new Date();
            java.sql.Date sqlDate = new java.sql.Date(currentDate.getTime());

            // 대여 기록 삽입
            String insertRentRecordQuery = "INSERT INTO 대여기록 (대여ID, 대여시작일, 반납일, 반납예정일, 공구ID, 항목ID, 관리자ID, 회원ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(insertRentRecordQuery)) {
                // 대여 ID 설정
                int rentID = getHighestRentID() + 1;

                pstmt.setInt(1, rentID);
                pstmt.setDate(2, sqlDate);
                pstmt.setNull(3, java.sql.Types.DATE);  // 반납일은 NULL로 설정
                pstmt.setDate(4, calculateReturnDueDate(sqlDate)); // 반납예정일은 대여시작일 + 7일
                pstmt.setInt(5, toolID);
                pstmt.setInt(6, getToolItemID(toolID));
                pstmt.setInt(7, 1);  // 관리자 ID는 1로 고정
                pstmt.setInt(8, memberID);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                	// 대여가 성공했으므로 공구의 보유수량을 감소시킴
                    JOptionPane.showMessageDialog(null, "공구가 성공적으로 대여되었습니다.");
                } else {
                    JOptionPane.showMessageDialog(null, "대여에 실패했습니다.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private int getAvailableQuantity(int toolID) throws SQLException {
        int availableQuantity = 0;

        String query = "SELECT 보유수량 FROM 공구 WHERE 공구ID = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, toolID);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                availableQuantity = rs.getInt("보유수량");
            }

            rs.close();
        }

        return availableQuantity;
    }

    private int getToolItemID(int toolID) throws SQLException {
        int toolItemID = 0;

        String query = "SELECT 항목ID FROM 공구항목 WHERE 공구ID = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, toolID);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                toolItemID = rs.getInt("항목ID");
            }

            rs.close();
        }

        return toolItemID;
    }

    // 대여기록에서 가장 높은 대여ID 가져오기
    private int getHighestRentID() throws SQLException {
        int highestRentID = 0;

        String query = "SELECT MAX(대여ID) AS 최고대여ID FROM 대여기록";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                highestRentID = rs.getInt("최고대여ID");
            }

            rs.close();
        }

        return highestRentID;
    }

    // 반납 예정일 계산 (대여시작일 + 7일)
    private java.sql.Date calculateReturnDueDate(java.sql.Date startDate) {
        long startDateMillis = startDate.getTime();
        long returnDueDateMillis = startDateMillis + (7 * 24 * 60 * 60 * 1000);  // 7일 후

        return new java.sql.Date(returnDueDateMillis);
    }
    
    // 대여성공 시 공구수량 업데이트
    private void updateToolQuantity(int toolID, int newQuantity) {
        String updateQuery = "UPDATE 공구 SET 보유수량 = ? WHERE 공구ID = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(updateQuery)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, toolID);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
