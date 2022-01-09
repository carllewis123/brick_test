package com.brick.service;

import com.brick.exporter.CSVExporter;
import com.brick.model.Product;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebScraperService {

    private static final int LIMIT_PRODUCT = 100;
    private static final String URL_HANDPHONE_PAGE ="https://www.tokopedia.com/p/handphone-tablet/handphone?page";
    private static WebDriver webDriver = null;

    static {
        initWebDriver();
    }
    
    private static void initWebDriver() {
        String driverPath = "C:/Users/carllewis/Downloads/jar/chromedriver.exe";

        System.setProperty("webdriver.chrome.driver", driverPath);

        ChromeOptions options = new ChromeOptions();
        // setting headless mode to true.. so there isn't any ui
        options.setHeadless(true);
        options.addArguments("user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.50 Safari/537.36");
        webDriver = new ChromeDriver(options);
        
    }

    // export all data product to csv
    public static void generateProductIntoFile() throws Exception {
        List<Product> listProduct = new ArrayList<Product>();

        int page = 1;
        do {
        	 System.out.println("page : "+page);
            listProduct.addAll(retrieveProductsAtPage(page));
           
            page++;
        } while (listProduct.size() < LIMIT_PRODUCT);

        System.out.println("Total products is: " + listProduct.size());
        CSVExporter.export(listProduct);
        webDriver.close();
    }
    
    // Used for unit test
    public static List<Product> retrieveProductsAtPageTest(Integer pageNum) throws InterruptedException {
		return retrieveProductsAtPage(pageNum);
    }

    private static List<Product> retrieveProductsAtPage(int pageNum) throws InterruptedException {
        List<Product> listProduct = new ArrayList<>();
        String currentHandle = webDriver.getWindowHandle();
        String url = URL_HANDPHONE_PAGE + pageNum;
        System.out.println("-----------------------------------------------");
        System.out.println();

        webDriver.get(url);
        Thread.sleep(4000);
        //String title = webDriver.getTitle();
        JavascriptExecutor jsExec = (JavascriptExecutor) webDriver;

        IntStream.range(1, 3).forEach(index -> {
            try {
               jsExec.executeScript("window.scrollTo(0, document.body.scrollHeight)");
               Thread.sleep(1000);
           } catch (Exception e) {
               e.printStackTrace();
           }
        });

        List<WebElement> containers = webDriver.findElements(By.cssSelector("a[data-testid=lnkProductContainer]"));
        System.out.println("Containers size: " + containers.size());
        int totalCon = 0;
        if (pageNum> 1 ) {
        	totalCon = (pageNum - 1) * containers.size();
        }        
        for (WebElement con : containers) {
            try {
                System.out.println(".............................................");
                
                listProduct.add(collectProduct(currentHandle, con));

            } catch (Exception e) {
                System.out.println("There is an exception when retrieving a product");
                e.printStackTrace();
            }
            totalCon++;
            System.out.println("Count "+totalCon);
            if (totalCon >= LIMIT_PRODUCT) {
                break;
            }
        }
        return listProduct;
    }

    private static Product collectProduct(String curWindowtHandle, WebElement con) throws Exception {
        Product p = new Product();
        
        setProductAttributes(curWindowtHandle,p, con);
        return p;
    }

    
    // set data product from element web 
    private static void setProductAttributes(String curWindowtHandle,Product p, WebElement con) throws Exception {
    	JavascriptExecutor jsExec = (JavascriptExecutor) webDriver;
    	
    	WebElement pNameElmt = con.findElement(By.cssSelector("span.css-1bjwylw"));
        WebElement pPriceElmt = con.findElement(By.cssSelector("span.css-o5uqvq"));
        WebElement merchantElmt = con.findElements(By.cssSelector("span.css-1kr22w3")).get(1);
        String urlDetailProduct = con.getAttribute("href");
        
        String productHRef = validateUrlProduct(urlDetailProduct);
        
        // Open New Window to get star,description and imgUrl data
        try {
            webDriver.switchTo().newWindow(WindowType.TAB);
            webDriver.navigate().to(productHRef);
            
            WebElement starElmt = null;
            WebElement descriptionElmt = null;
            WebElement imgElmt = null;
            jsExec.executeScript("window.scrollTo(0, document.body.scrollHeight)");

            try {
                WebDriverWait wait = new WebDriverWait(webDriver, 6);
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span[data-testid=lblPDPDetailProductRatingNumber]")));
                starElmt = webDriver.findElement(By.cssSelector("span[data-testid=lblPDPDetailProductRatingNumber]"));
            } catch (Exception e) {
                System.out.println("There's an exception when waiting for visibility of star element");
            }

            try {
                WebDriverWait wait = new WebDriverWait(webDriver, 3);
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-testid=lblPDPDescriptionProduk]")));
                descriptionElmt = webDriver.findElement(By.cssSelector("span[data-testid=lblPDPDetailProductRatingNumber]"));
            } catch (Exception e) {
                System.out.println("There's an exception when waiting for visibility of desc element");
            }
            
            String imgUrl= null;
            try {
                WebDriverWait wait = new WebDriverWait(webDriver, 6);
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-testid=PDPImageThumbnail]")));
                imgElmt = webDriver.findElement(By.cssSelector("img[crossorigin=anonymous]"));
                imgUrl = imgElmt.getAttribute("src");
            } catch (Exception e) {
                System.out.println("There's an exception when waiting for visibility of star element");
            }

            String description = null;
            if (descriptionElmt != null) {
                description = webDriver.findElement(By.cssSelector("div[data-testid=lblPDPDescriptionProduk]"))
                        .getAttribute("innerText");
                if (description.length() > 200) {
                    description = description.substring(0, 200);
                }

                description = description.replace("\n", ". ");

                p.setDescription(description);
            }

            String stars = null;
            if (starElmt != null) {
                stars = starElmt.getAttribute("innerText");
            }

            p.setStars(stars);
            p.setImageLink(imgUrl);
            
            // close new window 
            webDriver.close();
            webDriver.switchTo().window(curWindowtHandle);
        } catch (Exception e) {
            System.out.println("There's an exception when trying to get the detail");
            String s = webDriver.getWindowHandle();
            if (!s.equals(curWindowtHandle)) {
                webDriver.close();
                webDriver.switchTo().window(curWindowtHandle);
            }
        }
        
        
        p.setName(pNameElmt.getAttribute("innerText"));
        p.setPrice(pPriceElmt.getAttribute("innerText"));
        p.setSeller(merchantElmt.getAttribute("innerText"));

        
        System.out.println("Name: " + pNameElmt.getAttribute("innerText"));
        System.out.println("Price: " + pPriceElmt.getAttribute("innerText"));
        System.out.println("Img Link: " + p.getImageLink());
        System.out.println("Seller: " + merchantElmt.getAttribute("innerText"));
        System.out.println("Description: " + p.getDescription());
        System.out.println("Stars: " + p.getStars());
    }

  

    private static String validateUrlProduct(String urlDetailProduct) throws Exception {
        String productHRef = null;
        if (urlDetailProduct.startsWith("https://ta")) {
            String queryString = urlDetailProduct.substring(urlDetailProduct.indexOf("?") + 1);
            String[] parameters = queryString.split("&");
            
            for (String param : parameters) {
                String[] paramKeyAndValue = param.split("=");
                if (paramKeyAndValue[0].equalsIgnoreCase("r")) {
                    productHRef = paramKeyAndValue[1];
                    productHRef = URLDecoder.decode(productHRef, "ASCII");
                    break;
                }
            }
        } else {
            productHRef = urlDetailProduct;
        }
        return productHRef;
    }
}
