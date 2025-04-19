package cafeLogProject.cafeLog.api.favorite.service;

import cafeLogProject.cafeLog.api.favorite.dto.FavoriteCafeInfo;
import cafeLogProject.cafeLog.api.favorite.dto.ToggleFavoriteReq;
import cafeLogProject.cafeLog.api.favorite.dto.ToggleFavoriteRes;
import cafeLogProject.cafeLog.common.exception.cafe.CafeNotFoundException;
import cafeLogProject.cafeLog.common.exception.user.UserNotFoundException;
import cafeLogProject.cafeLog.domains.cafe.repository.CafeRepository;
import cafeLogProject.cafeLog.domains.favorite.domain.Favorite;
import cafeLogProject.cafeLog.domains.favorite.repository.FavoriteRepository;
import cafeLogProject.cafeLog.domains.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cafeLogProject.cafeLog.common.exception.ErrorCode.CAFE_NOT_FOUND_ERROR;
import static cafeLogProject.cafeLog.common.exception.ErrorCode.USER_NOT_FOUND_ERROR;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final CafeRepository cafeRepository;

    @Transactional
    public ToggleFavoriteRes toggleFavorite(Long userId, ToggleFavoriteReq toggleFavoriteReq) {

        if (!toggleFavoriteReq.getIsScrap()) {
            return removeFavorite(userId, toggleFavoriteReq);
        } else {
            return addFavorite(userId, toggleFavoriteReq.getCafeId());
        }
    }

    public List<FavoriteCafeInfo> getMyFavoriteCafeList(Long userId) {

        List<FavoriteCafeInfo> myFavoriteCafes = favoriteRepository.findMyFavoriteCafes(userId);
        if (myFavoriteCafes.isEmpty()) {
            log.info("User {} : scraped cafe is null", userId);
        }

        return myFavoriteCafes;
    }

    private ToggleFavoriteRes removeFavorite(Long userId, ToggleFavoriteReq toggleFavoriteReq) {

        favoriteRepository.deleteFavorite(userId, toggleFavoriteReq.getCafeId());
        log.info("User {} removed cafe {} from favorites", userId, toggleFavoriteReq.getCafeId());
        return new ToggleFavoriteRes("해당 카페를 즐겨찾기에서 해제했습니다.");
    }

    private ToggleFavoriteRes addFavorite(Long userId, Long cafeId) {

        if (!favoriteRepository.isExistFavorite(userId, cafeId)) {
            Favorite favorite = Favorite.builder()
                    .user(userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR)))
                    .cafe(cafeRepository.findById(cafeId).orElseThrow(() -> new CafeNotFoundException(CAFE_NOT_FOUND_ERROR)))
                    .build();

            favoriteRepository.save(favorite);
        }

        log.info("User {} added cafe {} from favorites", userId, cafeId);
        return new ToggleFavoriteRes("해당 카페를 즐겨찾기에 추가했습니다.");
    }
}

