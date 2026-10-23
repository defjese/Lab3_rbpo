-- Ограничения целостности. Выполнить ОДИН раз в pgAdmin (Query Tool базы tournament_db,
-- подключение под пользователем postgres), после первого запуска приложения.

-- название команды и турнира уникально
ALTER TABLE teams       ADD CONSTRAINT uk_teams_name       UNIQUE (name);
ALTER TABLE tournaments ADD CONSTRAINT uk_tournaments_name UNIQUE (name);

-- в турнире должно быть минимум 2 команды
ALTER TABLE tournaments ADD CONSTRAINT ck_tournaments_limit CHECK (team_limit > 1);

-- команда не может играть сама с собой
ALTER TABLE matches ADD CONSTRAINT ck_matches_diff_teams CHECK (team1_id <> team2_id);

-- счёт не может быть отрицательным
ALTER TABLE matches ADD CONSTRAINT ck_matches_scores
    CHECK (team1_score >= 0 AND team2_score >= 0);
