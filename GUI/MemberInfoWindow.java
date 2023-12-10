import oracle.jdbc.OracleTypes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberInfoWindow extends JFrame {
    private JTable memberTableForABestGrade;
    private JTable memberTableForBBestGrade;
    private NonEditableTableModel memberTableModelForABest; // 수정이 불가능한 모델 사용
    private NonEditableTableModel memberTableModelForBBest; // 수정이 불가능한 모델 사용
    private DB_Conn_Query dbConnection;
    
    private JLabel infoA;
    private JLabel infoB;
    
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
        memberTableModelForABest = new NonEditableTableModel(new String[]{"회원ID", "이름", "연락처", "이메일", "등록일", "등급", "대여횟수"}, 0);
        memberTableModelForBBest = new NonEditableTableModel(new String[]{"회원ID", "이름", "연락처", "이메일", "등록일", "등급", "대여횟수"}, 0);
        memberTableForABestGrade = new JTable(memberTableModelForABest);
        memberTableForBBestGrade = new JTable(memberTableModelForBBest);
        JScrollPane memberScrollPane_1 = new JScrollPane(memberTableForABestGrade);
        JScrollPane memberScrollPane_2 = new JScrollPane(memberTableForBBestGrade);
        memberScrollPane_1.setBounds(10, 50, 760, 250);
        memberScrollPane_2.setBounds(10, 340, 760, 250);
        add(memberScrollPane_1);
        add(memberScrollPane_2);

        try {
            loadMemberData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        JButton btnClose = new JButton("닫기");
        btnClose.setBounds(650, 600, 120, 30);
        add(btnClose);

        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();  // 창 닫기
            }
        });

        infoA = new JLabel("A 등급 중 대여횟수가 가장 많은 회원 목록");
        infoA.setBounds(10, 10, 240, 30);
        add(infoA);

        infoB = new JLabel("A 등급 중 대여횟수가 가장 많은 회원 목록");
        infoB.setBounds(10, 300, 240, 30);
        add(infoB);
        
        // 창을 화면 가운데로 위치시키는 코드
        setLocationRelativeTo(null);
    }

    // 회원정보를 JTABLE에 넣는 메서드
    private void loadMemberData() throws SQLException {
        CallableStatement cstmt = null;
        ResultSet resultSet = null;

        List<Object[]> A = new ArrayList<>();
        List<Object[]> B = new ArrayList<>();

        clearTable(memberTableModelForABest);
        clearTable(memberTableModelForBBest);
        try {
            cstmt = dbConnection.getConnection().prepareCall("{call UpdateUserGrade(?)}");
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            try {
                resultSet = (ResultSet) cstmt.getObject(1);

                while (resultSet.next()) {
                    String grade = resultSet.getString("등급");

                    Object[] row = new Object[7];
                    for (int i = 1; i <= 7; i++) {
                        row[i - 1] = resultSet.getObject(i);
                    }

                    compareAdd(row, grade.equals("A") ? A : B);
                }
            }catch (SQLException ex){
                ex.printStackTrace();
            }finally {
                cstmt.close();
                if(resultSet != null) resultSet.close();

                for(Object[] a : A){
                    memberTableModelForABest.addRow(a);
                }
                for(Object[] b : B){
                    memberTableModelForBBest.addRow(b);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {

            if(cstmt != null) cstmt.close();
        }
    }

    private void compareAdd(Object[] row, List<Object[]> rows){
        if(rows.isEmpty()){
            rows.add(row);
        }else{
            // 가장 최근에 추가된 항목의 대여횟수를 가져온다.
            int lastRentCnt = Integer.parseInt((String.valueOf(rows.get(rows.size() - 1)[6])));

            // 추가하고자 하는 항목의 대여횟수를 가져온다.
            int rentCnt = Integer.parseInt(String.valueOf(row[6]));

            // 추가하고자 하는 항목의 대여횟수가 마지막으로 추가했던 항목의 대여횟수보다
            // 크면 기존의 항목을 삭제하고 새 항목을 추가한다.
            if(lastRentCnt < rentCnt){
                rows.clear(); // 하나의 등급 테이블에는 대여 횟수가 같은 행만 존재할 수 있다.
                rows.add(row);
            }else if(lastRentCnt == rentCnt){
                rows.add(row);
            }
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
