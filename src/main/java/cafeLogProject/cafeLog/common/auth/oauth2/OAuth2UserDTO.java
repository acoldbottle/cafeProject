package cafeLogProject.cafeLog.common.auth.oauth2;

import cafeLogProject.cafeLog.domains.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2UserDTO {
    private Long userId;
    private String username;
    private String email;
    private String provider;
    private UserRole role;

    public OAuth2UserDTO(String username, String email, String provider, UserRole role) {
        this.username = username;
        this.email = email;
        this.provider = provider;
        this.role = role;
    }
}
