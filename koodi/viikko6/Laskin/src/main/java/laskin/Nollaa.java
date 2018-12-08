/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package laskin;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 *
 * @author tkarkine
 */
public class Nollaa implements Komento {
    private TextField tuloskentta;
    private TextField syotekentta;
    private Button nollaa;
    private Button undo;
    private Sovelluslogiikka sovellus;
    private int entinen;

    public Nollaa(TextField tuloskentta, TextField syotekentta, Button nollaa, Button undo, Sovelluslogiikka sovellus) {
        this.syotekentta = syotekentta;
        this.tuloskentta = tuloskentta;
        this.nollaa = nollaa;
        this.undo = undo;
        this.sovellus = sovellus;
        
    }

    @Override
    public void suorita() {
        entinen = Integer.parseInt(tuloskentta.getText());
        sovellus.nollaa();}

    @Override
    public void peru() {
          sovellus.plus(entinen);
        }
    
}
