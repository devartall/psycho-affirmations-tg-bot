# Psycho Affirmations Bot

Telegram бот для работы с аффирмациями. Бот позволяет получать случайные аффирмации и управлять их списком (для администраторов).

## Функциональность

- Получение случайных аффирмаций
- Административный интерфейс для управления аффирмациями
- Поддержка команд:
  - `/start` - Начать работу с ботом
  - `/auth <пароль>` - Получить права администратора
  - `/logout` - Отказаться от прав администратора (только для администраторов)
  - `/add <текст>` - Добавить новую аффирмацию (только для администраторов)
  - `/list` - Показать список всех аффирмаций (только для администраторов)
  - `/delete` - Удалить все аффирмации (только для администраторов)

## Технологии

- Kotlin
- Spring Boot
- PostgreSQL
- Liquibase
- Telegram Bot API

## Требования

- JDK 21 или выше
- Maven
- PostgreSQL 12 или выше
- Telegram Bot Token (получить можно у [@BotFather](https://t.me/botfather))

## Установка и запуск

1. Клонируйте репозиторий:

2. Настройте параметры в файле `docker-compose.yml`:
```yaml
services:
  app:
    environment:
      - BOT_TOKEN=your_bot_token_here
      - DB_USERNAME=your_username
      - DB_PASSWORD=your_password
```

3. Запустите с помощью Docker Compose:
```bash
docker-compose up -d
```

## Разработка

### Запуск тестов
```bash
mvn test
```

### Локальная разработка
1. Запустите PostgreSQL в Docker:
```bash
docker-compose up -d postgres
```

2. Запустите приложение в режиме разработки:
```bash
mvn spring-boot:run
```