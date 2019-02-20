package sudoku.solver;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.JOptionPane;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class MainWindow implements ActionListener, KeyListener {

	private JFrame frame;
	protected JTextField[][] textField = new JTextField[9][9];
	protected JPanel[] panels = new JPanel[9];
	private JMenuBar menuBar;
	private JMenu mnMenu;
	private JMenu mnPuzzle;
	private JMenuItem mntmOpen;
	private JMenuItem mntmSave;
	private JMenuItem mntmSaveAs;
	private JMenuItem mntmQuit;
	private JMenuItem mntmSolve;
	private JMenuItem mntmReset;
	private JMenuItem mntmTest;
	
	//Default parameters for JTextFields
	//private int horDist = 3;
	//private int verDist = 3;
	//private int areaTf = 79;
	
	//Default parameters for JPanels
	//private int hDist = 0;
	//private int vDist = 0;
	//private int areaPa = 246;
	
	public int[][] problem = new int[9][9]; //Stores integer values for each TextField to be worked on by solver function
	public static boolean solveActive = false; //Used for the getUserInput function to determine TextField colour
	public boolean invalidEntry = false; //Changes to true if any element in 'invalidEntries' is switched to true
	public boolean[][] invalidEntries = new boolean[9][9]; //Stores boolean state for each TextField 
	public boolean keyState = false;
	
	File save = new File("default_save.txt");
	JFileChooser fileChooser = new JFileChooser();
		
	@SuppressWarnings("serial")      //Enables the limiting of JTextField length to fixed number of characters
	public class JTextFieldLimit extends PlainDocument {
		private int limit;
		JTextFieldLimit(int limit) {
			super();
			this.limit = limit;
		}
		
		
	public void insertString(int offset, String str, AttributeSet attr)	throws BadLocationException {
			if(str == null)
				return;
			
			if((getLength() + str.length()) <= limit) {
				super.insertString(offset, str, attr);
			}
		}
	}

	
public static void main(String[] args) throws InterruptedException {
	EventQueue.invokeLater(new Runnable() {
		public void run() {
			try {
				MainWindow window = new MainWindow(); //Creates Sudoku window object
				window.frame.setVisible(true); 	
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	});
}

	
public MainWindow() { 						//Configuration for GUI
	
	frame = new JFrame();
	frame.setBounds(100, 100, 742, 788);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//frame.setLayout(new GridLayout(0,3));
	frame.getContentPane().setLayout(new GridLayout(0,3));
	//frame.getContentPane().setLayout(null);
	frame.setTitle("Sudoku Solver");
	frame.setResizable(false);
	
	//Menu Bar
	
	menuBar = new JMenuBar();
	UIManager.put("MenuBar.background", Color.WHITE);
	frame.setJMenuBar(menuBar);

	mnMenu = new JMenu("Menu");
	menuBar.add(mnMenu);
	
	mnPuzzle = new JMenu("Puzzle");
	menuBar.add(mnPuzzle);
	
	mntmOpen = new JMenuItem("Open...");
	mntmOpen.setHorizontalAlignment(SwingConstants.LEFT);
	mnMenu.add(mntmOpen);
	
	mntmSave = new JMenuItem("Save");
	mntmSave.setHorizontalAlignment(SwingConstants.LEFT);
	mnMenu.add(mntmSave);
	
	mntmSaveAs = new JMenuItem("Save As...");
	mntmSaveAs.setHorizontalAlignment(SwingConstants.LEFT);
	mnMenu.add(mntmSaveAs);
	
	mnMenu.addSeparator();
	
	mntmQuit = new JMenuItem("Quit");
	mntmQuit.setHorizontalAlignment(SwingConstants.LEFT);
	mnMenu.add(mntmQuit);
	
	mntmSolve = new JMenuItem("Solve");
	mntmSolve.setHorizontalAlignment(SwingConstants.LEFT);
	mnPuzzle.add(mntmSolve);
	
	mntmReset = new JMenuItem("Reset");
	mntmReset.setHorizontalAlignment(SwingConstants.LEFT);
	mnPuzzle.add(mntmReset);
	
	mnPuzzle.addSeparator();
	
	mntmTest = new JMenuItem("Test");
	mntmTest.setHorizontalAlignment(SwingConstants.LEFT);
	mnPuzzle.add(mntmTest);
	
	//Grid();   //NB this creates grid of JTextfields without JPanels and borders
	Panel(); //NB JFrame layout must also be switched to use this format
	


	mntmOpen.addActionListener(new ActionListener() { //Opens existing user input configuration from txt file		
		public void actionPerformed(ActionEvent e) {
			
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			int result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
			    File selectedFile = fileChooser.getSelectedFile();
			    System.out.println("Selected file: " + selectedFile.getAbsolutePath());
			    save = selectedFile;
			    try {
					Scanner input = new Scanner(selectedFile);
					for(int j=0;j<9;++j) {
						for(int i=0;i<9;++i) {
							problem[i][j] = input.nextInt();
						}
					}
					input.close();
					
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				}

			}
			showSolutionMatrix();
		}
		
	});
	
	mntmSave.addActionListener(new ActionListener() {	//Saves current user inputs to a txt file 	
		public void actionPerformed(ActionEvent e) {
	    
		try {
			PrintWriter output = new PrintWriter(save);
			getUserInput();
			for(int j=0;j<9;++j) {
				for(int i=0;i<9;++i) {
						output.print(problem[i][j] + " ");
					}
				output.println();
				}
		System.out.println("Save successful.");	
		output.close();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		}
	});
	

	mntmSaveAs.addActionListener(new ActionListener() {	//Allows user to create their own save file and choose location
		public void actionPerformed(ActionEvent e) {
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			int result = fileChooser.showSaveDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
			    File selectedFile = fileChooser.getSelectedFile();
			    System.out.println("Selected file: " + selectedFile.getAbsolutePath());
			    try {
			    	PrintWriter output = new PrintWriter(selectedFile);
					for(int j=0;j<9;++j) {
						for(int i=0;i<9;++i) {
							output.print(problem[i][j] + " ");
						}
						output.println();
					}
					System.out.println("Save successful.");
					output.close();
					
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				}

			}
		}
	});
	
	
	mntmQuit.addActionListener(new ActionListener() {	//Quits application	
		public void actionPerformed(ActionEvent e) {
			System.exit(0);			
		}
	});
	
	
	mntmReset.addActionListener(new ActionListener() {	//Resets All JTextfields
		public void actionPerformed(ActionEvent e) {
			solveActive = false;
			invalidEntry = false;	
			for(int j=0; j<9; ++j) {
				for(int i=0; i<9; ++i) {
					invalidEntries[i][j] = false; //New addition to try and counter existing bug
					textField[i][j].setText("");
					textField[i][j].setForeground(Color.BLACK);
				}
			}
		}
	});
	
	
	mntmTest.addActionListener(new ActionListener() {	//Test Button
		public void actionPerformed(ActionEvent e) {
			if(invalidEntry) {
				System.out.println("Invalid Entry is currently ACTIVE!");
			}
			
			else {
				System.out.println("Invalid Entry is currently INACTIVE.");
			}
			
			/*
			if(textField[0][0].getText().equals("")) {
				System.out.println("Selected text Field is empty.");
				JOptionPane.showMessageDialog(frame, "Selected textfield is empty.");
			}
			else {
				String s = textField[0][0].getText();
				System.out.println("The number in the selected text field is: " + s);
			}
			*/
		}
	});
	
	
	
	
	mntmSolve.addActionListener(new ActionListener() {		
		public void actionPerformed(ActionEvent e) {
			if(!keyState) {
				inputChecker(problem); 
				}
			if(!invalidEntry){
				solveActive = true;
				getUserInput();
				if (solve(0,0,problem)) { //Solves sudoku puzzle
	            showSolutionMatrix(); //Fills empty spaces in GUI 
	            System.out.println("SOLVED!");
				}
				else {
	            System.out.println("NO SOLUTION");
				}	
			 }
			 else {
				JOptionPane.showMessageDialog(frame, "Fix invalid inputs then try again.");	
				invalidEntry = false;
			 }
	   }
	});
	
	
	
  }


/*

	public void Grid() {
		
		for(int j=0; j<9; ++j) {    			//Create Jtextfield Sudoku grid [9][9]
			for(int i=0; i<9; ++i) {
			textField[i][j] = new JTextField("");	
			textField[i][j].setFont(new Font("Tahoma", Font.PLAIN, 40));
			textField[i][j].setHorizontalAlignment(SwingConstants.CENTER);
			textField[i][j].setDocument(new JTextFieldLimit(1));
			textField[i][j].addKeyListener(this);
				if(i==0) {
				horDist = 3;
				textField[i][j].setBounds(horDist, verDist, areaTf, areaTf);
				}
				else {
				horDist += 80;
				textField[i][j].setBounds(horDist, verDist, areaTf, areaTf);
				}
			frame.getContentPane().add(textField[i][j]);
			}
			verDist += 80;
		}
	
	}

*/

	public void Panel() {									//Create 9 JPanels
		for(int k=0; k<9; ++k) {
			panels[k] = new JPanel(new GridLayout(0,3));
			/*
			if(k==1) { // <-Selected JPanel
				panels[k].setBorder(new LineBorder(new Color(66, 244, 86), 2)); //Makes selected JPanel have green border
			}
			else {
				panels[k].setBorder(new LineBorder(new Color(0, 0, 0), 2));
			}
			*/
			panels[k].setBorder(new LineBorder(new Color(0, 0, 0), 2));
			frame.getContentPane().add(panels[k]);
		}
		
		for(int j=0; j<9; ++j) {    			
			for(int i=0; i<9; ++i) {
			textField[i][j] = new JTextField("");	
			textField[i][j].setFont(new Font("Tahoma", Font.PLAIN, 40));
			textField[i][j].setHorizontalAlignment(SwingConstants.CENTER);
			textField[i][j].setDocument(new JTextFieldLimit(1));
			textField[i][j].addKeyListener(this);
					if((i<3) && (j<3)) {
						panels[0].add(textField[i][j]);	
					}
					else if((i<3) && ((j>2) && (j<6))) {
						panels[3].add(textField[i][j]);	
					}
					else if((i<3) && ((j>5) && (j<9))) {
						panels[6].add(textField[i][j]);	
					}
					else if(((i>2) && (i<6)) && (j<3)) {
						panels[1].add(textField[i][j]);	
					}
					else if(((i>2) && (i<6)) && ((j>2) && (j<6))) {
						panels[4].add(textField[i][j]);	
					}
					else if(((i>2) && (i<6)) && ((j>5) && (j<9))) {
						panels[7].add(textField[i][j]);	
					}
					else if(((i>5) && (i<9)) && (j<3)) {
						panels[2].add(textField[i][j]);	
					}
					else if(((i>5) && (i<9)) && ((j>2) && (j<6))) {
						panels[5].add(textField[i][j]);	
					}
					else{
						panels[8].add(textField[i][j]);	
					}	
			}
		}
	}

	
	
	
	public void getUserInput() {    //Gets all user inputs and adds them to working array, changes TextField colour if Solve is selected
		for(int j=0; j<9; ++j) {
			for(int i=0; i<9; ++i) {
				if(textField[i][j].getText().isEmpty()) {
					problem[i][j] = 0; 
					if(solveActive) {
					textField[i][j].setForeground(Color.BLUE);
					}
				}
				else {
					problem[i][j] = Integer.parseInt(textField[i][j].getText());
					if(solveActive) {
					textField[i][j].setForeground(Color.BLACK);
					}
				}
			}
		}
	}


  public boolean solve(int i, int j, int[][] cells) {
	        if (i == 9) {
	            i = 0;
	            if (++j == 9)
	                return true;
	        }
	        if (cells[i][j] != 0)  //Skip filled cells
	            return solve(i+1,j,cells);
	
	        for (int val = 1; val <= 9; ++val) {
	            if (legal(i,j,val,cells)) {
	                cells[i][j] = val;
	                if (solve(i+1,j,cells))
	                    return true;
	            }
	        }
	        
	        cells[i][j] = 0; //Reset on backtrack
	        return false;
	    }
    
  public boolean legal(int i, int j, int val, int[][] cells) {
        for (int k = 0; k < 9; ++k)  //Row
            if (val == cells[k][j]) 
                return false;
            

        for (int k = 0; k < 9; ++k) //Column
            if (val == cells[i][k]) 
                return false;
            

        int boxRowOffset = (i / 3)*3;
        int boxColOffset = (j / 3)*3;
        for (int k = 0; k < 3; ++k) //Box
            for (int m = 0; m < 3; ++m)
                if (val == cells[boxRowOffset+k][boxColOffset+m]) {
                    return false;
                }
        return true; //No violations, so it's legal
    }	
  
  
  public boolean legalTest(int i, int j, int val, int[][] cells) {
	  
      for (int k = 0; k < 9; ++k)  //Row
          if ((val == cells[k][j]) && (k != i)) 
        	  return false;
           
          
	  
      for (int k = 0; k < 9; ++k) //Column
          if ((val == cells[i][k]) && (k != j)) 
              return false;
      
        
      int boxRowOffset = (i / 3)*3;
      int boxColOffset = (j / 3)*3;
      for (int k = 0; k < 3; ++k) //Box
          for (int m = 0; m < 3; ++m)
              if ((val == cells[boxRowOffset+k][boxColOffset+m]) && (((boxRowOffset+k) != i) && ((boxRowOffset+m) != j)) ) 
              {
                  return false;
              }
      
             return true;    
  }	
  
	public void showSolutionMatrix() { 		//Fills GUI with the elements of the integer array after converting them back to Strings
			for(int j=0; j<9; ++j) {
				for(int i=0; i<9; ++i) {
					if(problem[i][j] != 0) {
					textField[i][j].setText(Integer.toString(problem[i][j]));
					}
					else {
					textField[i][j].setText("");	
					}
			}
		}
			
	}
	
	public void inputChecker(int[][] cells) { //Checks that all of the current user inputs are legal entries and changes text colour
		invalidEntry = false;
		for(int j=0; j<9; ++j) {
			for(int i=0; i<9; ++i) {
				int val = cells[i][j];			
				if(val != 0) {
		            if (!legalTest(i,j,val,cells)) {
		                textField[i][j].setForeground(Color.RED); 
		                invalidEntries[i][j] = true;
		            }        
		            else {
		            	textField[i][j].setForeground(Color.BLACK);
		            	invalidEntries[i][j] = false;
		            }
		            
				}
		
			}
		}
		checkEntries();
  }
	
	public void checkEntries() {
		//System.out.println(); 
		for(int j=0; j<9; ++j) {
			for(int i=0; i<9; ++i) {
				/*
				if(!invalidEntries[i][j]) System.out.print("F" + " ");
				else System.out.print("T" + " ");
				*/
				if(invalidEntries[i][j]) {
					invalidEntry = true;
					
				}
			}
			//System.out.println();
		}
	}

	

	public void actionPerformed(ActionEvent e) {
	}
	
	
	
	public void keyPressed(KeyEvent e) {
	}

	
	
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_1 || e.getKeyCode() == KeyEvent.VK_2 || e.getKeyCode() == KeyEvent.VK_3 
		|| e.getKeyCode() == KeyEvent.VK_4 || e.getKeyCode() == KeyEvent.VK_5 || e.getKeyCode() == KeyEvent.VK_6 
		|| e.getKeyCode() == KeyEvent.VK_7 || e.getKeyCode() == KeyEvent.VK_8 || e.getKeyCode() == KeyEvent.VK_9 ) 
		{
			getUserInput();
			keyState = false;
			inputChecker(problem);
		}
		
		else if(e.getKeyCode() == KeyEvent.VK_NUMPAD1 || e.getKeyCode() == KeyEvent.VK_NUMPAD2 || e.getKeyCode() == KeyEvent.VK_NUMPAD3
			 || e.getKeyCode() == KeyEvent.VK_NUMPAD4 || e.getKeyCode() == KeyEvent.VK_NUMPAD5 || e.getKeyCode() == KeyEvent.VK_NUMPAD6
			 || e.getKeyCode() == KeyEvent.VK_NUMPAD7 || e.getKeyCode() == KeyEvent.VK_NUMPAD8 || e.getKeyCode() == KeyEvent.VK_NUMPAD9)
		 {
		
			getUserInput();
			keyState = false;
			inputChecker(problem);
		}
		
		else if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
			getUserInput();
			keyState = true;
			for(int j=0; j<9; ++j) {
				for(int i=0; i<9; ++i) {
			invalidEntries[i][j] = false;
			
				}
			}
			/*
			System.out.println(); //Prints grid containing current inputs to console
			for(int m=0;m<9;++m) {
				for(int n=0;n<9;++n) {
					System.out.print(problem[n][m] + " ");		
				}
				System.out.println();
			}
			*/
			inputChecker(problem);
	
		}
		
		/*
		System.out.println(); //Prints grid containing current inputs to console
		for(int m=0;m<9;++m) {
			for(int n=0;n<9;++n) {
				System.out.print(problem[n][m] + " ");		
			}
			System.out.println();
		}	
		*/
	}
	
	
	public void keyTyped(KeyEvent e) {
		/*char ch = e.getKeyChar();
		int num = Character.getNumericValue(ch);
		if((num>0) && (num<=9)) {
		System.out.println(num);
		getUserInput();
		System.out.println(problem[0][0]);
		}
		*/
	}
	
}


