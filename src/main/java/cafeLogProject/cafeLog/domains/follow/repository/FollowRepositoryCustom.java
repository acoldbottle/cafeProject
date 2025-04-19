package cafeLogProject.cafeLog.domains.follow.repository;

import cafeLogProject.cafeLog.api.follow.dto.UserFollowRes;
import cafeLogProject.cafeLog.domains.user.domain.User;

import java.util.List;

public interface FollowRepositoryCustom {

    void deleteFollow(User currentUser, User otherUser);

    boolean isFollowingOtherUser(Long currentUserId, Long otherUserId);

    List<UserFollowRes> getFollowerList(Long currentUserId, Long otherUserId, int limit, Long cursor, Integer isFollow);
    List<UserFollowRes> getFollowingList(Long currentUserId, Long otherUserId, int limit, Long cursor, Integer isFollow);
}
