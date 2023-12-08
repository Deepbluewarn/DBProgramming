import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class OverdueInfoWindow extends JFrame {
    private JTable OverdueTable;
    private NonEditableTableModel OverdueTableModel; // 수정이 불가능한 모델 사용
    private DB_Conn_Query dbConnection;

    private JLabel info;

    public OverdueInfoWindow(DB_Conn_Query dbConnection) {
        this.dbConnection = dbConnection;

        setTitle("연체 현황 조회");
        setSize(800, 700);
        setLayout(null);

        initComponents();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        OverdueTableModel = new NonEditableTableModel(new String[]{"회원ID", "이름", "대여시작일", "반납일", "반납예정일", "공구ID"}, 0);
        OverdueTable = new JTable(OverdueTableModel);
        JScrollPane memberScrollPane = new JScrollPane(OverdueTable);
        memberScrollPane.setBounds(10, 50, 760, 500);
        add(memberScrollPane);

        loadMemberData();

        JButton btnClose = new JButton("닫기");
        btnClose.setBounds(650, 560, 120, 30);
        add(btnClose);

        btnClose.addActionListener(e -> dispose());  // 창 닫기

        info = new JLabel("더블클릭 시 회원상세정보 생성");
        info.setBounds(10, 10, 200, 30);
        add(info);

        // 창을 화면 가운데로 위치시키는 코드
        setLocationRelativeTo(null);
    }

    // 회원 및 대여 정보를 JTABLE에 넣는 메서드
    private void loadMemberData() {
        clearTable(OverdueTableModel);
        try {
            Statement stmt = dbConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 대여기록.회원ID, 회원.이름, 대여기록.대여시작일, 대여기록.반납일, 대여기록.반납예정일, 대여기록.공구ID " +
                    "FROM 대여기록 " +
                    "JOIN 회원 ON 대여기록.회원ID = 회원.회원ID " +
                    "WHERE 대여기록.반납예정일 < SYSDATE");

            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getObject("회원ID"),
                        rs.getObject("이름"),
                        rs.getObject("대여시작일"),
                        rs.getObject("반납일"),
                        rs.getObject("반납예정일"),
                        rs.getObject("공구ID")
                };
                OverdueTableModel.addRow(row);
            }

            rs.close();
            stmt.close();

            // 14일 이상 연체한 행을 빨간색 굵은 글씨로 표시
            applyCellRenderer();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DB_Conn_Query dbConnection = new DB_Conn_Query();
            new OverdueInfoWindow(dbConnection);
        });
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

    // 연체 기록 행을 빨간색 또는 검은색 굵은 글씨로 표시하는 메서드
    private void applyCellRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // 테이블 모델에서 예정 반환일을 가져옴
                Date scheduledReturnDate = (Date) table.getValueAt(row, 4);
                // 테이블 모델에서 실제 반환일을 가져옴
                Date actualReturnDate = (Date) table.getValueAt(row, 3);

                // 현재 날짜와 비교
                long diff = scheduledReturnDate.getTime() - new Date().getTime();
                long daysDiff = diff / (24 * 60 * 60 * 1000);

                // 14일 이상 차이가 나는지 확인
                if (daysDiff < -14 && actualReturnDate == null) {
                    // 연체된 레코드에 대해 빨간 글자 및 볼드 적용
                    component.setFont(component.getFont().deriveFont(Font.BOLD));
                    component.setForeground(Color.RED);
                } else {
                    // 그 외의 경우에는 기본 스타일 적용
                    component.setFont(component.getFont().deriveFont(Font.PLAIN));
                    component.setForeground(table.getForeground());
                }

                return component;
            }
        };

        for (int i = 0; i < OverdueTable.getColumnCount(); i++) {
            OverdueTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }
}
