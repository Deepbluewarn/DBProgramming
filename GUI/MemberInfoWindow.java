import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MemberInfoWindow extends JFrame {
    private JTable memberTable;
    private NonEditableTableModel memberTableModel; // 수정이 불가능한 모델 사용
    private DB_Conn_Query dbConnection;
    
    private JLabel info;

    public MemberInfoWindow(DB_Conn_Query dbConnection) {
        this.dbConnection = dbConnection;

        setTitle("회원 조회");
        setSize(800, 700);
        setLayout(null);

        initComponents();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        memberTableModel = new NonEditableTableModel(new String[]{"회원ID", "이름", "연락처", "이메일", "등록일"}, 0);
        memberTable = new JTable(memberTableModel);
        JScrollPane memberScrollPane = new JScrollPane(memberTable);
        memberScrollPane.setBounds(10, 50, 760, 500);
        add(memberScrollPane);

        loadMemberData();

        JButton btnClose = new JButton("닫기");
        btnClose.setBounds(650, 560, 120, 30);
        add(btnClose);

        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();  // 창 닫기
            }
        });
        
        info = new JLabel("더블클릭 시 상세회원정보창 생성");
        info.setBounds(10, 10, 200, 30);
        add(info);
        
        // 창을 화면 가운데로 위치시키는 코드
        setLocationRelativeTo(null);
    }

    // 회원정보를 JTABLE에 넣는 메서드
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

    // JTABLE 초기화 메서드
    private void clearTable(DefaultTableModel tableModel) {
        int rowCount = tableModel.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            tableModel.removeRow(i);
        }
    }


    // JTABLE 더블클릭 시 행에서 자체적으로 수정이 되게함
    // JTABLE의 자체적인 수정을 제한한 메서드
    private static class NonEditableTableModel extends DefaultTableModel {
        public NonEditableTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
