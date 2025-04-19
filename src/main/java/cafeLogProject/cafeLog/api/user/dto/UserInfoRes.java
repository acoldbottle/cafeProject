package cafeLogProject.cafeLog.api.user.dto;

import cafeLogProject.cafeLog.domains.user.domain.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoRes {

    private Long userId;

    private String nickname;

    private String introduce;

    private String email;

    private Boolean isProfileImageExist;

    private int follower_cnt;

    private int following_cnt;

    private int review_cnt;

    @JsonProperty("isFollow")
    private boolean isFollow;

    public static UserInfoRes from(User user) {
        return UserInfoRes.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .introduce(user.getIntroduce())
                .email(user.getEmail())
                .isProfileImageExist(user.isImageExist())
                .build();
    }
}
