/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ohtuesimerkki;

import java.util.*;
import org.junit.*;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author ColdFish
 */
public class StatisticsTest {
 
    Reader readerStub = new Reader() {
 
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
 
    Statistics stats;

    @Before
    public void setUp(){
        // luodaan Statistics-olio joka käyttää "stubia"
        stats = new Statistics(readerStub);
    }  
    
    @Test
    public void  teamHasPlayers(){
       assertEquals(3, stats.team("EDM").size(), 0.01);
    }
    
        @Test
    public void  team2HasPlayers(){
       assertEquals(1, stats.team("PIT").size(), 0.01);
    }
    
            @Test
    public void  team0HasPlayers(){
       assertEquals(0, stats.team("PAT").size(), 0.01);
    }
    
                @Test
    public void  playerSearch1(){
       assertEquals(4, stats.search("Semenko").getGoals(), 0.01);
    }
    
                    @Test
    public void  playerSearch2(){
       assertEquals(37, stats.search("Kurri").getGoals(), 0.01);
    }
    
                       @Test
    public void  playerSearch3(){
        Player p = stats.search("Kulli");
       assertEquals(p, null);
    }
    
                        @Test
    public void  topScoreerBest(){
       assertEquals("Lemieux",  stats.topScorers(3).get(1).getName());
    }
                            @Test
    public void  topScoreerThird(){
       assertEquals("Kurri",  stats.topScorers(3).get(3).getName());
    }
}
