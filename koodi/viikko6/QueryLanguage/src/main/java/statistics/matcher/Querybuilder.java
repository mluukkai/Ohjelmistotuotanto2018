/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics.matcher;

import statistics.matcher.All;
import statistics.matcher.Matcher;

/**
 *
 * @author tkarkine
 */
public class Querybuilder {
    Matcher matcher;
    
    public Querybuilder() {
         matcher = new All();
    }
    
    public Matcher build(){
       return matcher;
    }
    
    public Querybuilder playsIn(String team) {
        this.matcher = new PlaysIn(team);
       return this;
    }
    
     public Querybuilder hasAtLeast(int value, String category) {
        this.matcher =  new And(this.matcher,new HasAtLeast(value, category));
       return this;
    }
    
     public Querybuilder hasFewerThan(int value, String category) {
        this.matcher =  new And(this.matcher,new HasFewerThan(value, category));
       return this;
     }
     
      public Querybuilder oneOf(Matcher...matchers) {
            Matcher m = new Or(matchers);
            this.matcher = new Or(this.matcher, m);
         
        return this;
    }
       
    
    
}
