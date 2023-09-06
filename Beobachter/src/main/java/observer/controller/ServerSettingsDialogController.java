package observer.controller;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import model.jsonstruct.ReducedGameStruct;
import observer.exceptions.FetchGameException;
import observer.exceptions.FetchGamesException;
import observer.exceptions.UnexpectedServerResponseException;
import observer.network.GamesFetcher;
import observer.network.RemoteGame;
import observer.network.RemoteServer;
import observer.view.dialogs.ServerSettingsDialog;
import observer.view.utils.JTextPaneGame;

public class ServerSettingsDialogController extends Controller {
	private final GUIController guiController;
	private final ServerSettingsDialog dialog;
	private final RemoteServer remoteServer;

	public ServerSettingsDialogController(RemoteServer remoteServer, GUIController guiController) {
		this.guiController = guiController;
		this.remoteServer = remoteServer;

		// Refresh games
		this.addGames();

		// Create a new dialog
		dialog = new ServerSettingsDialog(remoteServer, this);

		// Center the window in the middle of the GUI
		dialog.setLocationRelativeTo(guiController.getView());

		// Show an info message to inform about access tokens
		// if (dialog.getToken().isBlank())
		// JOptionPane.showMessageDialog(dialog, "Geben Sie ein gültiges Zugangstoken an, um Spiele beobachten zu können.", "Info", JOptionPane.INFORMATION_MESSAGE);

		// Enable the dialog
		dialog.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		// Disconnect from this server
		if (event.getActionCommand().equals("DISCONNECT")) {
			this.guiController.removeRemoteServer(this.remoteServer);
			dialog.dispose();
		}

		// Confirm the settings
		if (event.getActionCommand().equals("CONFIRM")) {
			this.close();
		}

		// Refresh the games
		if (event.getActionCommand().equals("REFRESH")) {
			this.addGames();
			filter(this.dialog.getComboBox());
			dialog.updateGameButtons();
		}

		// Move all buttons to the activated list
		if (event.getActionCommand().equals("ADD_ALL")) {
			for (RemoteGame game : this.remoteServer.getRemoteGames()) {
				JTextPaneGame button = game.getGameButton();

				if (!game.isActive() && button.isVisible()) {
					button.getRemoteGame().setActive(true);
					dialog.getActivatedGamesPanel().add(button);
				}
			}

			dialog.revalidate();
			dialog.repaint();
		}

		// Move all buttons to the deactivated list
		if (event.getActionCommand().equals("REMOVE_ALL")) {
			for (RemoteGame game : this.remoteServer.getRemoteGames()) {
				JTextPaneGame button = game.getGameButton();

				if (game.isActive() && button.isVisible()) {
					button.getRemoteGame().setActive(false);
					dialog.getDeactivatedGamesPanel().add(button);
				}
			}

			dialog.revalidate();
			dialog.repaint();
		}

		// Filter the games
		if (event.getActionCommand().equals("FILTER") && event.getSource() instanceof JComboBox<?>) {
			filter((JComboBox<?>) event.getSource());
		}
	}

	@Override
	public void mousePressed(MouseEvent event) {
		if (event.getSource() instanceof JTextPaneGame) {
			// Get the button
			JTextPaneGame button = ((JTextPaneGame) event.getSource());
			button.setBackground(UIManager.getColor("Button.background"));

			// Move the button between the to lists
			if (button.getRemoteGame().isActive()) {
				button.getRemoteGame().setActive(false);
				dialog.getDeactivatedGamesPanel().add(button);
			} else {
				button.getRemoteGame().setActive(true);
				dialog.getActivatedGamesPanel().add(button);
			}

			// Update the dialog
			dialog.revalidate();
			dialog.repaint();
		}
	}

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
	public void windowClosing(WindowEvent event) {
		close();
	}

	/**
	 * Filter the games based on the value of the combo box
	 * 
	 * @param comboBox
	 */
	private void filter(JComboBox<?> comboBox) {
		for (RemoteGame game : this.remoteServer.getRemoteGames()) {
			JTextPaneGame button = game.getGameButton();
			button.setVisible(true);

			boolean gameOver = button.getRemoteGame().isGameOver() || button.getWinningPlayerId() != -1;

			if (comboBox.getSelectedIndex() == 1/* .getSelectedItem().equals("Laufende Spiele") */ && gameOver) {
				button.setVisible(false);
			} else if (comboBox.getSelectedIndex() == 2/* .getSelectedItem().equals("Beendete Spiele") */ && !gameOver) {
				button.setVisible(false);
			}
		}
	}

	/**
	 * Add new games to the server and update existing ones
	 */
	private void addGames() {
		// Fetch data
		ArrayList<ReducedGameStruct> gameList = null;
		try {
			gameList = GamesFetcher.fetchGames(remoteServer.getConnection());
			for (ReducedGameStruct x : gameList) {
				// Find remote games
				RemoteGame game = new RemoteGame(remoteServer, x.gameId, x.toString());

				// Set player names on button
				game.getGameButton().setNames(x.players[0].name, x.players[1].name);
				game.getGameButton().setWinningPlayerId(x.winningPlayer);

				// Only add when not already there
				remoteServer.addRemoteGame(game);
			}
		} catch (UnexpectedServerResponseException e) {
			SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(guiController.getView(), "Die Antwort des Servers war fehlerhaft.", "Error", JOptionPane.ERROR_MESSAGE));
		} catch (FetchGamesException | FetchGameException e) {
			SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(guiController.getView(), "Das Spiel konnte nicht hinzugefügt werden.", "Error", JOptionPane.ERROR_MESSAGE));
			e.printStackTrace();
		}
	}

	/**
	 * Close the dialog
	 */
	private void close() {
		// Remove this controller from all Buttons:
		for (RemoteGame remoteGame : remoteServer.getRemoteGames()) {
			remoteGame.getGameButton().removeMouseListener(this);
			remoteGame.getGameButton().updateButtonText(false);

			// Add the game buttons to the main list
			if (remoteGame.isActive()) {
				remoteGame.getGameButton().setVisible(true);
				remoteServer.getServerCollapsible().getGamesPanel().add(Box.createVerticalStrut(1));
				remoteServer.getServerCollapsible().getGamesPanel().add(remoteGame.getGameButton());
				remoteServer.getServerCollapsible().getGamesPanel().add(Box.createVerticalStrut(1));
			}
		}

		if (this.guiController.isWatchedGameActive()) {
			this.guiController.resetWatchedGame();
		}

		this.remoteServer.getTournamentTable().updateTable();

		// this.remoteServer.getConnection().setToken(dialog.getToken());

		// Refresh corresponding ServerCollapsable
		this.remoteServer.getServerCollapsible().update();

		// Close dialog:
		dialog.dispose();
	}

	/**
	 * Returns the GUI controller of the main GUI
	 * 
	 * @return The GUI controller of the main GUI
	 */
	public GUIController getGUIController() {
		return this.guiController;
	}
}
