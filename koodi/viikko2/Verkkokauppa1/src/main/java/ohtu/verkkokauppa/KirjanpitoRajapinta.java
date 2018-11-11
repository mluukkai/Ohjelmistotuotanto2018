
package ohtu.verkkokauppa;

import java.util.ArrayList;


public interface KirjanpitoRajapinta {

    ArrayList<String> getTapahtumat();

    void lisaaTapahtuma(String tapahtuma);

}
