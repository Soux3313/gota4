package observer.controller;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import model.jsonstruct.ReducedGameStruct;
import model.player.Turn;
import observer.exceptions.ConnectionException;
import observer.exceptions.FetchGameException;
import observer.exceptions.FetchGamesException;
import observer.exceptions.UnexpectedServerResponseException;
import observer.model.BoardHistory;
import observer.network.Connection;
import observer.network.GamesFetcher;
import observer.network.RemoteGame;
import observer.network.RemoteServer;
import observer.view.GUI;
import observer.view.text.NoTextSelectionCaret;
import observer.view.utils.JTextPaneGame;
import observer.view.utils.ServerCollapsible;

public class GUIController extends Controller {
	// The GUI
	private final GUI view;

	// A list storing all connected servers
	private final ArrayList<RemoteServer> serverList = new ArrayList<>();
	private final Lock serverListLock = new ReentrantLock();

	// The currently watched game
	private Optional<RemoteGame> watchedGame = Optional.empty();
	private final Lock watchedGameLock = new ReentrantLock();

	// A timer to update the currently watched game
	private Timer updateTimer;

	private static TimerTask wrapTT(Runnable r) {
		return new TimerTask() {
			@Override
			public void run() {
				r.run();
			}
		};
	}

	/**
	 * The controller that handles all of the GUI's functionality
	 */
	public GUIController() {
		// Create a new GUI and set it visible
		this.view = new GUI(this);
		this.view.setVisible(true);

		// Update the winning players periodically every 1 second
		Timer tournamentUpdateTimer = new Timer(true);
		tournamentUpdateTimer.schedule(wrapTT(() -> {
			ArrayList<RemoteServer> removeBuffer = new ArrayList<>();
			this.serverListLock.lock();
			try {
				this.serverList.forEach(server -> {
					ArrayList<ReducedGameStruct> gameList;
					try {
						gameList = GamesFetcher.fetchGames(server.getConnection());
						for (ReducedGameStruct x : gameList) {
							// Find remote games
							RemoteGame game = new RemoteGame(server, x.gameId, x.toString());

							// Set player names on button
							game.getGameButton().setNames(x.players[0].name, x.players[1].name);
							game.getGameButton().setWinningPlayerId(x.winningPlayer);

							// Only add when not already there
							server.addRemoteGame(game);
						}
					} catch (UnexpectedServerResponseException | FetchGamesException | FetchGameException e) {
						SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this.view, "Es gab einen Fehler beim Abrufen der Spieldaten vom Server " + server.getConnection().toString() + ". Bitte verbinden Sie sich erneut mit diesem Server.", "Error", JOptionPane.ERROR_MESSAGE));
						removeBuffer.add(server);
					}

					// Update the tournament list
					server.getTournamentTable().updateTable();
				});
			} finally {
				this.serverListLock.unlock();
			}

			for (RemoteServer s : removeBuffer) {
				this.removeRemoteServer(s);
			}
		}), 0, 1000);
	}

	/**
	 * Update the currently watched game
	 */
	public void run() {
		// Main fetching loop
		this.watchedGameLock.lock();
		try {
			if (this.watchedGame.isPresent() && !this.watchedGame.get().isGameOver() && this.watchedGame.get().isActive()) {
				try {
					this.watchedGame.get().update();

					// If the game is over, update the game's button and display an info dialog to inform about the winning player
					if (this.watchedGame.get().isGameOver()) {
						// Stop the update timer since the game doesn't need to be updated anymore
						this.updateTimer.cancel();

						// Update the game's button
						this.watchedGame.get().getGameButton().setWinningPlayerId(this.watchedGame.get().getWinningPlayerId().get());
						this.watchedGame.get().getGameButton().getParent().revalidate();

						// Show the info dialog
						SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this.view, this.watchedGame.get().getWinningPlayerName().get() + " hat das Spiel gewonnen!", "Info", JOptionPane.INFORMATION_MESSAGE));
					}
				} catch (UnexpectedServerResponseException | FetchGameException e) {
					SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this.view, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
					this.watchedGame = Optional.empty();
				}
			}

			// Try repainting/animating the board
			try {
				this.view.getBoardPanel().repaint();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
			this.watchedGameLock.unlock();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		BoardHistory boardHistory = (BoardHistory) event.getSource();
		Turn turn = (Turn) event.getNewValue();

		// Add a new turn button when the turn history changes
		if (event.getPropertyName().equals("TurnAdded")) {
			this.view.addTurnButton(boardHistory, turn);
			// System.out.println("Turn added: " + turn.toString());
			this.view.getTurnsPanel().repaint();
		}

		// Update the board if a turn was applied
		if (event.getPropertyName().equals("TurnApplied")) {
			this.view.getBoardPanel().update();
		}

		// Update the turn label and the turn buttons to display accordingly to the selected turn
		this.updateTurnLabelAndButtons(boardHistory, turn);

		// Update the activeness of the turn navigation buttons
		this.updateNavigationButtons();
	}

	/**
	 * Update the turn label and buttons depending on the given turn and turn history
	 * 
	 * @param boardHistory
	 * @param turn
	 */
	private void updateTurnLabelAndButtons(BoardHistory boardHistory, Turn turn) {
		// Make every turn button have a gray background
		Arrays.stream(view.getTurnsPanel().getComponents())
				.filter(c -> c instanceof JButton)
				.forEach(c -> c.setBackground(UIManager.getColor("Button.background")));

		// A default id for the selected turn
		int id = 0;

		// Check if a turn is specified and get the turn id depending on whether the turn is the latest turn in the game or the currently watched turn
		if (turn != null) {
			if (boardHistory.hasPrevTurn() && turn.getId().get() == boardHistory.getLastTurn().getId().get()) {
				id = turn.getId().get();
			} else if (boardHistory.hasPrevTurn()) {
				id = boardHistory.getLastTurn().getId().get();
			}
		}

		// Update the turn label and the selected turn button
		final String buttonIdentifier = id + ": ";
		this.view.getCurrentTurnLabel().setText(id + "/" + boardHistory.getAllTurns().size());
		Arrays.stream(this.view.getTurnsPanel().getComponents())
				.filter(c -> c instanceof JButton && ((JButton) c).getText().startsWith(buttonIdentifier))
				.forEach(c -> c.setBackground(UIManager.getColor("List.selectionBackground")));
	}

	/**
	 * Update the turn navigation buttons
	 */
	private void updateNavigationButtons() {
		// Disable all buttons by default
		view.getNextTurnButton().setEnabled(false);
		view.getLastTurnButton().setEnabled(false);
		view.getPreviousTurnButton().setEnabled(false);
		view.getFirstTurnButton().setEnabled(false);

		// Enable the buttons again depending on whether or not they have a previous and/or next turn
		watchedGameLock.lock();
		try {
			if (watchedGame.isPresent() && watchedGame.get().getIntermediateBoard() != null) {
				if (watchedGame.get().getIntermediateBoard().hasNextTurn()) {
					view.getNextTurnButton().setEnabled(true);
					view.getLastTurnButton().setEnabled(true);
				}

				if (watchedGame.get().getIntermediateBoard().hasPrevTurn()) {
					view.getPreviousTurnButton().setEnabled(true);
					view.getFirstTurnButton().setEnabled(true);
				}
			}
		} finally {
			watchedGameLock.unlock();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Check if a connection to a server should be established
		if (e.getActionCommand().equals("CONNECT")) {
			try {
				// Get the server address from the GUI
				String url = this.view.getURL();
				int port = Integer.parseInt(this.view.getPort());
				String token = this.view.getToken();

				// Create a new connection
				Connection con = new Connection(url, port, token);
				RemoteServer server = new RemoteServer(con, this);

				// Check if a connection to the server already exists
				boolean alreadyConnected;
				this.serverListLock.lock();
				try {
					alreadyConnected = serverList.stream().anyMatch(n -> n.equals(server));
				} finally {
					this.serverListLock.unlock();
				}

				if (alreadyConnected) {
					SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this.view, "Der Server ist bereits in der Liste!", "Error", JOptionPane.ERROR_MESSAGE));
				} else {
					this.addRemoteServer(server);
					this.view.revalidate();
				}
			} catch (NumberFormatException exception) {
				SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this.view, "Ihre Eingaben sind fehlerhaft.", "Error", JOptionPane.ERROR_MESSAGE));
			} catch (ConnectionException exception) {
				SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this.view, "Der Server konnte nicht gefunden werden.", "Error", JOptionPane.ERROR_MESSAGE));
			}
		}

		watchedGameLock.lock();
		try {
			// If a game is being watched navigate through the turn history
			if (this.watchedGame != null && this.watchedGame.isPresent()) {
				if (((JButton) e.getSource()).getActionCommand().equals("NEXT_TURN")) {
					watchedGame.get().getIntermediateBoard().toNextTurn();
				}

				if (((JButton) e.getSource()).getActionCommand().equals("PREV_TURN")) {
					watchedGame.get().getIntermediateBoard().toPreviousTurn();
				}

				if (((JButton) e.getSource()).getActionCommand().equals("LAST_TURN")) {
					watchedGame.get().getIntermediateBoard().toLastTurn();
				}

				if (((JButton) e.getSource()).getActionCommand().equals("FIRST_TURN")) {
					watchedGame.get().getIntermediateBoard().toFirstTurn();
				}
			}
		} finally {
			watchedGameLock.unlock();
		}
	}

	/**
	 * Add a new server to the GUI
	 * 
	 * @param server
	 */
	public void addRemoteServer(RemoteServer server) {
		// Add the server to list
		this.serverListLock.lock();
		try {
			this.serverList.add(server);
		} finally {
			this.serverListLock.unlock();
		}

		// Add a tournament list for this server
		this.view.getServerTabbedPane().addTab(server.getConnection().toString(), server.getTournamentTable());

		// Create a new list for the game buttons
		ServerCollapsible collapsible = server.getServerCollapsible();
		collapsible.setTitle(server.getConnection().toString());
		collapsible.toggleVisibility();

		// Refresh the server's game buttons on theme change
		this.view.addRefreshable(server, () -> {
			for (RemoteGame game : server.getRemoteGames()) {
				JTextPaneGame gameButton = game.getGameButton();

				SwingUtilities.updateComponentTreeUI(gameButton);
				gameButton.setBorder(UIManager.getBorder("Button.border"));
				gameButton.setBackground(UIManager.getColor("Button.background"));
				gameButton.setCaret(new NoTextSelectionCaret(game.getGameButton()));
				gameButton.updateButtonText(!gameButton.getText().contains("\n"));
			}
		});

		// Try fetching new and updating existing games
		try {
			ArrayList<ReducedGameStruct> gameList = GamesFetcher.fetchGames(server.getConnection());
			for (ReducedGameStruct x : gameList) {
				// Find remote games
				RemoteGame game = new RemoteGame(server, x.gameId, x.toString());

				// Set player names on button
				game.getGameButton().setNames(x.players[0].name, x.players[1].name);
				game.getGameButton().setWinningPlayerId(x.winningPlayer);

				// Only add when not already there
				server.addRemoteGame(game);
			}
		} catch (FetchGamesException | FetchGameException | UnexpectedServerResponseException e) {
			// If not all data could be fetched, remove this server
			this.removeRemoteServer(server);
			SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this.view, "Es konnten nicht alle Daten vom Server abgerufen werden. Möglicherweise wurde ein falsches Token verwendet.", "Error", JOptionPane.ERROR_MESSAGE));
			return;
		}

		// Update the tournament list
		server.getTournamentTable().updateTable();

		// Add the game button list to the GUI
		this.view.getServerPanel().add(collapsible);
		this.view.refreshUI();
	}

	/**
	 * Remove a connection to a server
	 * 
	 * @param server
	 */
	public void removeRemoteServer(RemoteServer server) {
		// Remove the server from the server list
		this.serverListLock.lock();
		try {
			this.serverList.remove(server);
		} finally {
			this.serverListLock.unlock();
		}

		this.view.removeRefreshable(server);

		// Remove the tournament list
		if (this.view.getServerTabbedPane().indexOfTab(server.getConnection().toString()) >= 0) {
			this.view.getServerTabbedPane()
					.removeTabAt(this.view.getServerTabbedPane().indexOfTab(server.getConnection().toString()));
		}

		// If the currently watched game is from this server, reset it
		watchedGameLock.lock();
		try {
			if (this.watchedGame.isPresent() && this.watchedGame.get().getRemoteServer().equals(server)) {
				this.resetWatchedGame();
			}
		} finally {
			watchedGameLock.unlock();
		}

		// Need to deactivate all games, so the board knows that it shouldn't display any of those games anymore
		for (RemoteGame game : server.getRemoteGames()) {
			game.setActive(false);
		}

		// Remove the server's button list from the GUI
		this.view.getServerPanel().remove(server.getServerCollapsible());
		this.view.refreshUI();
	}

	/**
	 * Set the game to watch
	 * 
	 * @param game
	 */
	public void setWatchedGame(RemoteGame game) {
		// Switch to watched game tab
		this.view.getGameAndTournamentTabbedPane().setSelectedIndex(0);

		this.watchedGameLock.lock();
		try {
			// Return if the currently watched game is the game to set watched
			if (this.watchedGame.isPresent() && game.equals(this.watchedGame.get())) {
				return;
			}

			// Reset the currently watched game before setting a new one
			this.resetWatchedGame();

			// Cancel the update timer for the previously watched game
			if (this.updateTimer != null) {
				this.updateTimer.cancel();
			}

			// Set the new watched game
			this.watchedGame = Optional.of(game);

			// Set the current tab in the tournament list to be the tab of this game's server
			this.view.getServerTabbedPane().setSelectedIndex(this.view.getServerTabbedPane().indexOfTab(this.watchedGame.get().getRemoteServer().getConnection().toString()));

			// Update the player name labels
			this.view.getPlayerWhiteLabel().setText(game.getGameButton().getPlayer1Name());
			this.view.getPlayerBlackLabel().setText(game.getGameButton().getPlayer2Name());

			// Set a new turn history
			this.view.setHistoryBoard(watchedGame.get().getIntermediateBoard());
			game.getIntermediateBoard().removePropertyChangeListener(this);
			game.getIntermediateBoard().addPropertyChangeListener(this);
			// System.out.println("Listener added");

			// Update the turn list depending on whether or not a previous turn exists
			if (game.getIntermediateBoard().hasPrevTurn())
				this.updateTurnLabelAndButtons(game.getIntermediateBoard(), game.getIntermediateBoard().getLastTurn());
			else {
				this.updateTurnLabelAndButtons(game.getIntermediateBoard(), null);
			}

			// Update the turn navigation buttons
			this.updateNavigationButtons();

			try {
				// Update the Board
				game.update();

				// Start a new timer if the game is not over
				if (!game.isGameOver()) {
					updateTimer = new Timer(true);
					updateTimer.schedule(wrapTT(this::run), 0, 100);
				}
			} catch (UnexpectedServerResponseException | FetchGameException e) {
				e.printStackTrace();
			}
		} finally {
			this.watchedGameLock.unlock();
		}
	}

	/**
	 * Reset the currently watched game
	 */
	public void resetWatchedGame() {
		watchedGameLock.lock();
		try {
			// Remove the property change listener from the game's board
			this.watchedGame.ifPresent(remoteGame -> remoteGame.getIntermediateBoard().removePropertyChangeListener(this));

			// Reset the game
			this.watchedGame = Optional.empty();
		} finally {
			watchedGameLock.unlock();
		}

		// Reset the labels
		this.view.getPlayerWhiteLabel().setText("");
		this.view.getPlayerBlackLabel().setText("");
		this.view.getCurrentTurnLabel().setText("0/0");

		// Remove the board from the GUI
		if (this.view.getBoardPanel() != null)
			this.view.getBoardAndButtonsPanel().remove(this.view.getBoardPanel());

		// Remove the turn list from the GUI
		if (((BorderLayout) this.view.getGamePanel().getLayout()).getLayoutComponent(BorderLayout.EAST) != null) {
			this.view.getGamePanel().remove(((BorderLayout) this.view.getGamePanel().getLayout()).getLayoutComponent(BorderLayout.EAST));
		}
		this.view.getTurnsPanel().removeAll();

		// Disable the navigation buttons
		this.view.getNextTurnButton().setEnabled(false);
		this.view.getLastTurnButton().setEnabled(false);
		this.view.getPreviousTurnButton().setEnabled(false);
		this.view.getFirstTurnButton().setEnabled(false);
	}

	/**
	 * Returns the GUI
	 * 
	 * @return The GUI
	 */
	public GUI getView() {
		return this.view;
	}

	public boolean isWatchedGameActive() {
		watchedGameLock.lock();
		try {
			return this.watchedGame.map(RemoteGame::isActive).orElse(false);
		} finally {
			watchedGameLock.unlock();
		}
	}
}
