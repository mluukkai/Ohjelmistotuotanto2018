Tämä materiaali on tarkoitettu itseopiskeltavaksi ennen viikon 5, 6 ja 7 laskarien tekemistä. Materiaali täydentää [luennon 8](https://github.com/mluukkai/ohjelmistotuotanto2018/blob/master/kalvot/luento8.pdf?raw=true)
asiaa.

## Koheesio

Koheesiolla tarkoitetaan sitä, kuinka pitkälle metodissa, luokassa tai komponentissa oleva ohjelmakoodi on keskittynyt tietyn toiminnallisuuden toteuttamiseen. Hyvänä asiana pidetään mahdollisimman korkeaa koheesion astetta. Koheesioon tulee siis pyrkiä kaikilla ohjelman tasoilla, metodeissa, luokissa, komponenteissa ja jopa muuttujissa (samaa muuttujaa ei saa uusiokäyttää eri asioiden tallentamiseen). 

## Koheesio metoditasolla

Esimerkki artikkelista [http://www.ibm.com/developerworks/java/library/j-eaed4/index.html](http://www.ibm.com/developerworks/java/library/j-eaed4/index.html)

``` java
public void populate() throws Exception {
    Connection c = null;
    try {
        c = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(SQL_SELECT_PARTS);
        while (rs.next()) {
            Part p = new Part();
            p.setName(rs.getString("name"));
            p.setBrand(rs.getString("brand"));
            p.setRetailPrice(rs.getDouble("retail_price"));
            partList.add(p);
        }
    } finally {
        c.close();
    }
}
```

Metodissa tehdään montaa asiaa:

* luodaan yhteys tietokantaan
* tehdään tietokantakysely
* käydään kyselyn tulosrivit läpi ja luodaan jokaista tulosriviä kohti Part-olio
* suljetaan yhteys

Ikävänä seurauksena tästä on myös se, että metodi toimii monella abstraktiotasolla. Toisaalta käsitellään teknisiä tietokantatason asioita kuten tietokantayhteyden avaamista ja kyselyn tekemistä, toisaalta "bisnestason" olioita.

Metodi on helppo __refaktoroida__ pilkkomalla se pienempiin osiin joiden kutsumista alkuperäinen metodi koordinoi.

``` java
public void populate() throws Exception {
    Connection c = null;
    try {
        c = getDatabaseConnection();
        ResultSet rs = createResultSet(c);
        while (rs.next()){
            addPartToListFromResultSet(rs);
        }
    } finally {
        c.close();
    }
}

private ResultSet createResultSet(Connection c)throws SQLException {
    return c.createStatement().
            executeQuery(SQL_SELECT_PARTS);
}

private Connection getDatabaseConnection() throws ClassNotFoundException, SQLException {
    return DriverManager.getConnection(DB_URL,"webuser", "webpass");
}

private void addPartToListFromResultSet(ResultSet rs) throws SQLException {
    Part p = new Part();
    p.setName(rs.getString("name"));
    p.setBrand(rs.getString("brand"));
    p.setRetailPrice(rs.getDouble("retail_price"));
    partList.add(p);
}
```

Yksittäiset metodit ovat nyt kaikki samalla abstraktiotasolla toimivia ja hyvin nimettyjä.

Nyt aikaansaatu lopputulos ei ole vielä välttämättä ideaali koko ohjelman kontekstissa. [Artikkelissa](http://www.ibm.com/developerworks/java/library/j-eaed4/index.html) esimerkkiä jatketaankin eristäen tietokantaoperaatiot (joita myös muut ohjelman osat tarvitsevat) omaan luokkaansa.

# Viikon 5 laskareihin riittää tähän asti lukeminen

## Single responsibility -periaate eli koheesio luokkatasolla

Kurssin alussa tarkastelimme yksinkertaista laskinta:

``` java
public class Laskin {

    private Scanner lukija;

    public Laskin() {
        lukija = new Scanner(System.in);
    }

    public void suorita(){
        while( true ) {
            System.out.print("luku 1: ");
            int luku1 = lukija.nextInt();
            if ( luku1==-9999  ) return;

            System.out.print("luku 2: ");
            int luku2 = lukija.nextInt();
            if ( luku2==-9999  ) return;

            int vastaus = laskeSumma(luku1, luku2);
            System.out.println("summa: "+ vastaus);
        }
    }

    private int laskeSumma(int luku1, int luku2) {
        return luku1+luku2;
    }

}
```

Luokka rikkoo Single responsibility -periaatteen. Miksi? Periaate sanoo, että luokalla saa olla vain yksi vastuu eli syy muuttuua. Nyt luokalla on kuitenkin useita syitä muuttua:

* luokalle halutaan toteuttaa uusia laskutoimituksia
* kommunikointi käyttäjän kanssa halutaan hoitaa jotenkin muuten kuin konsolin välityksellä

Eriyttämällä käyttäjän kanssa kommunikointi omaan luokkaan ja eristämällä se rajapinnan taakse (eli kapseloimalla kommunikoinnin toteutustapa) saadaan luokan Laskin vastuita vähennettyä:

``` java
public interface IO {
    int nextInt();
    void print(String m);
}

public class Laskin {
    private IO io;

    public Laskin(IO io) {
        this.io = io;
    }

    public void suorita(){
        while( true ) {
            io.print("luku 1: ");
            int luku1 = io.nextInt();
            if ( luku1==-9999  ) return;

            io.print("luku 2: ");
            int luku2 = io.nextInt();
            if ( luku2==-9999 ) return;

            int vastaus = laskeSumma(luku1, luku2);
            io.print("summa: "+vastaus+"\n");
        }
    }

    private int laskeSumma(int luku1, int luku2) {
        return luku1+luku2;
    }
}
```

Nyt kommunikointitavan muutos ei edellytä luokkaan mitään muutoksia edellyttäen että uusikin kommunikoinitapa toteuttaa rajapinnan, jonka kautta Laskin hoitaa kommunikoinnin.

Vaikka luokka Laskin siis toteuttaakin edelleen käyttäjänsä näkökulmasta samat asiat kuin aiemmin, ei se hoida kaikkea itse vaan _delegoi_ osan vastuistaan muualle.

Kommunikointirajapinta voidaan toteuttaa esim. seuraavasti:

```java
public class KonsoliIO implements IO {
    private Scanner lukija;

    public KonsoliIO() {
        lukija = new Scanner(System.in);
    }

    public int nextInt() {
        return lukija.nextInt();
    }

    public void print(String m) {
        System.out.println(m);
    }
}
```

Ja laskin konfiguroidaan injektoimalla _IO_-rajapinnan toteuttava luokka konstruktorin parametrina:

```java
public class Main {
    public static void main(String[] args) {
        Laskin laskin = new Laskin( new KonsoliIO() );
        laskin.suorita();
    }
}
```

Testausta varten voidaan toteuttaa _stub_ eli valekomponentti, jonka avulla testi voi hallita "käyttäjän" syötteitä ja lukea ohjelman tulostukset:

```java
public class IOStub implements IO {

    int[] inputs;
    int mones;
    ArrayList<String> outputs;

    public IOStub(int... inputs) {
        this.inputs = inputs;
        this.outputs = new ArrayList<String>();
    }

    public int nextInt() {
        return inputs[mones++];
    }

    public void print(String m) {
        outputs.add(m);
    }
}
```

Parannellun laskimen rakenne luokkakaaviona

![](https://github.com/mluukkai/ohjelmistotuotanto2017/raw/master/images/os-1.png)

Luokka ei ole vielä kaikin osin laajennettavuuden kannalta optimaalinen. Palaamme asiaan hetken kuluttua.

## Favour composition over inheritance eli milloin ei kannata periä

Meillä on käytössämme luokka, joka mallintaa pankkitiliä:

``` java
public class Tili {
    private String tiliNumero;
    private String omistaja;
    private double saldo;
    private double korkoProsentti;

    public Tili(String tiliNumero, String omistaja, double korkoProsentti) {
        this.tiliNumero = tiliNumero;
        this.omistaja = omistaja;
        this.korkoProsentti = korkoProsentti;
    }

    public boolean siirraRahaaTililta(Tili tilille, double summa){
        if ( this.saldo<summa ) return false;

        this.saldo -= summa;
        tilille.saldo += summa;

        return true;
    }

    public void maksaKorko(){
        saldo += saldo*korkoProsentti*100;
    }
}
```

Huomaamme, että tulee tarve toisentyyppiselle tilille joko 1, 3, 6 tai 12 kuukaiden Euribor-korkoon perustuvalle tilille. päätämme tehdä uuden luokan EuriborTili perimällä luokan Tili ja ylikirjoittamalla metodin maksaKorko siten että Euribor-koron senhetkinen arvo haetaan verkosta:

``` java
public class EuriborTili extends Tili {
    private int kuukauden;

    public EuriborTili(String tiliNumero, String omistaja, int kuukauden) {
        super(tiliNumero, omistaja, 0);
        this.kuukauden = kuukauden;
    }

    @Override
    public void maksaKorko() {
        saldo += saldo*korko()*100;
    }

    private double korko() {
        try {
            Scanner lukija = new Scanner(new URL("http://www.euribor-rates.eu/current-euribor-rates.asp").openStream());
            int count = 0;
            while (lukija.hasNextLine()) {
                String sisalto = lukija.nextLine();
                if (sisalto.contains("Euribor - "+kuukauden+" month") && count==0){
                    count = 1;
                } else if (sisalto.contains("Euribor - "+kuukauden+" month") && count==1){
                    lukija.nextLine();
                    lukija.nextLine();
                    sisalto = lukija.nextLine();
                    return Double.parseDouble(sisalto.substring(0, sisalto.length()-1))/100;
                }
            }      
            
        } catch (Exception e) {}
        return 0;
    }
}
```

Huomaamme, että EuriborTili rikkoo Single Responsibility -periaatetta, sillä luokka sisältää normaalin tiliin liittyvän toiminnan lisäksi koodia, joka hakee tavaraa internetistä. Vastuut kannattaa selkeyttää ja korkoprosentin haku eriyttää omaan rajapinnan takana olevaan luokkaan:

``` java
public interface EuriborLukija {
    double korko();
}

public class EuriborTili extends Tili {
    private EuriborLukija euribor;

    public EuriborTili(String tiliNumero, String omistaja, int kuukauden) {
        super(tiliNumero, omistaja, 0);
        euribor = new EuriborlukijaImpl(kuukauden);
    }

    @Override
    public void maksaKorko() {
        saldo += saldo*euribor.korko()*100;
    }

}

public class EuriborlukijaImpl implements EuriborLukija {
    private int kuukauden;

    public EuriborlukijaImpl(int kuukauden) {
        this.kuukauden = kuukauden;
    }

    @Override
    public double korko() {
        try {
            Scanner lukija = new Scanner(new URL("http://www.euribor-rates.eu/current-euribor-rates.asp").openStream());
            int count = 0;
            while (lukija.hasNextLine()) {
                String sisalto = lukija.nextLine();
                if (sisalto.contains("Euribor - "+kuukauden+" month") && count==0){
                    count = 1;
                } else if (sisalto.contains("Euribor - "+kuukauden+" month") && count==1){
                    lukija.nextLine();
                    lukija.nextLine();
                    sisalto = lukija.nextLine();
                    return Double.parseDouble(sisalto.substring(0, sisalto.length()-1))/100;
                }
            }      
            
        } catch (Exception e) {}
        return 0;
    }
}
```

EuriborTili-luokka alkaa olla nyt melko siisti, EuriborLukijassa olisi paljon parantemisen varaa, mm. sen ainoan metodin koheesio on huono, metodi tekee aivan liian montaa asiaa. Palaamme siihen kuitenkin myöhemmin.

Seuraavaksi huomaamme että on tarvetta _Määräaikaistilille_, joka on muuten samanlainen kuin Tili mutta määräaikaistililtä ei voi siirtää rahaa muualle ennen kuin se tehdään mahdolliseksi tietyn ajan kuluttua. Eli ei ongelmaa, perimme jälleen luokan Tili:

``` java
public class MääräaikaisTili extends Tili {
    private boolean nostokielto;

    public MääräaikaisTili(String tiliNumero, String omistaja, double korkoProsentti) {
        super(tiliNumero, omistaja, korkoProsentti);
        nostokielto = true;
    }

    public void salliNosto(){
        nostokielto = false;
    }

    @Override
    public boolean siirraRahaaTililta(Tili tilille, double summa) {
        if ( nostokielto )
            return false;

        return super.siirraRahaaTililta(tilille, summa);
    }

}
```

Luokka syntyi tuskattomasti.

Ohjelman rakenne näyttää tässä vaiheessa seuraavalta:

![](https://github.com/mluukkai/ohjelmistotuotanto2017/raw/master/images/os-2.png)

Seuraavaksi tulee idea _Euribor-korkoa käyttävistä määräaikaistileistä_. 
Miten nyt kannattaisi tehdä? Osa toiminnallisuudesta on luokassa Määräaikaistili ja osa luokassa Euribor-tili...

Ehkä koronmaksun hoitaminen perinnän avulla ei ollutkaan paras ratkaisu, ja kannattaisi noudattaa "favor composition over inheritance"-periaatetta. Eli erotetaan koronmaksu omaksi luokakseen, tai rajapinnan toteuttaviksi luokiksi:

``` java
public interface Korko {
    double korko();
}

public class Tasakorko implements Korko {
    private double korko;

    public Tasakorko(double korko) {
        this.korko = korko;
    }

    public double korko() {
        return korko;
    }
}

public class EuriborKorko implements Korko {
    EuriborLukija lukija;

    public EuriborKorko(int kuukausi) {
        lukija = new EuriborlukijaImpl(kuukausi);
    }

    public double korko() {
        return korko();
    }
}
```

Nyt tarve erilliselle EuriborTili-luokalle katoaa, ja pelkkä Tili muutettuna riittää:

``` java
public class Tili {
    private String tiliNumero;
    private String omistaja;
    private double saldo;
    private Korko korko;

    public Tili(String tiliNumero, String omistaja, Korko korko) {
        this.tiliNumero = tiliNumero;
        this.omistaja = omistaja;
        this.korko = korko;
    }

    public boolean siirraRahaaTililta(Tili tilille, double summa){
        if ( this.saldo<summa ) return false;

        this.saldo -= summa;
        tilille.saldo += summa;

        return true;
    }

    public void maksaKorko(){
        saldo += saldo*korko.korko()*100;
    }
}
```

Erilaisia tilejä luodaan nyt seuraavasti:

``` java
Tili normaali = new Tili("1234-1234", "Kasper Hirvikoski", new Tasakorko(4));
Tili euribor12 = new Tili("4422-3355", "Tero Huomo", new EuriborKorko(12));
```

Ohjelman rakenne on nyt seuraava
![](https://github.com/mluukkai/ohjelmistotuotanto2017/raw/master/images/os-3.png)


Muutetaan luokkaa vielä siten, että tilejä saadaan luotua ilman konstruktoria:

``` java
public class Tili {

    private String tiliNumero;
    private String omistaja;
    private double saldo;
    private Korko korko;

    public static Tili luoEuriborTili(String tiliNumero, String omistaja, int kuukausia) {
        return new Tili(tiliNumero, omistaja, new EuriborKorko(kuukausia));
    }

    public static Tili luoMääräaikaisTili(String tiliNumero, String omistaja, double korko) {
        return new MääräaikaisTili(tiliNumero, omistaja, new Tasakorko(korko));
    }

    public static Tili luoKäyttöTili(String tiliNumero, String omistaja, double korko) {
        return new Tili(tiliNumero, omistaja, new Tasakorko(korko));
    }

    protected Tili(String tiliNumero, String omistaja, Korko korko) {
        this.tiliNumero = tiliNumero;
        this.omistaja = omistaja;
        this.korko = korko;
    }

    // ...

    public void vaihdaKorkoa(Korko korko) {
        this.korko = korko;
    }
}
```

Lisäsimme luokalle 3 staattista apumetodia helpottamaan tilien luomista. Tilejä voidaan nyt luoda seuraavasti:

``` java
Tili määräaikais = Tili.luoMääräaikaisTili("1234-1234", "Kasper Hirvikoski", 2.5);
Tili euribor12 = Tili.luoEuriborTili("4422-3355", "Tero Huomo", 12 );
Tili fyrkka = Tili.luoEuriborTili("7895-4571", "Esko Ukkonen", 10.75 );
```

### Factory

Käyttämämme periaate olioiden luomiseen staattisten metodien avulla on hyvin tunnettu suunnittelumalli *staattinen tehdasmetodi, engl. static factory method*.

Tili-esimerkissä käytetty static factory method on yksinkertaisin erilaisista tehdas-suunnittelumallin varianteista. Periaatteena suunnittelumallissa on, se, että luokalle tehdään staattinen tehdasmetodi tai metodeja, jotka käyttävät konstruktoria ja luovat luokan ilmentymät. Konstruktorin suora käyttö usein estetään määrittelemällä konstruktori privateksi.

Tehdasmetodin avulla voidaan piilottaa olion luomiseen liittyviä yksityiskohtia, esimerkissä Korko-rajapinnan toteuttavien olioiden luominen ja jopa olemassaolo oli tehdasmetodin avulla piilotettu tilin käyttäjältä. 

Tehdasmetodin avulla voidaan myös piilottaa käyttäjältä luodun olion todellinen luokka, esimerkissä näin tehtiin määräaikaistilin suhteen.

Tehdasmetodi siis auttaa _kapseloinnissa_, olion luomiseen liittyvät detaljit ja jopa olion todellinen luonne piilottuu olion käyttäjältä. Tämä taas mahdollistaa erittäin joustavan laajennettavuuden. 

Staattinen tehdasmetodi ei ole testauksen kannalta erityisen hyvä ratkaisu, esimerkissämme olisi vaikea luoda tili, jolle annetaan Korko-rajapinnan toteuttama mock-olio. Nyt se tosin onnistuu koska konstruktoria ei ole täysin piilotettu.

Lisätietoa factory-suunnittelumallista esim. seuraavissa https://sourcemaking.com/design_patterns/factory_method ja http://www.oodesign.com/factory-method-pattern.html

Tehdasmetodien avulla voimme siis kapseloida luokan todellisen tyypin. Kasperin tilihän on määräaikaistili, se kuitenkin pyydetään Tili-luokassa sijaitsevalta factoryltä, olion oikea tyyppi on piilotettu tarkoituksella käyttäjältä. Määräaikaistilin käyttäjällä ei siis ole enää konkreettista riippuvuutta luokkaan Määräaikaistili.

Teimme myös metodin jonka avulla tilin korkoa voi muuttaa. Kasperin tasakorkoinen määräaikaistili on helppo muuttaa lennossa kolmen kuukauden Euribor-tiliksi:

```java
määräaikais.vaihdaKorkoa(new EuriborKorko(3));
```

Eli luopumalla perinnästä selkeytyy oliorakenne huomattavasti ja saavutetaan ajonaikaista joustavuuttaa (koronlaskutapa) joka perintää käyttämällä ei onnistu.

### Strategy

Tekniikka jolla koronmaksu hoidetaan on myöskin suunnittelumalli nimeltään *strategia* eli *englanniksi strategy*. 

Strategyn avulla voidaan hoitaa tilanne, jossa eri olioiden käyttäytyminen on muuten sama, mutta tietyissä kohdissa on käytössä eri "algoritmi". Esimerkissämme tämä algoritmi oli korkoprosentin määritys. Sama tilanne voidaan hoitaa usein myös perinnän avulla käyttämättä erillisiä olioita, strategy kuitenkin mahdollistaa huomattavasti dynaamisemman ratkaisun, sillä strategia-olioa voi vaihtaa ajoaikana. Strategyn käyttö ilmentää hienosti "favour composition over inheritance"-periaatetta

Lisätietoa strategia-suunnittelumallista seuraavissa http://www.oodesign.com/strategy-pattern.html ja https://sourcemaking.com/design_patterns/strategy

### Tilin luominen

Loimme äsken luokalle _Tili_ staattiset apumetodit tilien luomista varten. Voisi kuitenkin olla järkevämpää siirtää vastuu tilien luomisesta erillisen luokan, _pankin_ vastuulle. Pankki voi helposti hallinnoida myös tilinumeroiden generointia:

``` java
public class Pankki {
    private int numero;
        
    private String generoiTilinro() {
        numero++;
        return "12345-"+numero;
    }
    
    public Tili kayttotili(String omistaja, double k){
        return new Tili(generoiTilinro(), omistaja, new Tasakorko(k));
    }
    
    public Tili maaraikaistili(String omistaja, double k){
        return new MaaraAikaisTili(generoiTilinro(), omistaja, new Tasakorko(k));
    }    
    
    public Tili euribortili(String tiliNumero, String omistaja, int kk){
        return new Tili(generoiTilinro(), omistaja, new EuriborKorko(kk));
    }        

    public Tili maaraaikaisEuribor(String tiliNumero, String omistaja, int kk){
        return new MaaraAikaisTili(tiliNumero, omistaja, new EuriborKorko(kk));
    } 
}
```

Tilejä luodaan pankin avulla seuraavasti:

``` java
Pankki spankki = new Pankki();

Tili euriborTili = spankki.euribortili("Kasper Hirvikoski", 6);
Tili maaraaikaistili = spankki.maaraikaistili("Arto Hellas", 0.15);
``` 

eli tililin luojan ei enää tarvitse huolehtia tilinumeroiden generoinnista.

Nyt tehdasmetodista on siis tehty luokan oman staattisen metdoin sijaan toiseen luokkaan sijoitettu oliometodi.

## Laskin ilman iffejä

Olemme laajentaneet Laskin-luokkaa osaamaan myös muita laskuoperaatioita:

``` java
public class Laskin {

    private IO io;

    public Laskin(IO io) {
        this.io = io;
    }

    public void suorita() {
        while (true) {
            io.print("komento: ");
            String komento = io.nextLine();
            if (komento.equals("lopetus")) {
                return;
            }

            io.print("luku 1: ");
            int luku1 = io.nextInt();

            io.print("luku 2: ");
            int luku2 = io.nextInt();

            int vastaus = 0;

            if ( komento.equals("summa") ){
                vastaus = laskeSumma(luku1, luku2);
            } else if ( komento.equals("tulo") ){
                vastaus = laskeTulo(luku1, luku2);
            } else if ( komento.equals("erotus") ){
                vastaus = laskeErotus(luku1, luku2);
            }

            io.print("summa: " + vastaus + "\n");
        }
    }

    private int laskeSumma(int luku1, int luku2) {
        return luku1 + luku2;
    }

    private int laskeTulo(int luku1, int luku2) {
        return luku1 * luku2;
    }

    private int laskeErotus(int luku1, int luku2) {
        return luku1-luku2;
    }
}
```

Ratkaisu ei ole kaikin puolin tyydyttävä. Entä jos haluamme muitakin operaatioita kuin summan, tulon ja erotuksen? if-hässäkkä tulee kasvamaan.

Päätämme siirtyä _strategia-suunnittelumallin_ käyttöön, eli hoidetaan laskuoperaatio omassa luokassaan. Rajapinnan sijasta käytämme tällä kertaa abstraktia luokkaa:

``` java
public abstract class Operaatio {

    protected int luku1;
    protected int luku2;

    public Operaatio(int luku1, int luku2) {
        this.luku1 = luku1;
        this.luku2 = luku2;
    }

    public static Operaatio luo(String operaatio, int luku1, int luku2) {
        if (operaatio.equals("summa")) {
            return new Summa(luku1, luku2);
        } else if (operaatio.equals("tulo")) {
            return new Tulo(luku1, luku2);
        }
        return new Erotus(luku1, luku2);
    }

    public abstract int laske();
}

public class Summa extends Operaatio {

    public Summa(int luku1, int luku2) {
        super(luku1, luku2);
    }

    @Override
    public int laske() {
        return luku1 + luku2;
    }
}

public class Tulo extends Operaatio {

    public Tulo(int luku1, int luku2) {
        super(luku1, luku2);
    }

    @Override
    public int laske() {
        return luku1 * luku2;
    }
}

public class Erotus extends Operaatio {

    public Erotus(int luku1, int luku2) {
        super(luku1, luku2);
    }

    @Override
    public int laske() {
        return luku1 - luku2;
    }
}
```

Laskin-luokka yksinkertaistuu huomattavasti:

``` java
public class Laskin {

    private IO io;

    public Laskin(IO io) {
        this.io = io;
    }

    public void suorita() {
        while (true) {
            io.print("komento: ");
            String komento = io.nextLine();
            if (komento.equals("lopetus")) {
                return;
            }

            io.print("luku 1: ");
            int luku1 = io.nextInt();

            io.print("luku 2: ");
            int luku2 = io.nextInt();

            Operaatio operaatio = Operaatio.luo(komento, luku1, luku2);

            io.print(komento + ": " + operaatio.laske() + "\n");
        }
    }
}
```

Hienona puolena laskimessa on nyt se, että voimme lisätä operaatioita ja Laskinta ei tarvitse muuttaa millään tavalla!

Rakenne näyttää seuraavalta
![](https://github.com/mluukkai/ohjelmistotuotanto2017/raw/master/images/os-4.png)


Entä jos haluamme laskimelle muunkinlaisia kuin 2 parametria ottavia operaatioita, esim. neliöjuuren?

Jatkamme muokkaamista seuraavassa luvussa

## laskin ja komento-olio

Muutamme Operaatio-luokan olemusta, päädymme jo oikeastaan Strategy-suunnittelumallin lähisukulaisen _Command_-suunnittelumallin puolelle ja annammekin sille nimen Komento ja teemmie siitä rajapinnan sillä siirrämme erillisten komento-olioiden luomisen Komentotehdas-luokalle:

``` java
public interface Komento {
    void suorita();
}
```

Komento-rajapinta on siis äärimmäisen yksinkertainen. Komennon voi ainoastaan suorittaa eikä se edes palauta mitään!

Komento-olioita luova komentotehdas on seuraavassa:

``` java
public class Komentotehdas {

    private IO io;

    public Komentotehdas(IO io) {
        this.io = io;
    }

    public Komento hae(String operaatio) {
        if (operaatio.equals("summa")) {
            return new Summa(io);
        } else if (operaatio.equals("tulo")) {
            return new Tulo(io);
        } else if (operaatio.equals("nelio")) {
            return new Nelio(io);
        } else if (operaatio.equals("lopeta")) {
            return new Lopeta();
        }
        return new Tuntematon(io);
    }
}
```
Komentotehdas siis palauttaa hae-metodin merkkijonoparametria vastaavan komennon. Koska vastuu käyttäjän kanssa kommunikoinnista on siirretty Komento-olioille, annetaan niille IO-olio konstruktorissa.

if-hässäkkä näyttää hieman ikävältä. Mutta hetkinen! Voisimme tallentaa erilliset komennon HashMap:iin:

``` java
public class Komentotehdas {
    private HashMap<String, Komento> komennot;

    public Komentotehdas(IO io) {
        komennot = new HashMap<String, Komento>();
        komennot.put("summa", new Summa(io));
        komennot.put("tulo", new Tulo(io));
        komennot.put("nelio", new Nelio(io));
        komennot.put("tuntematon", new Tuntematon(io));
    }

    public Komento hae(String operaatio) {
        Komento komento = komennot.get(operaatio);
        if (komento == null) {
            komento = komennot.get("tuntematon");
        }
        return komento;
    }
}
```

Pääsimme kokonaan eroon if-ketjusta, loistavaa!

Yksittäiset komennot ovat hyvin yksinkertaisia:

``` java
public class Nelio implements Komento {
    private IO io;

    public Nelio(IO io) {
        this.io = io;
    }

    @Override
    public void suorita() {
        io.print("luku 1: ");
        int luku = io.nextInt();

        io.print("vastaus: "+luku*luku);
    }
}

public class Tuntematon implements Komento {
    private IO io;

    public Tuntematon(IO io) {
        this.io = io;
    }

    @Override
    public void suorita() {
        io.print("sallitut komennot: summa, tulo, nelio, lopeta");
    }
}

public class Lopeta implements Komento {
    private IO io;

    public Lopeta(IO io) {
        this.io = io;
    }

    @Override
    public void suorita() {
        io.print("kiitos ja näkemiin");
        System.exit(0);
    }

}
```

Ohjelman rakenne tässä vaiheessa

![](https://github.com/mluukkai/ohjelmistotuotanto2017/raw/master/images/os-5.png)


### Command

Eristämme siis jokaiseen erilliseen laskuoperaatioon liittyvä toiminnallisuuden omaksi oliokseen command-suunnittelumallin ideaa nodattaen, eli siten, että kaikki operaatiot toteuttavat yksinkertaisen rajapinnan, jolla on ainoastaan metodi public <code>void suorita()</code>

Ohjelman edellisessä versiossa sovelsimme strategia-suunnittelumallia, missä erilliset laskuoperaatiot oli toteutettu omina olioinaan. Command-suunnittelumalli eroaa siinä, että olemme nyt kapseloineet koko komennon suorituksen, myös käyttäjän kanssa käytävän kommunikoinnin omiin olioihin. Komento-olioiden rajapinta on yksinkertainen, niillä on ainoastaan yksi metodi _suorita_. Strategia-suunnittelumallissa taas strategia-olioiden rajapinta vaihtelee tilanteen mukaan. 

Esimerkissä komennot luotiin tehdasmetodin tarjoavan olion avulla, if:it piilotettiin tehtaan sisälle. Komento-olioiden suorita-metodi suoritettiin esimerkissä välittömästi, näin ei välttämättä ole, komentoja voitaisiin laittaa esim. jonoon ja suorittaa myöhemmin. Joskus komento-olioilla metodin _suorita_ lisäksi myös metodi _peru_, mikä kumoaa komennon suorituksen aiheuttaman toimenpiteen. Esim. editorien undo- ja redo-toiminnallisuus toteutetaan säilyttämällä komento-olioita jonossa. Toteutamme viikon 6 laskareissa _peru_-toiminnallisuuden laskimen komennoille.

Lisää command-suunnittelimallista esim. seuraavissa ttp://www.oodesign.com/command-pattern.html
http://sourcemaking.com/design_patterns/command

### lisää komentoja

Jatketaan laskimen komentojen toteuttamista.

Koska kaksi parametria käyttäjältä kysyvillä komennoilla, kuten summa, tulo ja erotus on paljon yhteistä, luodaan niitä varten yliluokka:

``` java
public abstract class KaksiparametrinenLaskuoperaatio implements Komento {

    protected IO io;
    protected int luku1;
    protected int luku2;

    public KaksiparametrinenLaskuoperaatio(IO io) {
        this.io = io;
    }

    @Override
    public void suorita() {
        io.print("luku 1: ");
        int luku1 = io.nextInt();

        io.print("luku 2: ");
        int luku2 = io.nextInt();

        io.print("vastaus: "+laske());
    }

    protected abstract int laske();
}

public class Summa extends KaksiparametrinenLaskuoperaatio {

    public Summa(IO io) {
        super(io);
    }

    @Override
    protected int laske() {
        return luku1+luku2;
    }
}

public class Tulo extends KaksiparametrinenLaskuoperaatio {

    public Tulo(IO io) {
        super(io);
    }

    @Override
    public int laske() {
        return luku1*luku2;
    }
}
```

Ja lopulta luokka Laskin, jossa ei ole enää juuri mitään jäljellä:

``` java
public class Laskin {

    private IO io;
    private Komentotehdas komennot;

    public Laskin(IO io) {
        this.io = io;
        komennot = new Komentotehdas(io);
    }

    public void suorita() {
        while (true) {
            io.print("komento: ");
            String komento = io.nextLine();
            komennot.hae(komento).suorita();
        }
    }
}
```

Ohjelmasta on näinollen saatu laajennettavuudeltaan varsin joustava. Uusia operaatioita on helppo lisätä ja lisäys ei aiheuta muutoksia moneen kohtaan koodia. Laskin-luokallahan ei ole riippuvuuksia muualle kuin rajapintoihin IO ja Komento ja luokkaan Komentotehdas.

![](https://github.com/mluukkai/ohjelmistotuotanto2017/raw/master/images/os-6.png)

Hintana joustavuudelle on luokkien määrän kasvu. Nopealla vilkaisulla saattaakin olla vaikea havaita miten ohjelma toimii, varsinkaan jos ei ole vastaavaan tyyliin tottunut, mukaan on nimittäin piilotettu factory- ja command-suunnittelumallien lisäksi suunnittelumalli __template method__ (kaksiparametrisen komennon toteutukseen). 

### template method

Template method -suunnittelumallia sopii tilanteisiin, missä kahden tai useamman operation suoritus on hyvin samankaltainen ja poikkeaa ainoastaan yhden tai muutaman operaatioon liittyvän askeleen kohdalla.

Summa- ja Tulo-komentojen suoritus on oleellisesti samanlainen:

<pre>
Lue luku1 käyttäjältä
Lue luku2 käyttäjältä
Laske operaation tulos
Tulosta operaation tulos
</pre>

Ainoastaan kolmas vaihe eli operaation tuloksen laskeminen eroaa summaa ja tuloa selvitettäessä.

Template methodin hengessä asia hoidetaan tekemällä abstrakti yliluokka, joka sisältää metodin _suorita()_ joka toteuttaa koko komennon suorituslogiikan:

```java
public abstract class KaksiparametrinenLaskuoperaatio implements Komento {

    @Override
    public void suorita() {
        io.print("luku 1: ");
        int luku1 = io.nextInt();

        io.print("luku 2: ");
        int luku2 = io.nextInt();

        io.print("vastaus: "+laske());
    }

    protected abstract int laske();
}
```


Suorituslogiikan vaihtuva osa eli operaation laskun tulos on määritelty abstraktina metodina _laske()_ jota metodi _suorita()_ kutsuu.

Konkreettiset toteutukset Summa ja Tulo ylikirjoittavat abstraktin metodin _laske()_, määrittelemällä miten laskenta tietyssä konkreettisessa, esim. laskettaessa summaa tapahtuu:

```java
public class Summa extends KaksiparametrinenLaskuoperaatio {

    @Override
    protected int laske() {
        return luku1+luku2;
    }
}
```

Abstraktin luokan määrittelemä _suorita()_ on _template-metodi_, joka määrittelee suorituksen siten, että suorituksen eroava osa määritellään yliluokan abstraktina metodina, jonka aliluokat ylikirjoittavat. Template-metodin avulla siis saadaan määriteltyä "geneerinen algoritmirunko", jota voidaan aliluokissa erikoistaa sopivalla tavalla.

Template-metodeita voi olla useampiakin kuin yksi eroava osa, tällöin abstrakteja metodeja määritellään tarpeellinen määrä. 

Strategy-suunnittelumalli on osittain samaa sukua Template-metodin kanssa, siinä kokonainen algoritmi tai algoritmin osa korvataan erillisessä luokassa toteutetulla toteutuksella.
Strategioita voidaan vaihtaa ajonaikana, template-metodissa olio toimii samalla tavalla koko elinaikansa  

Lisää template method -suunnittelumallista seuraavissa
http://www.oodesign.com/template-method-pattern.html
http://www.netobjectives.com/PatternRepository/index.php?title=TheTemplateMethodPattern

## Koodissa olevan epätriviaalin copypasten poistaminen Strategy-patternin avulla, Java 8:a hyödyntävä versio

Tarkastellaan [Project Gutenbergistä](http://www.gutenberg.org/) löytyvien kirjojen sisällön analysointiin tarkoitettua luokkaa <code>GutenbergLukija</code>:

``` java
public class GutenbergLukija {

    private List<String> rivit;

    public GutenbergLukija(String osoite) throws IllegalArgumentException {
        rivit = new ArrayList<String>();
        try {
            URL url = new URL(osoite);
            Scanner lukija = new Scanner(url.openStream());
            while (lukija.hasNextLine()) {
                rivit.add(lukija.nextLine());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<String> rivit() {
        List<String> palautettavat = new ArrayList<>();

        for (String rivi : rivit) {
            palautettavat.add(rivi);
        }

        return palautettavat;
    }

    public List<String> rivitJotkaPaattyvatHuutomerkkiin() {
        List<String> ehdonTayttavat = new ArrayList<>();

        for (String rivi : rivit) {
            if (rivi.endsWith("!")) {
                ehdonTayttavat.add(rivi);
            }
        }

        return ehdonTayttavat;
    }

    public List<String> rivitJoillaSana(String sana) {
        List<String> ehdonTayttavat = new ArrayList<String>();

        for (String rivi : rivit) {
            if (rivi.contains(sana)) {
                ehdonTayttavat.add(rivi);
            }
        }

        return ehdonTayttavat;
    }
}
```

Luokalla on kolme metodia, kaikki kirjan rivit palauttava <code>rivit</code> sekä <code>rivitJotkaPaattyvatHuutomerkkiin</code> ja <code>rivitJoillaSana(String sana)</code> jotka toimivat kuten metodin nimi antaa ymmärtää.

Luokkaa käytetään seuraavasti:

```java
public static void main(String[] args) {
    String osoite = "https://www.gutenberg.org/files/2554/2554-0.txt";
    GutenbergLukija kirja = new GutenbergLukija(osoite);

    for( String rivi : kirja.rivitJoillaSana("beer") ) {
        System.out.println(rivi)
    }
}
```

Tutustutaan tehtävässä hieman [Java 8:n](http://docs.oracle.com/javase/8/docs/api/) tarjoamiin uusiin ominaisuuksiin. Monelle Java 8 on jo tuttu Ohjelmoinnin perusteiden ja jatkokurssin uudemmista versiosta.

Voimme korvata listalla olevien merkkijonojen tulostamisen kutsumalla listoilla (tarkemmin sanottuna rajapinnan [Interable](http://docs.oracle.com/javase/8/docs/api/java/lang/Iterable.html)-toteuttavilla) olevaa metodia <code>forEach</code> joka mahdollistaa listan alkioiden läpikäynnin "funktionaaliseen" tyyliin. Metodi saa parametrikseen "functional interfacen" (eli rajapinnan, joka määrittelee ainoastaan yhden toteutettavan metodin) toteuttavan olion. Tälläisiä ovat Java 8:ssa myös ns. lambda-lausekkeet (lambda expression), joka tarkoittaa käytännössä anonyymia mihinkään luokkaan liittymätöntä metodia.  Seuraavassa metodin palauttavien kirjan rivien tulostus forEachia ja lambdaa käyttäen:

``` java
public static void main(String[] args) {
    String osoite = "https://www.gutenberg.org/files/2554/2554-0.txt";
    GutenbergLukija kirja = new GutenbergLukija(osoite);

    kirja.rivitJoillaSana("beer").forEach(s->System.out.println(s));
}
```

Esimerkissä lambdan syntaksi oli seuraava:

``` java
s -> System.out.println(s)
```

parametri <code>s</code> saa arvokseen yksi kerrallaan kunkin läpikäytävän tekstirivin. Riveille suoritetaan "nuolen" oikealla puolella oleva tulostuskomento. Lisää lambdan syntaksista [täältä](http://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html). Huomionarvoista on se, että lambdan parametrin eli muuttujan <code>s</code> tyyppiä ei tarvitse määritellä, kääntäjä osaa päätellä sen iteroitavana olevan kokoelman perusteella.

Luokan <code>GutenbergLukija</code> tarjoamat 3 kirjan sisällön hakemiseen tarkoitettua metodia ovat selvästi rakenteeltaan hyvin samantapaisia. Kaikki käyvät jokaisen kirjan rivin läpi ja palauttavat niistä osan (tai kaikki) metodin kutsujalle. Metodit eroavat sen suhteen mitä kirjan riveistä ne palauttavat. Voidaankin ajatella, että jokaisessa metodissa on oma _strategiansa_ rivien palauttamiseen. Eriyttämällä rivien valintastrategia omaksi luokakseen, voitaisiin selvitä ainoastaan yhdellä rivien läpikäynnin hoitavalla metodilla.

Määritellään rivien valintaa varten rajapinta:

``` java
public interface Ehto {
    boolean test(String rivi);
}
```

Huom: metodin nimen valinta ei ollut täysin sattumanvarainen. Tulemme myöhemmin määrittelemään, että rajapinta <code>Ehto</code> laajentaa rajapinnan, joka vaatii että rajapinnalla on nimenomaan <code>test</code>-niminen metodi.

Ideana on luoda jokaista kirjojen erilaista _hakuehtoa_ kohti oma rajapinnan <code>Ehto</code> toteuttava luokka.

Seuraavassa ehto-luokka, joka tarkastaa sisältyykö tietty sana riville:

``` java
public class SisaltaaSanan implements Ehto {
    private String sana;

    public SisaltaaSanan(String sana) {
        this.sana = sana;
    }

    @Override
    public boolean test(String rivi) {
        return rivi.contains(sana);
    }
}
```

Jos luokasta luodaan ilmentymä

``` java
Ehto ehto = new SisaltaaSanan("olut");
```

voidaan luokan avulla tarkastella sisältävätkö merkkijonot sanan _olut_:


``` java
Ehto ehto = new SisaltaaSanan("olut");
ehto.test("internetin paras suomenkielinen olutsivusto on olutopas.info");
ehto.test("Java 8 ilmestyi 18.3.2014");
```

Ensimmäinen metodikutsuista palauttaisi _true_ ja jälkimäinen _false_.

Kirjasta voidaan nyt palauttaa oikean ehdon täyttävät sanat lisäämällä luokalle <code>GutenbergLukija</code> metodi:

``` java
    public List<String> rivitJotkaTayttavatEhdon(Ehto ehto) {
        List<String> palautettavatRivit = new ArrayList<>();

        for (String rivi : rivit) {
            if (ehto.test(rivi)) {
                palautettavatRivit.add(rivi);
            }
        }

        return palautettavatRivit;
    }
```

ja sanan _beer_ sisältävät rivit saadaan tulostettua seuraavasti:

``` java
    kirja.rivitJotkaTayttavatEhdon(new SisaltaaSanan("beer")).forEach(s->System.out.println(s));
```

Pääsemmekin sopivien ehto-luokkien määrittelyllä eroon alkuperäisistä rivien hakumetodeista. Sovellus tulee sikälikin huomattavasti joustavammaksi, että uusia hakuehtoja voidaan helposti lisätä määrittelemällä uusia rajapinnan <code>Ehto</code> määritteleviä luokkia.

Ehto-rajapinta on ns. _functional interface_ eli se määrittelee ainoastaan yhden toteutettavan metodin (huom: Java 8:ssa rajapinnat voivat määritellä myös [oletusarvoisen toteutuksen](http://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html) sisältämiä metodeja!). Java 8:n aikana voimme määritellä ehtoja myös lambda-lausekkeiden avulla. Eli ei ole välttämätöntä tarvetta määritellä eksplisiittisesti rajapinnan <code>Ehto</code> toteuttavia luokkia. Seuraavassa edellinen esimerkki käyttäen lambda-lauseketta ehdon määrittelemiseen:

``` java
kirja.rivitJotkaTayttavatEhdon(s->s.contains("beer")).forEach(s->System.out.println(s));
```

Käytännössä siis määrittelemme "lennossa" rajapinnan <code>Ehto</code> toteuttavan luokan, jonka ainoan metodin toiminnallisuuden määritelmä annetaan lambda-lausekkeen avulla.

Lambdojen avulla on helppoa määritellä mielivaltaisia ehtoja. Seuraavassa tulostetaan kaikki rivit, joilla esiintyy jompi kumpi sanoista _beer_ tai _vodka_. Ehdon ilmaiseva lambda-lauseke on nyt määritelty selvyyden vuoksi omalla rivillään:

``` java
Ehto ehto = s -> s.contains("beer") || s.contains("vodka");

kirja.rivitJotkaTayttavatEhdon(ehto).forEach(s->System.out.println(s));
```

Voimme hyödyntää Java 8:n uusia piirteitä myös luokan <code>GutenbergLukija</code> metodissa <code>rivitJotkaTayttavatEhdon</code>.

Metodi on tällä hetkellä seuraava:

``` java
public List<String> rivitJotkaTayttavatEhdon(Ehto ehto) {
    List<String> palautettavatRivit = new ArrayList<>();

    for (String rivi : rivit) {
        if (ehto.test(rivi)) {
            palautettavatRivit.add(rivi);
        }
    }

    return palautettavatRivit;
}
```

Java 8:ssa kaikki rajapinnan <code>Collection</code> toteuttavat luokat mahdollistavat alkioidensa käsittelyn <code>Stream</code>:ina eli "alkiovirtoina", ks. [API-kuvaus](http://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html). Kokoelmaluokasta saadaan sitä vastaava alkiovirta kutsumalla kokoelmalle metodia <code>stream</code>.

Alkiovirtoja on taas mahdollista käsitellä monin tavoin, ja meitä nyt kiinnostava metodi on <code>filter</code>, jonka avulla streamistä voidaan tehdä uusi streami, josta on poistettu ne alkiot, jotka eivät täytä filtterille annettua boolean-arvoista, funktionaalisen rajapinnan <code>Predicate<String></code> toteuttavaa ehtoa.

Määrittelemämme rajapinta <code>Ehto</code> on oikeastaan juuri tarkoitukseen sopiva, jotta voisimme käyttää rajapintaa, tulee meidän kuitenkin tyyppitarkastusten takia määritellä että rajapintamme laajentaa rajapintaa  <code>Predicate<String></code>:

``` java
import java.util.function.Predicate;

public interface Ehto extends Predicate<String>{
    boolean test(String rivi);
}
```

Nyt saamme muutettua kirjan rivien streamin _ehdon_ täyttävien rivien streamiksi seuraavasti:


``` java
public List<String> rivitJotkaTayttavatEhdon(Ehto ehto) {
    // ei toimi vielä
    rivit.stream().filter(ehto)
}
```

Metodin tulee palauttaa filtteröidyn streamin alkioista koostuva lista. Stream saadaan muutettua listaksi "keräämällä" sen sisältämät alkiot kutsumalla streamille metodia <code>collect</code> ja määrittelemällä, että palautetaan streamin sisältämät alkiot niemenomaan listana. Näin luotu filtteröity lista voidaan sitten palauttaa metodin kutsujalle.

Metodi on siis seuraavassa:

``` java
public List<String> rivitJotkaTayttavatEhdon(Ehto ehto) {
    return rivit.stream().filter(ehto).collect(Collectors.toList());
}
```

Kuten huomaamme, Javan version 8 tarjoamat funktionaaliset piirteet muuttavat lähes vallankumouksellisella tavalla kielen ilmaisuvoimaa!


## Komposiitti

Dokumentti koostuu erilaisista elementeistä. Elementtejä ovat mm.

* normaalit tekstielementit
* erotinelementit, erotin tulostuu viivana
* kooste-elementit
  * sisältävät listan elementtejä
  * kooste tulostuu samoin kuin sen sisältämän elementtilistan elementit tulostuvat

Haluamme käyttää dokumenttia seuraavaan tapaan:

``` java
public static void main(String[] args) {
    Dokumentti doku = new Dokumentti();

    Elementti detalji = Elementtitehdas.kooste(
            Elementtitehdas.teksti("kannattaa myös huomata builderi"),
            Elementtitehdas.teksti("sopii joihinkin tilanteisiin factoryä paremmin"));


    Elementti asiaa = Elementtitehdas.kooste(
            Elementtitehdas.teksti("Factory-metodit helpottavat olioiden luomista"),
            Elementtitehdas.teksti("ei tarvetta new:lle ja konkreettiset riippuvuudet vähenevät"),
            detalji);

    doku.lisaa(Elementtitehdas.teksti("Suunnittelumallit"));
    doku.lisaa(Elementtitehdas.erotin());
    doku.lisaa(asiaa);
    doku.lisaa(Elementtitehdas.teksti("yhteenvetona voidaan todeta, että kannattaa käyttää"));
    doku.lisaa(Elementtitehdas.erotin());

    doku.print();
    doku.tallenna("suunnittelumallit.txt");
}
```

Tulostuu:

<pre>
Suunnittelumallit
-------------------------
Factory-metodit helpottavat olioiden luomista
ei tarvetta new:lle ja konkreettiset riippuvuudet vähenevät
kannattaa myös huomata builderi
sopii joihinkin tilanteisiin factoryä paremmin
yhteenvetona voidaan todeta, että kannattaa käyttää
-------------------------
</pre>

Luokka Dokumentti on suoraviivainen:

``` java
public class Dokumentti {
    private List<Elementti> elementit;

    public Dokumentti() {
        elementit = new ArrayList<Elementti>();
    }

    public void lisaa(Elementti elementti){
        elementit.add(elementti);
    }

    public void print(){
        for (Elementti elementti : elementit) {
            elementti.tulosta();
        }
    }

    public void tallenna(String tiedosto){
        // to be implemented
    }
}
```

Käyttäjää varten on siis luotu elementtitehdas jonka avulla elementtejä voidaan muodostaa:

``` java
public class Elementtitehdas {
    public static Elementti erotin(){
        return new ErotinElementti();
    }

    public static Elementti teksti(String teksti){
        return new TekstiElementti(teksti);
    }

    public static Elementti kooste(Elementti... elementit){
        return new KoosteElementti(elementit);
    }
}
```
Ainoa huomionarvoinen seikka on viimeisen rakentajametodin varargs-tyyppinen parametri, jos se ei ole tuttu, ks esim: [http://www.javadb.com/using-varargs-in-java](http://www.javadb.com/using-varargs-in-java)

Käytännössä varargs-parametri tarkoittaa, että metodilla saa olla Elementti-tyyppisiä parametreja vapaavalintainen määrä.

Dokumentin sisältävien elementtien toteuttamiseen sopii erinomaisesti *komposiitti (engl composite) -suunnittelumalli*, ks. esim. [http://sourcemaking.com/design_patterns/composite](http://sourcemaking.com/design_patterns/composite)

Elementti on rajapinta joka määrittelee kaikkien elementtien yhteisen toiminnallisuuden:

``` java
public interface Elementti {
    void tulosta();
}
```

Yksinkertaiset elementit ovat triviaaleja:

``` java
public class ErotinElementti implements Elementti{

    public void tulosta() {
        System.out.println("-------------------------");
    }

}

public class TekstiElementti implements Elementti {

    String teksti;

    public TekstiElementti(String teksti) {
        this.teksti = teksti;
    }

    public void tulosta() {
        System.out.println(teksti);
    }
}
```

KoosteElementti sisältää listan elementtejä, lista annetaan konstruktorin parametrina, jälleen varargsia hyödyntäen. Kooste tulostaa itsensä pyytämällä kaikkia osiaan tulostumaan:

``` java
public class KoosteElementti implements Elementti {
    private List<Elementti> osat;

    public KoosteElementti(Elementti... osat) {
        this.osat = new ArrayList<Elementti>(Arrays.asList(osat));
    }

    public void tulosta() {
        for (Elementti osa : osat) {
            osa.tulosta();
        }
    }

}
```

Koska KoosteElementti toteuttaa itsekin rajapinnan Elementti, tarkoittaa tämä että kooste voi sisältää koosteita. Eli hyvin yksinkertaisella luokkarakenteella saadaan aikaan mielivaltaisista puumaisesti muodostuneista elementeistä koostuvia dokumentteja!

Ohjelman rakenne tällä hetkellä

![](https://github.com/mluukkai/ohjelmistotuotanto2017/raw/master/images/os-7.png)

Huomaamme, että <code>Elementti</code> on _funktionaalinen rejapinta_ eli se määrittelee ainoastaan yhden sen metodin joka rajapinnan toteuttavien luokkien on toteutettava. Kuten [edellisessä esimerkissä](https://github.com/mluukkai/ohjelmistotuotanto2018/blob/master/web/oliosuunnittelu.md#koodissa-olevan-epätriviaalin-copypasten-poistaminen-strategy-patternin-avulla-java-8a-hyödyntävä-versio) totesimme voimme käyttää Java 8:n lambda-lausekkeita korvaamaan funktionaalisen rajapinnan toteuttavien luokkien instanssien tilalla. Koska luokat <code>TekstiElementti</code>, <code>ErotinElementti</code> ja <code>KoosteElementti</code> ovat niin yksinkertaisia, ei luokkia välttämättä tarvitse määritellä eksplisiittisesti. Voimmekin palauttaa elementtitehtaasta niiden tilalla sopivat lambda-lausekkeen avulla määritellyt elementit:

``` java
public class Elementtitehdas {
    public static Elementti erotin(){
        return ()->{ System.out.println("-------------------------"); };
    }

    public static Elementti teksti(String teksti){
        return ()->{ System.out.println(teksti); };
    }

    public static Elementti kooste(Elementti... elementit){
        return () -> { Stream.of(elementit).forEach(e->e.tulosta()); };
    }
}
```

Riittää siis että kukin tehdasmetodi palauttaa lambda-lausekkeen, joka määrittelee kyseessä olevan elementin metodin <code>tulosta</code> toiminnallisuuden.

## Proxy

Oletetaan että asiakas haluaa elementtityypin WebElementti joka kapseloi tietyssä www-osoitteessa olevan sisällön. Ei ongelmaa:

``` java
public class WebElementti implements Elementti {

    private String source;

    public WebElementti(String url) {
        try {
            Scanner lukija = new Scanner(new URL(url).openStream());
            while( lukija.hasNextLine()) {
                source+= lukija.nextLine();
            }
        } catch (Exception e) {
            source = "page "+url+" does not exist";
        }
    }

    public void tulosta() {
        System.out.println(source);
    }
}
```

Hieman ruma koodi (konstruktori tekee vähän liian monta asiaa), mutta toimii.

Laajentamalla elementtitehdasta sopivasti pääsemme käyttämään dokumentin uusia ominaisuuksia:


``` java
public static void main(String[] args) {
    Dokumentti doku = new Dokumentti();

    doku.lisaa(Elementtitehdas.web("http://www.jatkoaika.fi"));
    doku.lisaa(Elementtitehdas.web("http://olutopas.info/"));

    doku.tallenna("webista.html");
}
```

Asiaks toteaa, että hänellä on usein tarve koostaa "varalta" webelementtejä sisältäviä dokumentteja. Dokumenteista ei kuitenkaan todellisuudessa tarvita kuin muutamaa, niitä pitää olla kuitenkin määriteltynä valmiina suuria määriä.

Ongelmaksi muodostuu nyt se, että elementtien lataaminen webistä on hidasta. Ja on ikävää jos elementtejä on pakko ladata suuria määriä kaiken varalta.

Proxy-suunnittelumalli tuo ongelmaan ratkaisun. Periaatteena on luoda varsinaiselle "raskaalle" oliolle edustaja joka toimii raskaan olion sijalla niin kauan kunnes olioa oikeasti tarvitaan. Tälläisessä tilanteessa edustaja sitten luo todellisen olion ja delegoi sille kaikki operaatiot.

Lisää proxystä esim. https://sourcemaking.com/design_patterns/proxy

Tehdään WebElementille proxy:


``` java
public class WebElementtiProxy implements Elementti {
    private String url;
    private WebElementti webElementti;

    public WebElementtiProxy(String url) {
        this.url = url;
    }

    public void tulosta() {
        if ( webElementti==null ) {
            webElementti = new WebElementti(url);
        }
        webElementti.tulosta();
    }
}
```

Eli proxy luo varsinaisen olion vasta kun metodia tulosta() kutsutaan ensimmäisen kerran.

Elementtitehdas konfiguroidaan antamaan WebElementin käyttäjille proxy. Käyttäjät eivät eivät tiedä proxystä mitään ja luulevat käyttävänsä koko ajan täysimittaista olioa!


``` java
public class Elementtitehdas {
    // ...

    public static Elementti web(String url){
        return new WebElementtiProxy(url);
    }
}
```

Ohjelman rakenne täydentyy muotoon

![](https://github.com/mluukkai/ohjelmistotuotanto2017/raw/master/images/os-8.png)

Asiakas on tyytyväinen aikaansaannokseemme.

## Dekoroitu Random

Alla on yksinkertainen sovellus, joka arpoo viikon lottorivin ja antaa käyttäjän tarkastaa kuinka monta käyttäjän syöttämistä numeroista oli viikon lottonumeroita.

``` java
public class ViikonLottonumerot {

    public static void main(String[] args) {
        ViikonLottonumerot lotto = new ViikonLottonumerot();

        int[] omat = { 1, 4, 7, 8, 33, 24, 12 };
        System.out.println( "omat numerot: "+Arrays.toString(omat));
        System.out.println( "oikein: "+lotto.oikeita(omat));
        System.out.println( "arvottulottorivi oli: "+lotto );

    }

    private ArrayList<Integer> viikonLottorivi;

    public ViikonLottonumerot() {
        Random arpa = new Random();

        viikonLottorivi = new ArrayList<Integer>();
        int vielaArvottavana = 7;
        while (vielaArvottavana > 0) {
            int arvottu = 1 + arpa.nextInt(39);
            if (!viikonLottorivi.contains(arvottu)) {
                viikonLottorivi.add(arvottu);
                vielaArvottavana--;
            }
        }

    }

    public int oikeita(int[] omatNumerot) {
        if (omatNumerot.length != 7) {
            throw new IllegalArgumentException();
        }

        int oikein = 0;

        for (int numero : omatNumerot) {
            if (viikonLottorivi.contains(numero)) {
                oikein++;
            }
        }

        return oikein;
    }

    @Override
    public String toString() {
        return viikonLottorivi.toString();
    }

}
```

Haluamme automatisoida luokan __ViikonLottonumerot__ testit. Miten se on mahdollista? Ensinnäkin muutamme luokan rakennetta siten, että lisäämme toisen konstruktorin, joka mahdollistaa __Random__-olion injektoinnin luokalle.

``` java
public class ViikonLottonumerot {

    private ArrayList<Integer> viikonLottorivi;

    public ViikonLottonumerot() {
        this(new Random());
    }

    public ViikonLottonumerot(Random arpa) {
        viikonLottorivi = new ArrayList<Integer>();
        int vielaArvottavana = 7;
        while (vielaArvottavana > 0) {
            int arvottu = 1 + arpa.nextInt(39);
            if (!viikonLottorivi.contains(arvottu)) {
                viikonLottorivi.add(arvottu);
                vielaArvottavana--;
            }
        }
    }

    // ...

}
```

Säästetään myös parametriton konstruktori jotta luokkaa voi käyttää myös ilman Randomin injektointia.

Tehdään sitten Randomista *dekoroitu versio*, eli toinen luokka, joka näyttää käyttäjän kannalta täysin randomilta, mutta osaa testaamisen kannalta hyödyllisiä asioita:

``` java
public class OmaRandom extends Random {

    private ArrayList<Integer> arvotut;

    public OmaRandom() {
        arvotut = new ArrayList<Integer>();
    }

    @Override
    public int nextInt(int i) {
        int arvottu = super.nextInt(i);
        arvotut.add(arvottu);

        return arvottu;
    }

    public ArrayList<Integer> arvotut(){
        return arvotut;
    }
}
```

OmaRandom perii luokan Random, eli mikä tahansa Randomia käyttävä luokka voi käyttää OmaRandom-luokkaa. Metodi joka arpoo kokonaisluvun on ylikirjoitettu siten, että OmaRandom ottaa talteen arvotun luvun. OmaRandom-luokalle on tehty myös metodi, jolla se palauttaa arvotut luvut.

Näin voimme käyttää dekoroitua randomia testissä siten, että testi tietää mitkä lottonumerot tuli arvotuksi:

``` java
public class ViikonLottonumerotTest {

    OmaRandom random;
    ViikonLottonumerot lotto;

    @Before
    public void setUp() {
        random = new OmaRandom();
        lotto = new ViikonLottonumerot(random);
    }

    @Test
    public void testi1() {
        ArrayList<Integer> oikeat = random.arvotut();

        int[] omat = {1, 2, 3, 4, 5, 6, 7};

        assertEquals(samoja(oikeat, omat), lotto.oikeita(omat));
    }

    // ...

    private int samoja(ArrayList<Integer> oikeat, int[] omat) {
        int sama = 0;
        for (int oma : omat) {
            if (oikeat.contains(oma)) {
                sama++;
            }
        }

        return sama;
    }
}
```

## Dekoroitu pino

Olemme toteuttaneet asiakkaalle pinon:

``` java
public class Pino {

    private LinkedList<String> alkiot;

    public Pino() {
        alkiot = new LinkedList<String>();
    }

    public void push(String alkio){
        alkiot.addFirst(alkio);
    }

    public String pop(){
        return alkiot.remove();
    }

    public boolean empty(){
        return alkiot.isEmpty();
    }
}

public static void main(String[] args) {
    Scanner lukija = new Scanner(System.in);
    Pino pino = new Pino();

    System.out.println("pinotaan, tyhjä lopettaa:");
    while (true) {
        String pinoon = lukija.nextLine();
        if (pinoon.isEmpty()) {
            break;
        }
        pino.push(pinoon);
    }
    System.out.println("pinossa oli: ");
    while (!pino.empty()) {
        System.out.println( pino.pop() );
    }
}
```

Asiakkaamme haluaa pinosta muutaman uuden version:

* KryptattuPino jossa alkiot talletetaan pinoon kryptattuina, alkiot tulevat pinosta ulos normaalisti
* LokiPino jossa tieto pinoamisoperaatioista ja niiden parametreista ja paluuarvoista talletetaan lokiin
* PrepaidPino joka lakkaa toimimasta kun sillä on suoritettu konstruktoriparametrina määritelty määrä operaatioita

On lisäksi toteutettava kaikki mahdolliset kombinaatiot:

* KryptattuLokiPino
* LokiKryptattuPino (erona edelliseen että lokiin ei kirjata parametreja kryptattuna)
* KryptattuPrepaidPino
* KryptattuLokiPrepaidPino
* LokiPrepaidPino

Alkaa kuulostaa pahalta varsinkin kun Product Owner vihjaa, että seuraavassa sprintissä tullaan todennäköisesti vaatimaan lisää versioita pinosta, mm. ÄänimerkillinenPino, RajallisenkapasiteetinPino ja tietysti kaikki kombinaatiot tarvitaan myös...

Onneksi dekoraattori sopii tilanteeseen kuin nyrkki silmään! Luodaan pinon kolme uutta versiota dekoroituina pinoina. Tarkastellaan ensin PrepaidPinoa:

``` java
public class PrepaidPino extends Pino {

    private Pino pino;
    private int krediitteja;

    public PrepaidPino(Pino pino, int krediitteja) {
        this.pino = pino;
        this.krediitteja = krediitteja;
    }

    @Override
    public String pop() {
        if (krediitteja == 0) {
            throw new IllegalStateException("pinossa ei enää käyttöoikeutta");
        }
        krediitteja--;

        return pino.pop();
    }

    @Override
    public void push(String alkio) {
        if (krediitteja == 0) {
            throw new IllegalStateException("pinossa ei enää käyttöoikeutta");
        }
        krediitteja--;
        pino.push(alkio);
    }

    @Override
    public boolean empty() {
        if (krediitteja == 0) {
            throw new IllegalStateException("pinossa ei enää käyttöoikeutta");
        }
        krediitteja--;
        return pino.empty();
    }
}
```

PrepaidPino siis perii pinon, mutta kun tarkkaa katsotaan, niin yliluokan operaatiot ylikirjoitetaan ja yliluokkaa ei hyödynnetä millään tavalla!

PrepaidPino siis perii luokan Pino, mutta se ei käytä "perittyä" pinouttaan, vaan sensijaan PrepaidPino __sisältää__ pinon, jonka se saa konstruktoriparametrina. Tätä sisältämäänsä pinoa PrepaidPino käyttää tallettamaan kaikki alkionsa. Eli jokainen PrepaidPinon operaatio delegoi operaation toiminnallisuuden toteuttamisen sisältämälleen pinolle.

PrepaidPino luodaan seuraavalla tavalla:

``` java
Pino pino = new PrepaidPino(new Pino(), 5);
```

Eli luodaan normaali Pino ja annetaan se PrepaidPinolle konstruktoriparametrina yhdessä pinon krediittien kanssa.

Muut kaksi:

``` java
public class KryptattuPino extends Pino{
    private Pino pino;

    public KryptattuPino(Pino pino) {
        this.pino = pino;
    }

    @Override
    public String pop() {
        String alkio = pino.pop();
        return dekryptaa(alkio);
    }

    @Override
    public void push(String alkio) {
        pino.push(kryptaa(alkio));
    }

    @Override
    public boolean empty() {
        return pino.empty();
    }

    private String dekryptaa(String alkio) {
        String dekryptattu = "";
        for (int i = 0; i < alkio.length(); i++) {
            dekryptattu += (char)(alkio.charAt(i)-1);
        }

        return dekryptattu;
    }

    private String kryptaa(String alkio) {
        String kryptattu = "";
        for (int i = 0; i < alkio.length(); i++) {
            kryptattu += (char)(alkio.charAt(i)+1);
        }

        return kryptattu;
    }
}

public class LokiPino extends Pino {

    private Pino pino;
    private PrintWriter loki;

    public LokiPino(Pino pino, PrintWriter loki) {
        this.pino = pino;
        this.loki = loki;
    }

    @Override
    public String pop() {
        String popattu = pino.pop();
        loki.println("pop: "+popattu);

        return popattu;
    }

    @Override
    public void push(String alkio) {
        loki.println("push: "+alkio);

        pino.push(alkio);
    }

    @Override
    public boolean empty() {
        loki.println("empty: "+pino.empty());

        return pino.empty();
    }
}
```

Eli periaate on sama, pinodekoraattorit LokiPino ja KryptattuPino delegoivat kaikki operaationsa sisältämilleen Pino-olioille.

Koska kaikki dekoraattorit perivät luokan Pino, voidaan dekoraattorille antaa parametriksi toinen dekoraattori. Esim. KryptattuLokiPino luodaan seuraavasti:

``` java
PrintWriter loki = new PrintWriter( new File("loki.txt") );
Pino pino = new KryptattuPino( new LokiPino( new Pino(), loki ) );
```

Dekoroinnin avulla saamme siis suhteellisen vähällä ohjelmoinnilla pinolle paljon erilaisia ominaisuuskombinaatioita. Jos olisimme yrittäneet hoitaa kaiken normaalilla perinnällä, olisi luokkien määrä kasvanut eksponentiaalisesti eri ominaisuuksien määrän suhteen ja uusiokäytöstäkään ei olisi tullut mitään.

Dekorointi siis ei oleellisesti ole perintää vaan delegointia, jälleen kerran oliosuunnitteun periaate "favour composition over inheritance" on näyttänyt voimansa.

Lisää dekoraattori-suunnittelumallista esim. osoitteessa https://sourcemaking.com/design_patterns/decorator 

## Pinotehdas

Huomaamme, että eri ominaisuuksilla varustettujen pinojen luominen on käyttäjän kannalta hieman ikävää. Teemmekin luomista helpottamaan pinotehtaan:

``` java
public class Pinotehdas {
    public Pino prepaidPino(int krediitit){
        return new PrepaidPino(new Pino(), krediitit);
    }

    public Pino lokiPino(PrintWriter loki){
        return new LokiPino(new Pino(), loki);
    }

    public Pino kryptattuPino(){
        return new KryptattuPino(new Pino());
    }

    public Pino kryptattuPrepaidPino(int krediitit){
        return new KryptattuPino(prepaidPino(krediitit));
    }

    public Pino kryptattuLokiPino(PrintWriter loki){
        return new KryptattuPino(lokiPino(loki));
    }

    public Pino prepaidKryptattuLokiPino(int krediitit, PrintWriter loki){
        return new PrepaidPino(kryptattuLokiPino(loki), krediitit);
    }

    // monta monta muuta rakentajaa...
}
```

Factoryluokka on ikävä ja sisältää hirveän määrän metodeja. Jos pinoon lisätään vielä ominaisuuksia, tulee factory karkaamaan käsistä.

Pinon luominen on kuitenkin factoryn ansiosta helppoa:

``` java
Pinotehdas tehdas = new Pinotehdas();

Pino omapino = tehdas.kryptattuPrepaidPino(100);
```
Factoryperiaate ei kyllä ole tilanteeseen ideaali. Kokeillaan rakentaja (engl. builder) -suunnittelumallia:

## Pinorakentaja

Rakentaja-suunnittelumalli sopii tilanteeseemme erittäin hyvin. Pyrkimyksenämme on mahdollistaa pinon luominen seuraavaan tyyliin:

``` java
Pinorakentaja rakenna = new Pinorakentaja();

Pino pino = rakenna.prepaid(10).kryptattu().pino();
```

Rakentajan metodinimet ja rakentajan muuttujan nimi on valittu mielenkiinoisella tavalla. On pyritty mahdollisimman luonnollista kieltä muistuttavaan ilmaisuun pinon luonnissa. Kyseessä onkin oikeastaan [DSL](https://martinfowler.com/bliki/DomainSpecificLanguage.html) (domain specific language) pinojen luomiseen!

Luodaan ensin rakentajasta perusversio, joka soveltuu vasta normaalien pinojen luomiseen:

``` java
    Pinorakentaja rakenna = new Pinorakentaja();

    Pino pino = rakenna.pino();
```

Saamme rakentajan ensimmäisen version toimimaan seuraavasti:

``` java
public class Pinorakentaja {
    Pino pino;

    public Pinorakentaja() {
        pino = new Pino();
    }

    public Pino pino() {
        return pino;
    }
}
```

eli kun <code>Rakentaja</code>-olio luodaan, rakentajan luo pinon. Rakentajan "rakennusvaiheen alla" olevan pinon voi pyytää rakentajalta kutsumalla metodia <code>pino()</code>.

Laajennetaan nyt rakentajaa siten, että voimme luoda prepaidpinoja seuraavasti:

``` java
Pinorakentaja rakenna = new Pinorakentaja();

Pino pino = rakenna.prepaid(10).pino();
```

Jotta edellinen menisi kääntäjästä läpi, tulee rakentajalle lisätä metodi jonka tyyppi on <code>Pinorakentaja prepaid(int kreditit)</code>, eli jotta metodin tuloksena olevalle oliolle voitaisiin kutsua metodia <code>pino</code>, on metodin <code>prepaid</code> palautettava rakentaja. Rakentajamme runko laajenee siis seuravasti:

``` java
public class Pinorakentaja {
    Pino pino;

    public Pinorakentaja() {
        pino = new Pino();
    }

    Pinorakentaja prepaid(int kreditit) {
        // ????
    }

    public Pino pino() {
        return pino;
    }
}
```

Rakentaja siis pitää oliomuuttujassa rakentumassa olevaa pinoa. Kun kutsumme rakentajalle metodia <code>prepaid</code> ideana on, että rakentaja dekoroi rakennuksen alla olevan pinon prepaid-pinoksi. Metodi palauttaa viitteen <code>this</code> eli rakentajan itsensä. Tämä mahdollistaa sen, että metodikutsun jälkeen päästään edelleen käsiksi työn alla olevaan pinoon. Koodi siis seuraavassa:

``` java
public class Pinorakentaja {
    Pino pino;

    public Pinorakentaja() {
        pino = new Pino();
    }

    public Pino pino() {
        return pino;
    }

    Pinorakentaja prepaid(int kreditit) {
        this.pino = new PrepaidPino(pino, kreditit);
        return this;
    }
}
```

Samalla periaatteella lisätään rakentajalle metodit, joiden avulla työn alla oleva pino saadaan dekoroitua lokipinoksi tai kryptaavaksi pinoksi:

``` java
public class Pinorakentaja {
    Pino pino;

    public Pinorakentaja() {
        pino = new Pino();
    }

    public Pino pino() {
        return pino;
    }

    Pinorakentaja prepaid(int kreditit) {
        this.pino = new PrepaidPino(pino, kreditit);
        return this;
    }

    Pinorakentaja kryptattu() {
        this.pino = new KryptattuPino(pino);
        return this;
    }

    Pinorakentaja loggaava(PrintWriter loki) {
        this.pino = new LokiPino(pino, loki);
        return this;
    }
}
```

Rakentajan koodi voi vaikuttaa aluksi hieman hämmentävältä.

Rakentajaa siis käytetään seuraavasti:

``` java
Pinorakentaja rakenna = new Pinorakentaja();

Pino pino = rakenna.kryptattu().prepaid(10).pino();
```
Tässä pyydettiin rakentajalta kryptattu prepaid-pino, jossa krediittejä on 10.

Vastaavalla tavalla voidaan luoda pinoja muillakin ominaisuuksilla:

``` java
Pinorakentaja rakenna = new Pinorakentaja();

Pino pino1 = rakenna.pino();  // luo normaalin pinon
Pino pino2 = rakenna.kryptattu().loggaava(loki).prepaid.pino();  // luo sen mitä odottaa saattaa!
```

Rakentajan toteutus perustuu tekniikkaan nimeltään [method chaining](http://en.wikipedia.org/wiki/Method_chaining) eli metodien ketjutukseen. Metodit jotka ovat muuten luonteeltaan void:eja onkin laitettu palauttamaan rakentajaolio. Tämä taas mahdollistaa metodin kutsumisen toisen metodin palauttamalle rakentajalle, ja näin metodikutsuja voidaan ketjuttaa peräkkäin mielivaltainen määrä. Metodiketjutuksen motivaationa on yleensä saada olion rajapinta käytettävyydeltään mahdollisimman luonnollisen kielen kaltaiseksi DSL:ksi. 

Tällä tekniikalla toteutetuista rajapinnoista käytetään myös nimitystä
[fluent interface](https://martinfowler.com/bliki/FluentInterface.html).

## Adapteri

Äsken käsiteltyjen suunnittelmallien, dekoraattorin, komposiitin ja proxyn yhteinen puoli on, että saman ulkokuoren eli rajapinnan takana voi olla yhä monimutkaisempaa toiminnallisuutta joka on kuitenkin täysin kapseloitu käyttäjältä.

Nyt tarkastelemme tilannetta, jossa meillä on käytettävissä luokka joka oleellisesti ottaen tarjoaa haluamamme toiminnallisuuden, mutta sen rajapinta on hieman vääränlainen. Emme kuitenkaan voi muuttaa alkuperäistä luokkaa sillä muutos rikkoisi luokan muut käyttäjät.

[Adapteri](http://sourcemaking.com/design_patterns/adapter)-suunnittelumalli sopii tälläisiin tilanteisiin. 

Tehdään aiemmasta esimerkistä tutulle Pinolle adapteri HyväPino joka muuttaa metodien nimiä ja tarjoaa muutaman lisätoiminnallisuuden:

``` java
public class HyväPino {
    private Pino pino;

    public HyväPino() {
        pino = new Pino();
    }

    public boolean onTyhja(){
        return pino.empty();
    }

    public boolean eiOleTyhja(){
        return !onTyhja();
    }

    public void pinoon(String pinottava){
        pino.push(pinottava);
    }

    public void pinoon(String... pinottavat){
        for (String pinottava : pinottavat) {
            pinoon(pinottava);
        }
    }

    public String pinosta(){
        return pino.pop();
    }

    public List<String> kaikkiPinosta(){
        ArrayList<String> alkiot = new ArrayList<>();

        while(eiOleTyhja()){
            alkiot.add(pinosta());
        }

        return alkiot;
    }
}
```

Eli adapteri __HyväPino__ kapseloi adaptoitavan Pino-olion jolle se delegoi kaikkien metodiensa toiminnallisuuden suorittamisen. Käyttäjä tuntee vaan HyväPino-luokan:


``` java
public static void main(String[] args) {
    HyväPino pino = new HyväPino();
    pino.pinoon("eka", "toka", "kolmas", "neljäs");

    System.out.println("pinossa oli: ");
    for (String alkio : pino.kaikkiPinosta()) {
        System.out.println( alkio );
    }
}
```

## MVC eli Model View Controller

Model View Controller (MVC) -mallilla tarkoitetaan periaatetta, jonka avulla _model_ eli sovelluslogiikan sisältävät oliot eristetään käyttöliittymän näytöt (view) generoivasta koodista. Toimintaa koordinoivana komponenttina ovat _kontrollerit_, jotka reagoivat käyttäjän syötteisiin kutsumalla tarvittavia model-oliota ja pyytämällä viewejä päivittämään näkymät operaatioiden edellyttämällä tavalla.

Periaatteena on, että model, eli sovellustalogiikka ei tunne kontrollereja eikä näyttöjä ja samaan modelissa olevaan dataan voikin olla useita näyttöjä.

Esim. Javalla tehdyissä käyttöliittymäsovelluksissa painikkeiden klikkailuun reagoimisesta vastaavat _tapahtumankuuntelijat_ ovat MVC-mallia sovellettaessa kontrollereja. 

Koska kontrollerit hoitavat käyttöliittymäspesifejä tehtäviä kuten painikkeisiin reagoimista, niiden ajatellaan esim. kerrosarkkitehtuurista puhuttaessa liittyvän käyttöliittymäkerrokseen. 

Teemme nyt erittäin yksinkertaisen MVC-periaatetta noudattavan sovelluksen käyttäen Javan FX -käyttöliittymäkirjastoa. 

Sovelluslogiikka on seuraavassa:


``` java
public class Sovelluslogiikka {
    private ArrayList<Integer> luvut;

    public Sovelluslogiikka() {
        luvut = new ArrayList<>();
    }

    public ArrayList<Integer> getLuvut() {
        return luvut;
    }

    public void arvoLuku() {
        int luku = 1 + new Random().nextInt(20);
        luvut.add(luku);
    }
}
```

Eli sovelluksella voi arpoa lukuja koko ajan uusia lukuja. Sovelluslogiikka muistaa kaikki arpomansa luvut.

Näkymässä on painike, jolla pyydetään uuden luvun arpomista sekä tekstikenttä, missä arvotut luvut näytetään:


``` java
public class Nakyma extends Pane {
    private Button nappi;
    private Label teksti;
    
    public Nakyma() {
        super();
        
        VBox box = new VBox(10);
        teksti  = new Label("[]");       
        nappi = new Button("uusi");          
             
        box.getChildren().addAll(teksti, nappi);   
        
        getChildren().add(box);
    }
      
    public void update(String sisalto){
        teksti.setText(sisalto);
    }

    public void asetaKontrolleri(EventHandler listener){
        nappi.setOnAction(listener);
    }
}
```

Näkymä on täysin passiivinen, se ei sisällä edes tapahtumakuuntelijaa joka on MVC:n hengen mukaisesti laitettu kontrolleriin:


``` java
import javafx.event.Event;
import javafx.event.EventHandler;

public class Kontrolleri implements EventHandler {
    private Sovelluslogiikka logiikka;
    private Nakyma nakyma;
    
    public Kontrolleri(Nakyma nakyma, Sovelluslogiikka logiikka) {
        this.logiikka = logiikka;
        this.nakyma = nakyma;
        this.nakyma.asetaKontrolleri(this);
    }
     
    @Override
    public void handle(Event event) {
        logiikka.arvoLuku();
        String dataNaytolle = logiikka.getLuvut().toString();
        nakyma.update(dataNaytolle);
    }

}
```

Kontrolleri tuntee _näytön_ ja sovelluslogiikan eli _modelin_. Konstruktorissa kontrolleri asettaa itsensä tapahtumakuuntelijaksi näytössä olevalle painikkeelle.

Kun nappia painetaan, eli metodin _handle_ suorituksen yhteydessä kontrolleri pyytää modelia arpomaan uuden luvun. Sen jälkeen kontrolleri hakee luvut modelilta ja asettaa ne tekstimuoisena näytölle käyttäen näytön update-metodia.

Itse sovellus ainoastaan luo oliot ja antaa näytön sekä modelin kontrollerille:

``` java
public class FxMVC extends Application {
    @Override
    public void start(Stage primaryStage) {
        VBox pane = new VBox();
            
        Sovelluslogiikka logiikka = new Sovelluslogiikka();
        
        Nakyma nakyma = new Nakyma();
        Kontrolleri k = new Kontrolleri(nakyma, logiikka);
        pane.getChildren().add(nakyma);          
  
        Scene scene = new Scene(pane, 300, 250);
        
        primaryStage.setTitle("MVC example app");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

Rakenne luokkakaaviona:

![](https://github.com/mluukkai/ohjelmistotuotanto2017/raw/master/images/os-8.png)

Model eli sovelluslogiikka on nyt täysin tietämätön siitä kuka sen kutsuu. 

Päätämme lisätä ohjelmaan useampia näyttöjä, joille kaikille tulee oma kontrolleri:

``` java
public class FxMVC extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        VBox pane = new VBox();
            
        Sovelluslogiikka logiikka = new Sovelluslogiikka();
        
        for (int i = 0; i < 3; i++) {
            Nakyma nakyma = new Nakyma();
            Kontrolleri k = new Kontrolleri(nakyma, logiikka);
            pane.getChildren().add(nakyma);          
        }
  
        Scene scene = new Scene(pane, 300, 250);
        
        primaryStage.setTitle("MVC example app");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    } 
}
```

Tilanne näyttää _oliokaaviona_ seuraavalta:

![](https://github.com/mluukkai/ohjelmistotuotanto2017/raw/master/images/os-8a.png)

Eli sovelluslogiikkaolioita on ainoastaan yksi, mutta _näyttö-kontrolleri_-pareja on kolme.

Sovelluksessamme on pieni ongelma. Haluaisimme kaikkien näyttöjen olevan koko ajan ajantasalla. Nyt ainoastaan se näyttö minkä nappia painetaan päivittyy ajantasaiseksi.

## Käyttöliittymän päivittäminen sovelluslogiikan tilan muuttuessa

Kerrosarkkitehtuurissa ja MVC-mallin mukaisissa sovelluksissa törmätään usein nyt kohdatun kaltaiseen tilanteeseen, missä sovelluslogiikan on kerrottava käyttöliittymäkerrokselle (joka siis sisältää näkymät ja kontrollerit) jonkin sovellusolion tilan muutoksesta, jotta käyttöliittymä pystyisi koko ajan näyttämään ajantasaista tietoa.

Meidän tapauksessamme siis käyttöliittymäkerroksessa on kolme eri näyttöä, ja niitä vastaavat kontrollerit, ja sovelluslogiikan muuttunut tila pitäisi saada päivitettyä yhtä aikaa jokaiseen näkymään. Nyt päivitys tapahtuu ainoastaan siihen näkymään, joka saa aikaan uuden luvun arpomisen.

Suoraviivainen toteutus saa aikaa ikävän riippuvuussyklin sovelluslogiikasta käyttöliittymään. 

Kuvitellaan, että sovelluslogiikka ilmoittaa muuttuneesta tilasta kutsumalla jonkin käyttöliittymän luokan toteuttamaa metodia _update_. Parametrina voidaan esim. kertoa muuttunut tieto. Tilanne näyttää UML:n _pakkauskaaviona_ seuraavalta:

![](https://github.com/mluukkai/ohjelmistotuotanto2017/raw/master/images/os-9.png)

Eli käyttöliittymäkerros on riippuvainen sovelluslogiikasta mutta myös sovelluslogiikka on riippuvainen käyttöliittymästä, sillä sen on  kutsuttava käyttöliittymän metodia update sovelluslogiikan päivityksen tapahtuessa. Sykliset riippuvuudet  eivät ole ollenkaan toivottavia.

## Observer

Suunnittelumalli [Observer](https://sourcemaking.com/design_patterns/observer) auttaa rikkomaan sykliset riippuvuudet.

Määritellään rajapinta, joka sisältää käyttöliittymäluokan päivitysmetodin _update_, jota sovellusluokka kutsuu.

``` java
public interface Observer {
    void update();
}
```

Käyttöliittymäluokka toteuttaa rajapinnan, eli käytännössä toteuttaa _update_-metodin haluamallaan tavalla. Sovellusluokalle riittää nyt tuntea ainoastaan rajapinta, jonka metodia _update_ se tarvittaessa kutsuu.

Nyt kaikki menee siististi, sovelluslogiikasta ei enää ole konkreettista riippuvuutta mihinkään käyttöliittymän luokkaan mutta se voi silti kutsua käyttöliittymän metodia. Sovellusluokka tuntee siis vain rajapinnan. Rajapinta voidaan tarvittaessa määritellä sovelluslogiikan kanssa samassa pakkauksessa, jolloin riippuvuudesta saadaan kooditasolla vieläkin hallitumpi:

![](https://github.com/mluukkai/ohjelmistotuotanto2017/raw/master/images/os-10.png)

Jos käyttöliittymäolio haluaa tarkkailla jonkun sovellusolion tilaa, se toteuttaa Observer-rajapinnan ja rekisteröi rajapintansa tarkkailtavalle sovellusoliolle kutsumalla sovelluslogiikan metodia _addObserver_. Näin sovellusolio saa tietoonsa kaikki sitä tarkkailevat rajapinnat.

Kun joku muuttaa sovellusolion tilaa, kutsuu se sovellusolion metodia _notifyObservers_, joka taas kutsuu kaikkien tarkkailijoiden metodeja _update_. 

Toiminnan logiikka sekvenssikaaviona:

![](https://github.com/mluukkai/ohjelmistotuotanto2017/raw/master/images/os-11.png)

## sovelluksen observeria käyttävä versio

Muutetaan nyt sovelluksemme käyttämään observer-suunnittelumallia.

Laajennetaan sovelluslogiikkaa siten, että tuntee joukon tarkkailijoita:

``` java
public class Sovelluslogiikka {
    private ArrayList<Integer> luvut;
    private List<Observer> tarkkailijat;

    public Sovelluslogiikka() {
        luvut = new ArrayList<>();
        tarkkailijat = new ArrayList<>();
    }

    public void addObserver(Observer tarkkailija) {
        tarkkailijat.add(tarkkailija);
    }

    public void notifyObservers() {
        for (Observer tarkkailija : tarkkailijat) {
            tarkkailija.update();
        }
    }

    public ArrayList<Integer> getLuvut() {
        return luvut;
    }

    public void arvoLuku(){
        int luku = 1 + new Random().nextInt(20);
        luvut.add(luku);
    }

}
```

Tarkkailijat voivat rekisteröidä itsensä sovellukselle metodilla _addObserver_. Kun sovelluksen metodia _notifyObservers_ kutsutaan, kutsuu sovelluslogiikka jokaisen rekisteröityneen tarkkailijan _update_-metodia.

Sovelluslogiikalla ei nyt ole konkreettista riippuvuutta mihinkään tarkkailijaan, se tuntee ne ainoastaan rajapinnan kautta.

Kontrolleri muuttuu seuraavasti:

``` java
public class Kontrolleri implements EventHandler, Observer {
    private Sovelluslogiikka logiikka;
    private Nakyma nakyma;
    
    public Kontrolleri(Nakyma nakyma, Sovelluslogiikka logiikka) {
        this.logiikka = logiikka;
        this.nakyma = nakyma;
        this.nakyma.asetaKontrolleri(this);
        this.logiikka.addObserver(this);
    }
     
    @Override
    public void handle(Event event) {
        logiikka.arvoLuku();
        logiikka.notifyObservers();
    }

    public void update() {
        String dataNaytolle = logiikka.getLuvut().toString();
        nakyma.update(dataNaytolle);
    }    
}
```

Kontrolleri toimii tarkkailijana eli toteuttaa rajapinnan _Observer_. Kun nappia painetaan, eli _handle_-metodissa, kontrolleri pyytää modelia arpomaan uuden luvun ja samalla pyytää modelia ilmoittamaan tarkkailijoille muuttuneen arvon.

_update_-metodia kutsuttaessa (jota siis sovelluslogiikka kutsuu kun sen tila muuttuu) hakee kontrolleri sovelluslogiikan uuden tilan ja suorittaa hallinnoimansa näytön päivityksen.

Luokkaa Naytto ei tässä ratkaisussa tarvitse muuttaa.

## Pelaajastatistiikkaa Java 8:lla 

Muokataan hieman jo tutuksi käynyttä NHL-pelaajastatistiikka-ohjelmaa, tällä kertaa [viikon 1 laskareiden](https://github.com/mluukkai/ohjelmistotuotanto2018/blob/master/laskarit/1.md#15-riippuvuuksien-injektointi-osa-2-nhl-tilastot) versiota.

### forEach

Pystymme tulostamaan 10 parasta pistemiestä metodin <code>public List<Player> topScorers(int howMany)</code> avulla seuraavasti:

``` java
public static void main(String[] args) {
    Statistics stats = new Statistics();

    for (Player player : stats.topScorers(10)) {
        System.out.println(player);
    }
}
```

Java 8:ssa kaikille rajapinnan  [Interable](http://docs.oracle.com/javase/8/docs/api/java/lang/Iterable.html) toteuttaville olioille kuten kokoelmille on lisätty metodi <code>forEach</code>, jonka avulla kokoelma on helppo käydä läpi. Metodille voidaan antaa parametriksi _lambda-lauseke_ jota metodi kutsuu jokaiselle kokoelman alkiolle:

``` java
    Statistics stats = new Statistics();

    stats.topScorers(10).forEach(s->{
        System.out.println(s);
    });
```

Nyt parametrina on lambda-lauseke <code>s->{ System.out.println(s); }</code>. Lausekkeen parametrina on nuolen vasemmalla puolella oleva _s_. Nuolen oikealla puolella on lausekkeen koodilohko, joka tulostaa parametrina olevan pelaajan. Metodi <code>forEach</code> siis kutsuu jokaiselle kokoelman pelaajalle lambda-lauseketta.

Lambda-lauseke olisi voitu kirjoittaa myös kokonaan yhdelle riville. Tällöin koodilohkoa ei ole välttämätöntä laittaa aaltosulkeisiin:

``` java
    stats.topScorers(3).forEach( s->System.out.println(s) );
```

Teknisesti ottaen metodi <code>forEach</code> saa parametrikseen rajapinnan <code>Consumer&#60;T></code> toteuttavan olion. Consumer on käytännössä luokka, joka toteuttaa metodin <code>void accept(T param)</code>. Consumer-rajapinnan toteuttavia olioita on helppo generoida edellä olevan esimerkin tapaan lambda-lausekkeiden avulla.

Java täydentääkin edellä määritellyn lambda-lausekkeen anonyymiksi sisäluokaksi:

``` java
    stats.topScorers(10).forEach(new Consumer<Player>() {
        @Override
        public void accept(Player s) {
            System.out.println(s);
        }
    });
```

Java 8:ssa on mahdollista viitata myös luokkien yksittäisiin metodeihin. Metodi joka ottaa parametrikseen merkkijonon ja ei palauta mitään onkin tyyppiä <code>Consumer&#60;String></code>

Voimmekin antaa metodille <code>forEach</code> parametriksi viittauksen metodiin. Java 8:ssa metodeihin viitataan syntaksilla <code>Luokka::metodi</code>.

Voimmekin muuttaa parhaiden pistemiesten tulostuksen seuraavaan muotoon

``` java
    stats.topScorers(10).forEach(System.out::println);
```

Nyt <code>forEach</code> kutsuu metodia <code>System.out.println</code> jokaiselle pelaajalle antaen vuorossa olevan pelaajan metodin parametriksi.

Tulostuskomento <code>System.out.println</code> on hieman ikävä kirjoittaa kokonaisuudessaan. Importataan <code>System.out</code> _staattisesti_ jolloin pystymme viittaamaan olioon <code>System.out</code> suoraan kirjoittamalla koodiin <code>out</code>:

``` java
import static java.lang.System.out;

public class Main {

    public static void main(String[] args) {
        Statistics stats = new Statistics();

        stats.topScorers(3).forEach(out::println);
    }

}
```

Staattisen importtauksen jälkeen voimme siis tulostaa ruudulle helpommin, kirjoittamalla <code>out.println("tekstiä")</code>.

### filter

Luokan <code>Statistics</code> metodit toimivat hyvin samaan tyyliin, ne käyvät läpi pelaajien listan ja palauttavat joko yksittäisen tai useampia pelaajia metodin määrittelemästä kriteeristä riippuen. Jos lisäisimme luokalle samalla periaatteella muita hakutoiminnallisuuksia (esim. kaikkien yli 10 maalia tehneiden pelaajien lista), joutuisimme "copypasteamaan" pelaajat läpikäyvää koodia vielä useampiin metodeihin.

Parempi ratkaisu olisikin ohjelmoida luokalle geneerinen etsintämetodi, joka saa hakukriteerin parametrina. Java 8:n oliovirrat eli [streamit](http://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html) tarjoavat sopivan välineen erilaisten hakujen toteuttamiseen. 

Muutetaan ensin metodi <code>List&#60;Player> team(String teamName)</code> käyttämään stream-apia:

``` java
    public List<Player> team(String teamName) {
        return players
                .stream()
                .filter(p->p.getTeam().contains(teamName))
                .collect(Collectors.toList());
    }
```

Sensijaan että pelaajien lista käytäisiin eksplisiittisesti läpi käsitelläänkin metodilla <code>stream</code> listasta saatavaa oliovirtaa. Virrasta filtteröidään ne jotka toteuttavat lambda-lausekkeella määritellyn ehdon. Tuloksena olevasta filtteröidystä streamista sitten "kerätään" metodin <code>collect</code> avulla oliot palautettavaksi listaksi.

Metodi <code>filter</code> saa parametrikseen rajapinnan <code>Predicate&#60;T></code> toteuttavan olion. Päätämmekin poistaa metodin <code>team</code> ja sensijaan lisätä luokalle geneerisemmän hakumetodin <code>find</code> joka saa etsintäehdon parametriksi:

``` java
    public List<Player> find(Predicate<Player> condition) {
        return players
                .stream()
                .filter(condition)
                .collect(Collectors.toList());
    }
```

Yleistetyn metodin avulla on nyt helppo tehdä mielivaltaisen monimutkaisia hakuja. Hakuehdon muodostaminen lambda-lausekkeiden avulla on suhteellisen helppoa:

``` java
    public static void main(String[] args) {
        Statistics stats = new Statistics();

        // tulostetaan vähintään 21 maalia ja syöttöä tehneet pelaajat
        stats.find(p->p.getGoals()>20 && p.getAssists()>20).forEach(out::println);
    }
```

Java 8:ssa rajapinnoilla voi olla oletustoteutuksen omaavia metodeja. Rajapinnalla <code>Predicate</code> löytyykin mukavasti valmiiksi toteutetut metodit <code>and</code>, <code>or</code> ja <code>negate</code>. Näiden avulla on helppo muodostaa yksittäisten esim. lambda-lausekkeen avulla muodostettujen ehtojen avulla mielivaltaisen monimutkaisia ehtoja. Seuraavassa edellisen esimerkin tuloksen tuottava haku [de Morganin lakia](https://fi.wikipedia.org/wiki/De_Morganin_lait) hyväksikäyttäen muodostettuna:

``` java
    Statistics stats = new Statistics();

    Predicate<Player> cond1 = p->p.getGoals()<=20;
    Predicate<Player> cond2 = p->p.getAssists()<=20;
    Predicate<Player> cond = cond1.or(cond2);

    stats.find(cond.negate()).forEach(out::println);
```

Eli ensin muodostettiin ehto "korkeintaan 20 maalia _tai_ syöttöä tehneet pelaajat." Tästä otettiin sitten negaatio jolloin tuloksena on de Morganin sääntöjen nojalla ehto "vähintään 21 maalia _ja_ 21 syöttöä tehneet pelaajat".

### järjestäminen

Metodin <code>topScorers(int howMany)</code> avulla on mahdollista tulostaa haluttu määrä pelaajia tehtyjen pisteiden mukaan järjestettynä. Metodin toteutus on hieman ikävä, sillä palautettavat pelaajat on kerättävä yksi kerrallaan erilliselle listalle:

``` java
    public List<Player> topScorers(int howMany) {
        Collections.sort(players);
        ArrayList<Player> topScorers = new ArrayList<>();
        Iterator<Player> playerIterator = players.iterator();

        while (howMany>=0) {
            topScorers.add( playerIterator.next() );
            howMany--;
        }

        return topScorers;
    }
```

Metodista on helppo tehtä Java 8:a hyödyntävä versio:

``` java
    public List<Player> topScorers(int howMany) {
        return players
                .stream()
                .sorted()
                .limit(howMany)
                .collect(Collectors.toList());
    }
```

Eli otamme jälleen pelaajista muodostuvat streamin. Stream muutetaan luonnollisen järjestyksen (eli luokan <code>Player</code> metodin <code>compareTo</code> määrittelemän järjestyksen) mukaisesti järjestetyksi streamiksi metodilla <code>sorted</code>. Metodilla <code>limit</code> rajoitetaan streamin koko haluttuun määrään pelaajia, ja näistä muodostettu lista palautetaan.

Jotta myös muunlaiset järjestykset olisivat mahdollisia, generalisoidaan metodi muotoon, joka ottaa parametriksi halutun järjestyksen määrittelevän <code>Comparator&#60;Player></code>-rajapinnan määrittelevän olion:

``` java
    public List<Player> sorted(Comparator<Player> compare, int number) {
        return players
                .stream()
                .sorted(compare)
                .limit(number)
                .collect(Collectors.toList());
    }
```

Metodin tarvitsema vertailijaolio on helppo luoda lambda-lausekkeena:

``` java
    Comparator<Player> byPoints = (p1, p2)->p2.getPoints()-p1.getPoints();

    System.out.println("sorted by points");
    stats.sorted(byPoints, 10).forEach(out::println);
```

Comparator-olioiden luominen on hieman ikävää, varsinkin jos joutuisimme luomaan useiden eri kriteerien avulla tapahtuvia vertailijoita:

``` java
    Comparator<Player> byPoints = (p1, p2)->p2.getPoints()-p1.getPoints();
    Comparator<Player> byGoals = (p1, p2)->p2.getGoals()-p1.getGoals();
    Comparator<Player> byAssists = (p1, p2)->p2.getAssists()-p1.getAssists();
    Comparator<Player> byPenalties = (p1, p2)->p2.getPenalties()-p1.getPenalties();
```

Koodi sisältää ikävästi copy-pastea.

Voimme siistiä koodia Comparatoreja rakentavan _tehdasmetodin_ avulla. Periaatteena on, että tehtaalle annetaan viitteenä getterimetodi, jonka perusteella <code>Player</code>-olioiden vertailu tehdään. Esim. pisteiden perusteella tapahtuvan vertailun tekevä vertailija luotaisiin seuraavasti:

``` java
    Comparator<Player> byPoints = by(Player::getPoints);
```

Tehdasmetodin nimi on siis <code>by</code>.

Koska <code>Player</code>-olioiden getterimetodit ovat parametrittomia ja palauttavat kokonaislukuarvon, ne toteuttavat rajapinnan <code>Function&#60;Player, Integer></code>. Tehtaan koodi on seuraavassa:

``` java
    public static Comparator<Player> by(Function<Player, Integer> getter){
        return (p1, p2)->getter.apply(p2)-getter.apply(p1);
    }
```

Tehtaan parametrina saaman getterimetodin kutsumistapa on hiukan erikoinen, esim. <code>getter.apply(p1)</code> siis tarkoittaa samaa kuin <code>p1.getPoints()</code> jos tehdasta on kutsuttu ylläolevalla tavalla eli antamalla parametriksi <code>Player::getPoints</code>.

Järjestäminen esim. maalien perusteella onnistuu nyt tehtaan avulla seuraavasti:

``` java
    Comparator<Player> byGoals = by(Player::getGoals);
    stats.sorted(byGoals, 10).forEach(out::println);
```

Vertailijaa ei ole oikeastaan edes tarvetta tallettaa muuttujaan, sillä tehdasmetodi on nimetty siten, että sen kutsuminen on sujuvaa suoraan <code>sorted</code>-metodin parametrina:

``` java
    stats.sorted(by(Player::getGoals), 10).forEach(out::println);
```

Comparator-rajapinnalle on lisätty pari kätevää oletustoteutuksen omaavaa metodia <code>thenComparing</code> ja <code>reversed</code>. Ensimmäinen näistä mahdollistaa toissijaisen järjestämiskriteerin määrittelemisen erillisen vertailijan avulla. Eli jos ensin sovellettu vertailija ei erottele järjestettäviä oliota, sovelletaan niihin toissijaista vertailijaa. Metodi <code>reversed</code> toimii kuten nimi antaa olettaa, eli se muodostaa vertailijasta käänteisesti toimivan vertailijan.

Seuraavassa pelaajat listattuna ensisijaisesti tehtyjen maalien ja toissijaisesti syöttöjen "vähyyden" perusteella:

``` java
    Comparator<Player> order = by(Player::getPoints)
                               .thenComparing(by(Player::getAssists).reversed());

    stats.sorted(order, 20).forEach(out::println);
```

Yhden kentän perusteella toimivan _Comparator_-olion luominen onnistuu itseasiassa helposti _Comparator_-luokan staattisen metodin <code>comparing</code> avulla. Metodi ottaa parametrikseen viitteen getteriin:

``` java
    Comparator<Player> order = Comparator.comparing(Player::getGoals);

    stats.sorted(order, 20).forEach(out::println);
```

Näin saamme 20 vähiten maaleja tehnyttä pelaajaa. Jos haluamme 20 eniten maaleja tehnyttä, teemme edellisestä comparatorista käänteisen version metodilla <code>reversed</code>:

``` java
    Comparator<Player> order = Comparator.comparing(Player::getGoals).reversed();
    
    stats.sorted(order, 20).forEach(out::println);
```

### numeerinen statistiikka

Haluaisimme laskea erilaisia numeerisia tilastoja pelaajista. Esim. yksittäisen joukkueen yhteenlasketun maalimäärän.

Ensimmäinen yrityksemme toteuttaa asia Java 8:lla on seuraava:

``` java
    int maalit = 0;
    stats.find(p->p.getTeam().equals("PHI")).forEach(p->{
        maalit += p.getGoals();
    });
    System.out.println(maalit);
```

Koodi ei kuitenkaan käänny. Syynä tälle on se, että lambda-lausekkeen sisältä ei pystytä muuttamaan metodin paikallista muuttujaa <code>maalit</code>. Asia korjaantuisi määrittelemällä muuttuja luokkatasolla. Emme kuitenkaan tee näin (se ei olisi edes streamien käsittelyn "hengen" mukaista, sillä streameilla operoitaessa tulisi käyttää ainoastaan ns. [puhtaita funktioita](https://en.wikipedia.org/wiki/Pure_function)), vaan pyrimme hyödyntämään vielä radikaalimmalla tavalla Java 8:n tarjoamia uusia ominaisuuksia.

Talletetaan ensin käsiteltävien olioiden stream muuttujaan:

``` java
    Stream<Player> playerStream = stats.find(p->p.getTeam().equals("PHI")).stream();
```

Streameille on määritelty metodi <code>map</code>, jonka avulla voimme muodostaa streamista uuden streamin, jonka jokainen alkio on muodostettu alkuperäisen streamin alkiosta suorittamalla tälle metodin <code>map</code> parametrina määritelty metodi tai lambda-lauseke.

Saamme muutettua pelaajien streamin pelaajien maalimääristä koostuvaksi streamiksi seuraavasti:

``` java
   playerStream.map(p->p.getGoals())
```

eli uusi streami muodostetaan siten, että jokaiselle alkuperäisen streamin alkiolle suoritetaan lambda-lauseke <code>p->p.getGoals()</code>.

Sama käyttäen metodireferenssejä olisi:

``` java
   playerStream.map(Player::getGoals)
```

Metodin <code>map</code> avulla muunnettu oliovirta voidaan tarvittaessa "kerätä" listaksi:

``` java
    List<Integer> maalit = playerStream.map(Player::getGoals).collect(Collectors.toList() );
```

Näin saisimme siis generoitua listan tietyn joukkueen pelaajien maalimääristä.

Streamille voidaan myös suorittaa metodi <code>reduce</code>, joka "laskee" streamin alkioiden perusteella määritellyn arvon. Määritellään seuraavassa maalien summan laskeva operaatio:

``` java
    int maalienSumma = playerStream.map(Player::getGoals).reduce((item1,item2)->item1+item2).get();
```

Metodi <code>reduce</code> saa parametrikseen lambda-lausekkeen joka saa ensimmäisellä kutsukerralla parametrikseen streamin 2 ensimmäistä alkiota. Nämä lasketaan yhteen ja toisella kutsukerralla lambda-lauseke saa parametrikseen streamin kolmannen alkion _ja_ edellisen reducen laskeman summan. Näin reduce "kuljettaa" mukanaan streamin alkioiden summaa ja kasvataa sitä aina seuraavalla vastaantulevalla alkiolla. Kun koko stream on käyty läpi, palauttaa reduce kaikkien alkioiden summan, joka muutetaan kokonaislukutyyppiseksi metodin <code>get</code> avulla.

## Builder revisited

Muutama metri ylempänä tässä materiaalissa toteutettiin monimutkaisen pinon luomista helpottava [rakentaja](https://github.com/mluukkai/ohjelmistotuotanto2018/blob/master/web/oliosuunnittelu.md#pinorakentaja). 

Rakentajan toteutuksessa kiinnitettiin erityisesti huomiota rajapinnan käytön luontevaan muotoon:

``` java
Pinorakentaja rakenna = new Pinorakentaja();

Pino pino = rakenna.kryptattu().prepaid(10).pino();
```

Toteutustekniikkana käytetiin viimeaikoina yleistynyttä metodien ketjutusta.

Java 8 avaa mielenkiintoisen uuden mahdollisuuden rakentaja-suunnittelumallin toteuttamiseen.

Tarkastellaan ensin yksinkertaisempaa tapausta, NHL-tulospalveluohjelmasta löytyvää luokkaa <code>Player</code>. Tavoitteenamme on, että pelaajaolioita pystyttäisiin luomaan normaalin konstruktorikutsun sijaan seuraavaa syntaksia käyttäen:

``` java
    Player pekka = Player.create(p->{
        p.setName("Pekka");
        p.setTeam("Sapko");
        p.setGoals(10);
        p.setAssists(20);
    });

    Player arto = Player.create(p->{
        p.setName("Arto");
        p.setTeam("Blues");
        p.setPenalties(200);
    });
```

Nyt siis luotava olio "konfiguroidaan" antamalle pelaajan luovalle metodille <code>create</code> lambda-lausekkeena määriteltävä koodilohko, joka asettaa pelaajan kentille sopivat alkuarvot.

Metodin toteutus näyttää seuraavalta:

``` java
public class Player implements Comparable<Player> {

    private String name;
    private String team;
    private int goals;
    private int assists;

    public Player() {
    }

    public static Player create(Consumer<Player> init){
        Player p = new Player();
        init.accept(p);
        return p;
    }

    // setterit ja getterit
}
```

Rakentajametodin _create_ parametrina on siis <code>Consumer&#60;Player></code>-tyyppinen olio. Käytännössä kyseessä on rajapinta, joka määrittelee että sen toteuttajalla on metodi <code>void accept(Player p)</code>. Rajapinnan toteuttava olio on helppo luoda lambda-lausekkeen avulla. Käytännössä siis rakentajametodi toimii siten, että se luo ensin pelaaja-olion ja kutsuu sen jälkeen metodin parametrina olevaa lambda-lausekkeen avulla määriteltyä koodilohkoa antaen luodun pelaaja-olion parametriksi. Näin koodilohkoon määritellyt setterikutsut suoritetaan luodulle pelaajaoliolle. Rakentajametodi palauttaa lopuksi luodun ja määritellyllä tavalla "konfiguroidun" olion kutsujalle.

Eli käytännössä jos rakentajaa kutsutaan seuraavasti:

``` java
    Player pekka = Player.create(p->{
        p.setName("Pekka");
        p.setTeam("Sapko");
        p.setGoals(10);
        p.setAssists(20);
    });
```

rakentajan sisällä suoritettava toiminnallisuus vastaa seuraavaa:

``` java
    public static Player create(Consumer<Player> init){
        Player p = new Player();
        // komento init.accept(p);
        // saa aikaan seuraavat
        p.setName("Pekka");
        p.setTeam("Sapko");
        p.setGoals(10);
        p.setAssists(20);
        return p;
    }
```

Rakentajan pelaaja-oliolle suorittamat komennot voidaan siis antaa lambda-lausekkeen muodossa parametrina.

Näin saadaan erittäin joustava tapa luoda olioita, toisin kuin normaalia konstruktoria käytettäessä, riittää että oliolle kutsutaan ainoastaan niitä settereitä, joiden avulla tietty kenttä halutaan alustuvan, muut kentät saavat oletusarvoisen alkuarvon.

Koska pelaaja-olion muodostaminen on suhteellisen suoraviivaista, rakentajasta ei tällä kertaa oltu tehty erillistä luokkaa ja rakentaminen hoitui staattisen metodin <code>create</code> sekä olion settereiden avulla.

Dekoroitujen pinojen rakentaminen on hieman monimutkaisempi operaatio, joten rakentajasta kannattaa tehdä oma luokkansa.

Seuraavassa esimerkki siitä, miten pinoja on tarkoitus rakentaa:

``` java
    PinonRakentaja builder = new PinonRakentaja();

    Pino pino1 = builder.create(p->{
        p.kryptaava();
        p.prepaid(10);
        p.logaava(tiedostoon);
    });


    Pino pino2 = builder.create(p->{
        p.kryptaava();
    });
```

Periaate on siis sama kuin pelaajaolioiden rakentamisessa. Pinolle liitettävät ominaisuudet määritellään lambda-lausekkeen avulla.

Pinolle ominaisuuksia dekoroivat metodit eivät tällä kertaa ole luokan Pino metodeja, vaan pinonrakentajan metodeja, joten rakentajan metodin <code>create</code> parametri onkin nyt tyypiltään pinonrakentaja:

``` java
public class PinonRakentaja {
    Pino pino;

    Pino create(Consumer<PinonRakentaja> init){
        pino = new Pino();
        init.accept(this);
        return pino;
    }

    void kryptaava() {
        this.pino = new KryptattuPino(pino);
    }

    void prepaid(int crediitit) {
        this.pino = new PrepaidPino(pino, crediitit);
    }

    void logaava(PrintWriter loki) {
        this.pino = new LokiPino(pino, loki);
    }
}
```

Eli kun kutsutaan esim:

``` java
    Pino pino1 = builder.create(p->{
        p.kryptaava();
        p.prepaid(10);
    });
```

rakentajan sisällä suoritettava toiminnallisuus vastaa seuraavaa:

``` java
    Pino create(Consumer<PinonRakentaja> init){
        pino = new Pino();
        // komento init.accept(this);
        // saa aikaan seuraavat:
        this.kryptaava();
        this.prepaid(10);
        return pino;
    }
```

Eli jälleen rakentajan suorittavat komennot annetaan lambda-lausekkeen muodossa parametrina. Rakentajametodien suoritusjärjestys on sama kuin komentojen järjestys lambda-lausekeessa.
