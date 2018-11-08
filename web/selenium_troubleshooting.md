# Selenium troubleshooting

Osalla on ollut ongelmia Seleniumin toiminnan kanssa. Alla muutamia tapoja, miten ongelmat on saatu ratkaisuta. Jos törmäät ongelmaan ja saat sen ratkaistua jollain alla mainitsemattomalla tavalla, lisää ohje dokumenttiin.

## tapa 1: HtmlUnit-driver

Lisää projektille riippuvuudeksi _HtmlUnitDriver_ :

```groovy
dependencies {
    // ...
    compile group: 'org.seleniumhq.selenium', name: 'selenium-htmlunit-driver',version: seleniumVersion  
}
```

[HtmlUnitDriver](https://github.com/SeleniumHQ/selenium/wiki/HtmlUnitDriver) on ns. [headless](https://en.wikipedia.org/wiki/Headless_browser)-selain, eli sillä ei ole graafista käyttöliittymää. Jos haluat tietää millä sivulla selain on menossa, joudut esim. tulostamaan sivun lähdekoodin konsoliin komennolla <code>System.out.println(driver.getPageSource());</code>.

Ota HtmlUnitDriver käyttöön seuraavasti:

```java
...
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class Tester {

    public static void main(String[] args) {
        WebDriver driver = new HtmlUnitDriver();
        driver.get("http://localhost:4567");
        
        // tulostetaan sivu konsoliin
        System.out.println(driver.getPageSource());
        
        WebElement element = driver.findElement(By.linkText("login"));
        element.click();

        // tulostetaan sivu konsoliin
        System.out.println(driver.getPageSource());
        
        // ...

        driver.quit();
    }
    
}
```

HtmlUnitDriver:in hyvä puoli on nopeus. Voit käyttää sitä myös testeissä. Testien debuggaaminen muuttuu hankalammaksi, mutta testit toimivat nopeasti. Testejä debugatessa best practice lienee sivun html-koodin tulostaminen konsoliin.


## tapa 2: chromedriverin downloadaus

Lataa [täältä](https://sites.google.com/a/chromium.org/chromedriver/downloads) ja asenna ChromeDriver.

Lataaminen ja asentaminen macillä tapahtuu komennolla `brew install chromedriver`. Tämän jälkeen pitäisi toimia ilman muuta määrittelyä. (brew pitää olla asennettuna etukäteen)

Tee seuraava määrittely seleniumia käyttävässä tiedostossa:

```java
// windowsissa
System.setProperty("webdriver.chrome.driver", "oma_polku/chromedriver.exe"); 

// macissa ja linuxeissa
System.setProperty("webdriver.chrome.driver", "oma_polku/chromedriver"); 
```

Testejä varten kannattaa määrittely sijoittaa luokan <code>ServerRule</code> metodiin <code>before</code>.

## Tapa 3: WebDriverManager

Lisää projektille riippuvuus _webdrivermanager_:

```groovy
dependencies {
    // ...
    compile ("io.github.bonigarcia:webdrivermanager:1.6.2") {
        exclude group: 'org.seleniumhq.selenium'
    }
}
```

[WebDriverManager](https://github.com/bonigarcia/webdrivermanager) pyrkii automaattisesti konfiguroimaan käytetyn selainajurin. Sitä kutsutaan ennen valitun ajurin luomista, esimerkiksi ChromeDriver:n yhteydessä:

```java
...
import io.github.bonigarcia.wdm.ChromeDriverManager;
...

ChromeDriverManager.getInstance().setup();
driver = new ChromeDriver();
```

Saadakseen sen cucumber testien yhteydessä käyttöön, ajurin alustuksen voi lisätä @Before annotaatiolla varustettuun funktioon samaan tapaan kuin jUnit testeissä:

```java
...
import cucumber.api.java.Before;
import io.github.bonigarcia.wdm.ChromeDriverManager;
...

@Before
public void setUp() {
    ChromeDriverManager.getInstance().setup();
    driver = new ChromeDriver();
}
```

## tapa 4: firefox-driver

Kokeile käyttää FirefoxDriveria Chromen sijaan. 

### vaihtoehto 1 (Testattu Linuxilla)

Projektiin oletusarvoisesti määritelty Selenium 2.41.0 tukee ainoastaan Firefoxin versiota 28. Se löytyy [täältä](https://ftp.mozilla.org/pub/firefox/releases/28.0/) kun klikkaat omaa arkkitehtuuriasi. Pura paketti ja ota polku talteen.

### vaihtoehto 2 (Testattu OSX:llä)

Päivitä tiedostossa _build.gradle_ määritelty selenium uudempaan versioon:

```groovy
project.ext {
    cucumberVersion = '1.2.5'
    seleniumVersion = '2.52.0'
}
```

ja päivitä _spark-core_ uudempaan versioon:

```groovy
dependencies {
    // vaihda tästä versionumeroa
    compile group: 'com.sparkjava', name: 'spark-core', version: '2.5.5'
    // lisää seuraava rivi
    compile group: 'org.slf4j', name: 'slf4j-simple', version: '1.7.25'
    // ...
}
```

Selenium 2.52.0 tukee hieman uudempia Firefoxeja, esim versiota 45.8.0. Se löytyy [täältä](https://ftp.mozilla.org/pub/firefox/releases/45.8.0esr/) kun klikkaat omaa arkkitehtuuriasi. Pura paketti ja ota polku talteen.

### molemmat vaihtoehdot jatkavat täältä

Määrittele seuraavasti:
```java
// ...
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

public class Tester {
    public static void main(String[] args) {
        File pathBinary = new File("polku/jonne/purit/firefoxin/firefox.exe");
        FirefoxBinary firefoxBinary = new FirefoxBinary(pathBinary);
        FirefoxProfile firefoxProfile = new FirefoxProfile();
        WebDriver driver = new FirefoxDriver(firefoxBinary, firefoxProfile);
    } 
}   
```

Määrittele <code>FirefoxDriver</code> vastaavalla tavalla testeissä.
 
## Tapa 5: Lataa Google Chrome koneellesi

Suorita ensin Tapa 2. Sen jälkeen vasta tämän ohjeen mukaan.

Jos saat seuraavanlaisen virheilmoituksen kun suoritat komennon gradle browse (toimii myös, jos virhe valittaa Chromen versiota): 

Starting ChromeDriver 2.33.506092 (733a02544d189eeb751fe0d7ddca79a0ee28cce4) on port 17195
Only local connections are allowed.
Exception in thread "main" org.openqa.selenium.WebDriverException: unknown error: cannot find Chrome binary
  (Driver info: chromedriver=2.33.506092 (733a02544d189eeb751fe0d7ddca79a0ee28cce4),platform=Linux 4.4.0-98-generic x86_64) (WARNING: The server did not provide any stacktrace information)
Command duration or timeout: 605 milliseconds
Build info: version: '2.41.0', revision: '3192d8a6c4449dc285928ba024779344f5423c58', time: '2014-03-27 11:29:39'

Lataa koneellesi Google Chrome vaikkapa tämän ohjeen avulla: 

Or if you want the actual Google Chrome, open a terminal and follow:

1. cd /tmp
2. wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
3. sudo dpkg -i google-chrome-stable_current_amd64.deb

The 32-bit version is no longer available.

If you encounter any errors simply use

4. sudo apt-get -f install

To run it from terminal use google-chrome or hit the super key and search Google or Chrome

Jos saat siis vaikkapa tämäntyylisen virheilmoituksen, kun suoritat komennon 3. (sudo dpkg -i google-chrome-stable_current_amd64.deb);

Selecting previously unselected package google-chrome-stable.
(Luetaan tietokantaa... 227676 files and directories currently installed.)
Preparing to unpack google-chrome-stable_current_amd64.deb ...
Unpacking google-chrome-stable (62.0.3202.94-1) ...
dpkg: dependency problems prevent configuration of google-chrome-stable:
 google-chrome-stable riippuu paketista libappindicator1; kuitenkin:
  Pakettia libappindicator1 ei ole asennettu.

dpkg: error processing package google-chrome-stable (--install):
 riippuvuusongelmia - jätetään asetukset säätämättä
Processing triggers for gnome-menus (3.13.3-6ubuntu3.1) ...
Processing triggers for desktop-file-utils (0.22-1ubuntu5) ...
Processing triggers for bamfdaemon (0.5.3~bzr0+16.04.20160701-0ubuntu1) ...
Rebuilding /usr/share/applications/bamf-2.index...
Processing triggers for mime-support (3.59ubuntu1) ...
Processing triggers for man-db (2.7.5-1) ...
Käsittelyssä tapahtui liian monta virhettä:
 google-chrome-stable
 
 niin suorita komento 4. (sudo apt-get -f install) ja suorita uudelleen komento 3.
 
 Nyt suorittamalla komennon  gradle browse, ongelma on korjaantunut.
