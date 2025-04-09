package cafeLogProject.cafeLog.domains.user.domain;

import cafeLogProject.cafeLog.common.domain.BaseEntity;
import jakarta.persistence.*;

import static cafeLogProject.cafeLog.domains.user.domain.UserRole.ROLE_USER;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/***
 * username = 사용자 이름, oauth2로만 로그인하기 때문에 중복을 피하기 위해 provider + "_" + providerID 와 같은 형식. -> ex) google_h3os13dh12712f9ajf
 * email = 사용자 이메일
 * provider = 구글, 페이스북, 네이버와 같은 사용자가 로그인한 소셜
 * role = 사용자 권한 (일반 사용자, 관리자)  -> spring security 를 사용자 권한을 기반으로 사용.
 */
@Entity
@Getter
@Table(name = "user_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String username;

    private boolean isImageExist;

    private String nickname;

    private String introduce;

    private String email;

    private String provider;

//    private int followerCnt = 0;
//
//    private int followingCnt= 0;

    @Enumerated(EnumType.STRING)
    private UserRole role = ROLE_USER;

    @Builder
    public User(String username, String email, String provider, String nickname, boolean isImageExist) {
        this.username = username;
        this.email = email;
        this.provider = provider;
        this.nickname = nickname;
        this.isImageExist = isImageExist;
        this.role = ROLE_USER;
    }

    public void updateImageExist(boolean isImageExist) {
        this.isImageExist = isImageExist;
    }

    public void updateUserNickname(String nickname) {

        if (nickname != null) this.nickname = nickname;
    }

    public void updateUserIntroduce(String introduce){

        if (introduce != null) this.introduce = introduce;
    }

    public void setNicknameFirstLogin(String randomNickname) {

        this.nickname = randomNickname;
    }

//    public void plusFollowerCnt() {
//        this.followerCnt += 1;
//    }
//
//    public void minusFollowerCnt() {
//        if (this.followerCnt > 0) {
//            this.followerCnt -= 1;
//        }
//    }
//
//    public void plusFollowingCnt() {
//        this.followingCnt += 1;
//    }
//
//    public void minusFollowingCnt() {
//        if (this.followingCnt > 0) {
//            this.followingCnt -= 1;
//        }
//    }
}
