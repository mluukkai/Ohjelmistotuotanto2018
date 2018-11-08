package ohtu.verkkokauppa;

import java.util.ArrayList;

public interface KirjanpitoInterface {
    void lisaaTapahtuma(String tapahtuma);
    ArrayList<String> getTapahtumat();
}