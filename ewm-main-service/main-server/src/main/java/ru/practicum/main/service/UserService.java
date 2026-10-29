package ru.practicum.main.service;

import org.springframework.stereotype.Service;
import ru.practicum.main.dto.NewUserRequest;
import ru.practicum.main.dto.UserDto;
import ru.practicum.main.error.ConflictException;
import ru.practicum.main.error.NotFoundException;
import ru.practicum.main.mapper.UserMapper;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto createUser(NewUserRequest request) {
        String email = request.getEmail();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Пользователь с email " + email + " уже существует");
        }

        User user = UserMapper.toUser(request);
        User savedUser = userRepository.save(user);
        return UserMapper.toDto(savedUser);
    }

    public List<UserDto> findUsersById(List<Long> ids, int from, int size) {
        List<User> users;
        if (ids == null) {
            users = userRepository.findUsers(from, size);
        } else {
            users = userRepository.findUsersById(ids, from, size);
        }
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            UserDto userDto = UserMapper.toDto(user);
            userDtos.add(userDto);
        }
        return userDtos;
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(
                    "Пользователь с id " + userId + " не найден"
            );
        }
        userRepository.deleteById(userId);
    }

}
