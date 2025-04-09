package cafeLogProject.cafeLog.api.follow.service;

import cafeLogProject.cafeLog.api.follow.dto.FollowRes;
import cafeLogProject.cafeLog.api.follow.dto.UserFollowRes;
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

import java.util.List;

import static cafeLogProject.cafeLog.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public FollowRes followUser(String username, Long otherUserId) {

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));

        if (!canFollow(currentUser, otherUser)) {
            return new FollowRes("이미 팔로우한 사용자입니다.");
        }

        addFollowAndPlusCount(currentUser, otherUser);
        return new FollowRes("사용자를 팔로우했습니다.");
    }

    @Transactional
    public FollowRes unfollowUser(String username, Long otherUserId) {

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));

        if (!canUnfollow(currentUser, otherUser)) {
            return new FollowRes("이미 팔로우하지 않은 사용자입니다.");
        }

        deleteFollowAndMinusCount(currentUser, otherUser);
        return new FollowRes("사용자를 언팔로우했습니다.");
    }

    public List<UserFollowRes> getFollowerList(String username, Long otherUserId, int limit, Long cursor) {

        User user = validateUser(username, otherUserId);

        return followRepository.getFollowerList(user.getId(), otherUserId, limit, cursor);
    }

    public List<UserFollowRes> getFollowingList(String username, Long otherUserId, int limit, Long cursor) {

        User user = validateUser(username, otherUserId);

        return followRepository.getFollowingList(user.getId(), otherUserId, limit, cursor);
    }

    private User validateUser(String username, Long otherUserId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR));

        boolean isExistUser = userRepository.existsById(otherUserId);
        if (!isExistUser) {
            throw new FollowUserNotFoundException(FOLLOW_USER_NOT_FOUND_ERROR);
        }
        return user;
    }

    private boolean canFollow(User currentUser, User otherUser) {

        if (currentUser.equals(otherUser)) {
            throw new FollowSelfException(FOLLOW_SELF_ERROR);
        }

        return !followRepository.existsByFollowerAndFollowing(currentUser, otherUser);
    }

    private void addFollowAndPlusCount(User currentUser, User otherUser) {

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

    private void deleteFollowAndMinusCount(User currentUser, User otherUser) {

        followRepository.deleteFollow(currentUser, otherUser);
        log.info("{} 님을 언팔로우했습니다.", otherUser.getNickname());
    }
}
