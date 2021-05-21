import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class CounterMainFrameDB extends JFrame {

	private static DB db = new DB();
	private JPanel order, customer, customer1;
	private JLabel orderlbl;
	private JPanel pcnum1, pcnum2, pcnum3;
	private LineBorder borderThickness4 = new LineBorder(Color.black, 4);
	private LineBorder borderThickness3 = new LineBorder(Color.black, 3);
	private LineBorder borderThickness2 = new LineBorder(Color.black, 2);
	private LineBorder borderThickness1 = new LineBorder(Color.black, 1);
	private DefaultListModel<String> orderedPC = new DefaultListModel<>();
	private String[] pclist1 = { "2번 PC", "4번 PC", "6번 PC", "8번 PC", "10번 PC" };
	private JList<String> pclist;
	private JPanel orderNorth;
	private JButton[] bt;
	private JLabel[] lb;
	private JLabel[] timerset;
	private Timer timer;
	private TimerTask task;
	private int[] times = new int[30];
	private boolean[] isOrder = new boolean[30];
	private String[] menus = {"후라이드 치킨", "소떡소떡", "너구리 라면", "참이슬"};

	public CounterMainFrameDB(String title, int width, int height) {
		setTitle(title);
		setSize(width, height);
		setLocationRelativeTo(this);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setLayout(new BorderLayout());

		/*
		orderedPC.addElement("안녕");
		*/
		
		// 주문목록

		order = new JPanel();
		order.setLayout(new BorderLayout());
		order.setBackground(Color.DARK_GRAY);
		order.setBorder(borderThickness4);
		add(order, BorderLayout.WEST);

		orderNorth = new JPanel();
		orderNorth.setBackground(Color.DARK_GRAY);
		orderNorth.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, Color.black));

		orderlbl = new JLabel("주문 목록");
		orderlbl.setFont(new Font("맑은 고딕", Font.BOLD, 30));
		orderlbl.setForeground(Color.YELLOW);

		orderNorth.add(orderlbl);
		order.add(orderNorth, BorderLayout.NORTH);

		pclist = new JList<>(orderedPC);
		pclist.setFont(new Font("맑은 고딕", Font.BOLD, 30));
		pclist.setBackground(Color.DARK_GRAY);
		pclist.setForeground(Color.orange);
		order.add(pclist);

		// 손님 이용하는거 보는 틀

		customer = new JPanel();
		customer.setLayout(new BorderLayout());
		customer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		customer.setBackground(Color.gray);

		customer1 = new JPanel();
		customer1.setLayout(new GridLayout(3, 1, 20, 20));
		customer1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		customer1.setBackground(Color.white);

		// 컴퓨터 자리 세팅
		
		pcnum1 = new JPanel();
		pcnum1.setLayout(new GridLayout(2, 5, 5, 5));

		pcnum2 = new JPanel();
		pcnum2.setLayout(new GridLayout(2, 5, 5, 5));

		pcnum3 = new JPanel();
		pcnum3.setLayout(new GridLayout(2, 5, 5, 5));

		bt = new JButton[30];
		lb = new JLabel[30];
		timerset = new JLabel[30];
		
		for (int i = 0; i < bt.length; i++) {
			bt[i] = new JButton();
			bt[i].setLayout(new BorderLayout());
			bt[i].setBorder(borderThickness2);
			bt[i].addMouseListener(new MyListener());
			
			lb[i] = new JLabel((i + 1) + "번 PC");
			lb[i].setFont(new Font("맑은 고딕", Font.BOLD, 20));
			lb[i].setHorizontalAlignment(JLabel.CENTER);
			bt[i].add(lb[i], BorderLayout.NORTH);
			
			timerset[i] = new JLabel();
			timerset[i].setHorizontalAlignment(JLabel.CENTER);
			timerset[i].setFont(new Font("맑은 고딕", Font.BOLD, 20));
			bt[i].add(timerset[i], BorderLayout.CENTER);
			
			// 초기값은 오프라인으로 설정
			Offline(i);
			
			if (i < 10) {
				pcnum1.add(bt[i]);
			}
			else if (i < 20) {
				pcnum2.add(bt[i]);
			}
			else if (i < 30) {
				pcnum3.add(bt[i]);
			}
		}

		customer1.add(pcnum1, BorderLayout.NORTH);
		customer1.add(pcnum2, BorderLayout.CENTER);
		customer1.add(pcnum3, BorderLayout.SOUTH);
		customer.add(customer1, BorderLayout.CENTER);
		add(customer, BorderLayout.CENTER);

		// 타이머 작동 시작시키기
		timer = new Timer();
		task = new TimerTask() {

			@Override
			public void run() {
				stateUpdateFromDB();
				for (int i = 0; i < 30; i++) {
					if (isOrder[i]) {
						times[i]++;
						timerset[i].setText(times[i] + "초");	
					}
				}
			}
		};
		timer.scheduleAtFixedRate(task, 0, 1000);

		setVisible(true);
	}
	
	private void stateUpdateFromDB() {
		String sql = "SELECT * FROM state";
		ResultSet rs = db.Query(sql);
		try {
			while(rs.next()) {
				int pcNum = rs.getInt("pcNum");
				boolean state = rs.getBoolean("statement");
				stateUpdate(pcNum-1, state);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("DB에서 데이터를 받아오지 못함");
		}
	}

	boolean[] isOnline = new boolean[30];
	
	private void stateUpdate(int i, boolean state) {
		// 온라인인 경우
		if(state) {
			if(isOnline[i] == false) {
				isOnline[i] = true;
				//DB의 state도 온라인으로 만들기
				DBSetState(i+1, true);
				Online(i);
			}
		}
		// 오프라인인 경우
		else {
			if(isOnline[i] == true) {
				isOnline[i] = false;
				DBSetState(i+1, false);
				Offline(i);
			}
		}
	}
	
	private void DBSetState(int i, boolean state) {
		String sql = "UPDATE state SET statement=" + state +" WHERE pcNum=" + i;
		db.Update(sql);
	}

	private void Online(int i) {
		bt[i].setBackground(Color.green);
		bt[i].setEnabled(true);
		timerset[i].setText("주문 대기 중");
	}
	private void Offline(int i) {
		bt[i].setBackground(Color.gray);
		bt[i].setEnabled(false);
		timerset[i].setText("오프라인");
	}

	private class MyListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			Object obj = e.getSource();
			for (int i = 0; i < 30; i++) {
				if (obj == bt[i]) {
					bt[i].setBackground(Color.white);
					isOrder[i] = true;
					
					if(e.getClickCount() == 2) {	//더블클릭
						new Counter_Order(14, bt[i], menus, 4000, "카드");
					}
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
	}

	public static void main(String[] args) {
		new CounterMainFrameDB("카운터 화면", 1200, 900);
	}

}