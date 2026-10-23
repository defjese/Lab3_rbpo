package com.example.l2_rbpo.config;

import com.example.l2_rbpo.entity.Team;
import com.example.l2_rbpo.entity.TeamApplication;
import com.example.l2_rbpo.entity.Tournament;
import com.example.l2_rbpo.entity.User;
import com.example.l2_rbpo.entity.UserRole;
import com.example.l2_rbpo.repository.TeamApplicationRepository;
import com.example.l2_rbpo.repository.TeamRepository;
import com.example.l2_rbpo.repository.TournamentRepository;
import com.example.l2_rbpo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// Тестовые данные: заполняются один раз, если таблица users пустая
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final TeamApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(
            UserRepository userRepository,
            TeamRepository teamRepository,
            TournamentRepository tournamentRepository,
            TeamApplicationRepository applicationRepository
    ) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        createUser("admin", "Администратор", UserRole.ADMIN);
        User ivan = createUser("ivan", "Иван Петров", UserRole.USER);
        User petr = createUser("petr", "Пётр Сидоров", UserRole.USER);
        User anna = createUser("anna", "Анна Ким", UserRole.USER);
        User olga = createUser("olga", "Ольга Смирнова", UserRole.USER);
        User sergey = createUser("sergey", "Сергей Волков", UserRole.USER);

        Team eagles = createTeam("Орлы", ivan, ivan, sergey);
        Team wolves = createTeam("Волки", petr, petr);
        createTeam("Тигры", anna, anna);
        createTeam("Львы", olga, olga);

        Tournament autumnCup = createTournament("Кубок осени", 4);
        createTournament("Весенний кубок", 8);

        createApplication(eagles, autumnCup);
        createApplication(wolves, autumnCup);
    }

    private User createUser(String username, String displayName, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setDisplayName(displayName);
        user.setRole(role);
        return userRepository.save(user);
    }

    private Team createTeam(String name, User captain, User... members) {
        Team team = new Team();
        team.setName(name);
        team.setCaptain(captain);
        team.setMembers(new ArrayList<>(List.of(members)));
        return teamRepository.save(team);
    }

    private Tournament createTournament(String name, int teamLimit) {
        Tournament tournament = new Tournament();
        tournament.setName(name);
        tournament.setTeamLimit(teamLimit);
        return tournamentRepository.save(tournament);
    }

    private void createApplication(Team team, Tournament tournament) {
        TeamApplication application = new TeamApplication();
        application.setTeam(team);
        application.setTournament(tournament);
        applicationRepository.save(application);
    }
}
