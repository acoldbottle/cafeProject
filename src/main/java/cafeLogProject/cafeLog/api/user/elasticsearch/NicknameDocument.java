package cafeLogProject.cafeLog.api.user.elasticsearch;

import cafeLogProject.cafeLog.domains.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Mapping(mappingPath = "elasticsearch/nickname-mappings.json")
@Setting(settingPath = "elasticsearch/nickname-settings.json")
@Document(indexName = "nicknames")
public class NicknameDocument {

    @Id
    private Long id;

    private String nickname;

    private Boolean isProfileImageExist;

    public static NicknameDocument from(User user) {
        return NicknameDocument.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .isProfileImageExist(user.isImageExist())
                .build();
    }
}
