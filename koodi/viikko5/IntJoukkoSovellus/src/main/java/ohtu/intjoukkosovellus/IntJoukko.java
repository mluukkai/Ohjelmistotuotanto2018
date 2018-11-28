
package ohtu.intjoukkosovellus;

public class IntJoukko {

    public final static int KAPASITEETTI = 5, // aloitustalukon koko
                            OLETUSKASVATUS = 5;  // luotava uusi taulukko on 
    // näin paljon isompi kuin vanha
    private int kasvatuskoko;     // Uusi taulukko on tämän verran vanhaa suurempi.
    private int[] ljono;      // Joukon luvut säilytetään taulukon alkupäässä. 
    private int alkioidenLkm;    // Tyhjässä joukossa alkioiden_määrä on nolla. 

    public IntJoukko() {
        alusta(KAPASITEETTI, OLETUSKASVATUS);
    }

    public IntJoukko(int kapasiteetti) {
        if (kapasiteetti < 0) {
            return;
        }
        alusta(kapasiteetti, OLETUSKASVATUS);

    }
    
    
    public IntJoukko(int kapasiteetti, int kasvatuskoko) {
        if (kapasiteetti < 0) {
            throw new IndexOutOfBoundsException("Kapasitteetti väärin");//heitin vaan jotain :D
        }
        alusta(kapasiteetti, kasvatuskoko);

    }
    
    private void alusta(int kapasiteetti, int kasvatuskoko) {
        ljono = new int[kapasiteetti];
        
        for (int i = 0; i < ljono.length; i++) {
            ljono[i] = 0;
        }
        
        alkioidenLkm = 0;
        this.kasvatuskoko = kasvatuskoko;
    }
    
    public boolean lisaa(int luku) {

        
        if (alkioidenLkm == 0) {
            return tyhjaTaulukko(luku);
        } else if (!kuuluu(luku)) {
            eiKuulunut(luku);
            return true;
        }
        return false;
    }
    private boolean tyhjaTaulukko(int luku) {
            ljono[0] = luku;
            alkioidenLkm++;
            return true;
    }
    
    private void eiKuulunut(int luku) {
        ljono[alkioidenLkm] = luku;
        alkioidenLkm++;
        if (alkioidenLkm % ljono.length == 0) {
            int[] taulukkoOld = new int[ljono.length];
            taulukkoOld = ljono;
            kopioiTaulukko(ljono, taulukkoOld);
            ljono = new int[alkioidenLkm + kasvatuskoko];
            kopioiTaulukko(taulukkoOld, ljono);
        }
    }
    
    public boolean kuuluu(int luku) {
        boolean kuuluuko = false;
        for (int i = 0; i < alkioidenLkm; i++) {
            if (luku == ljono[i]) {
                kuuluuko = true;
            }
        }
        return kuuluuko;
    }
    
    public boolean poista(int luku) {
        int apu;
        int kohta = poistaLuku(luku);
        if (kohta != -1) {
            this.eiLoytynytPoistaessa(luku, kohta);
        }
        
        return false;
    }
    
    public int poistaLuku(int luku) {
        int kohta = -1;
        for (int i = 0; i < alkioidenLkm; i++) {
            if (luku == ljono[i]) {
                kohta = i; //siis luku löytyy tuosta kohdasta :D
                ljono[kohta] = 0;
                break;
            }
        }
        return kohta;
    }
    
    private boolean eiLoytynytPoistaessa(int luku, int kohta) {
            int apu;
            for (int j = kohta; j < alkioidenLkm - 1; j++) {
                apu = ljono[j];
                ljono[j] = ljono[j + 1];
                ljono[j + 1] = apu;
            }
            alkioidenLkm--;
            return true;
    }
    
    private void kopioiTaulukko(int[] vanha, int[] uusi) {
        for (int i = 0; i < vanha.length; i++) {
            uusi[i] = vanha[i];
        }
    }

    public int mahtavuus() {
        return alkioidenLkm;
    }

    @Override
    public String toString() {
        if (alkioidenLkm < 2) {
            return palautaTyhjaTaiYksi();
        }
        return palautaJoukko();
    }
    private String palautaTyhjaTaiYksi() {
        String tulos = "";
        if (alkioidenLkm == 0) {
            tulos = "{}";
        }
        if (alkioidenLkm == 1) {
            tulos = "{" + ljono[0] + "}";
        }
        return tulos;
    }
    
    private String palautaJoukko() {
        String tuotos = "{";
        for (int i = 0; i < alkioidenLkm - 1; i++) {
            tuotos += ljono[i];
            tuotos += ", ";
        }
        tuotos += ljono[alkioidenLkm - 1];
        tuotos += "}";
        return tuotos;
    }
    
    public int[] toIntArray() {
        int[] taulu = new int[alkioidenLkm];
        for (int i = 0; i < taulu.length; i++) {
            taulu[i] = ljono[i];
        }
        return taulu;
    }
   
    public static IntJoukko yhdiste(IntJoukko a, IntJoukko b) {
        IntJoukko x = new IntJoukko();
        
        lisaaTaulukko(x, a.toIntArray());
        lisaaTaulukko(x, b.toIntArray());
        
        return x;
    }
    private static void lisaaTaulukko(IntJoukko joukko, int[] taulu) {
        for (int i = 0; i < taulu.length; i++) {
            joukko.lisaa(taulu[i]);
        }
    }
    public static IntJoukko leikkaus(IntJoukko a, IntJoukko b) {
        IntJoukko y = new IntJoukko();
        lisaaLeikkaavaLuku(y, a, b.toIntArray());
        return y;

    }
    private static void lisaaLeikkaavaLuku(IntJoukko joukko, IntJoukko a, int[] b) {
        for (int i = 0; i < a.alkioidenLkm; i++) {
//            for (int j = 0; j < bTaulu.length; j++) {
                if (a.kuuluu(b[i])) {
                    joukko.lisaa(b[i]);
                }
            }
     
    }
    public static IntJoukko erotus ( IntJoukko a, IntJoukko b) {
        IntJoukko z = new IntJoukko();
        
        joukkojenErotus(z, a.toIntArray(), b.toIntArray());
 
        return z;
    }
    
    public static void joukkojenErotus(IntJoukko z, int[] aTaulu, int[] bTaulu) {
        for (int i = 0; i < aTaulu.length; i++) {
            z.lisaa(aTaulu[i]);
        }
        for (int i = 0; i < bTaulu.length; i++) {
            z.poista(i);
        }
    }    
}