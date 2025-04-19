package cafeLogProject.cafeLog.api.follow.controller;

import cafeLogProject.cafeLog.api.follow.dto.FollowRes;
import cafeLogProject.cafeLog.api.follow.dto.UserFollowRes;
import cafeLogProject.cafeLog.api.follow.service.FollowService;
import cafeLogProject.cafeLog.common.auth.oauth2.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/follow/{otherUserId}")
    public ResponseEntity<FollowRes> followUser(@AuthenticationPrincipal CustomOAuth2User user,
                                                @PathVariable Long otherUserId) {

        FollowRes followRes = followService.followUser(user.getUserId(), otherUserId);
        return ResponseEntity.ok(followRes);
    }

    @DeleteMapping("/follow/{otherUserId}")
    public ResponseEntity<FollowRes> unfollowUser(@AuthenticationPrincipal CustomOAuth2User user,
                                                @PathVariable Long otherUserId) {

        FollowRes followRes = followService.unfollowUser(user.getUserId(), otherUserId);
        return ResponseEntity.ok(followRes);
    }

    @GetMapping("/users/{otherUserId}/follower")
    public ResponseEntity<List<UserFollowRes>> getFollowerList(@AuthenticationPrincipal CustomOAuth2User user,
                                                               @PathVariable Long otherUserId,
                                                               @RequestParam(defaultValue = "10") int limit,
                                                               @RequestParam(required = false) Long cursor
                                                               ) {

        List<UserFollowRes> followerList = followService.getFollowList(user.getUserId(), otherUserId, limit, cursor, FollowService.FOLLOWER_LIST);
        return ResponseEntity.ok(followerList);
    }

    @GetMapping("/users/{otherUserId}/following")
    public ResponseEntity<List<UserFollowRes>> getFollowingList(@AuthenticationPrincipal CustomOAuth2User user,
                                                                @PathVariable Long otherUserId,
                                                                @RequestParam(defaultValue = "10") int limit,
                                                                @RequestParam(required = false) Long cursor) {

        List<UserFollowRes> followingList = followService.getFollowList(user.getUserId(), otherUserId, limit, cursor, FollowService.FOLLOWING_LIST);
        return ResponseEntity.ok(followingList);
    }
}
