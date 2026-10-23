package com.example.l2_rbpo.service;

import com.example.l2_rbpo.dto.UserCreateRequest;
import com.example.l2_rbpo.dto.UserPatchRequest;
import com.example.l2_rbpo.dto.UserResponse;
import com.example.l2_rbpo.entity.Team;
import com.example.l2_rbpo.entity.User;
import com.example.l2_rbpo.entity.UserRole;
import com.example.l2_rbpo.repository.TeamRepository;
import com.example.l2_rbpo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public UserService(
            UserRepository userRepository,
            TeamRepository teamRepository
    ) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(getUser(id));
    }

    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username is already taken"
            );
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setDisplayName(request.displayName());
        user.setRole(UserRole.USER);

        return toResponse(userRepository.save(user));
    }

    public UserResponse update(Long id, UserPatchRequest request) {
        User user = getUser(id);

        if (request.username() != null) {
            if (!user.getUsername().equals(request.username())
                    && userRepository.existsByUsername(
                    request.username())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Username is already taken"
                );
            }

            user.setUsername(request.username());
        }

        if (request.password() != null) {
            user.setPasswordHash(
                    passwordEncoder.encode(request.password())
            );
        }

        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }

        return toResponse(userRepository.save(user));
    }

    public void delete(Long id) {
        User user = getUser(id);

        boolean isCaptain = !teamRepository
                .findByCaptainId(id).isEmpty();

        boolean isMember = teamRepository.findAll()
                .stream()
                .anyMatch(team -> team.getMembers()
                        .stream()
                        .anyMatch(member ->
                                member.getId().equals(id)));

        if (isCaptain || isMember) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a user who belongs to a team "
                            + "or is a team captain"
            );
        }

        userRepository.delete(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}