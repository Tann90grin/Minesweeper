import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import java.util.concurrent.TimeUnit;

class MineButton extends JButton{
	public int locX, locY;
	public boolean isMine;
	public boolean isClicked;
	public boolean isFlagged;
	
	public MineButton(int x, int y)
	{
		locX = x;
		locY = y;
		isMine = false;
		isClicked = false;
		isFlagged = false;
		this.setSize(40, 40);
	}
}

public class mineSweeper extends JFrame{
	Container container = getContentPane();
	public final int ROW = 9;
	public final int COLUMN = 9;
	public final int MINE_NUM = 10;
	ImageIcon mineIcon = new ImageIcon("bomb.png");
	ImageIcon number0 = new ImageIcon("0.png");
	ImageIcon number1 = new ImageIcon("1.png");
	ImageIcon number2 = new ImageIcon("2.png");
	ImageIcon number3 = new ImageIcon("3.png");
	ImageIcon number4 = new ImageIcon("4.png");
	ImageIcon number5 = new ImageIcon("5.png");
	ImageIcon number6 = new ImageIcon("6.png");
	ImageIcon number7 = new ImageIcon("7.png");
	ImageIcon number8 = new ImageIcon("8.png");
	ImageIcon flagged = new ImageIcon("flagged.png");

	int flagnumber = 10;
	int minecount = 10;
	int delay;
	int gold = 0;
	int silver = 0;
	int bronze = 0;
	int currentID = 0;
	boolean wincondition = false;
	
	MineButton  mines[][] = new MineButton[ROW][COLUMN];
	
	Panel menuPanel = new Panel();
	Panel gamePanel;
	javax.swing.Timer timer;
	JTextField showTime, showMine;
	int totalLatency = 0;
	
	Database db;
	

	// A class to represent a row of database table. 
	class ScoreRow 
	{ 
	    String name;
	    int score;
	    String date; 
	    int id;
	  
	    // Constructor 
	    public ScoreRow(String name, int score, String date, int id) { 
	        this.name = name; 
	        this.score = score; 
	        this.date = date; 
	        this.id = id;
	    } 
	  
	    // Used to print student details in main() 
	    public String toString() 
	    { 
	        return this.name + " " + this.score + " " + this.date + " " + this.id; 
	    } 
	} 
	
	class Sortbyroll implements Comparator<ScoreRow> 
	{ 
	    String sortColumn; 
	    public void SetSortColumn(String sortColumn)
	    {
	    	this.sortColumn = sortColumn;
	    }
	    

	    // Used for sorting in ascending order of 
	    // roll number 
	    public int compare(ScoreRow a, ScoreRow b) 
	    { 
	    	if ( sortColumn.equals("name") )
	    			return a.name.compareTo(b.name);
	    	else if ( sortColumn.equals("score") )
	    			return a.score - b.score;
	    	else if ( sortColumn.equals("date") )
    			return a.date.compareTo(b.date);
	    	
	    	return 0;
	    } 
	} 
	  
	public class Database{
		public Connection con;
		
		public Connection getConnection() {
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				System.out.println("Loaded");
			}catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			try {
				con=DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydemo ","root","henry7086");
				con.setAutoCommit(true);
				System.out.println("Connected");
			}catch (SQLException e){
				e.printStackTrace();
			}
			return con;
		}
		public boolean InsertScore(String name, int score){
			
			System.out.println("Name: " + name);
			System.out.println("Score: " + score);
			
				try {
					PreparedStatement sql = con.prepareStatement("insert into scoreboard (NAME, SCORE) values(?,?)");
					sql.setString(1, name);
					sql.setInt(2, score);
					sql.executeUpdate();
					
					return true;
				}catch(SQLException e){
					e.printStackTrace();
					return false;
				}
			}
		

		public  ArrayList<ScoreRow> GetAllScores()
		{
			 ArrayList<ScoreRow> rows = new ArrayList<ScoreRow>();
			 ScoreRow oneRow;
			 int goldScore = 0;
			 int silverScore = 0;
			 int bronzeScore = 0;
			try {
				Statement sql = db.getConnection().createStatement();
				ResultSet res = sql.executeQuery("select * from scoreboard");
				while(res.next()) {
					String name = res.getString("NAME");
					int score = res.getInt("SCORE");
					String date = res.getString("TIME");
					int id = res.getInt("ID");
					System.out.println("Name: " + name);
					System.out.println("Score: " + score);
					oneRow = new ScoreRow(name, score, date, id);
					rows.add(oneRow);
					
					//Find gold, silver, and bronze
					if (gold == 0) {
						gold = oneRow.id;
					}
					else if (oneRow.score < goldScore) {
						goldScore = oneRow.score;
						gold = oneRow.id;
					}
					else if (silver == 0) {
						silver = oneRow.id;
					}
					else if (oneRow.score < silverScore) {
						silverScore = oneRow.score;
						silver = oneRow.id;
					}
					else if (bronze == 0)
					{
						bronze = oneRow.id;
					}
					else if (oneRow.score < bronzeScore) {
						bronzeScore = oneRow.score;
						bronze = oneRow.id;
					}
				}
			}catch(SQLException e){
				e.printStackTrace();
			}
			
			return rows;
		}

		
		public void close()
		{
			try {
				con.close();
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
	}
	
	
	class DBTableModel extends AbstractTableModel {
		public ArrayList<ScoreRow> rows;
		public String[] columnNames = {"Name", "Time Spent(s)", "Date"};
		
		public DBTableModel(ArrayList<ScoreRow> rows)
		{
			this.rows = rows;
		}
		
	    public int getColumnCount() {
	        return 3;
	    }

	    public int getRowCount() {
	        return rows.size();
	    }

	    public String getColumnName(int col) {
	        return columnNames[col];
	    }
	    
	    @Override
	    public Class<?> getColumnClass(int columnIndex){

	         if (columnIndex !=1) 
	                 return String.class;
	         else
	                 return Integer.class;

	   }
	   
	    public Object getValueAt(int row, int col) {
	    	
//	    	System.out.println("Get row=" + row + " col=" + col);
	    	currentID = rows.get(row).id;
	    	if (col == 0)
	    		return rows.get(row).name;
	    	else if (col == 1)
	    		return  new Integer(rows.get(row).score);
	    	else if (col == 2)
	    		return rows.get(row).date;
	    	else
	    		return "";
	    }
	}
	
	//A class represents a database table view
	public class DatabaseTableView extends JFrame {
	    JFrame frame = new JFrame();
        ArrayList<ScoreRow> rows;
	    

        TableCellRenderer cellRender =  new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column) {
                Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
                if ( currentID == gold )
                {
                	c.setForeground(Color.YELLOW);
                	c.setBackground(Color.GREEN);
                }
                else if (currentID == silver) {
                	c.setForeground(Color.LIGHT_GRAY);
                	c.setBackground(Color.BLUE);
                }
                else if (currentID == bronze) {
                	c.setForeground(Color.CYAN);
                	c.setBackground(Color.ORANGE);
                }
                else {
                	c.setForeground(Color.BLACK);
                	c.setBackground(Color.WHITE);
                }
                
//                System.out.println("render row=" + row + " column=" + column + " value=" + value + " id=" + currentID);
                return c;
            }
        };
        
	    public DatabaseTableView()
	    {
	        super("Mine Score Board");
	        setBounds(100, 100, 500, 500);
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        Vector<String> columnNameV = new Vector<>();
	        columnNameV.add("Name");
	        columnNameV.add("Time Spent");
	        columnNameV.add("Date");
	
	        rows = db.GetAllScores();
	        
	        /*
	        Vector<Vector<String>> tableValueV = new Vector<>();
	        Iterator<ScoreRow> lt = rows.iterator();
	        while (lt.hasNext()) {
	        	ScoreRow one = lt.next();
	        	Vector v = new Vector();
	        	v.add(one.name);
	        	v.add("" + one.score);
	        	v.add(one.date);
	        	tableValueV.add(v);
	        }
	        JTable table = new JTable(tableValueV, columnNameV);
	        */
	        TableModel tm = new DBTableModel(rows);
	        JTable table = new JTable(tm);
	        table.setRowSorter(new TableRowSorter(tm));
	        getContentPane().add(table, BorderLayout.CENTER);
	        JTableHeader tableHeader = table.getTableHeader();
	        getContentPane().add(tableHeader, BorderLayout.NORTH);
	        
	        table.setDefaultRenderer(String.class, cellRender);
	        table.setDefaultRenderer(Integer.class, cellRender);
	        
	        this.setVisible(true);
			this.revalidate();
			this.repaint();
	    }

	}
	
	ActionListener timerListener = new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			totalLatency  += timer.getDelay();
			showTime.setText("Time spent: "+ totalLatency/1000 + " seconds");			
		}
	};
	
	public boolean IsAMine(int x, int y)
	{
		if (x < 0 || x > 8 || y < 0 || y > 8)
			return false;
		else
			return mines[x][y].isMine;
	}
	
	
	public void ShowResult(boolean isWin)
	{
		if (isWin) {
			timer.stop();
			JOptionPane d = new JOptionPane();
			db.con = db.getConnection();
			String name = d.showInputDialog(null, "You Win! In " + totalLatency/1000 + " seconds!\n" + "Save record as your name:");
			if(name != null && !name.equals(""))
				db.InsertScore(name, totalLatency/1000);
			gamePanel.setEnabled(false);
		}
		else {
			JOptionPane d = new JOptionPane();
			d.showMessageDialog(null, "You Failed! Press restart to start over.");
			for(int i = 0; i<ROW; i++) {
				for(int j = 0; j<COLUMN; j++) {
					MineButton me = mines[i][j];
					timer.stop();
					if (me.isMine) {
						me.setIcon(mineIcon);
					}
				}
			}
			gamePanel.setEnabled(false);
			
		}
	}
	
	public boolean IsWin()
	{
		for(int i = 0; i<ROW; i++) {
			for(int j = 0; j<COLUMN; j++) {
				MineButton me = mines[i][j];
				if( (me.isMine && !me.isFlagged) || (!me.isMine && me.isFlagged)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public int CountMine(MineButton me)
	{
		int count = 0;
		int x = me.locX;
		int y = me.locY;
		
		if (IsAMine(x-1, y-1)) 
			count ++;
		
		if (IsAMine(x-1, y)) 
			count ++;

		if (IsAMine(x-1, y+1)) 
			count ++;
		
		if (IsAMine(x, y-1)) 
			count ++;

		if (IsAMine(x, y+1)) 
			count ++;
		
		if (IsAMine(x+1, y-1)) 
			count ++;
		
		if (IsAMine(x+1, y)) 
			count ++;
		
		if (IsAMine(x+1, y+1)) 
			count ++;
		
		return count;
	}
	
	public boolean isEmpty(int x, int y) {
		if (x < 0 || x > 8 || y < 0 || y > 8)
			return false;
		else
			return CountMine(mines[x][y]) == 0 && !mines[x][y].isClicked;
	}
	
	public void setCellButton(MineButton me) {
		int numMine = CountMine(me);
		if (numMine == 0) {
			me.setIcon(number0);
			me.isClicked = true;
			checkEmpty(me);
		}
		if (numMine == 1) {
			me.setIcon(number1);
		}
		if (numMine == 2) {
			me.setIcon(number2);
		}
		if (numMine == 3) {
			me.setIcon(number3);
		}
		if (numMine == 4) {
			me.setIcon(number4);
		}
		if (numMine == 5) {
			me.setIcon(number5);
		}
		if (numMine == 6) {
			me.setIcon(number6);
		}
		if (numMine == 7) {
			me.setIcon(number7);
		}
		if (numMine == 8) {
			me.setIcon(number8);
		}
	
	}
	
	public void showCell(int x, int y) {
		
		if (x < 0 || x > 8 || y < 0 || y > 8)
			return ;
		else
		{
			MineButton me = mines[x][y];
			if (!me.isClicked)
				setCellButton(me);
		}
	}

	public void checkEmpty(MineButton me) {
		int x = me.locX;
		int y = me.locY;
		MineButton newme;
		System.out.println("Checkempty x: " + x+ "y: " + y);
		if (isEmpty(x-1, y-1)) {
			newme = mines[x-1][y-1];
			newme.setIcon(number0);
			newme.isClicked = true;
			checkEmpty(newme);
		}
		else {
			showCell(x-1, y-1);
		}

		if (isEmpty(x-1, y)) {
			newme = mines[x-1][y];
			newme.setIcon(number0);
			newme.isClicked = true;
			checkEmpty(newme);
		}
		else {
			showCell(x-1, y);
		}
		
		if (isEmpty(x-1, y+1)) {
			newme = mines[x-1][y+1];
			newme.setIcon(number0);
			newme.isClicked = true;
			checkEmpty(newme);
		}
		else {
			showCell(x-1, y+1);
		}
		
		if (isEmpty(x, y-1)) {
			newme = mines[x][y-1];
			newme.setIcon(number0);
			newme.isClicked = true;
			checkEmpty(newme);
		}
		else {
			showCell(x, y-1);
		}
		
		if (isEmpty(x, y+1)) {
			newme = mines[x][y+1];
			newme.setIcon(number0);
			newme.isClicked = true;
			checkEmpty(newme);
		}
		else {
			showCell(x, y+1);
		}
		
		if (isEmpty(x+1, y-1)) {
			newme = mines[x+1][y-1];
			newme.setIcon(number0);
			newme.isClicked = true;
			checkEmpty(newme);
		}
		else {
			showCell(x+1, y-1);
		}
		
		if (isEmpty(x+1, y)) {
			newme = mines[x+1][y];
			newme.setIcon(number0);
			newme.isClicked = true;
			checkEmpty(newme);
		}
		else {
			showCell(x+1, y);
		}
		
		if (isEmpty(x+1, y+1)) {
			newme = mines[x+1][y+1];
			newme.setIcon(number0);
			newme.isClicked = true;
			checkEmpty(newme);
		}
		else {
			showCell(x+1, y+1);
		}

	}
	
	ActionListener mineListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			MineButton me = (MineButton) e.getSource();
			System.out.println("Click "+ me.locX + " "+ me.locY);
			me.isClicked = true;
			if (me.isMine) {
				timer.stop();
				me.setIcon(mineIcon);
				gamePanel.setEnabled(false);
				JOptionPane d = new JOptionPane();
				d.showMessageDialog(null, "You Failed! Press restart to start over.");
			}
			else {
				setCellButton(me);
			}
		}
	};
	
	MouseListener flagListener = new MouseListener() {
		public void mouseClicked(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON3) {
				Component ce = e.getComponent();
				if (ce instanceof MineButton) {
					MineButton me = (MineButton) ce;
					if(!me.isClicked && !me.isFlagged) {
						me.setIcon(flagged);
						if ( minecount != 0 )
						{
							minecount --;
							showMine.setText("" + minecount);
							me.isFlagged = true;
						}
						if( minecount == 0) {
							JOptionPane d = new JOptionPane();
							int input = d.showConfirmDialog(ce, "Do you want to check your results?", "Select an Option...", JOptionPane.YES_NO_OPTION);
							if (input == 0)
							{
								ShowResult(IsWin());
							}
							
						}
					
					}
					else if(me.isFlagged){
						minecount ++;
						showMine.setText("" + minecount);
						me.isFlagged = false;
						me.setIcon(null);
					}
					}
				}
					
			}

		public void mouseReleased(MouseEvent e) {
			
		}
		
		public void mouseExited(MouseEvent e) {
			
		}
		
		public void mouseEntered(MouseEvent e) {
			
		}
		
		public void mousePressed(MouseEvent e) {
			
		}

	};
	
	public void CreateGamePanel() {
		gamePanel = new Panel();
		gamePanel.setLayout(new GridLayout(ROW, COLUMN));
		
		for (int i = 0; i < ROW; i++) {
			for (int j = 0; j < COLUMN; j++) {
				mines[i][j] = new MineButton(i, j);
				mines[i][j].addActionListener(mineListener);
				mines[i][j].addMouseListener(flagListener);
				gamePanel.add(mines[i][j]);
			}
		}
		
		int count = MINE_NUM;
		int x, y;
		
		while (count > 0)
		{
			x = (int) ( Math.random() * (ROW - 1));
			y = (int) ( Math.random() * (COLUMN - 1));
			
			if (!mines[x][y].isMine) {
				mines[x][y].isMine = true;
				count --;
				System.out.println("one mine [" + x + "," + y + "]" ); //
			}
				
		}
		container.add(gamePanel, "Center");
	}
	
	public mineSweeper() {

		db = new Database();
		this.setSize(960, 720);
		this.setVisible(true);
		this.setResizable(true);
		
		container.setLayout(new BorderLayout());

		JButton start = new JButton("Start");
		start.setBorderPainted(false);
		start.setOpaque(true);
		start.setMaximumSize(new Dimension(100,100));
		start.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				timer.setDelay(1000);
				timer.start();
				totalLatency = 0;
				start.setEnabled(false);
				CreateGamePanel();
				revalidate();
				repaint();
				showMine.setText("" + MINE_NUM);
			}
		});
		menuPanel.add(start);
		JButton reset = new JButton("Reset");
		reset.setOpaque(true);
		reset.setBorderPainted(false);
		reset.setMaximumSize(new Dimension(100,100));
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timer.stop();
				totalLatency = 0;
				showTime.setText("Time spent: 0 seconds");
				minecount = 10;
				flagnumber = 10;
				container.remove(gamePanel);
				start.setEnabled(true);
				revalidate();
				repaint();
			}
		});
		menuPanel.add(reset);
		
		JButton checkscore = new JButton("Check Scores");
		checkscore.setOpaque(true);
		checkscore.setBorderPainted(false);
		checkscore.setMaximumSize(new Dimension(100,100));
		checkscore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new DatabaseTableView();
			}
		});
		menuPanel.add(checkscore);
		
		JLabel j = new JLabel("Number of Mines:");
		menuPanel.add(j);
		
		showMine=new JTextField("" + minecount, 3);
		menuPanel.add(showMine);
		
		container.add(menuPanel, "North");

		timer = new javax.swing.Timer(1000, timerListener);
		showTime = new JTextField("Time spent: 0 seconds", 15);
		menuPanel.add(showTime);
		revalidate();
		repaint();

	}
	public static void main(String[]args){
		new mineSweeper();
	}
}