package observer.view.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import model.ids.GamePlayerId;
import observer.view.GUI;

@SuppressWarnings("serial")
public class JButtonTurn extends JButton {
	private GamePlayerId playerId;
	private String piecePos;
	private String arrowPos;
	private GUI view;
	private JPanelBoard board;

	private ImageIcon arrowIcon;
	private ImageIcon pieceIcon;

	public JButtonTurn(GUI view, int turnId, String piecePos, String arrowPos, GamePlayerId playerId, JPanelBoard board) {
		super("" + turnId + ": ");

		this.view = view;
		this.piecePos = piecePos;
		this.arrowPos = arrowPos;
		this.playerId = playerId;
		this.board = board;

		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.setFocusable(false);
		
		// Makes Turn Buttons looks more beautiful
		this.setSize(getMinimumSize());
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		// Paint the button's background, border and text
		super.paintComponent(graphics);

		// Convert the graphics to Graphics2D to allow rendering Hints
		Graphics2D g = (Graphics2D) graphics;

		// Set the font to anti-alias
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Draw the icons depending on the boards theme
		if (this.board.getTheme() == JPanelBoard.BoardTheme.CLASSIC) {
			arrowIcon = board.getArrowIcon();

			if (playerId == GamePlayerId.PLAYER1) {
				pieceIcon = board.getWhiteAmazonIcon();
			} else {
				pieceIcon = board.getBlackAmazonIcon();
			}
		} else {
			arrowIcon = board.getBananaIcon();

			if (playerId == GamePlayerId.PLAYER1) {
				pieceIcon = board.getWhiteMonkeyIcon();
			} else {
				pieceIcon = board.getBlackMonkeyIcon();
			}
		}

		Color curColor = g.getColor();
		Color iconBackgroundColor = this.view.getDarkmode() ? new Color(1f, 1f, 1f, 0.1f) : new Color(0f, 0f, 0f, 0.2f);

		int gap = this.getIconTextGap();

		int posX = this.getInsets().left;
		int stringPosY = this.getHeight() / 2 - g.getFontMetrics().getHeight() / 2 + g.getFontMetrics().getAscent();
		int iconPosY = this.getHeight() / 2 - arrowIcon.getIconHeight() / 2;

		// Increase the x position by the turn id's string width
		posX += g.getFontMetrics().stringWidth("000: ") + gap;

		// Draw a background behind the piece icon
		g.setColor(iconBackgroundColor);
		g.fillRoundRect(posX - 2, iconPosY - 2, pieceIcon.getIconWidth() + 4, pieceIcon.getIconHeight() + 4, 2, 2);

		// Draw the piece icon
		pieceIcon.paintIcon(this, g, posX, iconPosY);
		posX += pieceIcon.getIconWidth() + gap;

		// Draw the movement of the board piece
		g.setColor(curColor);
		g.drawString(this.piecePos, posX, stringPosY);
		posX += g.getFontMetrics().stringWidth("MM → MM") + 2 * gap;

		// Draw a background behind the arrow icon
		g.setColor(iconBackgroundColor);
		g.fillRoundRect(posX - 2, iconPosY - 2, arrowIcon.getIconWidth() + 4, arrowIcon.getIconHeight() + 4, 2, 2);

		// Draw the arrow icon
		arrowIcon.paintIcon(this, g, posX, iconPosY);
		posX += arrowIcon.getIconWidth() + gap;

		// Draw the movement of the arrow
		g.setColor(curColor);
		g.drawString(this.arrowPos, posX, stringPosY);
	}

	@Override
	public Dimension getMinimumSize() {
		return getParent() != null ? new Dimension(getParent().getWidth(), getPreferredSize().height) : super.getMinimumSize();
	}

	@Override
	public Dimension getMaximumSize() {
		return getParent() != null ? new Dimension(getParent().getWidth(), getPreferredSize().height) : super.getMinimumSize();
	}
}
