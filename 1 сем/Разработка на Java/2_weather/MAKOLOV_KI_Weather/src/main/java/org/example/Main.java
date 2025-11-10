package org.example;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Main  {
    private static final String apiKey = "476f9a76-d0bf-4ed7-8932-422c1395229c";
    private static final String apiBaseUrl = "https://api.weather.yandex.ru/v2/forecast";

    private static String lat;
    private static String lon;
    private static String limit;
    private static HttpClient client;
    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.print("Введите широту: ");
        var scanner = new Scanner(System.in);
        lat = scanner.nextLine();
        System.out.print("Введите долготу: ");
        lon = scanner.nextLine();

        System.out.print("Введите количество дней: ");
        limit = scanner.nextLine();
        client = HttpClient.newHttpClient();

        fullBody();
        showCurrentTemp();
        showMeanTemp();


    }
    private static void fullBody() throws IOException, InterruptedException
    {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(apiBaseUrl + "?lat=" + lat + "&lon=" + lon+"&limit="+limit))
                .header("X-Yandex-Weather-Key", apiKey)
                .GET()
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        var body = response.body();

        System.out.println(body);
    }

    private static void showCurrentTemp() throws IOException, InterruptedException
    {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(apiBaseUrl + "?lat=" + lat + "&lon=" + lon))
                .header("X-Yandex-Weather-Key", apiKey)
                .GET()
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        var body = response.body();

        JsonObject root;
        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            root = reader.readObject();
        }
        JsonObject fact = root.getJsonObject("fact");
        var temp = fact.getInt("temp");

        System.out.println("Температура сейчас: " + temp);
    }

    private static void showMeanTemp() throws IOException, InterruptedException
    {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(apiBaseUrl + "?lat=" + lat + "&lon=" + lon+"&limit="+limit))
                .header("X-Yandex-Weather-Key", apiKey)
                .GET()
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        var body = response.body();

        JsonObject root;
        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            root = reader.readObject();
        }

        JsonArray forecasts = root.getJsonArray("forecasts");
        double sum = 0;

        for (int i = 0; i < forecasts.size(); i++) {
            JsonObject dayObj = forecasts.getJsonObject(i);
            JsonObject parts = dayObj.getJsonObject("parts");
            JsonObject day = parts.getJsonObject("day");
            int tempAvg = day.getInt("temp_avg");
            sum += tempAvg;
        }

        double avg = sum / forecasts.size();
        System.out.println("Средняя температура за " + forecasts.size() + " суток: " + avg);
    }

}