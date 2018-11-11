package ohtu;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import ohtu.verkkokauppa.Kirjanpito;
import ohtu.verkkokauppa.Varasto;


public class Main {

    public static void main(String[] args) {
//        Kauppa kauppa = new Kauppa(varasto, pankki, viitegen);

        ApplicationContext ctx = new FileSystemXmlApplicationContext("src/main/resources/spring-context.xml");
// 
//        Kirjanpito kirjanpito = ctx.getBean(Kirjanpito.class);
//        KirjanpitoRajapinta kirjanpito = ctx.getBean(Kirjanpito.class);
        Varasto varasto =  ctx.getBean(Varasto.class);
//        Pankki pankki = ctx.getBean(Pankki.class);
//        Viitegeneraattori viitegen = new Viitegeneraattori();
        
//        Kauppa kauppa = ctx.getBean(Kauppa.class);
//        Kauppa kauppa = ctx.getBean(Kauppa.class);
//        
//        // kauppa hoitaa yhden asiakkaan kerrallaan seuraavaan tapaan:
//        kauppa.aloitaAsiointi();
//        kauppa.lisaaKoriin(1);
//        kauppa.lisaaKoriin(3);
//        kauppa.lisaaKoriin(3);
//        kauppa.poistaKorista(1);
//        kauppa.tilimaksu("Pekka Mikkola", "1234-12345");
//
//        // seuraava asiakas
//        kauppa.aloitaAsiointi();
//        for (int i = 0; i < 24; i++) {
//            kauppa.lisaaKoriin(5);
//        }
//
//        kauppa.tilimaksu("Arto Vihavainen", "3425-1652");

        // kirjanpito
//        for (String tapahtuma : kirjanpito.getTapahtumat()) {
//            System.out.println(tapahtuma);
//        }
    }
}
