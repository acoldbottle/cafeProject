package cafeLogProject.cafeLog.api.follow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;


@Data
public class UserFollowRes {

    private Long userId;

    private String nickname;

    private Boolean isProfileImageExist;

    private int followerCnt;

    private int reviewCnt;

    @JsonProperty("isFollow")
    private int isFollow;

    private Long followId;

    @QueryProjection
    public UserFollowRes(Long userId, String nickname, Boolean isProfileImageExist, int follower_cnt, int review_cnt, int isFollow, Long followId) {

        this.userId = userId;
        this.nickname = nickname;
        this.isProfileImageExist = isProfileImageExist;
        this.followerCnt = follower_cnt;
        this.reviewCnt = review_cnt;
        this.isFollow = isFollow;
        this.followId = followId;
    }
}
