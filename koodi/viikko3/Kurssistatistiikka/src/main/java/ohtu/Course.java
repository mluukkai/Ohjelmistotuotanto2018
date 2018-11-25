/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ohtu;

/**
 *
 * @author tkarkine
 */
public class Course {
    private String name;
    private String fullName;
    
    public void setName(String name){
        this.name=name;
       }
    
    public String getName(){
        return name;
    }
    
    public String getFullName(){
        return fullName;
    }
    
    
    @Override
    public String toString() {
        return ""+name;
    }
    
    
    
}
