package cafeLogProject.cafeLog.domains.follow.repository;

import cafeLogProject.cafeLog.api.follow.dto.QUserFollowRes;
import cafeLogProject.cafeLog.api.follow.dto.UserFollowRes;
import cafeLogProject.cafeLog.domains.user.domain.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static cafeLogProject.cafeLog.domains.follow.domain.QFollow.follow;
import static cafeLogProject.cafeLog.domains.review.domain.QReview.review;
import static cafeLogProject.cafeLog.domains.user.domain.QUser.user;


@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public void deleteFollow(User currentUser, User otherUser) {

        queryFactory.delete(follow)
                .where(follow.follower.id.eq(currentUser.getId())
                        .and(follow.following.id.eq(otherUser.getId())))
                .execute();
    }

    @Override
    public boolean isFollowingOtherUser(Long currentUserId, Long otherUserId) {

        Integer result = queryFactory
                .selectOne()
                .from(follow)
                .where(follow.follower.id.eq(currentUserId)
                        .and(follow.following.id.eq(otherUserId)))
                .fetchFirst();

        return result != null;
    }

    /**
     * isFollow == 2 -> 자기 자신
     * isFollow == 1 -> 내가 팔로잉하고 있는 유저
     * isFollow == 0 -> 내가 팔로잉하고 있지 않은 유저
     */
    @Override
    public List<UserFollowRes> getFollowerList(Long currentUserId, Long otherUserId, int limit, Long cursor, Integer isFollow) {

        NumberExpression<Integer> isFollowCase = createIsFollowCaseBuilder(currentUserId);
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(follow.following.id.eq(otherUserId));
        checkCursorAndIsFollow(currentUserId, otherUserId, cursor, isFollow, builder, isFollowCase);

        List<UserFollowRes> result = queryFactory
                .select(
                        new QUserFollowRes(
                                user.id,
                                user.nickname,
                                user.isImageExist,
                                JPAExpressions
                                        .select(follow.count().intValue())
                                        .from(follow)
                                        .where(follow.following.id.eq(user.id)),
                                review.count().intValue(),
                                isFollowCase,
                                follow.id
                        )
                )
                .from(follow)
                .leftJoin(user).on(user.id.eq(follow.follower.id))
                .leftJoin(review).on(review.user.eq(user))
                .where(builder)
                .groupBy(user.id, follow.id)
                .orderBy(
                        new OrderSpecifier<>(Order.DESC, isFollowCase),
                        follow.id.desc()
                )
                .limit(limit)
                .fetch();

        return result;
    }

    @Override
    public List<UserFollowRes> getFollowingList(Long currentUserId, Long otherUserId, int limit, Long cursor, Integer isFollow) {

        NumberExpression<Integer> isFollowCase = createIsFollowCaseBuilder(currentUserId);
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(follow.follower.id.eq(otherUserId));
        checkCursorAndIsFollow(currentUserId, otherUserId, cursor, isFollow, builder, isFollowCase);

        List<UserFollowRes> result = queryFactory
                .select(
                        new QUserFollowRes(
                                user.id,
                                user.nickname,
                                user.isImageExist,
                                JPAExpressions
                                        .select(follow.count().intValue())
                                        .from(follow)
                                        .where(follow.following.id.eq(user.id)),
                                review.count().intValue(),
                                isFollowCase,
                                follow.id
                        )
                )
                .from(follow)
                .leftJoin(user).on(user.id.eq(follow.following.id))
                .leftJoin(review).on(review.user.eq(user))
                .where(builder)
                .groupBy(user.id, follow.id)
                .orderBy(
                        new OrderSpecifier<>(Order.DESC, isFollowCase),
                        follow.id.desc()
                )
                .limit(limit)
                .fetch();

        return result;

    }


    private void checkCursorAndIsFollow(Long currentUserId, Long otherUserId, Long cursor, Integer isFollow, BooleanBuilder builder, NumberExpression<Integer> isFollowCase) {
        if (cursor != null) {
            if (isFollow == 0) {
                builder.and(isFollowCase.eq(0).and(follow.id.lt(cursor)));
            } else {
                builder.and(isFollowCase.eq(1).and(follow.id.lt(cursor)))
                        .or(isFollowCase.eq(0).and(follow.id.loe(
                                // isFollow==0 인 가장 큰 follow.id를 구하는 쿼리
                                // -> isFollow==1 인 값들의 조회가 끝났을때
                                // -> isFollow==0 인 값들을 조회해야 하는데
                                // -> cursor 보다 작은 follow_id 로 조회한다면 누락되는 데이터가 생긴다.
                                // -> isFollow==0 이고 follow_id 가 cursor 보다 큰 데이터가 존재할 확률이 크기 때문
                                // -> 따라서 isFollow==0 인 가장 큰 follow_id 부터 조회하기 위함
                                queryFactory
                                    .select(follow.id.max())
                                    .from(follow)
                                    .where(follow.follower.id.ne(currentUserId)
                                            .and(follow.following.id.eq(otherUserId)))
                                    .fetchOne())));
            }
        }
    }

    private NumberExpression<Integer> createIsFollowCaseBuilder(Long currentUserId) {
        return new CaseBuilder()
                .when(user.id.eq(currentUserId)).then(2)
                .when(JPAExpressions
                        .selectOne()
                        .from(follow)
                        .where(follow.follower.id.eq(currentUserId)
                                .and(follow.following.id.eq(user.id)))
                        .exists()).then(1)
                .otherwise(0);
    }

}
