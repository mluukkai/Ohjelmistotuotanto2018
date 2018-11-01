package ohtuesimerkki;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author miikahyttinen
 */
public class StatisticsTest {
    
    Reader readerStub;
 
    Statistics stats;
    
    public StatisticsTest() {
        this.readerStub = new Reader() {

            @Override
            public List<Player> getPlayers() {
                ArrayList<Player> players = new ArrayList<Player>();
                
                players.add(new Player("Semenko", "EDM", 4, 12));
                players.add(new Player("Lemieux", "PIT", 45, 54));
                players.add(new Player("Kurri",   "EDM", 37, 53));
                players.add(new Player("Yzerman", "DET", 42, 56));
                players.add(new Player("Gretzky", "EDM", 35, 89));
                
                return players;
            }
        };
    }
  
    @Before
    public void setUp() {
        stats = new Statistics(readerStub);
    }
    
    @Test
    public void playerSearchWorks() {
        assertEquals(stats.search("Semenko").getName(), "Semenko");
        assertEquals(stats.search("Semenko!23"), null);
    }
    
    @Test
    public void playersOfTeamWorks() {
        List<Player> edmPlayers = stats.team("EDM");
        boolean isAllPlayersEdm = true;
        for(Player p : edmPlayers) {
            if(!"EDM".equals(p.getTeam())) {
                isAllPlayersEdm = false;
                break;
            } else {
                isAllPlayersEdm = true;
            }
        }
        assertEquals(isAllPlayersEdm, true);        
    }
    
    @Test 
    public void topScoresWorks() {
        List<Player> topScore = stats.topScorers(1);
        assertEquals(topScore.get(0).getName().equals("Gretzky"), true);
    }
}
