package cafeLogProject.cafeLog.domains.favorite.repository;

import cafeLogProject.cafeLog.api.favorite.dto.FavoriteCafeInfo;
import cafeLogProject.cafeLog.api.favorite.dto.QFavoriteCafeInfo;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static cafeLogProject.cafeLog.domains.favorite.domain.QFavorite.favorite;

@RequiredArgsConstructor
public class FavoriteRepositoryImpl implements FavoriteRepositoryCustom{

    private final JPAQueryFactory queryFactory;


    @Override
    public void deleteFavorite(Long userId, Long cafeId) {

        queryFactory.delete(favorite)
                .where(favorite.user.id.eq(userId)
                        .and(favorite.cafe.id.eq(cafeId)))
                .execute();
    }

    @Override
    public boolean isExistFavorite(Long userId, Long cafeId) {

        return queryFactory
                .selectFrom(favorite)
                .where(favorite.user.id.eq(userId)
                        .and(favorite.cafe.id.eq(cafeId)))
                .fetchOne() != null;
    }

    @Override
    public List<FavoriteCafeInfo> findMyFavoriteCafes(Long userId) {

        return queryFactory
                .select(new QFavoriteCafeInfo(
                        favorite.cafe.id,
                        favorite.cafe.cafeName,
                        favorite.cafe.address
                ))
                .from(favorite)
                .where(favorite.user.id.eq(userId))
                .fetch();
    }
}
