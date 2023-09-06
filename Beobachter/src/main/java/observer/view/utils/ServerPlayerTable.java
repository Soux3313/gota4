package observer.view.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import model.player.Player;
import observer.network.RemoteGame;
import observer.network.RemoteServer;

@SuppressWarnings("serial")
public class ServerPlayerTable extends JScrollPane {
	private final JTable playerTable;
	private final RemoteServer server;

	public ServerPlayerTable(RemoteServer server) {
		this.server = server;
		
		// Center renderer to render text center aligned
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);

		// A table to display player data (name, wins, defeats and running games)
		playerTable = new JTable(new DefaultTableModel(new Object[] { "Name", "Gewonnen", "Verloren", "Laufend" }, 0)) {
			@Override
			public boolean isCellEditable(int row, int column) {
				// Prevent user from editing any cell in the table
				return false;
			}
		};

		playerTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		playerTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		playerTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(playerTable.getModel());
		playerTable.setRowSorter(sorter);

		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
		sortKeys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		
		// Remove unnecessary vertical space from table
		playerTable.setPreferredScrollableViewportSize(playerTable.getPreferredSize());

		// Add table to a scroll pane to allow column names to be displayed
		this.setViewportView(playerTable);
	}

	/**
	 * Updates the tournament list by recalculating each win, defeat and the running games
	 */
	public synchronized void updateTable() {
		// Add example value to table
		DefaultTableModel model = (DefaultTableModel) playerTable.getModel();

		for (int i = model.getRowCount() - 1; i > -1; i--)
			model.removeRow(i);

		HashMap<Integer, TournamentPlayer> players = new HashMap<Integer, TournamentPlayer>();

		for (RemoteGame game : server.getRemoteGames()) {
			Player player1 = game.getPlayer1();
			Player player2 = game.getPlayer2();

			if (player1 == null || player2 == null)
				continue;
			
			int id1 = player1.getPlayerId().get();
			int id2 = player2.getPlayerId().get();

			if (!players.containsKey(id1))
				players.put(id1, new TournamentPlayer(player1));
			
			if (!players.containsKey(id2))
				players.put(id2, new TournamentPlayer(player2));

			if (game.getWinningPlayerId().isPresent()) {
				if (game.getWinningPlayerId().get() == 0) {
					players.get(id1).wins++;
					players.get(id2).defeats++;
				} else if (game.getWinningPlayerId().get() == 1) {
					players.get(id1).defeats++;
					players.get(id2).wins++;
				}
			} else {
				players.get(id1).runningGames++;
				players.get(id2).runningGames++;
			}
		}

		players.forEach((k, v) -> model.addRow(new Object[] { v.player.getName(), v.wins, v.defeats, v.runningGames }));
	}

	private static class TournamentPlayer {
		public Player player;
		public int wins = 0;
		public int defeats = 0;
		public int runningGames = 0;
		
		public TournamentPlayer(Player player) {
			this.player = player;
		}
	}
}
