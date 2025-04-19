package cafeLogProject.cafeLog.service.reviewService;

import cafeLogProject.cafeLog.api.cafe.dto.SaveCafeReq;
import cafeLogProject.cafeLog.api.cafe.service.CafeService;
import cafeLogProject.cafeLog.api.follow.service.FollowService;
import cafeLogProject.cafeLog.api.review.dto.ShowReviewResponse;
import cafeLogProject.cafeLog.api.review.dto.ShowUserReviewRequest;
import cafeLogProject.cafeLog.api.review.service.ReviewService;
import cafeLogProject.cafeLog.domains.cafe.repository.CafeRepository;
import cafeLogProject.cafeLog.domains.review.domain.Review;
import cafeLogProject.cafeLog.domains.review.repository.ReviewRepository;
import cafeLogProject.cafeLog.domains.user.domain.User;
import cafeLogProject.cafeLog.domains.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "classpath:application-test.yml")
@ActiveProfiles("test")
@Transactional
public class FindFollowingUsersReviewTest {

    @Autowired
    ReviewService reviewService;
    @Autowired
    CafeService cafeService;
    @Autowired
    CafeRepository cafeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    FollowService followService;
    @Autowired
    ReviewRepository reviewRepository;

    @Test
    @DisplayName("팔로우한 사용자의 리뷰만 조회된다")
    public void findFollowingUsersReviewsSuccess() {
        // given
        User follower = userRepository.save(User.builder()
                .username("follower")
                .nickname("팔로워")
                .build());

        User following = userRepository.save(User.builder()
                .username("following")
                .nickname("팔로잉")
                .build());

        User notFollowing = userRepository.save(User.builder()
                .username("notFollowing")
                .nickname("미팔로잉")
                .build());

        Long cafeId = cafeService.saveCafe(SaveCafeReq.builder()
                .title("테스트카페")
                .category("카페")
                .roadAddress("도로명주소")
                .mapx("1111")
                .mapy("2222")
                .address("주소")
                .link("링크")
                .build());


        Review followingReview = reviewRepository.save(Review.builder()
                .content("팔로잉한 유저의 리뷰")
                .rating(5)
                .visitDate(LocalDate.now())
                .cafe(cafeRepository.findById(cafeId).get())
                .user(following)
                .build());

        Review notFollowingReview = reviewRepository.save(Review.builder()
                .content("팔로잉하지 않은 유저의 리뷰")
                .rating(5)
                .visitDate(LocalDate.now())
                .cafe(cafeRepository.findById(cafeId).get())
                .user(notFollowing)
                .build());

        // when 
        ShowUserReviewRequest request = ShowUserReviewRequest.builder()
                .limit(10)
                .timestamp(LocalDateTime.now().plusYears(1))
                .build();

        List<ShowReviewResponse> result = reviewService.findFollowingUsersReviews(
                follower.getUsername(),
                request
        );

        // then
        assertThat(result).hasSize(1); // 팔로우한 유저의 리뷰만 1개
        ShowReviewResponse response = result.getFirst();
        assertThat(response.getContent()).isEqualTo("팔로잉한 유저의 리뷰");
        assertThat(response.getUserId()).isEqualTo(following.getId());
        assertThat(response.getNickname()).isEqualTo(following.getNickname());
    }

    @Test
    @DisplayName("팔로우한 사용자의 리뷰가 생성일자 기준 최신순으로 정렬된다")
    public void findFollowingUsersReviewsSortedByCreatedAtDesc() {
        // given
        User follower = userRepository.save(User.builder()
                .username("follower")
                .nickname("팔로워")
                .build());

        User following = userRepository.save(User.builder()
                .username("following")
                .nickname("팔로잉")
                .build());

        Long cafeId = cafeService.saveCafe(SaveCafeReq.builder()
                .title("테스트카페")
                .category("카페")
                .roadAddress("도로명주소")
                .mapx("1111")
                .mapy("2222")
                .address("주소")
                .link("링크")
                .build());


        // 팔로잉유저가 리뷰 3개 작성 (시간차를 두고)
        Review oldestReview = reviewRepository.save(Review.builder()
                .content("가장 오래된 리뷰")
                .rating(5)
                .visitDate(LocalDate.now())
                .cafe(cafeRepository.findById(cafeId).get())
                .user(following)
                .build());

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Review middleReview = reviewRepository.save(Review.builder()
                .content("중간 리뷰")
                .rating(5)
                .visitDate(LocalDate.now())
                .cafe(cafeRepository.findById(cafeId).get())
                .user(following)
                .build());

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Review latestReview = reviewRepository.save(Review.builder()
                .content("가장 최근 리뷰")
                .rating(5)
                .visitDate(LocalDate.now())
                .cafe(cafeRepository.findById(cafeId).get())
                .user(following)
                .build());

        // when
        ShowUserReviewRequest request = ShowUserReviewRequest.builder()
                .limit(10)
                .timestamp(LocalDateTime.now().plusYears(1))
                .build();

        List<ShowReviewResponse> result = reviewService.findFollowingUsersReviews(
                follower.getUsername(),
                request
        );

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getContent()).isEqualTo("가장 최근 리뷰");
        assertThat(result.get(1).getContent()).isEqualTo("중간 리뷰");
        assertThat(result.get(2).getContent()).isEqualTo("가장 오래된 리뷰");
    }

    @Test
    @DisplayName("팔로우한 사용자가 없는 경우 빈 리스트가 반환된다")
    public void returnEmptyListWhenNoFollowing() {
        // given
        User user = userRepository.save(User.builder()
                .username("user")
                .nickname("유저")
                .build());

        // when
        ShowUserReviewRequest request = ShowUserReviewRequest.builder()
                .limit(10)
                .timestamp(LocalDateTime.now().plusYears(1))
                .build();

        List<ShowReviewResponse> result = reviewService.findFollowingUsersReviews(
                user.getUsername(),
                request
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("주어진 timestamp 이전의 리뷰만 조회된다")
    public void findReviewsBeforeTimestamp() {
        // given
        User follower = userRepository.save(User.builder()
                .username("follower")
                .nickname("팔로워")
                .build());

        User following = userRepository.save(User.builder()
                .username("following")
                .nickname("팔로잉")
                .build());

        Long cafeId = cafeService.saveCafe(SaveCafeReq.builder()
                .title("테스트카페")
                .category("카페")
                .roadAddress("도로명주소")
                .mapx("1111")
                .mapy("2222")
                .address("주소")
                .link("링크")
                .build());


        // 리뷰 3개 작성 (시간차를 두고)
        Review oldReview = reviewRepository.save(Review.builder()
                .content("오래된 리뷰")
                .rating(5)
                .visitDate(LocalDate.now())
                .cafe(cafeRepository.findById(cafeId).get())
                .user(following)
                .build());

        // 1초 대기
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LocalDateTime timestamp = LocalDateTime.now(); // 중간 시점 timestamp 저장

        // 1초 대기
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Review newReview = reviewRepository.save(Review.builder()
                .content("최근 리뷰")
                .rating(5)
                .visitDate(LocalDate.now())
                .cafe(cafeRepository.findById(cafeId).get())
                .user(following)
                .build());

        // when
        ShowUserReviewRequest request = ShowUserReviewRequest.builder()
                .limit(10)
                .timestamp(timestamp)
                .build();

        List<ShowReviewResponse> result = reviewService.findFollowingUsersReviews(
                follower.getUsername(),
                request
        );

        // then
        assertThat(result).hasSize(1); // timestamp 이전의 리뷰만 1개
        ShowReviewResponse response = result.get(0);
        assertThat(response.getContent()).isEqualTo("오래된 리뷰");
        assertThat(response.getUserId()).isEqualTo(following.getId());
        assertThat(response.getCreatedAt()).isBefore(timestamp);
    }

    @Test
    @DisplayName("pageable의 size만큼만 리뷰가 조회된다")
    public void findReviewsLimitedByPageSize() {
        // given
        User follower = userRepository.save(User.builder()
                .username("follower")
                .nickname("팔로워")
                .build());

        User following = userRepository.save(User.builder()
                .username("following")
                .nickname("팔로잉")
                .build());

        Long cafeId = cafeService.saveCafe(SaveCafeReq.builder()
                .title("테스트카페")
                .category("카페")
                .roadAddress("도로명주소")
                .mapx("1111")
                .mapy("2222")
                .address("주소")
                .link("링크")
                .build());


        for (int i = 1; i <= 5; i++) {
            reviewRepository.save(Review.builder()
                    .content("리뷰 " + i)
                    .rating(5)
                    .visitDate(LocalDate.now())
                    .cafe(cafeRepository.findById(cafeId).get())
                    .user(following)
                    .build());

            // 시간 차이를 주기 위해 대기
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // when
        ShowUserReviewRequest request = ShowUserReviewRequest.builder()
                .limit(3)
                .timestamp(LocalDateTime.now().plusYears(1))
                .build();

        List<ShowReviewResponse> result = reviewService.findFollowingUsersReviews(
                follower.getUsername(),
                request
        );

        // then
        assertThat(result).hasSize(3);

        // 최신순으로 정렬되어 있는지 확인
        assertThat(result.get(0).getContent()).isEqualTo("리뷰 5");
        assertThat(result.get(1).getContent()).isEqualTo("리뷰 4");
        assertThat(result.get(2).getContent()).isEqualTo("리뷰 3");
    }

    @Test
    @DisplayName("팔로우한 사용자의 리뷰가 없는 경우 빈 리스트가 반환된다")
    public void returnEmptyListWhenFollowingHasNoReviews() {
        // given
        User follower = userRepository.save(User.builder()
                .username("follower")
                .nickname("팔로워")
                .build());

        User following = userRepository.save(User.builder()
                .username("following")
                .nickname("팔로잉")
                .build());


        // when
        ShowUserReviewRequest request = ShowUserReviewRequest.builder()
                .limit(10)
                .timestamp(LocalDateTime.now().plusYears(1))
                .build();

        List<ShowReviewResponse> result = reviewService.findFollowingUsersReviews(
                follower.getUsername(),
                request
        );

        // then
        assertThat(result).isEmpty();
    }
}