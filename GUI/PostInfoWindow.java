import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostInfoWindow extends JFrame {
    private JTable PostTable;
    private NonEditableTableModel PostTableModel; // 수정이 불가능한 모델 사용
    private DB_Conn_Query dbConnection;

    private JLabel info;

    public PostInfoWindow(DB_Conn_Query dbConnection) {
        this.dbConnection = dbConnection;

        setTitle("후기 조회");
        setSize(800, 700);
        setLayout(null);

        initComponents();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        PostTableModel = new NonEditableTableModel(new String[]{"평점", "내용", "작성일", "회원ID", "공구ID", "공구이름"}, 0);
        PostTable = new JTable(PostTableModel);
        JScrollPane memberScrollPane = new JScrollPane(PostTable);
        memberScrollPane.setBounds(10, 50, 760, 500);
        add(memberScrollPane);


        // 후기 상세 조회 기능
        PostTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = PostTable.getSelectedRow();
                    int memberID = Integer.parseInt(PostTable.getValueAt(selectedRow, 0).toString());
                    openMemberDetailWindow(memberID);
                }
            }
        });

        loadMemberData();

        JButton btnClose = new JButton("닫기");
        btnClose.setBounds(650, 560, 120, 30);
        add(btnClose);

        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();  // 창 닫기
            }
        });

        info = new JLabel("더블클릭 시 후기상세정보 생성");
        info.setBounds(10, 10, 200, 30);
        add(info);

        // 창을 화면 가운데로 위치시키는 코드
        setLocationRelativeTo(null);
    }

    private int getColumnIndexByName(JTable table, String columnName) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnName(i).equals(columnName)) {
                return i;
            }
        }
        return -1;
    }

    // 후기정보를 JTABLE에 넣는 메서드
    private void loadMemberData() {
        clearTable(PostTableModel);
        try {
            Statement stmt = dbConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 후기.후기ID, 후기.평점, 후기.내용, 후기.작성일, 후기.회원ID, 후기.공구ID, 공구.공구이름 " +
                    "FROM 후기 " +
                    "JOIN 공구 ON 후기.공구ID = 공구.공구ID");

            // 셀 렌더러 정의
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    // "평점" 열의 인덱스를 가져오기
                    int ratingColumnIndex = getColumnIndexByName(table, "평점");

                    // "평점"이 2 이하인 행을 빨간색으로 강조 표시
                    if (column == ratingColumnIndex && Integer.parseInt(value.toString()) <= 2) {
                        c.setForeground(Color.RED);
                    } else {
                        c.setForeground(table.getForeground());
                    }

                    return c;
                }
            };

            // 렌더러를 "평점" 열에 적용
            PostTable.getColumnModel().getColumn(getColumnIndexByName(PostTable, "평점")).setCellRenderer(renderer);

            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getObject("평점"),
                        rs.getObject("내용"),
                        rs.getObject("작성일"),
                        rs.getObject("회원ID"),
                        rs.getObject("공구ID"),
                        rs.getObject("공구이름")
                };
                PostTableModel.addRow(row);
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

    // 회원상세정보창 생성
    private void openMemberDetailWindow(int memberID) {
        new PostDetailWindow(dbConnection, memberID);
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
