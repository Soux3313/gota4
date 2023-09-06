package observer.view.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;

import model.player.Turn;
import model.util.Position;
import observer.model.BoardHistory;
import observer.view.GUI;

@SuppressWarnings("serial")
public class JPanelBoard extends JPanel implements ActionListener {
	public enum BoardTheme {
		CLASSIC, HAPLORHINI
	}

	// The colors of the board
//	private final Color whiteBoardColor = new Color(197, 205, 158);
//	private final Color blackBoardColor = new Color(123, 141, 139);

	private final Color whiteBoardColor = new Color(240, 220, 180);
	private final Color blackBoardColor = new Color(170,140, 100);
	//private final Color movedColor = new Color(0, 0, 204);

	// The images for the classic pieces
	private Image whiteAmazonImage;
	private Image blackAmazonImage;
	private Image arrowImage;

	// The images for the haplorhini pieces
	private Image whiteMonkeyImage;
	private Image blackMonkeyImage;
	private Image bananaImage;

	// The images for the classic pieces
	private Image whiteAmazon;
	private Image blackAmazon;
	private Image arrow;

	// The images for the haplorhini pieces
	private Image whiteMonkey;
	private Image blackMonkey;
	private Image banana;
	
	private ImageIcon whiteAmazonIcon;
	private ImageIcon blackAmazonIcon;
	private ImageIcon arrowIcon;

	private ImageIcon whiteMonkeyIcon;
	private ImageIcon blackMonkeyIcon;
	private ImageIcon bananaIcon;
	
	private GUI view;
	
	// The drawn board
	private BoardHistory board;

	// The theme of the board (either classic or haplorhini)
	private BoardTheme theme = BoardTheme.CLASSIC;

	//Animation variables
	private Timer amazonTimer = new Timer(5, this);
	private Timer arrowTimer = new Timer(5, this);
	private Position amCurrentPos;
	private Position amEndPos;
	private Position arrCurrentPos;
	private Position arrEndPos;
	private Position amDir;
	private Position arrDir;
	private double speed = 6.0; //Speed to adjust
	private int pixelSpeed; //calculated speed based on size of board
	private boolean animationFinished;
	private boolean animateAll = false;

	class ResizeListener extends ComponentAdapter {
		public void componentResized(ComponentEvent e) {
			int width = getWidth() / 11;
			int height = getHeight() / 11;
	
			int size = Math.min(width, height);
	
			if (size < 5)
				size = 5;
	
			int pieceSize = (int) (size * 0.9f);

			// The scaled images for the classic pieces
			whiteAmazon = whiteAmazonImage.getScaledInstance(pieceSize, pieceSize, Image.SCALE_SMOOTH);
			blackAmazon = blackAmazonImage.getScaledInstance(pieceSize, pieceSize, Image.SCALE_SMOOTH);
			arrow = arrowImage.getScaledInstance(pieceSize, pieceSize, Image.SCALE_SMOOTH);

			// The scaled images for the haplorhini pieces
			whiteMonkey = whiteMonkeyImage.getScaledInstance(pieceSize, pieceSize, Image.SCALE_SMOOTH);
			blackMonkey = blackMonkeyImage.getScaledInstance(pieceSize, pieceSize, Image.SCALE_SMOOTH);
			banana = bananaImage.getScaledInstance(pieceSize, pieceSize, Image.SCALE_SMOOTH);


			//When window is resized an animation isn't finished yet: start it again
			if(!animationFinished) {
				update();
			} else {
				repaint();
			}
		}
	}

	public JPanelBoard(GUI gui, BoardHistory board) {
		this.board = board;
		this.view = gui;
		// Load the images of all pieces
		try {
			// Classic look
			this.whiteAmazonImage = ImageIO.read(getClass().getResource("/pieces/amazon_white.png"));
			this.blackAmazonImage = ImageIO.read(getClass().getResource("/pieces/amazon_black.png"));
			this.arrowImage = ImageIO.read(getClass().getResource("/pieces/arrow.png"));

			// Haplorhini look
			this.whiteMonkeyImage = ImageIO.read(getClass().getResource("/pieces/monkey_white.png"));
			this.blackMonkeyImage = ImageIO.read(getClass().getResource("/pieces/monkey_black.png"));
			this.bananaImage = ImageIO.read(getClass().getResource("/pieces/banana.png"));

			this.whiteAmazonIcon = new ImageIcon(this.whiteAmazonImage.getScaledInstance(10, 10, Image.SCALE_SMOOTH));
			this.blackAmazonIcon = new ImageIcon(this.blackAmazonImage.getScaledInstance(10, 10, Image.SCALE_SMOOTH));
			this.arrowIcon = new ImageIcon(this.arrowImage.getScaledInstance(10, 10, Image.SCALE_SMOOTH));

			this.whiteMonkeyIcon = new ImageIcon(this.whiteMonkeyImage.getScaledInstance(10, 10, Image.SCALE_SMOOTH));
			this.blackMonkeyIcon = new ImageIcon(this.blackMonkeyImage.getScaledInstance(10, 10, Image.SCALE_SMOOTH));
			this.bananaIcon = new ImageIcon(this.bananaImage.getScaledInstance(10, 10, Image.SCALE_SMOOTH));

		} catch (IOException e) {
			e.printStackTrace();
		}
		this.addComponentListener(new ResizeListener());
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);

		//################################## painting Variables ##################################
		// Get a suitable size for the board
		int width = this.getWidth() / 11;
		int height = this.getHeight() / 11;

		int size = Math.min(width, height);

		if (size < 5)
			size = 5;

		//int pieceSize = (int) (size * 0.9f);
		int offset = (int) (size * 0.05f);

		// The top left coordinates of the board
		int x = this.getWidth() / 2 - 5 * size;
		int y = this.getHeight() / 2 - 5 * size;


		//################################## no animation stuff ##################################
		for (int i = 0; i < 10; i++) {
			// Get the x coordinates for the current square
			int squareX = x + i * size;

			// Draw letters above the board
			graphics.setFont(new Font(graphics.getFont().getFontName(), graphics.getFont().getStyle(), size / 5));
			graphics.setColor(UIManager.getColor("TabbedPane.foreground"));
			int stringX = squareX + size / 2 - graphics.getFontMetrics().stringWidth("" + (char) (i + 65)) / 2;
			int stringY = y - graphics.getFontMetrics().getHeight() / 2;
			graphics.drawString("" + (char) (i + 65), stringX, stringY);

			for (int j = 0; j < 10; j++) {
				// Get the y coordinates for the current square
				int squareY = y + j * size;

				// Draw letters next to the board
				if (i == 0) {
					graphics.setColor(UIManager.getColor("TabbedPane.foreground"));
					stringX = x - graphics.getFontMetrics().stringWidth("" + (10 - j)) - graphics.getFontMetrics().getHeight() / 2;
					stringY = squareY + size / 2;
					graphics.drawString("" + (10 - j), stringX, stringY);
				}

				// Draw white and black tiles in alternating order
				if (i % 2 != j % 2)
					graphics.setColor(this.blackBoardColor);
				else
					graphics.setColor(this.whiteBoardColor);

				if(this.board.hasPrevTurn()) {
					Turn t = this.board.getLastTurn();
					Position pos = new Position(j, i);

					// TODO Maybe use lower alpha value
					//graphics.setColor(new Color(1, 1, 1, 0.5f));

					if (pos.equals(t.getShot().getShotPosition())) {
						graphics.setColor(new Color(184,189,95));
						graphics.fillRect(squareX, squareY, size, size);
						//Skip moved pieces
						continue;
					} else if (pos.equals(t.getMove().getStart())) {
						graphics.setColor(new Color(205,210,106));
						graphics.fillRect(squareX, squareY, size, size);
						//Skip moved pieces
						continue;
					} else if(pos.equals(t.getMove().getEnd())) {
						graphics.setColor(new Color(184,189,95));
						graphics.fillRect(squareX, squareY, size, size);
						//Skip moved pieces
						continue;
					}
				}
				graphics.fillRect(squareX, squareY, size, size);
				//Some Debugging. Please keep
//				graphics.setColor(Color.BLACK);
//				graphics.drawString("x="+i+",y="+j,squareX+1,squareY+20);

				// Check the value of the board at the current coordinates and draw the pieces depending on the currently selected style
				Integer[][] squares = this.board.toIntegerSquares();
				switch (squares[j][i]) {
					// Draw arrows
					case -2:
						graphics.drawImage(theme == BoardTheme.CLASSIC ? arrow : banana, squareX + offset, squareY + offset, null);
						break;

					// Draw white amazons
					case 0:
						if (theme == BoardTheme.CLASSIC) {
							graphics.drawImage(whiteAmazon, squareX + offset, squareY + offset, null);
						} else {
							graphics.drawImage(whiteMonkey, squareX + offset, squareY + offset, null);
						}
						break;

					// Draw black amazons
					case 1:
						if (theme == BoardTheme.CLASSIC) {
							graphics.drawImage(blackAmazon, squareX + offset, squareY + offset, null);
						} else {
							graphics.drawImage(blackMonkey, squareX + offset, squareY + offset, null);
						}
						break;
				}
			}
		}

		//################################## animation stuff ##################################
		if(board.hasPrevTurn()) {
			Turn t = board.getLastTurn();
//			System.out.println("Turn: " + t);
			//TODO Fix x,y swap. That line here is actually correct but it shouldn't:
			//End position is independent from running animations
			amEndPos = new Position(x + t.getMove().getEnd().getY() * size, y + t.getMove().getEnd().getX() * size);
			arrEndPos = new Position(x + t.getShot().getShotPosition().getY() * size, y + t.getShot().getShotPosition().getX() * size);

			//Animation is not running but also hasn't finished -> animation start:
			if(!amazonTimer.isRunning() && !arrowTimer.isRunning() && !animationFinished) {
				//TODO Fix x,y swap. That line here is actually correct but it shouldn't:
				//Reset position to starting position id animation is about to start
				amCurrentPos = new Position(x + t.getMove().getStart().getY() * size, y + t.getMove().getStart().getX() * size);
				arrCurrentPos = new Position(x + t.getMove().getEnd().getY() * size, y + t.getMove().getEnd().getX() * size);

				//Speed calculation:
				double max = Math.max(this.getWidth(), this.getHeight());
				double result = (max / 580) * speed; //Equivalent to value of speed in case of small window
				pixelSpeed = (int) Math.floor(result);
//				System.out.println("PixelSpeed: " + pixelSpeed);

				//get amazon dir:
				int x0 = amEndPos.getX() - amCurrentPos.getX();
				int y0 = amEndPos.getY() - amCurrentPos.getY();
				if (x0 != 0) { x0 = (x0 / Math.abs(x0)) * pixelSpeed; }
				if (y0 != 0) { y0 = (y0 / Math.abs(y0)) * pixelSpeed; }
				amDir = new Position(x0, y0);

				//get arrow dir:
				int x1 = arrEndPos.getX() - arrCurrentPos.getX();
				int y1 = arrEndPos.getY() - arrCurrentPos.getY();
				if (x1 != 0) { x1 = (x1 / Math.abs(x1)) * pixelSpeed; }
				if (y1 != 0) { y1 = (y1 / Math.abs(y1)) * pixelSpeed; }
				arrDir = new Position(x1, y1);
//				System.out.println("amStartPos: " + amCurrentPos);
//				System.out.println("amEndPos: " + amEndPos);
//				System.out.println("arrStartPos: " + arrCurrentPos);
//				System.out.println("arrEndPos: " + arrEndPos);

				amazonTimer.start();
			}

			//Draw animated pieces:
			if (amazonTimer.isRunning()) {
//				graphics.setColor(Color.CYAN);
//				graphics.fillRect(amCurrentPos.getX(), amCurrentPos.getY(), size, size);
				if (board.getLastTurn().getPlayerId().get() == 0) {
					graphics.drawImage(theme == BoardTheme.CLASSIC ? whiteAmazon : whiteMonkey, amCurrentPos.getX(), amCurrentPos.getY(), null);
				} else {
					graphics.drawImage(theme == BoardTheme.CLASSIC ? blackAmazon : blackMonkey, amCurrentPos.getX(), amCurrentPos.getY(), null);
				}
			} else {
//				graphics.setColor(Color.CYAN);
//				graphics.fillRect(amEndPos.getX(), amEndPos.getY(), size, size);
				if (board.getLastTurn().getPlayerId().get() == 0) {
					graphics.drawImage(theme == BoardTheme.CLASSIC ? whiteAmazon : whiteMonkey, amEndPos.getX()+offset, amEndPos.getY()+offset, null);
				} else {
					graphics.drawImage(theme == BoardTheme.CLASSIC ? blackAmazon : blackMonkey, amEndPos.getX()+offset, amEndPos.getY()+offset, null);
				}
			}

			if (arrowTimer.isRunning()) {
//				graphics.setColor(Color.yellow);
//				graphics.fillRect(arrCurrentPos.getX(), arrCurrentPos.getY(), size, size);
				graphics.drawImage(theme == BoardTheme.CLASSIC ? arrow : banana, arrCurrentPos.getX(), arrCurrentPos.getY(), null);
			} else if (!arrowTimer.isRunning() && !amazonTimer.isRunning()) {
//				graphics.setColor(Color.YELLOW);
//				graphics.fillRect(arrEndPos.getX(), arrEndPos.getY(), size, size);
				graphics.drawImage(theme == BoardTheme.CLASSIC ? arrow : banana, arrEndPos.getX()+offset, arrEndPos.getY()+offset, null);
			}
		} else {
			this.stop();
		}

//		//Some Debugging. Please keep
//		if(board.hasPrevTurn()) {
//			graphics.setColor(Color.MAGENTA);
//			Move m = board.getLastTurn().getMove();
//			Position s = board.getLastTurn().getShot().getShotPosition();
//			graphics.fillRect(x + m.getStart().getY() * size, y + m.getStart().getX() * size, 10, 10);
//			graphics.fillRect(x + m.getEnd().getY() * size, y + m.getEnd().getX() * size, 10, 10);
//			graphics.fillRect(x + s.getY() * size, y + s.getX() * size, 10, 10);
//			graphics.fillRect(arrEndPos.getX(), arrEndPos.getY(), 10, 10);
//			//System.out.println("x: " + x);
//			//System.out.println("y: " + y);
//		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Get a suitable size for the board
		int width = this.getWidth() / 11;
		int height = this.getHeight() / 11;

		int size = Math.min(width, height);

		if (size < 5)
			size = 5;

		// The top left coordinates of the board
		//int x = this.getWidth() / 2 - 5 * size;
		//int y = this.getHeight() / 2 - 5 * size;

		if(e.getSource().equals(amazonTimer) && board.hasPrevTurn()) {
			amCurrentPos = amCurrentPos.moveBy(amDir);
			repaint();
			if(amCurrentPos.isInDisc(amEndPos,pixelSpeed)) {
				//System.out.println("Amazon STOP");
				amazonTimer.stop();
				//Amazon is done, start arrow animation:
				arrowTimer.start();
			}
		}
		if(e.getSource().equals(arrowTimer) && board.hasPrevTurn()) {
			arrCurrentPos = arrCurrentPos.moveBy(arrDir);
			repaint();
			if(arrCurrentPos.isInDisc(arrEndPos,pixelSpeed)) {
				//System.out.println("Arrow STOP");
				arrowTimer.stop();
				animationFinished = true;
				if(animateAll && this.board.hasNextTurn()) {
					this.board.toNextTurn();
				} else {
					animateAll = false;
					view.updatePlayButton();
				}
			}
		}
	}

	public void play() {
		board.toNextTurn();
		animateAll = true;
		update();
	}

	public void stop() {
		animateAll = false;
		view.updatePlayButton();
	}

	public boolean isPlaying() {
		return animateAll;
	}

	public void update() {
		System.out.println("UPDATE");
		amazonTimer.stop();
		arrowTimer.stop();
		animationFinished = false;
		repaint();
	}
	
	public ImageIcon getBlackAmazonIcon() {
		return this.blackAmazonIcon;
	}
	
	public ImageIcon getWhiteAmazonIcon() {
		return this.whiteAmazonIcon;
	}
	
	public ImageIcon getArrowIcon() {
		return this.arrowIcon;
	}

	public ImageIcon getBlackMonkeyIcon() {return this.blackMonkeyIcon;}

	public ImageIcon getWhiteMonkeyIcon() {return this.whiteMonkeyIcon;}

	public ImageIcon getBananaIcon() {return this.bananaIcon;}

	/**
	 * Updates the board's theme and repaints it to make the changes visible
	 * 
	 * @param theme The new theme
	 */
	public void setTheme(BoardTheme theme) {
		this.theme = theme;
		this.repaint();
	}

	public BoardTheme getTheme() {
		return this.theme;
	}
}
