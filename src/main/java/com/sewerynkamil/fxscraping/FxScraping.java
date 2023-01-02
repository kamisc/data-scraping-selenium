package com.sewerynkamil.fxscraping;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class FxScraping implements AutoCloseable {

    private final WebDriver driver;

    public FxScraping(String browser) {
        driver = switch (browser) {
            case "chrome" -> {
                System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
                ChromeOptions chromeOptions = new ChromeOptions()
                        .setHeadless(false)
                        .addArguments("--disable-gpu", "--window-size=800,900", "--ignore-certificate-errors", "--silent");
                yield new ChromeDriver(chromeOptions);
            }
            case "firefox" -> {
                System.setProperty("webdriver.gecko.driver", "geckodriver.exe");
                FirefoxOptions firefoxOptions = new FirefoxOptions().setHeadless(false);
                yield new FirefoxDriver(firefoxOptions);
            }
            default -> throw new IllegalArgumentException("Unknown browser");
        };
    }

    public static void main(String[] args) {
        FxScraping fxScraping = new FxScraping("firefox");

        fxScraping.driver.get("https://www.wp.pl");


    }



    @Override
    public void close() throws Exception {
        driver.quit();
    }
}
