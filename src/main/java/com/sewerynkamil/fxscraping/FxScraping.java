package com.sewerynkamil.fxscraping;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        YearMonth ym = YearMonth.of(2022, 4);

        LocalDate fxDate = ym.atDay(14);
        Currency rateTo = Currency.GBP;
        Currency rateFrom = Currency.EUR;

        System.out.println("Converted currency rate: " + fxScraping.convertCurrency(fxDate, rateFrom, rateTo));
    }

    private double convertCurrency(LocalDate fxDate, Currency fromCurrency, Currency toCurrency) {
        driver.get("https://www.mastercard.us/en-us/personal/get-support/convert-currency.html");
        driver.manage().window().setSize(new Dimension(800, 900));

        try {
            Thread.sleep(3000);
            WebElement buttonPrivacyAccept = driver.findElement(By.id("onetrust-accept-btn-handler"));
            buttonPrivacyAccept.click();
        } catch (NoSuchElementException | InterruptedException e) {
            e.printStackTrace();
        }

        driver.findElement(By.id("txtTAmt")).clear();
        driver.findElement(By.id("txtTAmt")).sendKeys("1");

        driver.findElement(By.id("BankFee")).clear();
        driver.findElement(By.id("BankFee")).sendKeys("0.75");

        driver.findElement(By.id("tCurrency")).click();
        driver.findElement(By.linkText(fromCurrency.getLabel())).click();

        driver.findElement(By.id("cardCurrency")).click();
        driver.findElement(By.linkText(toCurrency.getLabel())).click();

        driver.findElement(By.id("getDate")).click();
        driver.findElement(By.className("ui-datepicker-year")).sendKeys(String.valueOf(fxDate.getYear()));
        driver.findElement(By.className("ui-datepicker-month")).sendKeys(fxDate.getMonth().toString());
        driver.findElement(By.linkText(String.valueOf(fxDate.getDayOfMonth()))).click();

        String rateString = driver.findElement(By.className("one-currency-amount")).getText().toUpperCase().split(" = ")[1];
        System.out.println("RATE: " + rateString);
        Matcher rateMatcher = Pattern.compile("^(\\d\\.\\d+)" + " " + toCurrency.getCurrencyName() + "$").matcher(rateString);

        if (rateMatcher.matches()) {
            return Double.parseDouble(rateMatcher.group(1));
        } else {
            throw new IllegalArgumentException("Page did not return the expected response");
        }
    }

    @Override
    public void close() throws Exception {
        driver.quit();
    }
}
