package ohtu;

import java.util.ArrayList;

public class Submission {
    private int week;
    private int hours;
    private ArrayList<Integer> exercises;
    private String course;
    

    public void setWeek(int week) {
        this.week = week;
    }
    
   

    public int getWeek() {
        return week;
    }
    
    public String getCourse() {
        return course;
    }
    
    public int getExercisesCount(){
        return exercises.size();
    }
    
    public int getHours(){
        return hours;
    }
    
    public String listExercises(){
        String lista= exercises.toString();
        lista =lista.replace("[","");
        lista =lista.replace("]","");
        return lista;
    }
    
   

    @Override
    public String toString() {
        return ""+week;
    }
    
}