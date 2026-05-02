# MAKOLOV-otp

Бэкенд-сервис для работы с одноразовыми паролями (OTP) с ролевым API, аутентификацией через JWT, хранением данных в PostgreSQL (через JDBC) и поддержкой нескольких каналов доставки кодов.

## Структура проекта

- `src/main/java/ru/makolov/otp/api` — HTTP-обработчики на `com.sun.net.httpserver`
- `src/main/java/ru/makolov/otp/service` — слой бизнес-логики
- `src/main/java/ru/makolov/otp/dao` — DAO-интерфейсы и реализации на JDBC
- `src/main/java/ru/makolov/otp/security` — генерация и проверка JWT
- `src/main/java/ru/makolov/otp/service/channel` — каналы доставки OTP
- `src/main/resources/web` — встроенный веб-клиент (`http://localhost:8080`)
- `/Отчет` - лежат скриншоты работы приложения

## Стек технологий

- Java 25  
- Maven  
- PostgreSQL 17  
- JDBC  
- com.sun.net.httpserver 
- SLF4J + Logback  
- JJWT  
- Angus Mail  
- jSMPP  

## Требования и настройка

1. Создайте базу данных PostgreSQL, например: `makolov_otp`.
2. Настройте `src/main/resources/application.properties` (или переменные окружения).
3. При необходимости настройте каналы:
   - `src/main/resources/email.properties`
   - `src/main/resources/sms.properties`
   - `src/main/resources/telegram.properties`

При запуске сервис автоматически выполняет `schema.sql` и создаёт администратора по умолчанию, если он отсутствует.

Данные администратора по умолчанию:

- логин: `admin`  
- пароль: `admin`  

## Запуск в IntelliJ IDEA

1. Откройте проект как Maven-проект.
2. Установите Project SDK: Java 25.
3. Запустите класс `ru.makolov.otp.Application`.
4. Базовый URL API: `http://localhost:8080`
5. Веб-клиент: `http://localhost:8080`

## Переменные окружения

- `SERVER_PORT`
- `FILE_CHANNEL_PATH`
- `OUTBOX_DIR`
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_TTL_SECONDS`
- `EMAIL_USERNAME`
- `EMAIL_PASSWORD`
- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_CHAT_ID`

## API endpoints

- `POST /api/auth/register` — регистрация пользователя  
- `POST /api/auth/login` — вход и получение JWT  
- `POST /api/user/otp/generate` — генерация OTP  
- `POST /api/user/otp/validate` — проверка OTP  
- `PUT /api/admin/config` — обновление конфигурации OTP (админ)  
- `GET /api/admin/users` — список пользователей (админ)  
- `DELETE /api/admin/users/{id}` — удаление пользователя (админ)  

## Сценарий использования

1. Зарегистрировать пользователя (`/api/auth/register`).
2. Выполнить вход (`/api/auth/login`) и получить JWT-токен.
3. Вызывать пользовательские методы с заголовком `Authorization: Bearer <jwt>`.
4. Сгенерировать OTP для операции и выбранного канала (`email`, `sms`, `telegram`, `file`).
5. Подтвердить OTP (одноразово); после этого статус меняется на `USED`.
6. Администратор может изменять конфигурацию OTP и управлять пользователями (кроме админов).

## Поведение каналов

- `email` — реальная отправка через SMTP при `email.enabled=true`, также всегда сохраняется копия в outbox-файл.
- `sms` — эмуляция отправки через SMPP при `sms.enabled=true`; также сохраняется копия в outbox-файл.
- `telegram` — реальная отправка через Telegram Bot API.
- `file` — запись OTP напрямую в файл `otp-codes.log`.

Директория outbox (по умолчанию):

- `notifications-outbox/email/*.txt`
- `notifications-outbox/sms/*.txt`

## Тестирование

Для облегчения проверки - сделан простой вебклиент

1. Открыть `http://localhost:8080`.
2. Зарегистрировать пользователя и выполнить вход.
3. Сгенерировать OTP через канал `file`.
4. Прочитать код из `otp-codes.log` и подтвердить его.
5. Войти как администратор и проверить методы `/api/admin/config` и `/api/admin/users`.

Ручной тест через curl (PowerShell):

```powershell
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"login\":\"user1\",\"password\":\"123456\",\"role\":\"USER\"}"
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"login\":\"user1\",\"password\":\"123456\"}"