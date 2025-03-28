package cafeLogProject.cafeLog.api.user.controller;

import cafeLogProject.cafeLog.api.user.dto.UserSearchRes;
import cafeLogProject.cafeLog.api.user.elasticsearch.ElasticsearchService;
import cafeLogProject.cafeLog.common.auth.oauth2.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SearchController {

    private final ElasticsearchService elasticsearchService;

    @PostMapping("/es/add")
    public ResponseEntity<String> addAllNicknameToES() {
        elasticsearchService.saveAllNickname();
        return ResponseEntity.ok("success");
    }

    @GetMapping("/es/search")
    public ResponseEntity<List<UserSearchRes>> searchUserByNickname(@RequestParam String nickname,
                                                                    @AuthenticationPrincipal CustomOAuth2User user) {
        List<UserSearchRes> result = elasticsearchService.searchUserByNickname(nickname, user.getName());
        return ResponseEntity.ok(result);
    }

}
