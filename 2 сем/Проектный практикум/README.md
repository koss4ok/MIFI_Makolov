# Lottery backend (MVP)

REST API для базовой лотерейной системы.

## Запуск

1) Поднимите сервисы:
```bash
docker-compose up --build
```

2) Приложение слушает `http://localhost:8080`.

## API

### Создать тираж
`POST /draws`
Пример:
```bash
curl -s -X POST http://localhost:8080/draws
```

### Получить активные тиражи
`GET /draws`
```bash
curl -s http://localhost:8080/draws
```

### Создать билет
`POST /draws/{id}/tickets`

```bash
curl -s -X POST http://localhost:8080/draws/<DRAW_ID>/tickets
```

### Завершить тираж
`POST /draws/{id}/complete`

```bash
curl -s -X POST http://localhost:8080/draws/<DRAW_ID>/complete
```

### Проверить билет
`GET /tickets/{id}`
```bash
curl -s http://localhost:8080/tickets/<TICKET_ID>
```

## Переменные окружения

* `PORT` (по умолчанию 8080)
* `DB_URL`, `DB_USER`, `DB_PASSWORD`
* `DRAW_NUM_COUNT` (по умолчанию 6)
* `DRAW_NUM_MIN` (по умолчанию 1)
* `DRAW_NUM_MAX` (по умолчанию 49)
