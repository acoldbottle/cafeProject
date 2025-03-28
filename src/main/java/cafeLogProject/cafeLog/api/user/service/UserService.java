package cafeLogProject.cafeLog.api.user.service;

import cafeLogProject.cafeLog.api.user.dto.*;
import cafeLogProject.cafeLog.api.user.elasticsearch.NicknameDocument;
import cafeLogProject.cafeLog.api.user.elasticsearch.NicknameDocumentRepository;
import cafeLogProject.cafeLog.common.auth.jwt.JWTUserDTO;
import cafeLogProject.cafeLog.common.exception.user.UserNicknameException;
import cafeLogProject.cafeLog.common.exception.user.UserNotFoundException;
import cafeLogProject.cafeLog.domains.user.domain.User;
import cafeLogProject.cafeLog.domains.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cafeLogProject.cafeLog.common.exception.ErrorCode.*;


@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final NicknameDocumentRepository nicknameDocumentRepository;

    public UserInfoRes getUserInfo(String username) {

        return userRepository.findMyProfileWithReviewCount(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));
    }

    @Transactional
    public void updateUser(String username, UserUpdateReq userUpdateReq) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));

        validateNickname(username, userUpdateReq);

        user.updateUserNickname(userUpdateReq.getNickName());
        user.updateUserIntroduce(userUpdateReq.getIntroduce());

        User updatedUser = userRepository.save(user);
        nicknameDocumentRepository.save(NicknameDocument.from(updatedUser));
    }

    public IsExistNicknameRes isExistNickname(String username, String nickname) {

        if (!userRepository.existsNicknameExcludingSelf(username, nickname)) {
            return new IsExistNicknameRes(nickname, false);
        }

        return new IsExistNicknameRes(nickname, true);
    }

    public OtherUserInfoRes getOtherUserInfo(String currentUsername, Long otherUserId) {

        return userRepository.findOtherUserInfo(currentUsername, otherUserId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));
    }

    public List<UserSearchRes> searchUserByNickname(String searchNickname, String currentUsername) {

        if (searchNickname == null || searchNickname.trim().isEmpty()) {
            throw new UserNicknameException(USER_NICKNAME_NULL_ERROR);
        }

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));

        List<UserSearchRes> findUsers = userRepository.findUsersByNickname(searchNickname);

        return userRepository.searchUserByNickname(searchNickname, user.getId(), findUsers);
    }

    private void validateNickname(String userName, UserUpdateReq userUpdateReq) {

        if (userUpdateReq.getNickName() != null && userRepository.existsNicknameExcludingSelf(userName, userUpdateReq.getNickName())) {
            log.warn("nickname is duplicate. user = {}, nickname = {}", userName, userUpdateReq.getNickName());
            throw new UserNicknameException(USER_NICKNAME_ERROR);
        }
    }

    public JWTUserDTO findByUsername(String username) {

        return userRepository.findByUsername(username)
                .map(user -> new JWTUserDTO(user.getId(), user.getUsername(), user.getRole()))
                .orElseThrow(() -> {
                    log.error("User not found: {}", username); // 로깅 추가
                    return new UserNotFoundException(USER_NOT_FOUND_ERROR);
                });
    }
}
