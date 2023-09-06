package observer.view;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.text.PlainDocument;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import model.player.Turn;
import observer.controller.Controller;
import observer.interfaces.Refreshable;
import observer.model.BoardHistory;
import observer.view.dialogs.ServerSettingsDialog;
import observer.view.text.IntegerDocumentFilter;
import observer.view.text.VerticalLabelUI;
import observer.view.utils.JButtonTurn;
import observer.view.utils.JPanelBoard;
import observer.view.utils.JPanelBoard.BoardTheme;
import observer.view.utils.JTextFieldPlaceholder;
import observer.view.utils.ServerCollapsible;

@SuppressWarnings("serial")
public class GUI extends JFrame {
	// A panel containing all available server
	private JPanel panel_servers;

	// The panel displaying the board and game information
	private JPanel panel_game;

	// A panel displaying the current board
	private JPanelBoard panel_board;

	// A panel displaying the current board
	private JPanel panel_boardAndPlayers;

	// A panel containing all turns
	private JPanel panel_turns;

	// A label displaying the name of player one from the watched game
	private JLabel label_player1;

	// A label displaying the name of player two from the watched game
	private JLabel label_player2;

	// A text field, the user can enter a server IP or URL into of a server to connect to
	private JTextFieldPlaceholder textField_serverUrl;

	// A text field, the user can enter a server port of a server into to connect to
	private JTextFieldPlaceholder textField_serverPort;

	// A text field, the user can enter a token into to gain access to every data from the server
	private JTextFieldPlaceholder textField_serverToken;

	// A button to toggle between haplorhini and classic look of the board pieces
	private JToggleButton toggleButton_pieceLook;

	// A button to toggle between dark and light mode
	private JToggleButton toggleButton_darkmode;

	// A tabbed pane to switch between the watched game and the tournament list
	private JTabbedPane tabbedPane_gameAndPlayers;

	// A tabbed pane containing the tournament list for each server
	private JTabbedPane tabbedPane_servers;

	// A button used to play all animations starting from the selected turn
	private JButton button_play;

	// Buttons used to navigate between turns
	private JButton button_firstTurn = new JButton("First");
	private JButton button_previousTurn = new JButton("Prev");
	private JButton button_nextTurn = new JButton("Next");
	private JButton button_lastTurn = new JButton("Last");

	// A label displaying the currently selected turn and the total amount of turns
	private JLabel label_currentTurn;

	// A panel containing the current board and the buttons to go back and forth in the game's history
	private JPanel panel_boardAndButtons = new JPanel();

	// Necessary to refresh certain elements on look-and-feel change as they would otherwise lose some rendering properties
	private HashMap<Object, Refreshable> updateOnRefresh = new HashMap<Object, Refreshable>();

	// The controller used to determine the GUI's behavior
	private Controller controller;

	/**
	 * Create the application.
	 */
	public GUI(Controller controller) {
		// Install the two different look-and-feel variants
		FlatDarkLaf.install();
		FlatLightLaf.install();

		// Assign this GUI's controller
		this.controller = controller;

		// Initialize the window
		initialize();

		// Set the default look-and-feel to dark
		this.setLookAndFeel(true);
	}

	/**
	 * Initialize the contents of this frame.
	 */
	private void initialize() {
		this.setBounds(0, 0, 848, 480);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().setLayout(new BorderLayout(0, 0));
		this.setTitle("Amazonenspiel");

		// Center the window in the middle of the screen
		this.setLocationRelativeTo(null);

		// A tool bar to toggle between light/dark mode, haplorhini/classic look and to
		// connect to the game server
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		this.getContentPane().add(toolBar, BorderLayout.NORTH);

		// A text field for the server url
		// TODO Remove default text 'localhost'
		this.textField_serverUrl = new JTextFieldPlaceholder("");
		this.textField_serverUrl.setMaximumSize(this.textField_serverUrl.getPreferredSize());
		this.textField_serverUrl.setPlaceholder("Serveradresse    ");
		this.textField_serverUrl.setToolTipText("Die URL oder IP-Adresse des Zielservers");

		// A text field for the server port
		// TODO Remove default text '8000'
		this.textField_serverPort = new JTextFieldPlaceholder("");
		this.textField_serverPort.setPlaceholder("Port");
		this.textField_serverPort.setMaximumSize(this.textField_serverPort.getPreferredSize());
		this.textField_serverPort.setToolTipText("Der Port des Zielservers");

		// Only allow up to 5 digits for the port
		PlainDocument doc = (PlainDocument) this.textField_serverPort.getDocument();
		doc.setDocumentFilter(new IntegerDocumentFilter(5));

		// A text field for the server token
		// TODO Remove default text 'hackeraccesstoken'
		this.textField_serverToken = new JTextFieldPlaceholder("");
		this.textField_serverToken.setMaximumSize(this.textField_serverToken.getPreferredSize());
		this.textField_serverToken.setPlaceholder("Token         ");
		this.textField_serverToken.setToolTipText("Der Sicherheitstoken zum Abrufen des Zielservers");

		// A button to connect to the server
		JButton button_serverConnect = new JButton("Verbinden");
		button_serverConnect.setActionCommand("CONNECT");
		button_serverConnect.addActionListener(this.controller);

		// Add the button, labels and input fields to the tool bar
		toolBar.add(this.textField_serverUrl);
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(this.textField_serverPort);
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(this.textField_serverToken);
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(button_serverConnect);

		// Keep the toggle buttons to the right of the GUI
		toolBar.add(Box.createHorizontalGlue());

		// A button to toggle between the classic and haplorhini look of the pieces
		this.toggleButton_pieceLook = new JToggleButton("Haplorhini");
		this.toggleButton_pieceLook.setSelected(true);
		this.toggleButton_pieceLook.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (panel_board != null)
					panel_board.setTheme(event.getStateChange() == ItemEvent.SELECTED ? BoardTheme.CLASSIC : BoardTheme.HAPLORHINI);

				panel_turns.repaint();
				toggleButton_pieceLook.setText(event.getStateChange() == ItemEvent.SELECTED ? "Haplorhini" : "Klassisch");
			}
		});
		toolBar.add(this.toggleButton_pieceLook);

		// A button to toggle between the light and dark look-and-feels
		this.toggleButton_darkmode = new JToggleButton("Lightmode");
		this.toggleButton_darkmode.setSelected(true);
		this.toggleButton_darkmode.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				setLookAndFeel(event.getStateChange() == ItemEvent.SELECTED);
				toggleButton_darkmode.setText(event.getStateChange() == ItemEvent.SELECTED ? "Lightmode" : "Darkmode");
			}
		});
		toolBar.add(this.toggleButton_darkmode);

		// A panel containing the tabbed pane for the game and the player list
		JPanel panel_gameAndPlayers = new JPanel();
		panel_gameAndPlayers.setLayout(new BorderLayout(0, 0));

		// The tabbed pane allowing to switch between the spectated game and the player list
		tabbedPane_gameAndPlayers = new JTabbedPane(JTabbedPane.RIGHT);
		panel_gameAndPlayers.add(tabbedPane_gameAndPlayers, BorderLayout.CENTER);

		// A panel used to display the board and the player names of the two competing players
		this.panel_game = new JPanel();
		this.panel_game.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane_gameAndPlayers.addTab(null, this.panel_game);
		this.panel_game.setLayout(new BorderLayout(0, 0));

		// A panel used to display a list of all turns to navigate between
		this.panel_turns = new JPanel();

		// A button used to start animating all turns
		this.button_play = new JButton("Play");

		try {
			// Load the icons for the play button
			BufferedImage iconPlay = ImageIO.read(getClass().getResource("/icons/play.svg"));
			BufferedImage iconStop = ImageIO.read(getClass().getResource("/icons/stop.svg"));

			// Remove the buttons text if the icons loaded successfully and assign one of them to the button
			this.button_play.setText("");
			this.button_play.setIcon(new ImageIcon(GUI.getColoredImage(iconPlay, UIManager.getColor("TabbedPane.foreground"), 15, 0)));

			// Remove certain rendering properties from the button
			this.button_play.setBorderPainted(false);
			this.button_play.setContentAreaFilled(false);
			this.button_play.setFocusPainted(false);
			this.button_play.setOpaque(false);
			this.button_play.setFocusable(false);

			// ...but draw the background if the button is hovered
			this.button_play.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					button_play.setContentAreaFilled(true);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					button_play.setContentAreaFilled(false);
				}
			});

			// Add an action listener to the button to update its icon
			this.button_play.addActionListener(e -> {
				this.button_play.setIcon(new ImageIcon(GUI.getColoredImage(panel_board.isPlaying() ? iconStop : iconPlay, UIManager.getColor("TabbedPane.foreground"), 15, 0)));
			});

			// Refresh the buttons icon color on theme change
			this.updateOnRefresh.put(this.button_play, () -> {
				if (this.panel_board != null) {
					if (this.panel_board.isPlaying())
						this.button_play.setIcon(new ImageIcon(GUI.getColoredImage(iconStop, UIManager.getColor("TabbedPane.foreground"), 15, 0)));
					else
						this.button_play.setIcon(new ImageIcon(GUI.getColoredImage(iconPlay, UIManager.getColor("TabbedPane.foreground"), 15, 0)));
				}
			});
		} catch (IOException exception) {
			exception.printStackTrace();
		}

		// Add another action listener to the button to make it start and stop animations
		this.button_play.addActionListener(e -> {
			if (this.panel_board != null) {
				if (this.panel_board.isPlaying()) {
					this.panel_board.stop();
				} else {
					this.panel_board.play();
				}
			}
		});

		// A panel containing the board and player names
		this.panel_boardAndPlayers = new JPanel();
		this.panel_boardAndPlayers.setLayout(new BorderLayout(0, 0));
		this.panel_game.add(this.panel_boardAndPlayers);

		// A panel used to display the names of the two competing players
		JPanel panel_playerNames = new JPanel();
		this.panel_boardAndPlayers.add(panel_playerNames, BorderLayout.SOUTH);
		panel_playerNames.setLayout(new GridLayout(0, 5, 0, 5));

		// A placeholder label to keep the 'player vs. player' labels centered
		JLabel label_placeholderLeft = new JLabel("");
		panel_playerNames.add(label_placeholderLeft);

		// A label used to display the white player's name
		this.label_player1 = new JLabel("");
		this.label_player1.setHorizontalAlignment(SwingConstants.CENTER);
		this.label_player1.setOpaque(true);
		this.label_player1.setBackground(UIManager.getColor("TabbedPane.foreground"));
		this.label_player1.setForeground(UIManager.getColor("TabbedPane.hoverColor"));

		// Update the player label's colors on refresh to match the current theme
		this.updateOnRefresh.put(this.label_player1, () -> {
			if (toggleButton_darkmode.isSelected()) {
				this.label_player1.setBackground(UIManager.getColor("TabbedPane.foreground"));
				this.label_player1.setForeground(UIManager.getColor("TabbedPane.hoverColor"));
			} else {
				this.label_player1.setBackground(UIManager.getColor("Button.background"));
				this.label_player1.setForeground(UIManager.getColor("TabbedPane.foreground"));
			}
		});

		panel_playerNames.add(this.label_player1);

		// A label used to display a 'vs.' text between the player names
		JLabel label_vs = new JLabel("vs.");
		label_vs.setHorizontalAlignment(SwingConstants.CENTER);
		panel_playerNames.add(label_vs);

		// A label used to display the black player's name
		this.label_player2 = new JLabel("");
		this.label_player2.setHorizontalAlignment(SwingConstants.CENTER);
		this.label_player2.setOpaque(true);
		this.label_player2.setBackground(UIManager.getColor("TabbedPane.hoverColor"));
		this.label_player2.setForeground(UIManager.getColor("TabbedPane.foreground"));

		// Update the player label's colors on refresh to match the current theme
		this.updateOnRefresh.put(this.label_player2, () -> {
			if (toggleButton_darkmode.isSelected()) {
				this.label_player2.setBackground(UIManager.getColor("TabbedPane.hoverColor"));
				this.label_player2.setForeground(UIManager.getColor("TabbedPane.foreground"));
			} else {
				this.label_player2.setBackground(UIManager.getColor("TabbedPane.foreground"));
				this.label_player2.setForeground(UIManager.getColor("TabbedPane.hoverColor"));
			}
		});

		panel_playerNames.add(this.label_player2);

		// A placeholder label to keep the 'player vs. player' labels centered
		JLabel label_placeholderRight = new JLabel("");
		panel_playerNames.add(label_placeholderRight);

		// A panel containing the current board and the buttons to go back and forth in the game's history
		this.panel_boardAndPlayers.add(this.panel_boardAndButtons, BorderLayout.CENTER);
		this.panel_boardAndButtons.setLayout(new BorderLayout(0, 0));

		// The panel containing all the buttons to move in a game's history
		JPanel panel_buttons = new JPanel();
		this.panel_boardAndButtons.add(panel_buttons, BorderLayout.SOUTH);
		panel_buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		// Buttons to navigate between turns
		panel_buttons.add(this.button_firstTurn);
		this.button_firstTurn.setEnabled(false);
		this.button_firstTurn.setActionCommand("FIRST_TURN");
		this.button_firstTurn.addActionListener(this.controller);

		panel_buttons.add(this.button_previousTurn);
		this.button_previousTurn.setEnabled(false);
		this.button_previousTurn.setActionCommand("PREV_TURN");
		this.button_previousTurn.addActionListener(this.controller);

		this.label_currentTurn = new JLabel("0/0");
		this.label_currentTurn.setHorizontalAlignment(SwingConstants.CENTER);
		int height = this.label_currentTurn.getPreferredSize().height;
		this.label_currentTurn.setPreferredSize(new Dimension(40, height));
		panel_buttons.add(this.label_currentTurn);

		panel_buttons.add(this.button_nextTurn);
		this.button_nextTurn.setEnabled(false);
		this.button_nextTurn.setActionCommand("NEXT_TURN");
		this.button_nextTurn.addActionListener(this.controller);

		panel_buttons.add(this.button_lastTurn);
		this.button_lastTurn.setEnabled(false);
		this.button_lastTurn.setActionCommand("LAST_TURN");
		this.button_lastTurn.addActionListener(this.controller);

		try {
			// Load the icons for the buttons
			BufferedImage iconFirst = ImageIO.read(getClass().getResource("/icons/doublearrow_left.svg"));
			BufferedImage iconPrev = ImageIO.read(getClass().getResource("/icons/arrow_left.svg"));
			BufferedImage iconNext = ImageIO.read(getClass().getResource("/icons/arrow_right.svg"));
			BufferedImage iconLast = ImageIO.read(getClass().getResource("/icons/doublearrow_right.svg"));

			JButton[] buttons = { this.button_firstTurn, this.button_previousTurn, this.button_nextTurn, this.button_lastTurn };
			BufferedImage[] icons = { iconFirst, iconPrev, iconNext, iconLast };

			// Iterate over each button
			for (int i = 0; i < buttons.length; i++) {
				JButton button = buttons[i];
				BufferedImage icon = icons[i];

				// If the icon loaded properly
				if (icon != null) {
					// Remove the button text and set the corresponding icon as the button's icon
					button.setText("");
					button.setIcon(new ImageIcon(GUI.getColoredImage(icon, UIManager.getColor("TabbedPane.foreground"), 20, 0)));

					// Disable background drawing of the button...
					button.setBorderPainted(false);
					button.setContentAreaFilled(false);
					button.setFocusPainted(false);
					button.setOpaque(false);
					button.setFocusable(false);

					// ...but draw the background if the button is hovered
					button.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseEntered(MouseEvent e) {
							button.setContentAreaFilled(true);
						}

						@Override
						public void mouseExited(MouseEvent e) {
							button.setContentAreaFilled(false);
						}
					});

					// Refresh the buttons icon color on theme change
					this.updateOnRefresh.put(button, () -> {
						button.setIcon(new ImageIcon(GUI.getColoredImage(icon, UIManager.getColor("TabbedPane.foreground"), 20, 0)));
					});
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// A panel containing the list of players
		JPanel panel_players = new JPanel();
		panel_players.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane_gameAndPlayers.addTab(null, panel_players);
		panel_players.setLayout(new BorderLayout(0, 0));

		// A tabbed pane containing the tournament lists for each server
		this.tabbedPane_servers = new JTabbedPane();
		panel_players.add(this.tabbedPane_servers, BorderLayout.CENTER);

		// A label to vertically display the tab name 'Current game'
		JLabel label_tabGame = new JLabel("Aktuelles Spiel");
		label_tabGame.setUI(new VerticalLabelUI(true));
		tabbedPane_gameAndPlayers.setTabComponentAt(0, label_tabGame);

		// Keep the text of the label vertical after theme change
		this.updateOnRefresh.put(label_tabGame, () -> {
			label_tabGame.setUI(new VerticalLabelUI(true));
		});

		// A label to vertically display the tab name 'Player list'
		JLabel label_tabPlayers = new JLabel("Spielerliste");
		label_tabPlayers.setUI(new VerticalLabelUI(true));
		tabbedPane_gameAndPlayers.setTabComponentAt(1, label_tabPlayers);

		// Keep the text of the label vertical after theme change
		this.updateOnRefresh.put(label_tabPlayers, () -> {
			label_tabPlayers.setUI(new VerticalLabelUI(true));
		});

		// A scroll pane containing the list of available games
		JScrollPane scrollPane_servers = new JScrollPane();
		scrollPane_servers.setPreferredSize(new Dimension(this.getWidth() / 5, this.getHeight()));
		scrollPane_servers.getVerticalScrollBar().setUnitIncrement(16);

		// A panel displaying the buttons to select a game
		this.panel_servers = new JPanel();
		scrollPane_servers.setViewportView(this.panel_servers);
		this.panel_servers.setLayout(new BoxLayout(this.panel_servers, BoxLayout.Y_AXIS));

		// A split pane, splitting the list of available games and the currently viewed
		// game/the player list
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.15);

		// Add the halves to the split pane
		splitPane.setLeftComponent(scrollPane_servers);
		splitPane.setRightComponent(panel_gameAndPlayers);

		// Allow the icon for the server settings to be displayed accordingly to the dark or light theme
		this.updateOnRefresh.put(toggleButton_darkmode, () -> {
			try {
				BufferedImage imgSettings = ImageIO.read(getClass().getResource("/icons/settings.svg"));
				ServerCollapsible.ICON_IMAGE = GUI.getColoredImage(imgSettings, UIManager.getColor("TabbedPane.foreground"), ServerCollapsible.ICON_SIZE, 0);

				BufferedImage imgReload = ImageIO.read(getClass().getResource("/icons/refresh.svg"));
				ServerSettingsDialog.ICON_IMAGE = GUI.getColoredImage(imgReload, UIManager.getColor("TabbedPane.foreground"), ServerSettingsDialog.ICON_SIZE, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		// Add the split pane to the frame's content pane
		this.getContentPane().add(splitPane, BorderLayout.CENTER);

		try {
			this.setIconImage(ImageIO.read(getClass().getResource("/pieces/monkey_black.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return the text that is currently written in the server URL text field.
	 * 
	 * @return The URL/IP address of the server to connect to.
	 */
	public String getURL() {
		return this.textField_serverUrl.getText();
	}

	/**
	 * Return the text that is currently written in the server port text field.
	 * 
	 * @return The port of the server to connect to.
	 */
	public String getPort() {
		return this.textField_serverPort.getText();
	}

	/**
	 * Return the text that is currently written in the server token text field.
	 * 
	 * @return The safety token of the server to connect to.
	 */
	public String getToken() {
		return this.textField_serverToken.getText();
	}

	/**
	 * Returns whether or not dark mode is selected.
	 * 
	 * @return true if dark mode is selected, false otherwise
	 */
	public boolean getDarkmode() {
		return this.toggleButton_darkmode.isSelected();
	}

	/**
	 * Returns the tabbed pane containing the watched game and the tournament lists
	 * 
	 * @return The tabbed pane containing the watched game and the tournament lists
	 */
	public JTabbedPane getGameAndTournamentTabbedPane() {
		return this.tabbedPane_gameAndPlayers;
	}

	/**
	 * Returns the JPanel component that stores the connected servers.
	 * 
	 * @return The JPanel component that is used to store the list of collapsible server panels
	 */
	public JPanel getServerPanel() {
		return this.panel_servers;
	}

	/**
	 * Returns the JPanel component that displays the board.
	 * 
	 * @return The JPanel component that is used to display the watched board
	 */
	public JPanelBoard getBoardPanel() {
		return this.panel_board;
	}

	/**
	 * Assigns a new board history to the turn history panel.
	 * 
	 * @param board The new board history
	 */
	public void setHistoryBoard(BoardHistory board) {
		// Create a new board
		this.panel_board = new JPanelBoard(this, board);
		this.panel_board.setLayout(new BorderLayout(0, 0));
		this.panel_board.setTheme(this.toggleButton_pieceLook.isSelected() ? BoardTheme.CLASSIC : BoardTheme.HAPLORHINI);

		// The panel containing the panel for the turn buttons and the play button
		JPanel turnsAndButtonPanel = new JPanel();
		turnsAndButtonPanel.setLayout(new BorderLayout(0, 5));

		// The panel containing all the turn buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		this.panel_turns.setLayout(new BoxLayout(this.panel_turns, BoxLayout.Y_AXIS));

		// A scroll pane containing the list of all turns
		JScrollPane scrollPane_turns = new JScrollPane(this.panel_turns);
		scrollPane_turns.setPreferredSize(new Dimension(210, this.getHeight()));
		scrollPane_turns.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane_turns.setViewportView(this.panel_turns);

		// Add a button for each turn
		for (Turn turn : board.getAllTurns()) {
			this.addTurnButton(board, turn);
		}

		// Refresh the turns panel
		this.getTurnsPanel().repaint();

		// Add all the components
		buttonPanel.add(this.button_play, BorderLayout.SOUTH);
		turnsAndButtonPanel.add(scrollPane_turns, BorderLayout.CENTER);
		turnsAndButtonPanel.add(buttonPanel, BorderLayout.SOUTH);
		this.panel_boardAndButtons.add(this.panel_board, BorderLayout.CENTER);
		this.panel_game.add(turnsAndButtonPanel, BorderLayout.EAST);

		this.refreshUI();
	}

	/**
	 * Adds a new turn button to the turn list.
	 * 
	 * @param boardHistory
	 * @param turn
	 */
	public void addTurnButton(BoardHistory boardHistory, Turn turn) {
		String startY = "" + (char) (turn.getMove().getStart().getY() + 65);
		String startX = "" + (10 - turn.getMove().getStart().getX());
		String endY = "" + (char) (turn.getMove().getEnd().getY() + 65);
		String endX = "" + (10 - turn.getMove().getEnd().getX());
		String arrowY = "" + (char) (turn.getShot().getShotPosition().getY() + 65);
		String arrowX = "" + (10 - turn.getShot().getShotPosition().getX());

		JButtonTurn btn = new JButtonTurn(this, turn.getId().get(), startY + startX + " → " + endY + endX, "→ " + arrowY + arrowX, turn.getPlayerId(), this.panel_board);

		btn.addActionListener(e -> {
			boardHistory.moveToTurn(turn);
			this.panel_board.stop();
		});

		this.getTurnsPanel().add(btn);
		this.getTurnsPanel().add(Box.createVerticalStrut(2));
	}

	/**
	 * Returns the JPanel component that displays the board and the navigation buttons.
	 * 
	 * @return The JPanel component that is used to display the watched board and the navigation buttons
	 */
	public JPanel getBoardAndButtonsPanel() {
		return this.panel_boardAndButtons;
	}

	/**
	 * Returns the JPanel component that displays the board and the turn list.
	 * 
	 * @return The JPanel component that is used to display the watched board and the turn list
	 */
	public JPanel getGamePanel() {
		return this.panel_game;
	}

	/**
	 * Returns the JPanel component that displays the turn list.
	 * 
	 * @return The JPanel component that is used to display the turn list
	 */
	public JPanel getTurnsPanel() {
		return panel_turns;
	}

	/**
	 * Updates the button used to play animations depending on whether it should start or stop playing animations.
	 */
	public void updatePlayButton() {
		this.updateOnRefresh.get(this.button_play).onRefresh();
	}

	/**
	 * Returns the button used to jump to the first turn.
	 * 
	 * @return The JButton used to perform this action
	 */
	public JButton getFirstTurnButton() {
		return this.button_firstTurn;
	}

	/**
	 * Returns the button used to jump to the previous turn.
	 * 
	 * @return The JButton used to perform this action
	 */
	public JButton getPreviousTurnButton() {
		return this.button_previousTurn;
	}

	/**
	 * Returns the label displaying the current and total turns.
	 * 
	 * @return The label displaying the turns
	 */
	public JLabel getCurrentTurnLabel() {
		return label_currentTurn;
	}

	/**
	 * Returns the button used to jump to the next turn.
	 * 
	 * @return The JButton used to perform this action
	 */
	public JButton getNextTurnButton() {
		return this.button_nextTurn;
	}

	/**
	 * Returns the button used to jump to the last turn.
	 * 
	 * @return The JButton used to perform this action
	 */
	public JButton getLastTurnButton() {
		return this.button_lastTurn;
	}

	/**
	 * Return the tabbed pane displaying the tournament lists.
	 * 
	 * @return The tabbed pane displaying the tournament lists
	 */
	public JTabbedPane getServerTabbedPane() {
		return tabbedPane_servers;
	}

	/**
	 * Returns the label displaying the white player's name.
	 *
	 * @return The label that display's the white player's name.
	 */
	public JLabel getPlayerWhiteLabel() {
		return this.label_player1;
	}

	/**
	 * Returns the label displaying the black player's name.
	 *
	 * @return The label that display's the black player's name.
	 */
	public JLabel getPlayerBlackLabel() {
		return this.label_player2;
	}

	/**
	 * Adds a refresh operation to the onRefresh list.
	 *
	 * @param object      The object that this refresh operation is bound to (used to remove refreshable objects)
	 * @param refreshable The refresh operation to perform on an UI refresh
	 */
	public void addRefreshable(Object object, Refreshable refreshable) {
		this.updateOnRefresh.put(object, refreshable);
	}

	/**
	 * Removes a refresh operation from the onRefresh list.
	 *
	 * @param object The object that a refresh operation is bound to
	 */
	public void removeRefreshable(Object object) {
		this.updateOnRefresh.remove(object);
	}

	/**
	 * Refreshes certain components on the GUI since those components would lose their style on theme change.
	 */
	public void refreshUI() {
		SwingUtilities.updateComponentTreeUI(this);

		for (Refreshable refresher : this.updateOnRefresh.values())
			refresher.onRefresh();
	}

	/**
	 * Updates the GUI to either a dark or a light theme.
	 * 
	 * @param darkmode If true, the GUI will be updated to a dark theme, otherwise to a light theme
	 */
	private void setLookAndFeel(boolean darkmode) {
		try {
			UIManager.setLookAndFeel(darkmode ? new FlatDarkLaf() : new FlatLightLaf());
			this.refreshUI();
		} catch (Exception exception) {
			System.err.println("Failed to initialize " + (darkmode ? "dark" : "light") + " Look and Feel.");
		}
	}

	/**
	 * Gets a colored variant of an image
	 * 
	 * @param image  The image to get the color variant from
	 * @param color  The color used to tint the image
	 * @param size   The size of the image
	 * @param offset How much further to the center the image should be scaled
	 * 
	 * @return Returns an image tinted with a specified color and scaled to a specified size
	 */
	public static BufferedImage getColoredImage(Image image, Color color, int size, int offset) {
		BufferedImage coloredImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = coloredImage.createGraphics();
		g.drawImage(image.getScaledInstance(size - 2 * offset, size - 2 * offset, Image.SCALE_SMOOTH), offset, offset, null);
		g.setComposite(AlphaComposite.SrcAtop);
		g.setColor(color);
		g.fillRect(0, 0, size, size);
		g.dispose();

		return coloredImage;
	}
}
