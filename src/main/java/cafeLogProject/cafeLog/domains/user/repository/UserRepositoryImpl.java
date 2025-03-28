package cafeLogProject.cafeLog.domains.user.repository;

import cafeLogProject.cafeLog.api.user.dto.*;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.*;

import static cafeLogProject.cafeLog.domains.follow.domain.QFollow.follow;
import static cafeLogProject.cafeLog.domains.review.domain.QReview.review;
import static cafeLogProject.cafeLog.domains.user.domain.QUser.user;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsNicknameExcludingSelf(String username, String newNickname) {

        return queryFactory
                .selectFrom(user)
                .where(user.nickname.eq(newNickname)
                        .and(user.username.ne(username)))
                .fetchOne() != null;
    }

    @Override
    public Optional<OtherUserInfoRes> findOtherUserInfo(String currentUsername, Long otherUserId) {

        OtherUserInfoRes otherUser = queryFactory
                .select(new QOtherUserInfoRes(
                        user.id,
                        user.nickname,
                        user.introduce,
                        user.email,
                        user.isImageExist,
                        follow.id.isNotNull().as("isFollow"),
                        user.followerCnt,
                        user.followingCnt,
                        review.count().intValue()
                ))
                .from(user)
                .leftJoin(follow)
                .on(follow.follower.username.eq(currentUsername)
                        .and(follow.following.id.eq(otherUserId)))
                .leftJoin(review)
                .on(review.user.eq(user))
                .where(user.id.eq(otherUserId))
                .groupBy(user.id, follow.id)
                .fetchOne();

        return Optional.ofNullable(otherUser);
    }

    @Override
    public Optional<UserInfoRes> findMyProfileWithReviewCount(String username) {

        UserInfoRes reviewCnt = queryFactory
                .select(new QUserInfoRes(
                        user.id,
                        user.nickname,
                        user.introduce,
                        user.email,
                        user.isImageExist,
                        user.followerCnt,
                        user.followingCnt,
                        review.count().intValue()
                ))
                .from(user)
                .leftJoin(review).on(review.user.eq(user))
                .where(user.username.eq(username))
                .groupBy(user.id)
                .fetchOne();

        return Optional.ofNullable(reviewCnt);
    }

    @Override
    public List<UserSearchRes> findUsersByNickname(String searchNickname) {
        return queryFactory
                .select(new QUserSearchRes(
                        user.id,
                        user.nickname,
                        user.isImageExist
                ))
                .from(user)
                .where(user.nickname.containsIgnoreCase(searchNickname))
                .fetch();
    }
    /**
     *
     * @param searchNickname 검색하려는 닉네임
     * @param currentUserId 현재 로그인한 사용자
     *
     * findUsers -> searchNickname을 자신의 닉네임에 포함하고 있는 유저들 리스트
     * myFollowingList -> 현재 로그인한 사용자의 팔로잉 리스트
     * mutualFollowers -> findUsers로 나온 유저의 팔로워 리스트에도 존재하고 현재 로그인한 사용자의 팔로잉 리스트에도 존재하는 유저들 닉네임
     * mutualFollowersMap -> mutualFollowers 정리
     *                      => Map<findUsers로 나온 유저 아이디, List<findUsers로 나온 유저의 팔로워 리스트에도 존재하고 현재 로그인한 사용자의 팔로잉 리스트에도 존재하는 유저들 닉네임>>
     *
     * @method setIsFollow -> 현재 로그인한 사용자가 findUsers로 나온 유저를 팔로잉 하고 있는지 여부
     * @method setFollowerCountMessage -> mutualFollowersMap에서 나온 정보를 기반으로 사용자에게 적절한 메시지 설정
     * @method sortSearchList -> findUsers 정렬
     *                          1) searchNickname 을 닉네임으로 가진 유저
     *                          2) 겹치는 팔로우들의 수(=List<findUsers로 나온 유저의 팔로워 리스트에도 존재하고 현재 로그인한 사용자의 팔로잉 리스트에도 존재하는 유저들 닉네임>.size())
     *
     */
    @Override
    public List<UserSearchRes> searchUserByNickname(String searchNickname, Long currentUserId, List<UserSearchRes> findUsers) {

        List<Long> myFollowingList = queryFactory
                .select(follow.following.id)
                .from(follow)
                .where(follow.follower.id.eq(currentUserId))
                .fetch();

        List<Tuple> mutualFollowers = queryFactory
                .select(follow.following.id, follow.follower.nickname)
                .from(follow)
                .join(user).on(follow.following.id.eq(user.id))
                .where(user.nickname.containsIgnoreCase(searchNickname))
                .where(follow.follower.id.in(myFollowingList))
                .fetch();

        Map<Long, List<String>> mutualFollowersMap = new HashMap<>();

        for (Tuple followerResult : mutualFollowers) {
            Long followingUserId = followerResult.get(0, Long.class);
            String followerNickname = followerResult.get(1, String.class);

            mutualFollowersMap
                    .computeIfAbsent(followingUserId, id -> new ArrayList<>())
                    .add(followerNickname);
        }


        findUsers.forEach(searchedUser -> {
            setIsFollow(currentUserId, searchedUser, myFollowingList);
            setFollowerCountMessage(searchedUser, mutualFollowersMap);
        });

        sortSearchList(searchNickname, findUsers, mutualFollowersMap);
        return findUsers
                .stream()
                .limit(10)
                .toList();

    }

    private void setIsFollow(Long currentUserId, UserSearchRes searchedUser, List<Long> myFollowingList) {
        if (searchedUser.getUserId().equals(currentUserId)) {
            searchedUser.setIsFollow(2);
        } else if (myFollowingList.contains(searchedUser.getUserId())) {
            searchedUser.setIsFollow(1);
        } else {
            searchedUser.setIsFollow(0);
        }
    }

    private void setFollowerCountMessage(UserSearchRes searchedUser, Map<Long, List<String>> mutualFollowersMap) {
        List<String> commonFollow = mutualFollowersMap.get(searchedUser.getUserId());
        if (commonFollow != null && !commonFollow.isEmpty()) {
            if (searchedUser.getIsFollow() != 2) {
                if (commonFollow.size() > 1) {
                    searchedUser.setFollowerCountMessage(commonFollow.get(0) + "님 외 " + (commonFollow.size() - 1) + "명이 팔로우합니다.");
                } else {
                    searchedUser.setFollowerCountMessage(commonFollow.get(0) + "님이 팔로우합니다.");
                }
            }
        }
    }

    private void sortSearchList(String searchNickname, List<UserSearchRes> findUsers, Map<Long, List<String>> mutualFollowersMap) {
        Collections.sort(findUsers, (user1, user2) -> {

            boolean isUser1Matched = user1.getNickname().equalsIgnoreCase(searchNickname);
            boolean isUser2Matched = user2.getNickname().equalsIgnoreCase(searchNickname);

            if (isUser1Matched) return -1;
            if (isUser2Matched) return 1;

            int size1 = Optional.ofNullable(mutualFollowersMap.get(user1.getUserId())).map(List::size).orElse(0);
            int size2 = Optional.ofNullable(mutualFollowersMap.get(user2.getUserId())).map(List::size).orElse(0);

            return Integer.compare(size2, size1);
        });
    }

}
