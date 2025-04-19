package cafeLogProject.cafeLog.api.user.service;

import cafeLogProject.cafeLog.api.common.CacheService;
import cafeLogProject.cafeLog.api.user.dto.*;
import cafeLogProject.cafeLog.api.user.elasticsearch.NicknameDocument;
import cafeLogProject.cafeLog.api.user.elasticsearch.NicknameDocumentRepository;
import cafeLogProject.cafeLog.common.auth.jwt.JWTUserDTO;
import cafeLogProject.cafeLog.common.exception.user.UserNicknameException;
import cafeLogProject.cafeLog.common.exception.user.UserNotFoundException;
import cafeLogProject.cafeLog.domains.follow.repository.FollowRepository;
import cafeLogProject.cafeLog.domains.user.domain.User;
import cafeLogProject.cafeLog.domains.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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
    private final CacheService userCacheService;
    private final FollowRepository followRepository;

    public UserInfoRes getUserInfo(Long userId) {

        UserInfoRes userinfoRes = userCacheService.getUserBasicInfo(userId);
        return userRepository.getFollowCntAndReviewCnt(userinfoRes);
    }

    @Transactional
    @CacheEvict(value = "cacheUser", key = "'users:' + #userId + ':info'")
    public void updateUser(Long userId, UserUpdateReq userUpdateReq) {

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));

        validateNickname(user.getId(), userUpdateReq);

        user.updateUserNickname(userUpdateReq.getNickName());
        user.updateUserIntroduce(userUpdateReq.getIntroduce());

        User updatedUser = userRepository.save(user);
        nicknameDocumentRepository.save(NicknameDocument.from(updatedUser));
    }

    public IsExistNicknameRes isExistNickname(Long userId, String nickname) {

        if (!userRepository.existsNicknameExcludingSelf(userId, nickname)) {
            return new IsExistNicknameRes(nickname, false);
        }

        return new IsExistNicknameRes(nickname, true);
    }

    public UserInfoRes getOtherUserInfo(Long currentUserId, Long otherUserId) {

        UserInfoRes otherUserBasicInfo = userCacheService.getUserBasicInfo(otherUserId);
        UserInfoRes otherUserInfo = userRepository.getFollowCntAndReviewCnt(otherUserBasicInfo);
        boolean isFollowing = followRepository.isFollowingOtherUser(currentUserId, otherUserId);
        otherUserInfo.setFollow(isFollowing);

        return otherUserInfo;
    }

    public List<UserSearchRes> searchUserByNickname(String searchNickname, Long currentUserId) {

        if (searchNickname == null || searchNickname.trim().isEmpty()) {
            throw new UserNicknameException(USER_NICKNAME_NULL_ERROR);
        }

        List<UserSearchRes> findUsers = userRepository.findUsersByNickname(searchNickname);

        return userRepository.searchUserByNickname(searchNickname, currentUserId, findUsers);
    }

    private void validateNickname(Long userId, UserUpdateReq userUpdateReq) {

        if (userUpdateReq.getNickName() != null && userRepository.existsNicknameExcludingSelf(userId, userUpdateReq.getNickName())) {
            log.warn("nickname is duplicate. user = {}, nickname = {}", userId, userUpdateReq.getNickName());
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
