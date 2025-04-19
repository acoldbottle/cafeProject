package cafeLogProject.cafeLog.common.auth.oauth2;

import cafeLogProject.cafeLog.common.auth.jwt.JWTUserDTO;
import cafeLogProject.cafeLog.domains.user.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {


    private final OAuth2UserDTO oAuth2UserDTO;
    private Map<String, Object> attributes;

    public CustomOAuth2User(OAuth2UserDTO user, Map<String, Object> attributes) {
        this.oAuth2UserDTO = user;
        this.attributes = attributes;
    }


    public CustomOAuth2User(JWTUserDTO jwtUserDTO) {
        OAuth2UserDTO oAuth2UserDTO = new OAuth2UserDTO();
        oAuth2UserDTO.setUserId(jwtUserDTO.getUserId());
        oAuth2UserDTO.setUsername(jwtUserDTO.getUsername());
        oAuth2UserDTO.setRole(jwtUserDTO.getUserRole());
        this.oAuth2UserDTO = oAuth2UserDTO;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(() -> String.valueOf(oAuth2UserDTO.getRole()));
    }

    @Override
    public String getName() {
        return oAuth2UserDTO.getUsername();
    }

    public Long getUserId(){
        return oAuth2UserDTO.getUserId();
    }
}
