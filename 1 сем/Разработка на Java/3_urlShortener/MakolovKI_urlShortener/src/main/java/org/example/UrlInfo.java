package org.example;

import java.awt.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.UUID;

import static org.example.Main.csvPath;
import static org.example.Main.domain;

public class UrlInfo
{
    public String FullUrl;
    public String ShortUrl;
    public UUID AuthorId;
    public LocalDateTime ValidUntil;
    public int Limit;
    public int LinkOpens;

    private UrlInfo(String line){
        var data = line.split(";");
        this.FullUrl = data[0];
        this.ShortUrl = data[1];
        this.AuthorId = UUID.fromString(data[2]);
        this.ValidUntil = LocalDateTime.parse(data[3]);
        this.Limit = Integer.parseInt(data[4]);
        this.LinkOpens = Integer.parseInt(data[5]);

    }
    public UrlInfo(String fullUrl, UUID authorId, int daysOfActive , int limit ) throws Exception{
        this.FullUrl = fullUrl;
        this.ValidUntil = LocalDateTime.now().plusDays(daysOfActive);
        this.Limit = limit;
        this.AuthorId = authorId;
        this.LinkOpens = 0;
        this.ShortUrl = createShortUrl(fullUrl, authorId);
        Files.writeString(csvPath, this.fieldsToString(),  StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
    public static UrlInfo TryGetUrl (UUID authorId, String shortUrl) throws Exception {
        var temp = GetAllInfo().stream().filter(item-> item.ShortUrl.equals(shortUrl) && item.AuthorId.equals(authorId)).findFirst().orElse(null);
        if(temp == null)
            return null;
        if(temp.Limit <= temp.LinkOpens){
            System.out.println("У ссылки закончились переходы");
            DeleteLinkData(temp);
            return null;
        }
        if(temp.ValidUntil.isBefore(LocalDateTime.now())){
            System.out.println("Срок жизни ссылки закончился");
            DeleteLinkData(temp);
            return null;
        }

        return temp;
    }
    private static void DeleteLinkData(UrlInfo url) throws Exception {
        var list = GetAllInfo();
        list.removeIf(item-> item.fieldsToString().equals(url.fieldsToString()));
        Files.writeString(csvPath, "");
        for(var item :list){
            Files.writeString(csvPath, item.fieldsToString(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
    }
    private static void UpdateCounter(UrlInfo url) throws Exception {
        var list = GetAllInfo();
        list.removeIf(item-> item.fieldsToString().equals(url.fieldsToString()));
        url.LinkOpens++;
        list.add(url);
        Files.writeString(csvPath, "");
        for(var item :list){
            Files.writeString(csvPath, item.fieldsToString(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
    }
    public void Open() throws Exception {
        UpdateCounter(this);
        Desktop.getDesktop().browse(new URI(this.FullUrl));
    }

    private String fieldsToString() {
        return this.FullUrl + ";" + this.ShortUrl + ";" + this.AuthorId + ";" + this.ValidUntil + ";" + this.Limit + ";" + this.LinkOpens+"\r\n";
    }
    public static ArrayList<UrlInfo> GetAllInfo() throws Exception {
        var list = new ArrayList<UrlInfo>();
        for(var line: Files.readAllLines(csvPath)){
            var item = new UrlInfo(line);
            list.add(item);
        }
        return list;
    }

    public static String createShortUrl(String fullUrl, UUID userId) throws Exception{
        var stringToHash = userId.toString()+fullUrl;
        var digest = MessageDigest.getInstance("SHA-256");
        var hashBytes = digest.digest(stringToHash.getBytes());
        var hash = HexFormat.of().formatHex(hashBytes);
        return domain + hash.substring(0,10);
    }
}