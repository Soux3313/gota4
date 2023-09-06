package observer.network;

import java.net.URI;
import java.util.ArrayList;

import observer.controller.GUIController;
import observer.view.utils.ServerCollapsible;
import observer.view.utils.ServerPlayerTable;

public class RemoteServer {
    private final ArrayList<RemoteGame> remoteGames = new ArrayList<>();
    private final Connection connection;
    private final ServerCollapsible serverCollapsible;
    private final ServerPlayerTable tournamentTab;

    public RemoteServer(Connection connection, GUIController controller) {
        this.connection = connection;
        this.serverCollapsible = new ServerCollapsible(this, controller);
        this.tournamentTab = new ServerPlayerTable(this);
    }

    public void addRemoteGame(RemoteGame game) {
        if(remoteGames.stream().noneMatch(n -> n.equals(game))) {
            remoteGames.add(game);
        } else {
        	remoteGames.stream().filter(n -> n.equals(game)).forEach(n -> {
        		if (game.getWinningPlayerId().isPresent() && n.getWinningPlayerId().isEmpty()) {
        			n.setWinningPlayerId(game.getWinningPlayerId().get());
        			n.getGameButton().setWinningPlayerId(game.getWinningPlayerId().get());
        		}
        	});
        }
    }

    public ServerCollapsible getServerCollapsible() {
        return serverCollapsible;
    }
   
    public ServerPlayerTable getTournamentTable() {
    	return this.tournamentTab;
    }

    public ArrayList<RemoteGame> getRemoteGames() {
        return remoteGames;
    }

    public Connection getConnection() {
        return connection;
    }

    public URI getRequestURI() {
		return this.connection.toURI();
    }

    public boolean equals(RemoteServer remoteServer) {
        return this.connection.equals(remoteServer.getConnection());
    }

    @Override
    public String toString() {
        return getRequestURI().toString();
    }
}
