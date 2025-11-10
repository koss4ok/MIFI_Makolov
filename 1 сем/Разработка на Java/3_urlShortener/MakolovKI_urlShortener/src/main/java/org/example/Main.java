package org.example;

import java.awt.*;
import java.nio.file.Path;
import java.util.*;


public class Main {
    protected static Path csvPath = Path.of("./DB.csv");
    protected static String domain = "http://Makolov.ru/";
    protected static Scanner scanner;
    protected static UUID currentUserId;
    public static void main(String[] args) throws Exception {
        scanner = new Scanner(System.in);
        auth();
        while (true){
            System.out.println("Введите команду. Список доступных комманд можно увидеть написав ?");
            var command = scanner.nextLine().strip();
            if (command.startsWith("http")) {
                if(isShort(command)){
                    var curUrl = UrlInfo.TryGetUrl(currentUserId, command);
                    if (curUrl != null){
                        curUrl.Open();
                    }
                    else
                        System.out.println("Ссылка не найдена");
                }
                else{
                    System.out.println("Сколько дней должна быть активна ссылка?");
                    var daysOfActive = Integer.parseInt(scanner.nextLine().strip());
                    System.out.println("Сколько переходов по ссылке можно осуществить?");
                    var limit = Integer.parseInt(scanner.nextLine().strip());
                    var newUrl = new UrlInfo(command, currentUserId, daysOfActive, limit);
                    System.out.println("Короткая ссылка создана: " + newUrl.ShortUrl);
                }
            }
            if(command.equals("?")) {
                System.out.println(
                        """
                        Список доступных комманд:
                        1) Exit - выход и завершение работы.
                        2) ? - список доступных команд.
                        3) Switch - смена текущего пользователя
                        3) Ввод полной ссылки - автоматически будет сформированна короткая ссылка.
                        4) Ввод короткой ссылки - будет выполнен редирект и открыта страница из полной ссылки.""");
            }
            if(command.equals("Switch"))
                Switch();
            if(command.equals("Exit")){
                return;
            }
        }
    }
    private static void Switch(){
        while (true) {
            System.out.println("Вы хотите сменить пользователя? (y/n)");
            if(scanner.nextLine().strip().equals("y")){
                auth();
                return;
            }
            if(scanner.nextLine().strip().equals("n"))
                return;
        }
    }
    private static void auth(){
        System.out.println("Введите свой Id или просто нажмите Enter для нового пользователя");
        var answer = scanner.nextLine().trim();
        if(answer.isBlank()){
            var id = UUID.randomUUID();
            System.out.println("Ваш Id = " + id);
            currentUserId =  id;
        }
        else
            currentUserId = UUID.fromString(answer);
    }

    private static boolean isShort(String url){
        return url.startsWith(domain);
    }


}