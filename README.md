# Спортивный турнир: REST API

Учебный сервис для организации спортивных турниров. Команды подают заявки на участие, организатор одобряет их, запускает турнир, назначает матчи, фиксирует результаты и завершает турнир с итоговой таблицей.

**Стек:** Java 17, Spring Boot 4.1, Spring Data JPA (Hibernate), PostgreSQL, Maven.

## Предметная модель

| Сущность | Таблица | Основные поля |
|---|---|---|
| Пользователь (`User`) | `users` | логин (уникальный), хеш пароля, имя, роль |
| Команда (`Team`) | `teams` | название (уникальное), капитан (`User`), состав (`team_members`) |
| Турнир (`Tournament`) | `tournaments` | название (уникальное), лимит команд (больше 1), статус |
| Заявка (`TeamApplication`) | `team_applications` | команда, турнир, статус |
| Матч (`Match`) | `matches` | турнир, две команды, счёт, статус |

**Связи**

- команда → капитан: многие к одному (`User`);
- команда ↔ участники: многие ко многим (таблица `team_members`);
- заявка → команда и турнир: многие к одному;
- матч → турнир и две команды: многие к одному.

**Статусы**

- турнир: `PLANNED` → `IN_PROGRESS` → `FINISHED`;
- заявка: `PENDING` → `APPROVED` или `REJECTED`;
- матч: `SCHEDULED` → `FINISHED`.

**Ограничения целостности в БД**

- одна заявка от команды на турнир (`UNIQUE(team_id, tournament_id)`);
- уникальные логин, название команды и название турнира;
- `team_limit > 1`;
- `team1_id <> team2_id` (команда не играет сама с собой);
- счёт матча не отрицательный;
- внешние ключи и `NOT NULL` на обязательных полях;
- удаление сущности, на которую ссылаются другие, запрещено (ответ `409`), поэтому недействительных ссылок не остаётся.

## Бизнес-операции

| Операция | Запрос | Что делает | Условия (иначе `409`) |
|---|---|---|---|
| Одобрить заявку | `POST /api/team-applications/{id}/approve` | Переводит заявку в `APPROVED` | Турнир `PLANNED`, заявка `PENDING`, лимит команд не исчерпан |
| Запустить турнир | `POST /api/tournaments/{id}/start` | Турнир становится `IN_PROGRESS`, неодобренные заявки автоматически отклоняются | Турнир `PLANNED`, одобрено минимум 2 команды |
| Назначить матч | `POST /api/matches/schedule` | Создаёт матч `SCHEDULED` | Турнир `IN_PROGRESS`, обе команды с одобренной заявкой, команды разные (`400`), такой пары в турнире ещё нет |
| Завершить матч | `POST /api/matches/{id}/finish` | Фиксирует счёт, матч становится `FINISHED` | Матч не завершён, турнир `IN_PROGRESS` |
| Завершить турнир | `POST /api/tournaments/{id}/finish` | Турнир становится `FINISHED`, возвращает таблицу (победа 3 очка, ничья 1) | Турнир `IN_PROGRESS`, есть матчи, все матчи завершены |

Целостность обеспечивается транзакциями (`@Transactional`), проверкой состояния перед изменением и блокировкой строки турнира на время операции (два одновременных одобрения не превысят лимит).

Кроме бизнес-операций, для каждой сущности доступен CRUD: `GET /api/{resources}`, `GET /api/{resources}/{id}`, `POST`, `PATCH /{id}`, `DELETE /{id}` для `users`, `teams`, `tournaments`, `team-applications`, `matches`.

## Структура проекта

```
controller/   приём запросов, коды ответов
service/      бизнес-логика и проверки состояния
repository/   интерфейсы Spring Data JPA
entity/       JPA-сущности и enum статусов
dto/          запросы и ответы API
config/       настройки безопасности и тестовые данные
exception/    обработка ошибок БД
```

## Запуск

1. Установить PostgreSQL, создать базу и пользователя с ограниченными правами:

   ```sql
   CREATE USER tournament_app WITH PASSWORD 'ваш_пароль';
   CREATE DATABASE tournament_db;
   -- затем, подключившись к tournament_db:
   GRANT CONNECT ON DATABASE tournament_db TO tournament_app;
   GRANT USAGE, CREATE ON SCHEMA public TO tournament_app;
   ALTER DEFAULT PRIVILEGES IN SCHEMA public
       GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO tournament_app;
   ALTER DEFAULT PRIVILEGES IN SCHEMA public
       GRANT USAGE, SELECT ON SEQUENCES TO tournament_app;
   ```

2. Скопировать `.env.example` в `.env` и указать пароль (файл `.env` в репозиторий не попадает):

   ```
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=tournament_db
   DB_USERNAME=tournament_app
   DB_PASSWORD=
   ```

3. Запустить приложение (`L2RbpoApplication` в IDE или `./mvnw spring-boot:run`). Таблицы создаются автоматически, при пустой базе добавляются тестовые данные (пользователи, команды, турниры, заявки).

4. Один раз выполнить `db/constraints.sql` в pgAdmin под пользователем `postgres` (уникальные названия и CHECK-ограничения).

API доступен на `http://localhost:8080`.

## Проверка

Импортировать `tournament.postman_collection.json` в Postman и запустить коллекцию целиком (Run collection). Она создаёт данные, проходит CRUD по каждой сущности, выполняет все бизнес-операции (успешные и отклонённые сценарии) и в конце удаляет созданное.
