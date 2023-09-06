package observer.view.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.UIManager;

import observer.controller.GUIController;
import observer.controller.ServerSettingsDialogController;
import observer.network.RemoteServer;

@SuppressWarnings("serial")
public class ServerCollapsible extends JPanelCollapsible {
	private RemoteServer remoteServer;
	private JPanel gamesPanel;
	private GUIController controller;

	// The settings icon and the preferred size
	public static BufferedImage ICON_IMAGE;
	public static int ICON_SIZE = 20;

	// Properties for the settings icon
	private final Dimension iconSize = new Dimension(20, 20);
	private final int iconOffset = 20;
	private Color iconBackground = UIManager.getColor("TabbedPane.background");
	private boolean iconHovered = false;

	public ServerCollapsible(RemoteServer server, GUIController controller) {
		// Allow for a custom mouse listener
		super(false);

		// Add the custom mouse listener
		this.addMouseListener(this.mouseListener);
		this.addMouseMotionListener((MouseMotionListener) this.mouseListener);

		// Adds a panel to this collapsible to allow displaying the game buttons
		this.gamesPanel = new JPanel();
		this.gamesPanel.setLayout(new BoxLayout(gamesPanel, BoxLayout.Y_AXIS));
		this.add(gamesPanel);

		this.controller = controller;
		this.remoteServer = server;
	}

	/**
	 * A mouse listener needed for the settings button behavior
	 */
	private MouseListener mouseListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent event) {
			// Open the server settings dialog if the settings button is clicked, otherwise expand or retract the panel
			if (!new Rectangle(getWidth() - iconOffset, 0, iconSize.width, iconSize.height).contains(event.getPoint())) {
				toggleVisibility();
			} else {
				iconHovered = false;
				repaint();
				remoteServer.getServerCollapsible().getGamesPanel().removeAll();
				new ServerSettingsDialogController(remoteServer, controller);
			}
		}

		@Override
		public void mouseEntered(MouseEvent event) {
			// Update the panels and the settings button's backgrounds
			setBackground(UIManager.getColor("TabbedPane.hoverColor"));
			iconBackground = getBackground();
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent event) {
			// Update the panels and the settings button's backgrounds
			iconHovered = false;
			setBackground(UIManager.getColor("TabbedPane.background"));
			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent event) {
			// Update the panels and the settings button's backgrounds
			if (new Rectangle(getWidth() - iconOffset, 0, iconSize.width, iconSize.height).contains(event.getPoint())) {
				iconHovered = true;

				if (!getBackground().equals(UIManager.getColor("TabbedPane.background"))) {
					setBackground(UIManager.getColor("TabbedPane.background"));
				}
			} else {
				iconHovered = false;

				if (!getBackground().equals(UIManager.getColor("TabbedPane.hoverColor"))) {
					setBackground(UIManager.getColor("TabbedPane.hoverColor"));
				}
			}
		}
	};

	public GUIController getGUIController() {
		return this.controller;
	}

	/**
	 * Adds a game button to this collapsible's game button panel. Mainly a convenience function to avoid calling ServerCollapsible.getGamesPanel().add(gameButton);
	 */
	public void addGameButton(JTextPaneGame gameButton) {
		this.gamesPanel.add(gameButton);
	}

	/**
	 * Returns the remote server that holds the information for this collapsible's games
	 * 
	 * @return The server this collapsible is attached to
	 */
	public RemoteServer getRemoteServer() {
		return this.remoteServer;
	}

	/**
	 * Returns the panel the game buttons should be added to since adding them directly to the collapsible would make them invisible
	 * 
	 * @return Returns a panel that the game buttons should be added to
	 */
	public JPanel getGamesPanel() {
		return this.gamesPanel;
	}

	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);

		// Paint the icon background
		graphics.setColor(this.iconBackground);
		graphics.fillRoundRect(this.getWidth() - this.iconOffset, 0, this.iconSize.width, this.iconSize.height, 5, 5);

		// Paint the settings icon
		if (ICON_IMAGE != null)
			graphics.drawImage(ICON_IMAGE, this.getWidth() - this.iconOffset, 0, null);
	}

	@Override
	public void repaint() {
		// Reset the settings icon's background color if the look and feel theme changes
		if (this.iconHovered)
			this.iconBackground = UIManager.getColor("TabbedPane.hoverColor");
		else
			this.iconBackground = this.getBackground();

		super.repaint();
	}

	@Override
	public Insets getInsets() {
		// Add to pixels to the top insets to prevent the settings icon from overlapping the game buttons
		Insets insets = super.getInsets();
		insets.top += 2;
		return insets;
	}
}
