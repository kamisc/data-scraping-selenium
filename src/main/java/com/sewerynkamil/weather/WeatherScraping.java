package com.sewerynkamil.weather;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherScraping implements AutoCloseable {
    private final WebDriver driver;

    JavascriptExecutor js;

    Pattern temperatureExtractor = Pattern.compile("([+-]?\\d+(\\.\\d+)?)\n°C");
    Pattern precipitationExtractor = Pattern.compile("(\\d+(\\.\\d+)?)\nmm");
    Pattern pressureExtractor = Pattern.compile("(\\d+(\\.\\d+)?)\nhPa");


    public WeatherScraping(String browser) {
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
        js = (JavascriptExecutor)driver;
    }

    public static void main(String[] args) {
        WeatherScraping scraper = new WeatherScraping("firefox");
        YearMonth ym = YearMonth.of(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        LocalDate weatherDate = ym.atDay(14);
        WeatherLocation location = WeatherLocation.valueOf("GB");

        System.out.println(location.getWebsiteCode());

        for (WeatherLocation loc : WeatherLocation.values()) {
            System.out.println(loc.name());
        }

        scraper.getWeatherData(weatherDate, location);

    }

    public WeatherData getWeatherData(LocalDate atDay, WeatherLocation location) {
        String url = String.format("https://meteostat.net/en/place/%1$s/%2$s?s=%3$s&t=%4$tF/%4$tF", location.name(), location.getCity(), location.getWebsiteCode(), atDay);
        driver.get(url);

        try {
            Thread.sleep(2000);
            WebElement buttonPrivacyAccept = driver.findElement(By.cssSelector(".btn-primary:nth-child(3)"));
            buttonPrivacyAccept.click();
        } catch (NoSuchElementException | InterruptedException e) {
            e.printStackTrace();
        }

        WebElement tempRow = driver.findElement(By.cssSelector(".col-6:nth-child(1) .card-title"));
        Matcher tempMatcher = temperatureExtractor.matcher(tempRow.getText());
        double temperature = 0.0;
        if (tempMatcher.matches()) {
            temperature = Double.parseDouble(tempMatcher.group(1));
        }
        System.out.println("Temperature: " + temperature);

        WebElement precipitationRow = driver.findElement(By.cssSelector(".col-6:nth-child(2) .card-title"));
        Matcher precipitationMatcher = precipitationExtractor.matcher(precipitationRow.getText());
        double precipitation = 0.0;
        if (precipitationMatcher.matches()) {
            precipitation = Double.parseDouble(precipitationMatcher.group(1));
        }
        System.out.println("Precipitation: " + precipitation);

        WebElement pressureRow = driver.findElement(By.cssSelector(".col-6:nth-child(4) .card-title"));
        Matcher pressureMatcher = pressureExtractor.matcher(pressureRow.getText());
        double pressure = 0.0;
        if (pressureMatcher.matches()) {
            pressure = Double.parseDouble(pressureMatcher.group(1));
        }
        System.out.println("Pressure: " + pressure);

        return new WeatherData(temperature, precipitation, pressure);
    }

    @Override
    public void close() {
        driver.close();
    }
}
