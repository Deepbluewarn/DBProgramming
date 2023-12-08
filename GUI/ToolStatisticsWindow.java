import javax.swing.*;
import oracle.jdbc.OracleTypes;
import java.awt.*;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ToolStatisticsWindow extends JFrame {

    private DB_Conn_Query dbConnection;
    private JPanel chartPanel;

    private int currentgraphX = 10; // 초기 x 값
    private int currentLabelX = 20;
    private int currentRentalX = 8;
    
    public ToolStatisticsWindow(DB_Conn_Query dbConnection) {
        this.dbConnection = dbConnection;
        setTitle("공구 대여 통계");

        initComponents();

        try {
            getToolRentalStatistics();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "공구 대여 통계를 가져오는 중에 오류가 발생했습니다.");
        }
        setSize(800, 700);  // 화면 크기 조정
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        chartPanel = new JPanel();
        chartPanel.setLayout(null); // layout을 null로 설정
        
        JScrollPane scrollPane = new JScrollPane(chartPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        // chartPanel의 preferred size를 설정하고, 가로 스크롤의 길이를 조절
        chartPanel.setPreferredSize(new Dimension(20050, 700));

        add(scrollPane, BorderLayout.CENTER);
    }

    private void getToolRentalStatistics() throws SQLException {
        ResultSet resultSet = null;

        String query = "{call UpdateRentalCount(?)}";
        try (CallableStatement cstmt = dbConnection.getConnection().prepareCall(query)) {
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            try {
                resultSet = (ResultSet) cstmt.getObject(1);

                while (resultSet.next()) {
                    int toolId = resultSet.getInt("공구ID");
                    int rentalCount = resultSet.getInt("대여횟수");

                    System.out.print(toolId + ", ");
                    System.out.println(rentalCount);
                    drawBarChartForTool(toolId, rentalCount);

                    
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "공구 대여 통계를 가져오는 중에 오류가 발생했습니다.");
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void drawBarChartForTool(int toolId, int rentalCount) {
        BarChartPanel barChartPanel = new BarChartPanel(toolId, rentalCount);
        
        barChartPanel.setBounds(currentgraphX, 0, 60, 550);
        
        JLabel idLabel = new JLabel("ID: " + toolId); // 공구 ID
        JLabel rentalID = new JLabel("대여수: " + rentalCount);
        
        idLabel.setBounds(currentLabelX, 560, 50, 20); // 좌표와 크기를 원하는 값으로 설정
        rentalID.setBounds(currentRentalX, 585, 60, 20); // 좌표와 크기를 원하는 값으로 설정
        
        chartPanel.add(idLabel);
        chartPanel.add(rentalID);

        currentLabelX += 100; // x값을 조정하여 다음 컴포넌트가 오른쪽으로 나열되도록 함
        currentgraphX += 100;
        currentRentalX += 100;
        
        chartPanel.add(barChartPanel);

        chartPanel.revalidate(); // chartPanel을 다시 그리도록 revalidate 호출
    }

    private class BarChartPanel extends JPanel {
        private final int toolId;
        private final int rentalCount;

        public BarChartPanel(int toolId, int rentalCount) {
            this.toolId = toolId;
            this.rentalCount = rentalCount;

            // 크기를 고정하고 싶은 값을 설정
            int preferredWidth = 60;
            int preferredHeight = 550;

            // 크기를 설정
            setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawBarChart(g);
        }

        private void drawBarChart(Graphics g) {
            int barWidth = 60; // 막대 폭
            int barHeight = rentalCount * 25; // 대여횟수에 따라 동적으로 높이 설정
            int x = (getWidth() - barWidth) / 2;
            int y = getHeight() - barHeight; // 아래로 그리기 위해 y 위치 설정

            System.out.println(y);

            // 그래프 그리기
            g.setColor(Color.blue);
            g.fillRect(x, y, barWidth, barHeight);

            // ID 표시
            // g.setColor(Color.black);
            // FontMetrics fontMetrics = g.getFontMetrics();
            // int textWidth = fontMetrics.stringWidth("ID: " + toolId);
            // int textHeight = fontMetrics.getHeight();
            // int textX = x + (barWidth - textWidth) / 2;
            // int textY = y + barHeight + textHeight + 5; // 텍스트 위치 아래쪽에 조정

            // g.drawString("ID: " + toolId, textX, textY);
        }
    }

}
