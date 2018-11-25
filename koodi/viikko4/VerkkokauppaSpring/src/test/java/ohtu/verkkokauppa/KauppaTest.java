

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ohtu.verkkokauppa;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 *
 * @author tkarkine
 */
public class KauppaTest {
    Pankki pankki;
    Viitegeneraattori viite;
    Varasto varasto;
    
    @Before
    public void alustaPankkijaViitegeneraattori(){
     // luodaan ensin mock-oliot
    pankki = mock(Pankki.class);
    viite = mock(Viitegeneraattori.class);
    varasto = mock(Varasto.class);
    }
    
    @Test
    public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaan() {
     // määritellään että viitegeneraattori palauttaa viitten 42
    when(viite.uusi()).thenReturn(42);

    // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
    when(varasto.saldo(1)).thenReturn(10); 
    when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

    // sitten testattava kauppa 
    Kauppa k = new Kauppa(varasto, pankki, viite);              

    // tehdään ostokset
    k.aloitaAsiointi();
    k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
    k.tilimaksu("pekka", "12345");

    // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
    verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(),anyInt());   
    // toistaiseksi ei välitetty kutsussa käytetyistä parametreista
}
      @Test
    public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaanOikeillaArvoilla() {
    // määritellään että viitegeneraattori palauttaa viitten 42
    when(viite.uusi()).thenReturn(42);

     // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
    when(varasto.saldo(1)).thenReturn(10); 
    when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
   
    // sitten testattava kauppa 
    Kauppa k = new Kauppa(varasto, pankki, viite);              

    // tehdään ostokset
    k.aloitaAsiointi();
    k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
   k.tilimaksu("pekka", "12345"); 

    // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
    verify(pankki).tilisiirto("pekka", 42 , "12345", "33333-44455",5);   
    // toistaiseksi ei välitetty kutsussa käytetyistä parametreista
}
         
    @Test
    public void kahdenEriTuotteenostonPaaytyttyaPankinMetodiaTilisiirtoKutsutaanOikeillaArvoilla() {
    // määritellään että viitegeneraattori palauttaa viitten 42
    when(viite.uusi()).thenReturn(42);

    // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
    when(varasto.saldo(1)).thenReturn(10); 
    when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
    //ja määritellään että toinentuote numero 2 on peruna jonka hinta on 2 ja saldo 100
    when(varasto.saldo(2)).thenReturn(100); 
    when(varasto.haeTuote(2)).thenReturn(new Tuote(1, "peruna", 2));

    // sitten testattava kauppa 
    Kauppa k = new Kauppa(varasto, pankki, viite);              

    // tehdään ostokset
    k.aloitaAsiointi();
    k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
    k.lisaaKoriin(2);     // ostetaan tuotetta numero 2 eli perunaa
    k.tilimaksu("pekka", "12378"); //annetaan myös vaimon tili

    // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
    verify(pankki).tilisiirto("pekka", 42 , "12378", "33333-44455",7);   
    // palautearvot oikeat
}
    @Test
    public void kahdenSamanTuotteenostonPaaytyttyaPankinMetodiaTilisiirtoKutsutaanOikeillaArvoilla() {
    // määritellään että viitegeneraattori palauttaa viitten 72
    when(viite.uusi()).thenReturn(72);

    // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
    when(varasto.saldo(1)).thenReturn(10); 
    when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
   
    // sitten testattava kauppa 
    Kauppa k = new Kauppa(varasto, pankki, viite);              

    // tehdään ostokset
    k.aloitaAsiointi();
    k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
    k.lisaaKoriin(1);     // ostetaan toinenkin maito
    k.tilimaksu("pekka", "12378"); //annetaan myös vaimon tili

    // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
    verify(pankki).tilisiirto("pekka", 72 , "12378", "33333-44455",10);   
    // kaksi maitoa maksaa 10 vaihtelua tilinumerossa ja viitteessä
}    
    
    @Test
    public void vainEkaaTuotettaOnOstonPaaytyttyaPankinMetodiaTilisiirtoKutsutaanOikeillaArvoilla() {
    // määritellään että viitegeneraattori palauttaa viitten 42
    when(viite.uusi()).thenReturn(42);

    // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
    when(varasto.saldo(1)).thenReturn(10); 
    when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
    //ja määritellään että toinentuote numero 2 on peruna jonka hinta on 2 ja saldo 100
    when(varasto.saldo(2)).thenReturn(0); 
    
    // sitten testattava kauppa 
    Kauppa k = new Kauppa(varasto, pankki, viite);              

    // tehdään ostokset
    k.aloitaAsiointi();
    k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
    k.lisaaKoriin(2);     // yritetään ostaa tuotetta numero 2 eli perunaa
    k.tilimaksu("pekka", "12378"); //annetaan myös vaimon tili

    // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
    verify(pankki).tilisiirto("pekka", 42 , "12378", "33333-44455",5);   
    // vain maito maksettavaa
}
    
    @Test
    public void asioinninAloitusNollaaOstoskorin() {
     // edellinen asiakas pekka
    when(viite.uusi()).thenReturn(42);

    
    // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
    when(varasto.saldo(1)).thenReturn(10); 
    when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

    // sitten testattava kauppa 
    Kauppa k = new Kauppa(varasto, pankki, viite);              

    // tehdään ostokset
    k.aloitaAsiointi();
    k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
    k.tilimaksu("pekka", "12345");
    // pekka poistui ja sirpa aloittaa asioinnin, ostaa 2 maitoa
    k.aloitaAsiointi();
    k.lisaaKoriin(1); 
     k.lisaaKoriin(1);
    // ostetaan tuotetta numero 1 eli maitoa
    k.tilimaksu("sirpa", "98765");
    

    // sitten suoritetaan varmistus, ettei pekan ostokset ole mukana korissa
    verify(pankki).tilisiirto("sirpa", 42 , "98765", "33333-44455",10);   
    // summa pitää olla 10 eikä 15
}
    
        
    @Test
    public void kummallekinAsiakkaalleOmaViite() {
     // edellinen asiakas pekka
    when(viite.uusi()).thenReturn(42);

    
    // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
    when(varasto.saldo(1)).thenReturn(10); 
    when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

    // sitten testattava kauppa 
    Kauppa k = new Kauppa(varasto, pankki, viite);              

    // tehdään ostokset
    k.aloitaAsiointi();
    //vielä ei ole kutsuttu
    verify(viite, times(0)).uusi();
    k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
    k.tilimaksu("pekka", "12345");
     //pekalle kutsuttu kerran
    verify(viite, times(1)).uusi();
   
    // pekka poistui ja sirpa aloittaa asioinnin, ostaa myös maitoa
    k.aloitaAsiointi();
    k.lisaaKoriin(1); 
       // ostetaan tuotetta numero 1 eli maitoa
    k.tilimaksu("sirpa", "98765");
     //sirpalle kutsuttu toinen viite
    verify(viite, times(2)).uusi();
   }
    
     @Test
    public void poistetunTuotteenJalkeenOstonPaaytyttyaPankinMetodiaTilisiirtoKutsutaanOikeillaArvoilla() {
    // määritellään että viitegeneraattori palauttaa viitten 72
    when(viite.uusi()).thenReturn(72);

    // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
    when(varasto.saldo(1)).thenReturn(10); 
    when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
   
    // sitten testattava kauppa 
    Kauppa k = new Kauppa(varasto, pankki, viite);              

    // tehdään ostokset
    k.aloitaAsiointi();
    k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
    k.lisaaKoriin(1);   // ostetaan toinenkin maito
    k.poistaKorista(1);    //ei sittenkään, muuten rahat ei riitä pelikoneeseen
    k.tilimaksu("pekka", "12378"); //annetaan myös vaimon tili

    // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
    verify(pankki).tilisiirto("pekka", 72 , "12378", "33333-44455", 5);   
    // yksi maito vain vitosen
}    
    
}
