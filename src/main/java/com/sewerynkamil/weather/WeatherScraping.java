package com.sewerynkamil.weather;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
import java.util.HashMap;
import java.util.Map;
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
        YearMonth ym = YearMonth.of(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        int lastDayOfMonth = ym.atEndOfMonth().getDayOfMonth();
        Map<LocalDate, Map<WeatherLocation, WeatherData>> weather = new HashMap<>();

        try (WeatherScraping weatherScraping = new WeatherScraping("firefox")) {
            for (int day = 1; day <= lastDayOfMonth; ++day) {
                LocalDate weatherDate = ym.atDay(day);
                HashMap<WeatherLocation, WeatherData> weatherForDay = new HashMap<>();

                for (WeatherLocation location : WeatherLocation.values()) {
                    int retries = 3;

                    while (retries > 0) {
                        try {
                            WeatherData data = weatherScraping.getWeatherData(ym.atDay(day), location);
                            System.out.printf("Weather in %s on %d/%d/%d = %s%n", location, day, ym.getMonthValue(), ym.getYear(), data);
                            weatherForDay.put(location, data);
                            retries = 0;
                        } catch (NoSuchElementException | InterruptedException e) {
                            if (--retries <= 0) {
                                throw new IllegalStateException("Exceeded number of retries (" + e.getMessage() + ")");
                            }
                        }
                    }
                }
                weather.put(weatherDate, weatherForDay);
            }
            try (FileOutputStream fos = new FileOutputStream("datasink/weather-" + ym + ".ser")) {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(weather);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public WeatherData getWeatherData(LocalDate atDay, WeatherLocation location) throws InterruptedException {
        String url = String.format("https://meteostat.net/en/place/%1$s/%2$s?s=%3$s&t=%4$tF/%4$tF", location.name(), location.getCity(), location.getWebsiteCode(), atDay);
        driver.get(url);

        try {
            Thread.sleep(2000);
            WebElement buttonPrivacyAccept = driver.findElement(By.cssSelector(".btn-primary:nth-child(3)"));
            buttonPrivacyAccept.click();
        } catch (NoSuchElementException | InterruptedException e) {
            e.printStackTrace();
        }
        Thread.sleep(2000);

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
