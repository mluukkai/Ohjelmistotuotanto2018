package ohtu;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class Tester {

    public static void main(String[] args) {
//        WebDriver driver = new ChromeDriver();
        WebDriver driver = new HtmlUnitDriver();
         driver.get("http://localhost:4567");
        
//        // tulostetaan sivu konsoliin
//        System.out.println(driver.getPageSource());
//        
        WebElement element = driver.findElement(By.linkText("register new user"));
        element.click();

        

        sleep(2);

        element = driver.findElement(By.name("username"));
        element.sendKeys("pekka72");
        element = driver.findElement(By.name("password"));
        element.sendKeys("akkep134");
        element = driver.findElement(By.name("passwordConfirmation"));
        element.sendKeys("akkep134");
        //element = driver.findElement(By.name("login"));
        
       
        element.submit();

        sleep(3);
        
        element = driver.findElement(By.linkText("continue to application mainpage"));
        element.click();
        
        sleep(1);
        element = driver.findElement(By.linkText("logout"));
        element.click();
        
        
        // tulostetaan sivu konsoliin
        System.out.println(driver.getPageSource());
        
        
        driver.quit();
    }
    
    private static void sleep(int n){
        try{
            Thread.sleep(n*1000);
        } catch(Exception e){}
    }
}
