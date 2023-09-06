package model.jsonstruct;
import model.ids.GlobalPlayerId;
import model.player.Player;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * This class tests all methods of class PlayerStruct
 */
public class PlayerStructTest {

    final Player player = new Player(new GlobalPlayerId(15), "Hans", "omfgdogs.com");

    /**
     * This method tests fromModel by using an example Player, without authentification
     */
    @Test
    public void fromModelTestNotAuthenticated(){
        // initializing playerstruct with fromModel without authentication
        PlayerStruct playerStruct = PlayerStruct.fromModel(player, false);
        // check if values are correct
        assertEquals(playerStruct.playerId, Integer.valueOf(15));
        assertEquals(playerStruct.name, "Hans");
        assertNull(playerStruct.url);
    }

    /**
     * This method tests fromModel by using an example Player, with authentification
     */
    @Test
    public void fromModelTestAuthenticated(){
        // initializing playerstruct with fromModel with authentication
        PlayerStruct playerStruct = PlayerStruct.fromModel(player, true);
        // check if values are correct
        assertEquals(playerStruct.playerId, Integer.valueOf(15));
        assertEquals(playerStruct.name, "Hans");
        assertEquals(playerStruct.url, "omfgdogs.com");
    }

    /**
     * This method tests intoModel by using an example Player with authentification
     * assuming fromModel works
     */
    @Test
    public void intoModelTestAuthenticated(){
        // initializing playerstruct with fromModel with authentication
        PlayerStruct playerStruct = PlayerStruct.fromModel(player, true);
        Player newPlayer = playerStruct.intoModel();
        assertEquals(newPlayer.getPlayerId().get(), 15);
        assertEquals(newPlayer.getName(), "Hans");
        assertEquals(newPlayer.getUrl(), "omfgdogs.com");
    }

    /**
     * This method tests intoModel by using an example Player without authentification
     * assuming fromModel works
     */
    @Test
    public void intoModelTestNotAuthenticated(){
        // initializing playerstruct with fromModel with authentication
        PlayerStruct playerStruct = PlayerStruct.fromModel(player, false);
        Player newPlayer = playerStruct.intoModel();

        // check values
        assertEquals(newPlayer.getPlayerId().get(), 15);
        assertEquals(newPlayer.getName(), "Hans");

        // I dont know if this is the right behaviour
        assertNull(newPlayer.getUrl());
    }
}
