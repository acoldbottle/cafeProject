package cafeLogProject.cafeLog.api.follow.service;

import cafeLogProject.cafeLog.api.follow.dto.FollowRes;
import cafeLogProject.cafeLog.api.follow.dto.UserFollowRes;
import cafeLogProject.cafeLog.api.common.CacheService;
import cafeLogProject.cafeLog.common.exception.FollowCursorException;
import cafeLogProject.cafeLog.common.exception.follow.FollowNoMoreException;
import cafeLogProject.cafeLog.common.exception.follow.FollowSelfException;
import cafeLogProject.cafeLog.common.exception.follow.FollowUserNotFoundException;
import cafeLogProject.cafeLog.common.exception.user.UserNotFoundException;
import cafeLogProject.cafeLog.domains.follow.domain.Follow;
import cafeLogProject.cafeLog.domains.follow.repository.FollowRepository;
import cafeLogProject.cafeLog.domains.user.domain.User;
import cafeLogProject.cafeLog.domains.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static cafeLogProject.cafeLog.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    public static final String FOLLOWER_LIST = "followerList";
    public static final String FOLLOWING_LIST = "followingList";

    @Transactional
    public FollowRes followUser(Long currentUserId, Long otherUserId) {

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));

        if (!canFollow(currentUser, otherUser)) {
            return new FollowRes("이미 팔로우한 사용자입니다.");
        }

        addFollow(currentUser, otherUser);
        return new FollowRes("사용자를 팔로우했습니다.");
    }

    @Transactional
    public FollowRes unfollowUser(Long currentUserId, Long otherUserId) {

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));

        if (!canUnfollow(currentUser, otherUser)) {
            return new FollowRes("이미 팔로우하지 않은 사용자입니다.");
        }

        deleteFollow(currentUser, otherUser);
        return new FollowRes("사용자를 언팔로우했습니다.");
    }

    public List<UserFollowRes> getFollowList(Long currentUserId, Long otherUserId, int limit, Long cursor, String type) {

        validateUser(currentUserId, otherUserId);
        List<UserFollowRes> result = null;
        Integer lastIsFollow = validateCursor(currentUserId, otherUserId, cursor, type);

        switch (type) {
            case FOLLOWER_LIST -> result = followRepository.getFollowerList(currentUserId, otherUserId, limit, cursor, lastIsFollow);
            case FOLLOWING_LIST -> result = followRepository.getFollowingList(currentUserId, otherUserId, limit, cursor, lastIsFollow);
        }

        if (cursor != null && result == null || result.isEmpty()) {
            throw new FollowNoMoreException(FOLLOW_NO_MORE_EXCEPTION);
        }

        if (result.size() == limit) {
            cacheService.cacheLastResult(currentUserId, otherUserId, result.getLast().getIsFollow(), result.getLast().getFollowId(), type);
        } else if (result.size() < limit) {
            cacheService.deleteLastResult(currentUserId, otherUserId, type);
        }

        return result;
    }


    private void validateUser(Long currentUserId, Long otherUserId) {
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));

        boolean isExistUser = userRepository.existsById(otherUserId);
        if (!isExistUser) {
            throw new FollowUserNotFoundException(FOLLOW_USER_NOT_FOUND_ERROR);
        }
    }

    private Integer validateCursor(Long currentUserId, Long otherUserId, Long cursor, String type) {
        Integer lastIsFollow;
        if (cursor == null) {
            lastIsFollow = null;
        } else {
            String lastResult = cacheService.cachingLastResult(currentUserId, otherUserId, type);
            if (lastResult == null) {
                throw new FollowNoMoreException(FOLLOW_NO_MORE_EXCEPTION);
            }

            String[] lastResultArr = lastResult.split(":");
            lastIsFollow = Integer.parseInt(lastResultArr[0]);
            long lastFollowId = Long.parseLong(lastResultArr[1]);
            if (!cursor.equals(lastFollowId)) {
                throw new FollowCursorException(FOLLOW_CURSOR_EXCEPTION);
            }
        }
        return lastIsFollow;
    }

    private boolean canFollow(User currentUser, User otherUser) {

        if (currentUser.equals(otherUser)) {
            throw new FollowSelfException(FOLLOW_SELF_ERROR);
        }

        return !followRepository.existsByFollowerAndFollowing(currentUser, otherUser);
    }

    private void addFollow(User currentUser, User otherUser) {

        Follow follow = Follow.builder()
                .follower(currentUser)
                .following(otherUser)
                .build();

        followRepository.save(follow);
        log.info("{} 님을 팔로우했습니다.", otherUser.getNickname());
    }

    private boolean canUnfollow(User currentUser, User otherUser) {

        return followRepository.existsByFollowerAndFollowing(currentUser, otherUser);
    }

    private void deleteFollow(User currentUser, User otherUser) {

        followRepository.deleteFollow(currentUser, otherUser);
        log.info("{} 님을 언팔로우했습니다.", otherUser.getNickname());
    }

}
