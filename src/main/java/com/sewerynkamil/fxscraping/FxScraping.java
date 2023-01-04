package com.sewerynkamil.fxscraping;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
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

    public static void main(String[] args) throws Exception {
        Set<Currency> ratesFrom = new HashSet<>();
        ratesFrom.add(Currency.EUR);
        ratesFrom.add(Currency.JPY);
        ratesFrom.add(Currency.CAD);

        Currency rateTo = Currency.GBP;

        YearMonth ym = YearMonth.of(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        int lastDayOfMonth = ym.atEndOfMonth().getDayOfMonth();
        Map<LocalDate, Map<Currency, Double>> rates = new HashMap<>();

        try (FxScraping fxScraping = new FxScraping("chrome")) {
            for (int day = 1; day <= lastDayOfMonth; ++day) {
                LocalDate fxDate = ym.atDay(day);
                HashMap<Currency, Double> ratesForDay = new HashMap<>();
                for (Currency rateFrom : ratesFrom) {
                    int retries = 3;
                    while (retries > 0) {
                        try {
                            double rate = fxScraping.convertCurrency(fxDate, rateFrom, rateTo);
                            System.out.printf("Rate from %s to %s on %d/%d/%d = %f%n", rateFrom, rateTo, day, ym.getMonthValue(), ym.getYear(), rate);
                            ratesForDay.put(rateFrom, rate);
                            retries = 0;
                        } catch (NoSuchElementException e) {
                            if (--retries <= 0) {
                                throw new IllegalStateException("Exceeded number of retries (" + e.getMessage() + ")");
                            }
                        }
                    }
                    rates.put(fxDate, ratesForDay);
                }
            }
        }
        try (FileOutputStream fos = new FileOutputStream("datasink/rates-" + ym + ".ser")) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(rates);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double convertCurrency(LocalDate fxDate, Currency fromCurrency, Currency toCurrency) throws InterruptedException {
        driver.get("https://www.mastercard.us/en-us/personal/get-support/convert-currency.html");

        try {
            Thread.sleep(750);
            WebElement buttonPrivacyAccept = driver.findElement(By.id("onetrust-accept-btn-handler"));
            buttonPrivacyAccept.click();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }

        driver.findElement(By.id("tCurrency")).click();
        driver.findElement(By.linkText(fromCurrency.getLabel())).click();

        driver.findElement(By.id("cardCurrency")).click();
        driver.findElement(By.linkText(toCurrency.getLabel())).click();

        driver.findElement(By.id("BankFee")).clear();
        driver.findElement(By.id("BankFee")).sendKeys("0.75");

        driver.findElement(By.id("txtTAmt")).clear();
        driver.findElement(By.id("txtTAmt")).sendKeys("1");

        driver.findElement(By.id("getDate")).click();
        driver.findElement(By.className("ui-datepicker-year")).sendKeys(String.valueOf(fxDate.getYear()));
        driver.findElement(By.className("ui-datepicker-month")).sendKeys(fxDate.getMonth().toString());
        driver.findElement(By.linkText(String.valueOf(fxDate.getDayOfMonth()))).click();

        String rateString = driver.findElement(By.className("one-currency-amount")).getText().toUpperCase().split(" = ")[1];
        Matcher rateMatcher = Pattern.compile("^(\\d\\.\\d+)" + " " + toCurrency.getCurrencyName() + "$").matcher(rateString);

        if (rateMatcher.matches()) {
            return Double.parseDouble(rateMatcher.group(1));
        } else {
            throw new IllegalArgumentException("Page did not return the expected response");
        }
    }

    @Override
    public void close() {
        driver.quit();
    }
}
