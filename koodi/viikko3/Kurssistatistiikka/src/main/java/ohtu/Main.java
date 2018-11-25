package ohtu;

import com.google.gson.Gson;
import java.io.IOException;
import org.apache.http.client.fluent.Request;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// jätin tämän tehtävän muotoilut kesken, että saan muut tehtävät tehtyä
// on kuitenkin haettu ja päästään käsiksi tietoihin kaikista haettavista lähteistä.

public class Main {

    public static void main(String[] args) throws IOException {
        int hours=0;
        int exercises=0;
        // ÄLÄ laita githubiin omaa opiskelijanumeroasi
        String studentNr = "012345678";
        if ( args.length>0) {
            studentNr = args[0];
        }

        String url = "https://studies.cs.helsinki.fi/courses/students/"+studentNr+"/submissions";

        String bodyText = Request.Get(url).execute().returnContent().asString();

        System.out.println("opiskelijanumero 012345678");
       
        Gson mapper = new Gson();
        Submission[] subs = mapper.fromJson(bodyText, Submission[].class);
        
                  // haetaan kurssien tiedot
         String url2 = "https://studies.cs.helsinki.fi/courses/courseinfo";

        String courseText = Request.Get(url2).execute().returnContent().asString();

        
        Gson mapper2 = new Gson();
        Course[] courses = mapper2.fromJson(courseText, Course[].class);
                  
                  
                  
                  // haetaan opiskelijoiden palautukset
        url = "https://studies.cs.helsinki.fi/courses/ohtu2018/stats";
        String statsResponse = Request.Get(url).execute().returnContent().asString();
        JsonParser parser = new JsonParser();
        JsonObject parsittuData = parser.parse(statsResponse).getAsJsonObject();
        for (Object key : parsittuData.keySet()) {
        //tällä katsottu mitä avaimia käytetty
        String keyStr = (String)key;
        Object keyvalue = parsittuData.get(keyStr);
       
        //Nyt voitasiin tuo key muotoilla viikoksi mapperilla, mutta ei jumituta tehtävään
        System.out.println("key: "+ keyStr + " value: " + keyvalue);
        System.out.println();
         }
        for (Submission submission : subs) {
            // tulostetaan kurssin tiedot
           for (Course course : courses){
               if (submission.getCourse().equals(course.getName())){
                   System.out.println(course.getFullName());
                   System.out.println();
               }
           }
           System.out.println("viikko "+submission.getWeek()+":");
            
            
            
            System.out.println(" tehtyjä tehtäviä yhteensä "+submission.getExercisesCount()+
                    " aikaa kului "+submission.getHours()+
                    " tehdyt tehtävät "+submission.listExercises()
                    );
            //talletetaan viikon tunnit ja harjoitukset yhteenlaskua varten
            hours+=submission.getHours();
            exercises+=submission.getExercisesCount();
            
             }
        // tulostus kurssi yhteensä
        System.out.println();
             System.out.println("yhteensä: "+ exercises +
                     " tehtävää "+hours+" tuntia");
       

    }
}