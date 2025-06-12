package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getUser(long userId) {
        User user = getUserByIdOrThrow(userId);
        return new UserResponse(user.getId(), user.getEmail());
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {

        User user = getUserByIdOrThrow(userId);

        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }


    /* 도메인 로직 분리 */

    public User getUserByIdOrThrow(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
    }

    public User getManagerByIdOrThrow(long managerUserId) {
        return userRepository.findById(managerUserId)
                .orElseThrow(() -> new InvalidRequestException("등록하려고 하는 담당자 유저가 존재하지 않습니다."));
    }

    public User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new InvalidRequestException("가입되지 않은 유저입니다."));
    }

    public void ensureEmailIsUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new InvalidRequestException("이미 존재하는 이메일입니다.");
        }
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

}
