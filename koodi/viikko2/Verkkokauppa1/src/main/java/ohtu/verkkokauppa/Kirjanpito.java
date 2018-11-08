package ohtu.verkkokauppa;

import java.util.ArrayList;
import org.springframework.stereotype.Component;

@Component
public class Kirjanpito implements KirjanpitoInterface {
    
    private ArrayList<String> tapahtumat;

    public Kirjanpito() {
        tapahtumat = new ArrayList<String>();
    }
    
    public void lisaaTapahtuma(String tapahtuma) {
        tapahtumat.add(tapahtuma);
    }

    public ArrayList<String> getTapahtumat() {
        return tapahtumat;
    }       
}
