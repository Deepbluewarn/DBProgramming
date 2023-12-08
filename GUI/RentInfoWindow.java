import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.math.BigDecimal;

public class RentInfoWindow extends JFrame {
    private JTable rentTable;
    private DefaultTableModel rentTableModel;
    private DB_Conn_Query dbConnection;

    public RentInfoWindow(DB_Conn_Query dbConnection) {
        this.dbConnection = dbConnection;

        setTitle("공구 대여 조회");
        setSize(800, 700);
        setLayout(null);

        initComponents();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        rentTableModel = new DefaultTableModel(new String[]{"대여ID", "대여시작일", "반납일", "반납예정일", "공구ID", "항목ID", "관리자ID", "회원ID"}, 0);
        rentTable = new JTable(rentTableModel);
        JScrollPane rentScrollPane = new JScrollPane(rentTable);
        rentScrollPane.setBounds(10, 50, 760, 500);
        add(rentScrollPane);

        loadRentData();

        JButton btnClose = new JButton("닫기");
        btnClose.setBounds(650, 560, 120, 30);
        add(btnClose);

        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();  // 창 닫기
            }
        });

        JButton btnReturn = new JButton("반납");
        btnReturn.setBounds(500, 560, 120, 30);
        add(btnReturn);

        btnReturn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 선택된 행의 대여ID 가져오기
                int selectedRow = rentTable.getSelectedRow();
                if (selectedRow != -1) {
                	BigDecimal rentIDBigDecimal = (BigDecimal) rentTableModel.getValueAt(selectedRow, 0);
                	int rentID = rentIDBigDecimal.intValue();

                    // 반납일 업데이트
                    updateReturnDate(rentID);

                    // 대여 기록 다시 로드
                    loadRentData();
                } else {
                    JOptionPane.showMessageDialog(null, "반납할 대여 기록을 선택하세요.");
                }
            }
        });

        // 창을 화면 가운데로 위치시키는 코드
        setLocationRelativeTo(null);
    }

    private void updateReturnDate(int rentID) {
        try {
            // 반납일을 현재 날짜로 업데이트
            String updateQuery = "UPDATE 대여기록 SET 반납일 = CURRENT_DATE WHERE 대여ID = ?";
            PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(updateQuery);
            preparedStatement.setInt(1, rentID);
            preparedStatement.executeUpdate();

            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadRentData() {
        clearTable(rentTableModel);
        try {
            Statement stmt = dbConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM 대여기록");

            while (rs.next()) {
                Object[] row = new Object[8];
                for (int i = 1; i <= 8; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                rentTableModel.addRow(row);
            }

            rs.close();
            stmt.close();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DB_Conn_Query dbConnection = new DB_Conn_Query();
            new RentInfoWindow(dbConnection);
        });
    }
}
