package cafeLogProject.cafeLog.domains.review.repository;

import cafeLogProject.cafeLog.api.review.dto.ShowReviewResponse;
import cafeLogProject.cafeLog.domains.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepositoryCustom {
    // review id로 검색, ShowReviewResponse형으로 리턴
    Optional<ShowReviewResponse> findShowReviewResponseById(Long reviewId);

    // 특정 카페의 모든 리뷰 검색
    List<ShowReviewResponse> searchByCafeId(Long cafeId, LocalDateTime createdAt, Pageable pageable);

    // 특정 유저의 모든 리뷰 검색
    List<ShowReviewResponse> searchByUser(User user, LocalDateTime createdAt, Pageable pageable);

    // 모든 리뷰 조회 & 필터링
    // 특정 매개변수가 null인 경우 조건 건너뜀
    List<ShowReviewResponse> search(String orderMethod, List<Integer> tagIds, Integer currentRating, LocalDateTime createdAt, Pageable pageable);

    List<ShowReviewResponse> searchByFollowingUsers(Long id, LocalDateTime timestamp, Pageable pageable);
}
