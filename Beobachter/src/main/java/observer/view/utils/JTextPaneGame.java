package observer.view.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import observer.network.RemoteGame;
import observer.view.text.NoTextSelectionCaret;

@SuppressWarnings("serial")
public class JTextPaneGame extends JTextPane {
	// The game to show if the button is clicked
	private RemoteGame game;
	// The names of the players to display in the button
	private String player1Name = "";
	private String player2Name = "";
	private int winningPlayerId = -1;

	public JTextPaneGame(RemoteGame game) {
		this.game = game;

		// Prevent the user to select or edit the text on this text pane
		this.setCaret(new NoTextSelectionCaret(this));
		this.setEditable(false);
		this.setFocusable(false);

		// Make the border and background of the text pane look like a button
		this.setBorder(UIManager.getBorder("Button.border"));
		this.setBackground(UIManager.getColor("Button.background"));

		// Add a mouse and mouse motion listener
		this.addMouseListener(this.mouseListener);
		this.addMouseMotionListener((MouseMotionListener) this.mouseListener);
	}

	// Handle clicking of the button
	private MouseListener mouseListener = new MouseAdapter() {
		@Override
		public void mouseEntered(MouseEvent event) {
			if (event.getSource() instanceof JTextPaneGame) {
				event.getComponent().setBackground(UIManager.getColor("Button.hoverBackground"));
			}
		}

		@Override
		public void mouseExited(MouseEvent event) {
			if (event.getSource() instanceof JTextPaneGame) {
				event.getComponent().setBackground(UIManager.getColor("Button.background"));
			}
		}

		@Override
		public void mousePressed(MouseEvent event) {
			if (event.getSource() instanceof JTextPaneGame) {
				event.getComponent().setBackground(UIManager.getColor("Button.pressedBackground"));
			}
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			if (event.getSource() instanceof JTextPaneGame) {
				if (new Rectangle(0, 0, event.getComponent().getWidth(), event.getComponent().getHeight()).contains(event.getPoint())) {
					event.getComponent().setBackground(UIManager.getColor("Button.pressedBackground"));
				}
			}
		};

		@Override
		public void mouseReleased(MouseEvent event) {
			if (event.getSource() instanceof JTextPaneGame) {
				event.getComponent().setBackground(UIManager.getColor("Button.background"));

				if (new Rectangle(0, 0, event.getComponent().getWidth(), event.getComponent().getHeight()).contains(event.getPoint())) {
					game.getRemoteServer().getServerCollapsible().getGUIController().setWatchedGame(((JTextPaneGame) event.getComponent()).getRemoteGame());
				}
			}
		}
	};

	/**
	 * Updates the player names displayed on the button
	 * 
	 * @param name1 The name of player one (the white player)
	 * @param name2 The name of player two (the black player)
	 */
	public void setNames(String name1, String name2) {
		this.player1Name = name1;
		this.player2Name = name2;

		this.updateButtonText(false);
	}

	/**
	 * Returns the name of the white player
	 * 
	 * @return The name of player one
	 */
	public String getPlayer1Name() {
		return this.player1Name;
	}

	/**
	 * Returns the name of the black player
	 * 
	 * @return The name of player two
	 */
	public String getPlayer2Name() {
		return this.player2Name;
	}

	/**
	 * Updates the player names on the button to be either displayed in multiple lines or one line
	 * 
	 * @param oneLine true if the text should be displayed in one line, false otherwise
	 */
	public void updateButtonText(boolean oneLine) {
		String player1 = this.player1Name;
		String player2 = this.player2Name;

		if (this.winningPlayerId == 0) {
			player1 += " 👑";
		} else if (this.winningPlayerId == 1) {
			player2 += " 👑";
		}

		// Set the text of this text pane to display the game's player names
		this.setText(player1 + (oneLine ? " vs. " : "\nvs.\n") + player2);

		// Get the styled document of this text pane to allow formatting the text
		StyledDocument styledDocument = this.getStyledDocument();
		
		SimpleAttributeSet white = new SimpleAttributeSet();
		StyleConstants.setForeground(white, UIManager.getColor("TabbedPane.foreground"));

		SimpleAttributeSet left = new SimpleAttributeSet();
		StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
		// StyleConstants.setForeground(left, this.getForeground());

		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);

		SimpleAttributeSet right = new SimpleAttributeSet();
		StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);

		styledDocument.setParagraphAttributes(0, styledDocument.getLength(), left, false);
		styledDocument.setCharacterAttributes(0, styledDocument.getLength(), white, false);

		// If oneLine is true, center the text in one line
		if (oneLine) {
			styledDocument.setParagraphAttributes(0, styledDocument.getLength(), center, false);
		} else {
			// Set the center line ('vs.') to be centered
			styledDocument.setParagraphAttributes(player1.length() + 1, styledDocument.getLength(), center, false);

			// Set the bottom line (second player's name) to be aligned to the right
			styledDocument.setParagraphAttributes(styledDocument.getLength() - player2.length(), styledDocument.getLength(), right, false);
		}

		this.colorizeWinner(styledDocument);
		
	}

	/**
	 * Colorizes the crown behind the winning player
	 * 
	 * @param styledDocument
	 */
	private void colorizeWinner(StyledDocument styledDocument) {
		SimpleAttributeSet crown = new SimpleAttributeSet();
		StyleConstants.setForeground(crown, new Color(212, 175, 55));

		if (this.getText().indexOf("👑") < 0)
			return;

		if (this.winningPlayerId == 0) {
			styledDocument.setCharacterAttributes(/*crownOffset*/this.getText().indexOf("👑"), 2, crown, false);
		} else if (this.winningPlayerId == 1) {
			styledDocument.setCharacterAttributes(/*styledDocument.getLength()*/this.getText().lastIndexOf("👑"), 2, crown, false);
		}
	}

	/**
	 * Returns the remote game that stores the data for the game to watch if this button is clicked.
	 * 
	 * @return The button's remote game
	 */
	public RemoteGame getRemoteGame() {
		return this.game;
	}

	@Override
	public void addMouseListener(MouseListener mouseListener) {
		// Remove the default mouse listener if a new one is added
		if (mouseListener != null && !mouseListener.equals(this.mouseListener))
			this.removeMouseListener(this.mouseListener);

		super.addMouseListener(mouseListener);
	}

	@Override
	public void removeMouseListener(MouseListener mouseListener) {
		super.removeMouseListener(mouseListener);

		// Use the default mouse listener if an old one is removed
		if (mouseListener != null && !mouseListener.equals(this.mouseListener))
			this.addMouseListener(this.mouseListener);
	}

	@Override
	public Dimension getMaximumSize() {
		// Stretch to parent width
		return new Dimension(this.getParent().getWidth(), this.getPreferredSize().height);
	}

	@Override
	public Dimension getMinimumSize() {
		// Stretch to parent width
		return new Dimension(this.getParent().getWidth(), this.getPreferredSize().height);
	}

	public int getWinningPlayerId() {
		return this.winningPlayerId;
	}

	public void setWinningPlayerId(Integer winningPlayer) {
		if (winningPlayer == null || (winningPlayer != 0 && winningPlayer != 1)) {
			return;
		}

		this.winningPlayerId = winningPlayer;
		this.updateButtonText(!this.getText().contains("\n"));
	}
}
