package cafeLogProject.cafeLog.api.user.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;

import java.util.LinkedList;

@Data
public class UserInfoRes {

    private Long userId;

    private String nickname;

    private String introduce;

    private String email;

    private Boolean isProfileImageExist;

    private int follower_cnt;

    private int following_cnt;

    private int review_cnt;

    @QueryProjection
    public UserInfoRes(Long userId, String nickname, String introduce, String email, Boolean isProfileImageExist, int follower_cnt, int following_cnt, int review_cnt) {
        this.userId = userId;
        this.nickname = nickname;
        this.introduce = introduce;
        this.email = email;
        this.isProfileImageExist = isProfileImageExist;
        this.follower_cnt = follower_cnt;
        this.following_cnt = following_cnt;
        this.review_cnt = review_cnt;
    }
}
