package cafeLogProject.cafeLog.domains.follow.repository;

import cafeLogProject.cafeLog.api.follow.dto.QUserFollowRes;
import cafeLogProject.cafeLog.api.follow.dto.UserFollowRes;
import cafeLogProject.cafeLog.common.exception.ErrorCode;
import cafeLogProject.cafeLog.common.exception.FollowCursorException;
import cafeLogProject.cafeLog.domains.follow.domain.QFollow;
import cafeLogProject.cafeLog.domains.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cafeLogProject.cafeLog.domains.follow.domain.QFollow.follow;
import static cafeLogProject.cafeLog.domains.review.domain.QReview.review;
import static cafeLogProject.cafeLog.domains.user.domain.QUser.user;


@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void deleteFollow(User currentUser, User otherUser) {

        queryFactory.delete(follow)
                .where(follow.follower.id.eq(currentUser.getId())
                        .and(follow.following.id.eq(otherUser.getId())))
                .execute();
    }

    @Override
    public List<UserFollowRes> getFollowerList(Long currentUserId, Long otherUserId, int limit, Long cursor) {
        return getFollowList(currentUserId, otherUserId, limit, cursor, true);
    }

    @Override
    public List<UserFollowRes> getFollowingList(Long currentUserId, Long otherUserId, int limit, Long cursor) {
        return getFollowList(currentUserId, otherUserId, limit, cursor, false);
    }

    /**
     *
     * @param currentUserId 현재 로그인한 사용자
     * @param otherUserId 해당 아이디를 가진 사용자의 팔로우,팔로잉 리스트 조회
     * @param isFollower [true -> 팔로워 리스트], [false -> 팔로잉 리스트]
     *
     * @method getMyFollowingList() -> 내가 팔로잉하고 있는 유저들 리스트로 반환
     * @method setIsFollow() -> 해당 아이디를 가진 사용자의 팔로우,팔로잉 리스트에 나오는 유저들
     *                          1) 리스트 안에 있는 유저가 나 자신일 경우(우선순위1) -> isFollow = 2
     *                               - front 에서 팔로우,언팔로우 버튼 x (구분을 위해 2로 설정)
     *                          2) 리스트 안에 있는 유저를 내가 팔로잉 하고 있을 경우(우선순위2) -> isFollow = 1
     *                          3) 리스트 안에 있는 유저를 내가 팔로잉 하고 있지 않을 경우(우선순위3) -> isFollow = 0
     *                          *) isFollow 값이 같은 경우에는 팔로우 아이디로 역순(최신순)
     *
     * return 우선순위에 따라 커서 기반 페이지네이션
     */
    private List<UserFollowRes> getFollowList(Long currentUserId, Long otherUserId, int limit, Long cursor, boolean isFollower) {

        QFollow followerFollow = new QFollow("followerFollow");

        List<UserFollowRes> followList = queryFactory
                .select(new QUserFollowRes(
                        user.id,
                        user.nickname,
                        user.isImageExist,
                        followerFollow.follower.count().intValue(),
                        review.count().intValue(),
                        follow.id
                ))
                .from(follow)
                .leftJoin(user).on(isFollower ? user.id.eq(follow.follower.id) : user.id.eq(follow.following.id))
                .leftJoin(review).on(review.user.eq(user))
                .leftJoin(followerFollow)
                .on(followerFollow.following.eq(user))
                .where(isFollower ? follow.following.id.eq(otherUserId) : follow.follower.id.eq(otherUserId))
                .groupBy(user.id, follow.id)
                .fetch();;

        List<Long> myFollowingList = getMyFollowingList(currentUserId);
        setIsFollow(currentUserId, followList, myFollowingList);

        return paginateFollowList(limit, cursor, followList, currentUserId, otherUserId, isFollower);
    }

    private List<Long> getMyFollowingList(Long currentUserId) {

        List<Long> followedUserIds = queryFactory
                .select(follow.following.id)
                .from(follow)
                .where(follow.follower.id.eq(currentUserId))
                .fetch();

        return followedUserIds;
    }

    private void setIsFollow(Long currentUserId, List<UserFollowRes> followList, List<Long> myFollowingList) {

        for (UserFollowRes follower : followList) {
            if (follower.getUserId() == currentUserId) {
                follower.setIsFollow(2);
            } else if (myFollowingList.contains(follower.getUserId())) {
                follower.setIsFollow(1);
            } else {
                follower.setIsFollow(0);
            }
        }
    }

    private List<UserFollowRes> paginateFollowList(int limit, Long cursor, List<UserFollowRes> followList, Long currentUserId, Long otherUserId, boolean isFollower) {

        List<UserFollowRes> result = new ArrayList<>();

        if (cursor == null) {
            result.addAll(getSortedFollowList(limit, followList));
        } else {
            Integer isFollowFromCache = getIsFollowFromCache(currentUserId, otherUserId, cursor, isFollower);
            if (isFollowFromCache == 1) {
                addFollowingUsers(result, followList, cursor, limit, cursor, otherUserId);
            } else {
                addUnfollowingUsers(result, followList, cursor, limit);
            }
        }

        if (!result.isEmpty()) {
            UserFollowRes lastFollowRes = result.get(result.size() - 1);
            cacheLastResult(lastFollowRes.getFollowId(), currentUserId, otherUserId, lastFollowRes.getIsFollow(), isFollower);
        }

        return result;
    }

    /**
     * cursor == null -> 정렬후에 limit 만큼 조회 결과 반환
     */
    private List<UserFollowRes> getSortedFollowList(int limit, List<UserFollowRes> followList) {
        Collections.sort(followList, (user1, user2) -> {
            if (user1.getIsFollow() < user2.getIsFollow()) return 1;
            else if (user1.getIsFollow() > user2.getIsFollow()) return -1;
            else return Long.compare(user2.getFollowId(), user1.getFollowId());
        });

        List<UserFollowRes> list = followList.stream()
                .limit(limit)
                .toList();
        return list;
    }

    /**
     * @return 마지막으로 조회한 팔로우 아이디와 내가 팔로잉했는지 여부
     * return 1 --> 내가 팔로잉
     * return 0 --> 내가 팔로잉하지 않음
     *
     * 마지막으로 조회했던 팔로우 아이디를 커서로 사용하지 않는 경우 예외 처리
     */
    private Integer getIsFollowFromCache(Long currentUserId, Long otherUserId, Long cursor, boolean isFollower) {
        String listType = isFollower ? "-follower" : "-following";
        String key = currentUserId + ":" + otherUserId + listType;
        String value = redisTemplate.opsForValue().get(key);
        String[] values = value.split(":");

        if (values[0].equals(cursor.toString())) {
            return Integer.valueOf(values[1]);
        } else {
            throw new FollowCursorException(ErrorCode.FOLLOW_CURSOR_EXCEPTION);
        }
    }

    /**
     * 마지막 조회 결과를 레디스에 저장
     * 마지막으로 조회한 follow가 삭제되는 경우도 있기 때문
     * 키는 현재유저아이디:타겟유저아이디:팔로워 or 팔로잉 --> 유저마다 isFollow 값이 다르기 때문에
     * 값은 마지막으로 조회한 followId:isFollow
     */
    private void cacheLastResult(Long lastResultFollowId, Long currentUserId, Long otherUserId, int isFollow, boolean isFollower) {
        String listType = isFollower ? "follower" : "following";
        String key = currentUserId + ":" + otherUserId + ":" + listType;
        String value = lastResultFollowId + ":" + isFollow;
        redisTemplate.opsForValue().set(key, value, 1, TimeUnit.HOURS);
    }

    /**
     *  내가 팔로잉하고 있는 유저 커서 기반 우선 조회. isFollow = 1 인 유저.
     *  반환 조회 결과가 limit 보다 작다면 cursor 의 값을 임의로 내가 팔로잉하고 있지 않은 유저 중에서 제일 큰 follow.id 값 + 1로 설정.
     *                  -> Ex) isFollow = 1 인 유저들 중에 내가 마지막으로 조회한 유저의 follow.id 가 1 일 경우.
     *                         -> isFollow = 0 인 유저들이 조회 안됨 -> 1보다 작은 follow.id 는 없기 때문.
     *                         => 따라서 isFollow = 0 인 유저들을 조회하기 위해 isFollow = 0 인 유저의 follow.id 최댓값 + 1 으로 설정
     *                              -> follow.id 최댓값에 + 1 을 안할 경우에 follow.id 최댓값을 가진 유저가 조회 안되기 때문
     *
     * @method getMaxFollowIdWithIsUnfollowing() -> return (isFollow = 0 인 유저의 follow.id 최댓값 + 1)
     * @method addUnfollowingUsersUpToMaxFollowId -> isFollow = 0 인 유저를 최신순으로 조회
     */
    private void addFollowingUsers(List<UserFollowRes> result, List<UserFollowRes> followList, Long cursor, int limit, Long currentUserId, Long otherUserId) {
        List<UserFollowRes> followingList = followList.stream()
                .filter(follower -> follower.getIsFollow() == 1 && follower.getFollowId() < cursor)
                .sorted(Comparator.comparingLong(UserFollowRes::getFollowId).reversed())
                .limit(limit)
                .toList();

        result.addAll(followingList);

        if (result.size() < limit) {
            Long maxFollowId = getMaxFollowIdWithIsUnfollowing(currentUserId, otherUserId);
            addUnfollowingUsersUpToMaxFollowId(result, followList, maxFollowId, limit - result.size());
        }
    }


    private Long getMaxFollowIdWithIsUnfollowing(Long currentUserId, Long otherUserId) {

        Long maxFollowIdWithIsUnfollowing = queryFactory
                .select(follow.id.max())
                .from(follow)
                .where(follow.follower.id.ne(currentUserId)
                        .and(follow.following.id.eq(otherUserId)))
                .fetchOne();

        if (maxFollowIdWithIsUnfollowing != null) {
            maxFollowIdWithIsUnfollowing += 1;
        }

        return maxFollowIdWithIsUnfollowing;
    }

    private void addUnfollowingUsersUpToMaxFollowId(List<UserFollowRes> result, List<UserFollowRes> followList, Long maxFollowId, int remain) {
        List<UserFollowRes> remainingList = followList.stream()
                .filter(follower -> follower.getIsFollow() == 0 && follower.getFollowId() < maxFollowId)
                .sorted(Comparator.comparingLong(UserFollowRes::getFollowId).reversed())
                .limit(remain)
                .toList();

        result.addAll(remainingList);
    }

    /**
     * 내가 팔로잉 하지 않은 유저들 -> isFollow=0 인 유저들을 커서 기반 조회
     */
    private void addUnfollowingUsers(List<UserFollowRes> result, List<UserFollowRes> followList, Long cursor, int limit) {
        List<UserFollowRes> unfollowingList = followList.stream()
                .filter(follower -> follower.getIsFollow() == 0 && follower.getFollowId() < cursor)
                .sorted(Comparator.comparingLong(UserFollowRes::getFollowId).reversed())
                .limit(limit)
                .toList();

        result.addAll(unfollowingList);
    }

}