package cafeLogProject.cafeLog.domains.favorite.repository;

import cafeLogProject.cafeLog.api.favorite.dto.FavoriteCafeInfo;

import java.util.List;

public interface FavoriteRepositoryCustom {

    void deleteFavorite(Long userId, Long cafeId);

    boolean isExistFavorite(Long userId, Long cafeId);

    List<FavoriteCafeInfo> findMyFavoriteCafes(Long userId);
}
