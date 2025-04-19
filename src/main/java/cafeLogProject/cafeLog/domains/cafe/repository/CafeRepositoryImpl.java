package cafeLogProject.cafeLog.domains.cafe.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static cafeLogProject.cafeLog.domains.review.domain.QReview.review;

@RequiredArgsConstructor
public class CafeRepositoryImpl implements CafeRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public double calculateAvgRating(Long cafeId) {

        Double avgRating = queryFactory.select(review.rating.avg().doubleValue())
                .from(review)
                .where(review.cafe.id.eq(cafeId))
                .fetchOne();

        if (avgRating == null) {
            return 0.0;
        }

        return Math.round(avgRating * 10.0) / 10.0;
    }


}
