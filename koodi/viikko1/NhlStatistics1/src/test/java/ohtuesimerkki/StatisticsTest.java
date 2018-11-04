/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ohtuesimerkki;


import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertNull;
import org.junit.Before;

/**
 *
 * @author tkarkine
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
    public void pelaajaLoytyy(){
        Player pelaaja = stats.search("Semenko");
         assertEquals("Semenko",pelaaja.getName());
    }
    
    @Test
    public void pelaajaEiloydy(){
        Player pelaaja = stats.search("Ketterer");
        assertNull(pelaaja);
    }
    
    @Test
    public void joukkueEiloydy(){
       List<Player> pelaajat = stats.team("HJK");
       assertEquals(true,pelaajat.isEmpty());
    }
    @Test
    public void joukkueYksipelaaja(){
        List<Player> pelaajat = stats.team("PIT");
       assertEquals(1,pelaajat.size());
    }
    @Test
    public void joukkueKolme(){
      List<Player> pelaajat = stats.team("EDM");
         assertEquals(3, pelaajat.size());
    }
    
     @Test
    public void neljaParasta(){
      List<Player> pelaajat = stats.topScorers(2);
      assertEquals(pelaajat.size(), pelaajat.size());
    }
    
    
}
