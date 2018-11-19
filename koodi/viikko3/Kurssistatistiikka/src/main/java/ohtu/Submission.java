package ohtu;

public class Submission {
    private int week;

    public void setWeek(int week) {
        this.week = week;
    }

    public int getWeek() {
        return week;
    }

    @Override
    public String toString() {
        return ""+week;
    }
    
}