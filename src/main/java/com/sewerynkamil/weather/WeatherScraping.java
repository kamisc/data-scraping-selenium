package com.sewerynkamil.weather;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.LocalDate;
import java.time.YearMonth;

public class WeatherScraping implements AutoCloseable {
    private final WebDriver driver;

    JavascriptExecutor js;

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
        String url = String.format("https://meteostat.net/en/place/%1$s/%2$s?t=%3$tF/%3$tF", location.name(), location.getCity(), atDay);
        driver.get(url);

        return null;
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }
}
