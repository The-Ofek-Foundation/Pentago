/*	Ofek Gila
	July 11th, 2014
	Pentago.java
	This program lets the user play the well known game, Pentago
*/

import java.awt.*;			// Imports
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Scanner;
import java.net.MalformedURLException;
import javax.sound.sampled.*;
import java.io.*;			// classes File, IOException
import javax.imageio.*;	// class ImageIO
import java.awt.image.*;
import java.net.URL;

public class Pentago extends JApplet	{
	public JFrame frame;

	public static void main(String[] pumpkins) {
		Pentago GUIT = new Pentago();
		GUIT.run();
	}
	public void run(){
		frame = new JFrame("Pentago");	// ask why I extend JApplet or implement all of those things ^_^
		frame.setContentPane(new PentagoPanel());
		frame.setSize(820+250, 700+250);		// Sets size of frame
		frame.setResizable(true);						// Makes it so you can't resize the frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
class PentagoPanel extends JPanel	{

	ArrayKit AK = new ArrayKit();

	JPanel sidePanel;
	JPanel mainPanel;

	JButton undo;
	JButton redo;
	JButton next;
	JButton newgame;
	JButton watchgame;
	JToggleButton vanisharrows;
	JButton[] Twist = new JButton[8];

	Rectangle[][] board = new Rectangle[6][6];
	char[][]	boardValues = new char[6][6];

	int twist;
	byte stage;
	char turn;
	int lastTwistHold, lastTwist;
	int sum;
	char victor;

	int logOn;
	int lastLog;
	char[][][] 	boardLog = new char[1000][7][6];

	boolean initial = true;

	Timer WatchGameTimer = new Timer(300, new WatchGame());

	PentagoPanel()	{
		setLayout(new BorderLayout());
		sidePanel = new SidePanel();
		mainPanel = new MainPanel();
		add(sidePanel, BorderLayout.EAST);
		add(mainPanel, BorderLayout.CENTER);
	}
	class SidePanel extends JPanel implements ActionListener	{
		SidePanel()	{
			setPreferredSize(new Dimension(120, 1000));
			createUndo();
			createRedo();
			createNext();
			createNewGame();
			createReplayGame();
			createVanishArrows();
		}
		void createUndo()	{
			undo = new JButton("Undo");
			undo.setPreferredSize(new Dimension(100, 60));
			undo.addActionListener(this);
			add(undo);
		}
		void createRedo()	{
			redo = new JButton("Redo");
			redo.setPreferredSize(new Dimension(100, 60));
			redo.addActionListener(this);
			add(redo);
		}
		void createNext()	{
			next = new JButton("Next");
			next.setPreferredSize(new Dimension(100, 60));
			next.addActionListener(this);
			add(next);
		}
		void createNewGame()	{
			newgame = new JButton("New Game");
			newgame.setPreferredSize(new Dimension(100, 60));
			newgame.addActionListener(this);
			add(newgame);
		}
		void createReplayGame()	{
			watchgame = new JButton("Replay");
			watchgame.setPreferredSize(new Dimension(100, 60));
			watchgame.addActionListener(this);
			add(watchgame);
		}
		void createVanishArrows()	{
			vanisharrows = new JToggleButton("Vanish Arrows");
			vanisharrows.setFont(new Font("Arial", Font.PLAIN, 10));
			vanisharrows.setPreferredSize(new Dimension(100, 60));
			vanisharrows.addActionListener(this);
			add(vanisharrows);
		}
		public void actionPerformed(ActionEvent evt)	{
			Object source = evt.getSource();
			if (source == next)	{
				lastTwist = lastTwistHold;
				lastTwistHold = -1;
				checkVictory();
				stage=1;
				next.setEnabled(false);
				twist = 0;
				for (int i = 0; i < Twist.length; i++)
					if (!vanisharrows.isSelected()) Twist[i].setEnabled(false);
					else removeArrow(i);
				changeTurn();
				saveBoardLog();
			}
			if (source == undo)	{
				undo();
			}
			if (source == redo)	{
				redo();
			}
			if (source == newgame)	{
				initial = true;
				boardLog = new char[1000][7][6];
				Repaint();
			}
			if (source == vanisharrows)	{
				if (vanisharrows.isSelected())	{
					for (int i = 0; i < Twist.length; i++)
						if (!Twist[i].isEnabled())
							removeArrow(i);
				}
				else {
					for (int i = 0; i < Twist.length; i++)
						addArrow(i);
				}
			}
			if (source == watchgame)	{
				lastLog = logOn;
				undo.setEnabled(false);
				redo.setEnabled(false);
				next.setEnabled(false);
				newgame.setEnabled(false);
				logOn = -1;
				for (int i = 0; i < Twist.length; i++)
					removeArrow(i);
				victor = 'R';
				WatchGameTimer.start();
			}
		}
	}
	void Repaint()	{
		repaint();
	}
	int width, height;
	int spare;
	int boxwidth;
	class MainPanel extends JPanel implements MouseListener, KeyListener 	{
		Graphics g;
		BufferedImage arrow;
		boolean veryInitial = true;
		MainPanel()	{
			setLayout(null);
			setBackground(Color.red);
			addMouseListener(this);
			addKeyListener(this);
		}
		public void paintComponent(Graphics g)	{
			requestFocus();
			super.paintComponent(g);
			this.g = g;
			if (getWidth() != width || getHeight() != height)	{
				width = getWidth();
				height = getHeight();
				calculateWidth();
				if (!veryInitial) {
					setRectangleBounds();
					setArrowBounds();
				}
				repaint();
			}
			if (veryInitial)	{
				veryInitial = false;
				createBoardRectangles();
				createTwists();
			}
			if (initial)	{
				initial = false;
				logOn = -1;
				for (int i = 0; i < board.length; i++)
					for (int a = 0; a < board.length; a++)
						boardValues[i][a] = 'R';
				turn = 'B';
				victor = 'R';
				stage = 1;
				lastTwist = lastTwistHold = -1;
				next.setEnabled(false);
				undo.setEnabled(false);
				redo.setEnabled(false);
				newgame.setEnabled(false);
				watchgame.setEnabled(false);
				//newgame.setEnabled(false);
				for (int i = 0; i < Twist.length; i++)	{
					if (vanisharrows.isSelected()) removeArrow(i);
					Twist[i].setEnabled(false);
				}
				saveBoardLog();
			}
			drawBoard();
		}
		void createTwists()	{
			URL url = this.getClass().getResource("Arrow.png");
			try	{
				arrow = ImageIO.read(url);
			}	catch (IOException e){}
			createDownArrows();
			createLeftArrows();
			createUpArrows();
			createRightArrows();
			setArrowBounds();
		}
		void setArrowBounds()	{
			Twist[0].setBounds(9*boxwidth - 150 + spare/2, spare/2 - 50, 150, 45);
			Twist[1].setBounds(9*boxwidth+spare/2 + 5, spare/2, 45, 150);
			Twist[2].setBounds(9*boxwidth + spare/2 + 5, 9*boxwidth + spare/2 - 150, 45, 150);
			Twist[3].setBounds(9*boxwidth - 150 + spare/2, 9*boxwidth + 5 + spare/2, 150, 45);
			Twist[4].setBounds(spare/2, 9*boxwidth + spare/2 + 5, 150, 45);
			Twist[5].setBounds(spare/2 - 50, 9*boxwidth + spare/2 - 150, 45, 150);
			Twist[6].setBounds(spare/2 - 50, spare/2, 45, 150);
			Twist[7].setBounds(spare/2, spare/2 - 50, 150, 45);
		}
		void createDownArrows()	{
			Twist[1] = new JButton(new ImageIcon(arrow));							// down arrows
			Twist[1].addActionListener(new TwistListener());
			Twist[6] = new JButton(new ImageIcon(arrow));
			Twist[6].addActionListener(new TwistListener());
			add(Twist[1]);
			add(Twist[6]);
		}
		void createLeftArrows()	{
			arrow = AK.rotateClockWise(arrow);
			Twist[0] = new JButton(new ImageIcon(arrow));
			Twist[0].addActionListener(new TwistListener());
			Twist[3] = new JButton(new ImageIcon(arrow));
			Twist[3].addActionListener(new TwistListener());
			add(Twist[0]);
			add(Twist[3]);
		}
		void createUpArrows()	{
			arrow = AK.rotateClockWise(arrow);
			Twist[2] = new JButton(new ImageIcon(arrow));
			Twist[2].addActionListener(new TwistListener());
			Twist[5] = new JButton(new ImageIcon(arrow));
			Twist[5].addActionListener(new TwistListener());
			add(Twist[2]);
			add(Twist[5]);
		}
		void createRightArrows()	{
			arrow = AK.rotateClockWise(arrow);
			Twist[4] = new JButton(new ImageIcon(arrow));
			Twist[4].addActionListener(new TwistListener());
			Twist[7] = new JButton(new ImageIcon(arrow));
			Twist[7].addActionListener(new TwistListener());
			add(Twist[4]);
			add(Twist[7]);
		}
		void createBoardRectangles()	{
			for (int i = 0; i < board.length; i++)
				for (int a = 0; a < board.length; a++)	{
					double x, y;
					x = i * boxwidth * 1.5;
					y = a * boxwidth * 1.5;
					if (i > 2) x += boxwidth / 2;
					if (a > 2) y += boxwidth / 2;
					x+=spare/2;
					y+=spare/2;
					int newX = (int)(x + 0.5);
					int newY = (int)(y + 0.5);
					board[i][a] = new Rectangle(newX, newY, boxwidth, boxwidth);
				}
		}
		void setRectangleBounds()	{
			for (int i = 0; i < board.length; i++)
				for (int a = 0; a < board.length; a++)	{
					double x, y;
					x = i * boxwidth * 1.5;
					y = a * boxwidth * 1.5;
					if (i > 2) x += boxwidth / 2;
					if (a > 2) y += boxwidth / 2;
					x+=spare/2;
					y+=spare/2;
					int newX = (int)(x + 0.5);
					int newY = (int)(y + 0.5);
					board[i][a].setBounds(newX, newY, boxwidth, boxwidth);
				}
		}
		void calculateWidth()	{
			int x;
			if (width < height) x = width - 20;
			else				x = height - 20;
			boxwidth = x / 11;
			if (width < height) spare = width - boxwidth * 9;
			else				spare = height - boxwidth * 9;
		}
		void drawBoard()	{
			for (int i = 0; i < board.length; i++)
				for (int a = 0; a < board.length; a++)	{
					char color = boardValues[i][a];
					if (color == 'B')	g.setColor(Color.black);
					if (color == 'W')	g.setColor(Color.white);
					if (color == 'R')	g.setColor(Color.red);
					g.fillRect((int)board[i][a].getX(), (int)board[i][a].getY(), (int)board[i][a].getWidth(), (int)board[i][a].getHeight());
					g.setColor(Color.black);
					g.drawRect((int)board[i][a].getX(), (int)board[i][a].getY(), (int)board[i][a].getWidth(), (int)board[i][a].getHeight());
				}
			if (turn == 'B')	g.setColor(Color.black);
			if (turn == 'W')	g.setColor(Color.white);
			g.fillRect(boxwidth * 4 + spare/2, spare/4 - boxwidth/2, boxwidth, boxwidth);
			g.setColor(Color.black);
			g.drawRect(boxwidth * 4 + spare/2, spare/4 - boxwidth/2, boxwidth, boxwidth);
			if (victor != 'R')	{
				if (victor == 'B')	g.setColor(Color.black);
				if (victor == 'W')	g.setColor(Color.white);
				g.fillRect(boxwidth*4 + spare/2, spare/4 - boxwidth/2, boxwidth, boxwidth*11);
				g.fillRect(spare/4 - boxwidth/2, boxwidth*4 + spare/2, boxwidth*11, boxwidth);
				stage = 3;
				next.setEnabled(false);
				newgame.setEnabled(true);
				watchgame.setEnabled(true);
			}
		}
		public void mouseDragged(MouseEvent evt)	{	}
		public void mouseMoved(MouseEvent evt)	{	}
		public void mouseEntered(MouseEvent evt) {	} 
		public void mousePressed(MouseEvent evt) {	
			int x = evt.getX();
			int y = evt.getY();
			if (stage == 1 && charHere(x, y) == 'R')	{
				placeCharHere(x, y);
				stage=2;
				for (int i = 0; i < Twist.length; i++)
					if (i != lastTwist)	{
						if (vanisharrows.isSelected()) addArrow(i);
						Twist[i].setEnabled(true);
					}
				next.setEnabled(true);
				checkVictory();
				saveBoardLog();
			}
			repaint();
		}
		char charHere(int x, int y)	{
			for (int i = 0; i < board.length; i++)
				for (int a = 0; a < board.length; a++)
					if (board[i][a].contains(x, y))
						return boardValues[i][a];
			return 0;
		}
		void placeCharHere(int x, int y)	{
			for (int i = 0; i < board.length; i++)
				for (int a = 0; a < board.length; a++)
					if (board[i][a].contains(x, y))
						boardValues[i][a] = turn;
		}
    	public void mouseExited(MouseEvent evt) {	} 
    	public void mouseReleased(MouseEvent evt) {  } 
    	public void mouseClicked(MouseEvent evt) { }
    	public void keyPressed(KeyEvent evt)	{	
    		if (evt.getKeyCode() == KeyEvent.VK_ENTER && next.isEnabled())	{
    			lastTwist = lastTwistHold;
				lastTwistHold = -1;
				checkVictory();
				stage=1;
				next.setEnabled(false);
				twist = 0;
				for (int i = 0; i < Twist.length; i++)	{
					if (vanisharrows.isSelected()) removeArrow(i);
					Twist[i].setEnabled(false);
				}
				changeTurn();
				saveBoardLog();
    		}
   		}
   		public void keyReleased(KeyEvent evt)	{	}
    	public void keyTyped(KeyEvent evt)	{	}
   	}
   	void addArrow(int i)	{
    	switch (i)	{
			case 0:	Twist[0].setBounds(9*boxwidth - 150 + spare/2, spare/2 - 50, 150, 45);
			case 1:	Twist[1].setBounds(9*boxwidth+spare/2 + 5, spare/2, 45, 150);
			case 2:	Twist[2].setBounds(9*boxwidth + spare/2 + 5, 9*boxwidth + spare/2 - 150, 45, 150);
			case 3:	Twist[3].setBounds(9*boxwidth - 150 + spare/2, 9*boxwidth + 5 + spare/2, 150, 45);
			case 4:	Twist[4].setBounds(spare/2, 9*boxwidth + spare/2 + 5, 150, 45);
			case 5:	Twist[5].setBounds(spare/2 - 50, 9*boxwidth + spare/2 - 150, 45, 150);
			case 6:	Twist[6].setBounds(spare/2 - 50, spare/2, 45, 150);
			case 7:	Twist[7].setBounds(spare/2, spare/2 - 50, 150, 45);
		}
	}
	void removeArrow(int i)	{
		Twist[i].setBounds(0, 0, 0, 0);
	}
	class TwistListener implements ActionListener {
		public void actionPerformed(ActionEvent evt)	{
			Object source = evt.getSource();
			if (stage != 2)	return;
			for (int i = 0; i < Twist.length; i++)
				if (source == Twist[i])
					twistSquare(i);

		}
	}
	void twistSquare(int twist)	{
		int clockwise;
		int square = twist / 2;
		if (twist % 2 == 0) {	clockwise = 3;	if (this.twist == -1) return; this.twist--;	}
		else 				{	clockwise = 1;	if (this.twist ==  1) return; this.twist++;	}
		char[][] twisted = new char[3][3];
		switch (square)	{
			case 0: twisted = AK.subArray(boardValues, 3, 6, 0, 3); break;
			case 1: twisted = AK.subArray(boardValues, 3, 6, 3, 6);	break;
			case 2: twisted = AK.subArray(boardValues, 0, 3, 3, 6); break;
			case 3: twisted = AK.subArray(boardValues, 0, 3, 0, 3); break;
		}
		for (int i = 0; i < clockwise; i++)
			twisted = AK.rotateClockWise(twisted);
		switch(square)	{
			case 0:	boardValues = AK.mergeArray(boardValues, twisted, 3, 0); break;
			case 1: boardValues = AK.mergeArray(boardValues, twisted, 3, 3); break;
			case 2: boardValues = AK.mergeArray(boardValues, twisted, 0, 3); break;
			case 3: boardValues = AK.mergeArray(boardValues, twisted, 0, 0); break;
		}
		//System.out.println((lastTwistHold+1)%2+square*2);
		lastTwistHold = (twist + 1) % 2 + square * 2;
		for (int i = 0; i < Twist.length; i++)
			if (i / 2 != square || i == twist)	{
				if (vanisharrows.isSelected())	removeArrow(i);
				Twist[i].setEnabled(false);
			}
		if (this.twist == 0)
			for (int i = 0; i < Twist.length; i++)
				if (i != lastTwist)	{
					if (vanisharrows.isSelected()) addArrow(i);
					Twist[i].setEnabled(true);
				}
		saveBoardLog(square);
		repaint();
	}
	void checkVictory()	{
		boolean victory = false;
		for (int i = 0; i < board.length; i++)	{	//checks for vertical victories
			sum = 0;
			for (int a = 0; a < board[i].length; a++)	{
				if (boardValues[i][a] == 'R' && Math.abs(sum) > 0) {
					sum = 0;
				}
				if (boardValues[i][a] == 'B') {
					if (sum >= 0) sum++;
					else sum = 1;
				}
				if (boardValues[i][a] == 'W')	{
					if (sum <= 0) sum--;
					else sum = -1;
				}
				if (Math.abs(sum) >= 5)	{
					victory = true;
					break;
				}
			}
			if (victory) break;
		}
		if (!victory)
		for (int a = 0; a < board[0].length; a++)	{	//checks for horizontal victories
			sum = 0;
			for (int i = 0; i < board.length; i++)	{
				if (boardValues[i][a] == 'R' && Math.abs(sum) > 0) {
					sum = 0;
				}
				if (boardValues[i][a] == 'B') {
					if (sum >= 0) sum++;
					else sum = 1;
				}
				if (boardValues[i][a] == 'W')	{
					if (sum <= 0) sum--;
					else sum = -1;
				}
				if (Math.abs(sum) >= 5)	{
					victory = true;
					break;
				}
			}
			if (victory) break;
		}
		if (!victory)	{	//checks for diagonal wins
			if (Math.abs(diagonal1Victory(4, 0)) >= 5 || Math.abs(diagonal1Victory(5, 0)) >= 5 || Math.abs(diagonal1Victory(5, 1)) >= 5)	{
				victory = true;
			}
			else if (Math.abs(diagonal2Victory(0, 1)) >= 5 || Math.abs(diagonal2Victory(0, 0)) >= 5 || Math.abs(diagonal2Victory(1, 0)) >= 5)	{
				victory = true;
			}
		}
		if (victory) {
			if (sum < 0) victor = 'W';
			else         victor = 'B';
		}
	}
	int diagonal1Victory(int i, int a)	{
		sum = 0;
		while (i >= 0 && a <= 5)	{
			if (boardValues[i][a] == 'R' && Math.abs(sum) > 0) {
				sum = 0;
			}
			if (boardValues[i][a] == 'B') {
				if (sum >= 0) sum++;
				else sum = 1;
			}
			if (boardValues[i][a] == 'W')	{
				if (sum <= 0) sum--;
				else sum = -1;
			}
			if (Math.abs(sum) >= 5)	{
				return sum;
			}
			i-=1;
			a+=1;
		}
		return sum;
	}
	int diagonal2Victory(int i, int a)	{
		sum = 0;
		while (i <= 5 && a <= 5)	{
			if (boardValues[i][a] == 'R' && Math.abs(sum) > 0) {
				sum = 0;
			}
			if (boardValues[i][a] == 'B') {
				if (sum >= 0) sum++;
				else sum = 1;
			}
			if (boardValues[i][a] == 'W')	{
				if (sum <= 0) sum--;
				else sum = -1;
			}
			if (Math.abs(sum) >= 5)	{
				return sum;
			}
			i+=1;
			a+=1;
		}
		//System.out.println(sum);
		return sum;
	}
	void changeTurn()	{
		if (turn == 'B') turn = 'W';
		else turn = 'B';
		repaint();
	}
	void saveBoardLog()	{
		logOn++;
		for (int i = 0; i < boardValues.length; i++)
			for (int a = 0; a < boardValues[i].length; a++)
				boardLog[logOn][i][a] = boardValues[i][a];
		boardLog[logOn][6][0] = turn;
		boardLog[logOn][6][1] = (char)stage;
		boardLog[logOn][6][2] = (char)twist;
		boardLog[logOn][6][4] = (char)lastTwist;
		redo.setEnabled(false);
		if (logOn > 0) {
			undo.setEnabled(true);
			watchgame.setEnabled(true);
			newgame.setEnabled(true);
		}
	}
	void saveBoardLog(int square)	{
		logOn++;
		for (int i = 0; i < boardValues.length; i++)
			for (int a = 0; a < boardValues[i].length; a++)
				boardLog[logOn][i][a] = boardValues[i][a];
		boardLog[logOn][6][0] = turn;
		boardLog[logOn][6][1] = (char)stage;
		boardLog[logOn][6][2] = (char)twist;
		boardLog[logOn][6][3] = (char)square;
		boardLog[logOn][6][4] = (char)lastTwist;
		redo.setEnabled(false);
		if (logOn > 0) {
			undo.setEnabled(true);
			watchgame.setEnabled(true);
			newgame.setEnabled(true);
		}
	}
	void undo()	{
		//watchgame.setEnabled(false);
		logOn--;
		for (int i = 0; i < boardValues.length; i++)
			for (int a = 0; a < boardValues[i].length; a++)
				boardValues[i][a] = boardLog[logOn][i][a];
		turn = boardLog[logOn][6][0];
		stage = (byte)boardLog[logOn][6][1];
		twist = (int)boardLog[logOn][6][2];
		lastTwist = (int)boardLog[logOn][6][4];
		if (stage == 2)	{
			next.setEnabled(true);
			if (twist == 0)
				for (int i = 0; i < Twist.length; i++)
					if (i != lastTwist)	{
						if (vanisharrows.isSelected()) addArrow(i);
						Twist[i].setEnabled(true);
					}
			if (twist != 0)
				for (int i = 0; i < Twist.length; i++)
					if (i / 2 != (int)boardLog[logOn][6][3])	{
						if (vanisharrows.isSelected()) removeArrow(i);
						Twist[i].setEnabled(false);
					}
					else {
						if (vanisharrows.isSelected()) addArrow(i);
						Twist[i].setEnabled(true);
					}
		}			
		else {
			next.setEnabled(false);
			for (int i = 0; i < Twist.length; i++)	{
				if (!vanisharrows.isSelected()) removeArrow(i);
				Twist[i].setEnabled(false);
			}
		}
		if (logOn == 0) undo.setEnabled(false);
		victor = 'R';
		redo.setEnabled(true);
		repaint();
	}
	void redo()	{
		logOn++;
		for (int i = 0; i < boardValues.length; i++)
			for (int a = 0; a < boardValues[i].length; a++)
				boardValues[i][a] = boardLog[logOn][i][a];
		turn = boardLog[logOn][6][0];
		stage = (byte)boardLog[logOn][6][1];
		twist = (int)boardLog[logOn][6][2];
		lastTwist = (int)boardLog[logOn][6][4];
		if (stage == 2)	{
			next.setEnabled(true);
			if (twist == 0)
				for (int i = 0; i < Twist.length; i++)
					if (i != lastTwist)	{
						if (vanisharrows.isSelected()) addArrow(i);
						Twist[i].setEnabled(true);
					}
			if (twist != 0)
				for (int i = 0; i < Twist.length; i++)
					if (i / 2 != (int)boardLog[logOn][6][3])	{
						if (vanisharrows.isSelected()) removeArrow(i);
						Twist[i].setEnabled(false);
					}
					else {
						if (vanisharrows.isSelected()) addArrow(i);
						Twist[i].setEnabled(true);
					}
		}
		else {
			next.setEnabled(false);
			for (int i = 0; i < Twist.length; i++)	{
				if (!vanisharrows.isSelected()) removeArrow(i);
				Twist[i].setEnabled(false);
			}
		}
		if (logOn == 0) undo.setEnabled(false);
		if ((int)boardLog[logOn+1][6][1] == 0) redo.setEnabled(false);
		repaint();
	}
	void watchNext()	{
		logOn++;
		for (int i = 0; i < boardValues.length; i++)
			for (int a = 0; a < boardValues[i].length; a++)
				boardValues[i][a] = boardLog[logOn][i][a];
		turn = boardLog[logOn][6][0];
		repaint();
	}
	class WatchGame implements ActionListener {
		public void actionPerformed(ActionEvent evt)	{
			watchNext();
			if (logOn == lastLog)	{
				WatchGameTimer.stop();
				undo.setEnabled(true);
				newgame.setEnabled(true);
				width = 0;
				stage = (byte)boardLog[logOn][6][1];
				twist = (int)boardLog[logOn][6][2];
				lastTwist = (int)boardLog[logOn][6][4];
				if (stage == 2)	{
					next.setEnabled(true);
					if (twist == 0)
						for (int i = 0; i < Twist.length; i++)
							if (i != lastTwist)	{
								if (vanisharrows.isSelected()) addArrow(i);
								Twist[i].setEnabled(true);
							}
					if (twist != 0)
						for (int i = 0; i < Twist.length; i++)
							if (i / 2 != (int)boardLog[logOn][6][3])	{
								if (vanisharrows.isSelected()) removeArrow(i);
								Twist[i].setEnabled(false);
							}
							else {
								if (vanisharrows.isSelected()) addArrow(i);
								Twist[i].setEnabled(true);
							}
				}
				else {
					next.setEnabled(false);
					for (int i = 0; i < Twist.length; i++)	{
						if (!vanisharrows.isSelected()) removeArrow(i);
						Twist[i].setEnabled(false);
					}
				}
				checkVictory();

				repaint();
			}
		}
	}
}
