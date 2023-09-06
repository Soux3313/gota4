package observer.view.dialogs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.ui.FlatRoundBorder;

import observer.controller.ServerSettingsDialogController;
import observer.network.RemoteGame;
import observer.network.RemoteServer;
import observer.view.utils.JTextPaneGame;

@SuppressWarnings("serial")
public class ServerSettingsDialog extends JDialog {
	// The icon properties for the refresh icon
	public static BufferedImage ICON_IMAGE;
	public static int ICON_SIZE = 16;

	// A panel containing the buttons for visible games
	private JPanel panel_activatedGames;

	// A panel containing the buttons for hidden games
	private JPanel panel_deactivatedGames;

	// A combo box used to filter between running or ended games
	private JComboBox<String> comboBox_filter;

	// The server
	private RemoteServer server;

	// The dialog controller
	private ServerSettingsDialogController controller;

	/**
	 * The dialog used to change server properties
	 * 
	 * @param server
	 * @param controller
	 */
	public ServerSettingsDialog(RemoteServer server, ServerSettingsDialogController controller) {
		// The server to change properties from
		this.server = server;

		// The controller
		this.controller = controller;

		// Set a title to this dialog
		this.setTitle("Serveroptionen (" + server.toString() + ")");
		this.setModalityType(ModalityType.APPLICATION_MODAL);

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.addWindowListener(controller);

		// Set a minimum size
		this.setSize(640, 360);
		this.setMinimumSize(this.getSize());
		this.getContentPane().setLayout(new BorderLayout(0, 0));

		// A toolbar at the bottom to confirm or disconnect
		JToolBar toolBar_serverInformation = new JToolBar();
		toolBar_serverInformation.setFloatable(false);
		this.getContentPane().add(toolBar_serverInformation, BorderLayout.SOUTH);

		// The button used to confirm the settings
		JButton button_confirm = new JButton("Bestätigen");
		button_confirm.setActionCommand("CONFIRM");
		button_confirm.addActionListener(controller);
		button_confirm.setFocusable(false);
		button_confirm.setBorder(new FlatRoundBorder());
		button_confirm.setBorderPainted(true);
		button_confirm.setAlignmentX(CENTER_ALIGNMENT);

		// The button used to disconnect from the server
		JButton button_disconnect = new JButton("Verbindung trennen");
		button_disconnect.setActionCommand("DISCONNECT");
		button_disconnect.addActionListener(controller);
		button_disconnect.setFocusable(false);
		button_disconnect.setBorderPainted(true);
		button_disconnect.setAlignmentX(CENTER_ALIGNMENT);

		// Add the confirm button to the toolbar
		toolBar_serverInformation.add(Box.createHorizontalStrut(2));
		toolBar_serverInformation.add(Box.createHorizontalGlue());
		toolBar_serverInformation.add(button_confirm);
		toolBar_serverInformation.add(Box.createHorizontalGlue());

		// Add the disconnect button to the toolbar
		int width1 = 98;// this.textField_token.getPreferredSize().width;
		int width2 = button_disconnect.getPreferredSize().width;
		toolBar_serverInformation.add(Box.createHorizontalStrut((width1 - width2) * 3));
		toolBar_serverInformation.add(button_disconnect);
		toolBar_serverInformation.add(Box.createHorizontalStrut(2));

		// A panel containing the descriptions for the lists and the lists themselves
		JPanel panel_descriptionsAndGames = new JPanel();
		panel_descriptionsAndGames.setLayout(new BorderLayout());
		this.getContentPane().add(panel_descriptionsAndGames);

		// A panel containing the descriptions for the lists
		JPanel panel_descriptions = new JPanel();
		panel_descriptions.setLayout(new GridLayout(0, 3));
		panel_descriptions.setBorder(new EmptyBorder(2, 2, 2, 2));
		panel_descriptionsAndGames.add(panel_descriptions, BorderLayout.NORTH);

		// Add descriptions above the lists
		JLabel label_activatedGames = new JLabel("Anzuzeigende Spiele");
		label_activatedGames.setHorizontalAlignment(JLabel.CENTER);
		label_activatedGames.setVerticalAlignment(JLabel.BOTTOM);
		JLabel label_deactivatedGames = new JLabel("Verborgene Spiele");
		label_deactivatedGames.setHorizontalAlignment(JLabel.CENTER);
		label_deactivatedGames.setVerticalAlignment(JLabel.BOTTOM);

		// A panel containing the combo box for filtering
		JPanel panel_filter = new JPanel();
		panel_filter.setLayout(new BoxLayout(panel_filter, BoxLayout.X_AXIS));

		// The filter combo box
		this.comboBox_filter = new JComboBox<String>();
		this.comboBox_filter.addItem("Alle Spiele");
		this.comboBox_filter.addItem("Laufende Spiele");
		this.comboBox_filter.addItem("Beendete Spiele");
		this.comboBox_filter.setMaximumSize(comboBox_filter.getPreferredSize());
		this.comboBox_filter.setAlignmentX(CENTER_ALIGNMENT);
		this.comboBox_filter.setActionCommand("FILTER");
		this.comboBox_filter.addActionListener(controller);

		// A button to refresh the lists
		JButton button_refresh = new JButton();
		button_refresh.setActionCommand("REFRESH");
		button_refresh.addActionListener(controller);
		button_refresh.setIcon(new ImageIcon(ICON_IMAGE));
		button_refresh.setBorderPainted(false);
		button_refresh.setContentAreaFilled(false);
		button_refresh.setFocusPainted(false);
		button_refresh.setOpaque(false);
		button_refresh.setFocusable(false);

		button_refresh.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				button_refresh.setContentAreaFilled(true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button_refresh.setContentAreaFilled(false);
			}
		});

		panel_filter.add(Box.createHorizontalStrut(button_refresh.getPreferredSize().width));
		panel_filter.add(Box.createHorizontalGlue());
		panel_filter.add(this.comboBox_filter);
		panel_filter.add(Box.createHorizontalGlue());
		panel_filter.add(button_refresh);

		panel_descriptions.add(label_deactivatedGames);
		panel_descriptions.add(panel_filter);
		panel_descriptions.add(label_activatedGames);

		// A panel containing the lists for the game buttons
		JPanel panel_games = new JPanel();
		panel_games.setLayout(new GridLayout());
		panel_descriptionsAndGames.add(panel_games, BorderLayout.CENTER);

		this.panel_activatedGames = new JPanel();
		this.panel_activatedGames.setLayout(new BoxLayout(this.panel_activatedGames, BoxLayout.Y_AXIS));

		JScrollPane scrollPane_activatedGames = new JScrollPane();
		scrollPane_activatedGames.setViewportView(this.panel_activatedGames);

		this.panel_deactivatedGames = new JPanel();
		this.panel_deactivatedGames.setLayout(new BoxLayout(this.panel_deactivatedGames, BoxLayout.Y_AXIS));

		JScrollPane scrollPane_deactivatedGames = new JScrollPane();
		scrollPane_deactivatedGames.setViewportView(this.panel_deactivatedGames);

		JPanel panel_buttons = new JPanel();
		panel_buttons.setLayout(new BoxLayout(panel_buttons, BoxLayout.Y_AXIS));

		// A button to move all games to the right
		JButton button_addAll = new JButton(">>");
		button_addAll.setActionCommand("ADD_ALL");
		button_addAll.addActionListener(controller);
		button_addAll.setFocusable(false);
		button_addAll.setAlignmentX(CENTER_ALIGNMENT);

		// A button to move all games to the left
		JButton button_removeAll = new JButton("<<");
		button_removeAll.setActionCommand("REMOVE_ALL");
		button_removeAll.addActionListener(controller);
		button_removeAll.setFocusable(false);
		button_removeAll.setAlignmentX(CENTER_ALIGNMENT);

		panel_buttons.add(Box.createVerticalGlue());
		panel_buttons.add(button_addAll);
		panel_buttons.add(Box.createVerticalStrut(10));
		panel_buttons.add(button_removeAll);
		panel_buttons.add(Box.createVerticalGlue());

		panel_games.add(scrollPane_deactivatedGames);
		panel_games.add(panel_buttons);
		panel_games.add(scrollPane_activatedGames);

		this.updateGameButtons();
		this.getContentPane().requestFocusInWindow();
	}

	/**
	 * Update the game buttons to display winners
	 */
	public void updateGameButtons() {
		// Add all GameButtons
		for (RemoteGame remoteGame : this.server.getRemoteGames()) {
			JTextPaneGame button = remoteGame.getGameButton();
			button.updateButtonText(true);
			button.removeMouseListener(this.controller);
			button.addMouseListener(this.controller);

			if (remoteGame.isActive()) {
				this.panel_activatedGames.add(button);
			} else {
				this.panel_deactivatedGames.add(button);
			}
		}
		this.pack();
		this.repaint();
	}

	/**
	 * Returns the panel displaying all hidden games
	 */
	public JPanel getDeactivatedGamesPanel() {
		return this.panel_deactivatedGames;
	}

	/**
	 * Returns the panel displaying all visible games
	 */
	public JPanel getActivatedGamesPanel() {
		return this.panel_activatedGames;
	}

	/**
	 * Returns the combo box to filter games
	 */
	public JComboBox<?> getComboBox() {
		return comboBox_filter;
	}
}
