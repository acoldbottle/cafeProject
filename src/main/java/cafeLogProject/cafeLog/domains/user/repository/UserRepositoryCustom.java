package cafeLogProject.cafeLog.domains.user.repository;

import cafeLogProject.cafeLog.api.user.dto.OtherUserInfoRes;
import cafeLogProject.cafeLog.api.user.dto.UserInfoRes;
import cafeLogProject.cafeLog.api.user.dto.UserSearchRes;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryCustom {

    boolean existsNicknameExcludingSelf(String username, String newNickname);
    Optional<OtherUserInfoRes> findOtherUserInfo(String currentUsername, Long otherUserId);
    Optional<UserInfoRes> findMyProfileWithReviewCount(String username);
    List<UserSearchRes> searchUserByNickname(String searchNickname, Long currentUserId, List<UserSearchRes> findUsers);

    List<UserSearchRes> findUsersByNickname(String searchNickname);
}
