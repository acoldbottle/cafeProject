package cafeLogProject.cafeLog.common.auth.oauth2;

import cafeLogProject.cafeLog.api.user.elasticsearch.NicknameDocument;
import cafeLogProject.cafeLog.api.user.elasticsearch.NicknameDocumentRepository;
import cafeLogProject.cafeLog.common.auth.oauth2.provider.FacebookUser;
import cafeLogProject.cafeLog.common.auth.oauth2.provider.NaverUser;
import cafeLogProject.cafeLog.common.auth.exception.UserCreateException;
import cafeLogProject.cafeLog.common.auth.oauth2.provider.GoogleUser;
import cafeLogProject.cafeLog.common.auth.oauth2.provider.OAuth2UserResponse;
import cafeLogProject.cafeLog.common.exception.ErrorCode;
import cafeLogProject.cafeLog.domains.user.domain.User;
import cafeLogProject.cafeLog.domains.user.domain.UserRole;
import cafeLogProject.cafeLog.domains.user.repository.UserRepository;
import cafeLogProject.cafeLog.domains.user.util.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final NicknameGenerator nicknameGenerator;
    private final NicknameDocumentRepository nicknameDocumentRepository;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserResponse oAuth2UserResponse = getOAuth2UserResponse(registrationId, oAuth2User);;

        OAuth2UserDTO user = createOAuth2UserDTO(oAuth2UserResponse);

        registerOrLogin(user);

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    /**
     * 구글,페이스북,네이버 중에 어떤 소셜로 로그인하는지 체크하고 소셜 별 유저정보를 반환
     */
    private OAuth2UserResponse getOAuth2UserResponse(String registrationId, OAuth2User oAuth2User) {

        switch (registrationId) {
            case "google" -> {return new GoogleUser(oAuth2User.getAttributes());}
            case "facebook" -> {return new FacebookUser(oAuth2User.getAttributes());}
            case "naver" -> {
                Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");
                return new NaverUser(response);
            }
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜입니다");
        }
    }

    /**
     * 소셜에서 받은 유저 정보로 OAuth2UserDTO 생성
     */
    private OAuth2UserDTO createOAuth2UserDTO(OAuth2UserResponse oAuth2UserResponse) {

        String username = oAuth2UserResponse.getProvider() + "_" + oAuth2UserResponse.getProviderId();
        String email = oAuth2UserResponse.getEmail();
        String provider = oAuth2UserResponse.getProvider();

        return new OAuth2UserDTO(username, email, provider, UserRole.ROLE_USER);
    }

    /**
     * 소셜 로그인 하려는 user 가 userDB에 없을 때 -> 새로 생성 || 에러 발생
     */
    private void registerOrLogin(OAuth2UserDTO user) {

        User existingUser = userRepository.findByUsername(user.getUsername())
                .orElseGet(() -> {
                    try {
                        User newUser = User.builder()
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .provider(user.getProvider())
                                .build();

                        newUser.setNicknameFirstLogin(nicknameGenerator.generateNickname());
                        User savedUser = userRepository.save(newUser);
                        nicknameDocumentRepository.save(NicknameDocument.from(savedUser));
                        log.info("새로운 사용자 등록, username={}", user.getUsername());
                        return newUser;
                    } catch (Exception e) {
                        log.error("{} 등록 실패: {}", user.getUsername(), e.getMessage());
                        throw new UserCreateException(ErrorCode.USER_CREATE_ERROR);
                    }
                });

        log.info("사용자 로그인, username={}", existingUser.getUsername());
    }
}
