package cafeLogProject.cafeLog.domains.user.repository;

import cafeLogProject.cafeLog.api.user.dto.UserInfoRes;
import cafeLogProject.cafeLog.api.user.dto.UserSearchRes;

import java.util.List;

public interface UserRepositoryCustom {

    boolean existsNicknameExcludingSelf(Long userId, String newNickname);
    List<UserSearchRes> searchUserByNickname(String searchNickname, Long currentUserId, List<UserSearchRes> findUsers);

    List<UserSearchRes> findUsersByNickname(String searchNickname);

    UserInfoRes getFollowCntAndReviewCnt(UserInfoRes userInfoRes);
}
