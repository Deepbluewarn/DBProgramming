import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ToolProgramGUI extends JFrame {
    private JButton btnRentTool, btnPostInfo, btnOverdue, btnMemberInfo,btnToolStats ;
    private DB_Conn_Query dbConnection;

    public ToolProgramGUI() {
        setTitle("공구 대여 프로그램");
        setSize(300, 250);
        setLayout(null);

        dbConnection = new DB_Conn_Query();

        initComponents();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        btnRentTool = new JButton("공구대여");
        btnPostInfo = new JButton("후기조회");
        btnOverdue = new JButton("연체현황조회");
        btnMemberInfo = new JButton("회원대여통계");
        btnToolStats = new JButton("공구대여통계");
        

        btnRentTool.setBounds(10, 60, 120, 30);
        btnPostInfo.setBounds(140, 60, 120, 30);
        btnOverdue.setBounds(140, 100, 120, 30);
        btnMemberInfo.setBounds(10, 140, 120, 30);
        btnToolStats.setBounds(140, 140, 120, 30);
        
        add(btnRentTool);
        add(btnPostInfo);
        add(btnOverdue);
        add(btnMemberInfo);
        add(btnToolStats);

        btnRentTool.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openRentToolWindow();
            }
        });

        btnPostInfo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	openPostInfoWindow();
            }
        });
        
        btnOverdue.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openOverdueInfoWindow();
            }
        });
        
        btnMemberInfo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	openMemberInfoWindow();
            }
        });
        
        btnToolStats.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	openToolStatisticsWindow();
            }
        });
        
        // 창을 화면 가운데로 위치시키는 코드
        setLocationRelativeTo(null);
    }

    private void openRentToolWindow() {
        RentToolWindow rentToolWindow = new RentToolWindow(dbConnection);
    }
    
    private void openPostInfoWindow() {
        PostInfoWindow postInfoWindow = new PostInfoWindow(dbConnection);
    }
    
    private void openOverdueInfoWindow() {
        OverdueInfoWindow overdueInfoWindow = new OverdueInfoWindow(dbConnection);
    }
    
    private void openMemberInfoWindow() {
    	MemberInfoWindow memberInfoWindow = new MemberInfoWindow(dbConnection);
    }

    private void openToolStatisticsWindow() {
    	ToolStatisticsWindow toolStatistics = new ToolStatisticsWindow(dbConnection);
    }
    
    public static void main(String[] args) {
        new ToolProgramGUI();
    }
}
