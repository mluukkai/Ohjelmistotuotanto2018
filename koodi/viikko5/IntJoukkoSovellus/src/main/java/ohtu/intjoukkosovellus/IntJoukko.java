
package ohtu.intjoukkosovellus;

import java.util.Arrays;

public class IntJoukko {

    public final static int KAPASITEETTI = 5, // aloitustalukon koko
                            OLETUSKASVATUS = 5;  // luotava uusi taulukko on 
    // näin paljon isompi kuin vanha
    private int kasvatuskoko;     // Uusi taulukko on tämän verran vanhaa suurempi.
    private int[] ljono;      // Joukon luvut säilytetään taulukon alkupäässä. 
    private int alkioidenLkm;    // Tyhjässä joukossa alkioiden_määrä on nolla. 

    public IntJoukko() {
        ljono = new int[KAPASITEETTI];
        alkioidenLkm = 0;
        this.kasvatuskoko = OLETUSKASVATUS;
    }

    public IntJoukko(int kapasiteetti) {
        if (kapasiteetti < 0) {
            return;
        }
        ljono = new int[kapasiteetti];
        alkioidenLkm = 0;
        this.kasvatuskoko = OLETUSKASVATUS;
    }
    
    
    public IntJoukko(int kapasiteetti, int kasvatuskoko) {
        if (kapasiteetti < 0) {
            throw new IndexOutOfBoundsException("Kapasitteetti väärin");//heitin vaan jotain :D
        }
        if (kasvatuskoko < 0) {
            throw new IndexOutOfBoundsException("kapasiteetti2");//heitin vaan jotain :D
        }
        ljono = new int[kapasiteetti];
        alkioidenLkm = 0;
        this.kasvatuskoko = kasvatuskoko;
    }

    public boolean lisaa(int luku) {
        if (!kuuluu(luku)) {
            ljono[alkioidenLkm] = luku;
            alkioidenLkm++;
            //jos seuraavalisäys ei mahtuisi kasvatetaan
            if (alkioidenLkm % ljono.length == 0) kasvata();
            return true;
        }
        return false;
    }

    public boolean kuuluu(int luku) {
        return (indeksi(luku)>=0);
        //indeksi palauttaa -1 jos lukua ei löydy joukosta
    }

    public boolean poista(int luku) {
        if (kuuluu(luku)){
            ljono[indeksi(luku)] = ljono[alkioidenLkm-1];
            alkioidenLkm--;
            return true;
        }
       return false;
    }

    private void kopioiTaulukko(int[] vanha, int[] uusi) {
        System.arraycopy(vanha, 0, uusi, 0, vanha.length);
    }

    public int mahtavuus() {
        return alkioidenLkm;
    }


    @Override
    public String toString() {
        int [] alkiot = toIntArray(); //käytössä olevat alkiot
        String joukko = Arrays.toString(alkiot);
        joukko = joukko.replace('[','{' );
        joukko = joukko.replace(']','}' );
        return joukko;
    }

    public int[] toIntArray() {
        int[] taulu = new int[alkioidenLkm];
        System.arraycopy(ljono, 0, taulu, 0, taulu.length);
        return taulu;
    }
   

    public static IntJoukko yhdiste(IntJoukko a, IntJoukko b) {
        IntJoukko x = new IntJoukko();
        for (int i = 0; i < a.alkioidenLkm; i++) {
            x.lisaa(a.ljono[i]);
        }
        for (int i = 0; i < b.alkioidenLkm ; i++) {
            x.lisaa(b.ljono[i]);
        }
        return x;
    }

    public static IntJoukko leikkaus(IntJoukko a, IntJoukko b) {
        IntJoukko y = new IntJoukko();
        int alkio;
        for (int i = 0; i < a.alkioidenLkm;i++){
            alkio = a.ljono[i];
            if (b.kuuluu(alkio)) y.lisaa(alkio);
        }
        return y;

    }
    
    public static IntJoukko erotus( IntJoukko a, IntJoukko b) {
        IntJoukko z = new IntJoukko();
        for (int i = 0; i < a.alkioidenLkm;i++){
            if (!b.kuuluu(a.ljono[i])) z.lisaa(a.ljono[i]);
        }
        return z;
    }
    
    private int indeksi(int luku) {
        for (int i = 0; i < alkioidenLkm; i++) {
            if (luku == ljono[i]) return i;
        }
       return -1;
       // koska indeksi voi olla 0, palauttaa -1 jos ei lukua ole
    }
    
    private void kasvata() {
        int[] taulukkoOld =  new int[alkioidenLkm + kasvatuskoko];
        kopioiTaulukko(ljono, taulukkoOld);
        ljono = taulukkoOld;
    }
        
}