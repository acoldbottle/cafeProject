package cafeLogProject.cafeLog.api.favorite.controller;

import cafeLogProject.cafeLog.api.favorite.dto.FavoriteCafeInfo;
import cafeLogProject.cafeLog.api.favorite.dto.ToggleFavoriteReq;
import cafeLogProject.cafeLog.api.favorite.dto.ToggleFavoriteRes;
import cafeLogProject.cafeLog.api.favorite.service.FavoriteService;
import cafeLogProject.cafeLog.common.auth.oauth2.CustomOAuth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/cafes/scraps")
    public ResponseEntity<ToggleFavoriteRes> toggleFavorite(@AuthenticationPrincipal CustomOAuth2User user,
                                                            @RequestBody @Valid ToggleFavoriteReq toggleFavoriteReq) {

        ToggleFavoriteRes toggleFavoriteRes = favoriteService.toggleFavorite(user.getUserId(), toggleFavoriteReq);
        return ResponseEntity.ok(toggleFavoriteRes);
    }

    @GetMapping("/my/cafes")
    public ResponseEntity<List<FavoriteCafeInfo>> getMyFavoriteCafeList(@AuthenticationPrincipal CustomOAuth2User user,
                                                                        @RequestParam boolean scraped) {

        return ResponseEntity.ok(favoriteService.getMyFavoriteCafeList(user.getUserId()));
    }
}
