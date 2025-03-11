package cafeLogProject.cafeLog.api.review.service;

import cafeLogProject.cafeLog.api.image.service.ImageUtil;
import cafeLogProject.cafeLog.api.review.dto.*;
import cafeLogProject.cafeLog.common.auth.exception.UserNotAuthenticatedException;
import cafeLogProject.cafeLog.common.exception.ErrorCode;
import cafeLogProject.cafeLog.common.exception.UnexpectedServerException;
import cafeLogProject.cafeLog.common.exception.cafe.CafeNotFoundException;
import cafeLogProject.cafeLog.common.exception.draftReview.DraftReviewNotFoundException;
import cafeLogProject.cafeLog.domains.cafe.repository.CafeRepository;
import cafeLogProject.cafeLog.domains.draftReview.domain.DraftReview;
import cafeLogProject.cafeLog.domains.draftReview.repository.DraftReviewRepository;
import cafeLogProject.cafeLog.domains.image.domain.ReviewImage;
import cafeLogProject.cafeLog.domains.image.repository.ReviewImageRepository;
import cafeLogProject.cafeLog.domains.review.domain.Review;
import cafeLogProject.cafeLog.common.exception.review.ReviewNotFoundException;
import cafeLogProject.cafeLog.domains.review.domain.Tag;
import cafeLogProject.cafeLog.domains.review.repository.ReviewRepository;
import cafeLogProject.cafeLog.domains.review.repository.TagRepository;
import cafeLogProject.cafeLog.domains.user.domain.User;
import cafeLogProject.cafeLog.common.exception.user.UserNotFoundException;
import cafeLogProject.cafeLog.domains.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReviewService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final TagRepository tagRepository;
    private final DraftReviewRepository draftReviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final CafeRepository cafeRepository;

    @CacheEvict(value = "reviews", allEntries = true)
    @Transactional
    public ShowReviewResponse addReview(String username, Long draftReviewId, RegistReviewRequest registReviewRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(() ->{
            throw new UserNotFoundException(username, ErrorCode.USER_NOT_FOUND_ERROR);
        });
        DraftReview draftReview = validateIdentityWithDraftReview(username, draftReviewId);
        List<UUID> imageIds = draftReview.getImageIds();
        List<Integer> tagIds = registReviewRequest.getTagIds();

        Review newReview = reviewRepository.save(registReviewRequest.toEntity(user, draftReview.getCafe()));
        saveTags(tagIds, newReview);
        saveReviewImage(imageIds, newReview);
        draftReviewRepository.delete(draftReview);
        return ShowReviewResponse.builder()
                .review(newReview)
                .imageIds(imageIds)
                .tagIds(tagIds)
                .build();
    }

    @CacheEvict(value = "reviews", allEntries = true)
    @Transactional
    public ShowReviewResponse updateReview(String username, long reviewId, UpdateReviewRequest updateReviewRequest) {
        Review oldReview = validateIdentityWithReview(username, reviewId);
        Review updatedReview = reviewRepository.save(updateReviewRequest.toEntity(oldReview));
        List<Integer> updateTagIds = updateReviewRequest.getTagIds();
        if (updateTagIds != null) {
            updateTags(updateTagIds, updatedReview);
        }
        return findReview(updatedReview.getId());
    }

    @CacheEvict(value = "reviews", allEntries = true)
    @Transactional
    public void deleteReview(String username, long reviewId) {
        Review review = validateIdentityWithReview(username, reviewId);
        deleteAllReviewImageInReview(review);
        deleteAllTagsInReview(review);
        reviewRepository.delete(review);
    }

    public List<ShowReviewResponse> findCafeReviews(Long cafeId, ShowCafeReviewRequest request){
        cafeRepository.findById(cafeId).orElseThrow(() -> {
            throw new CafeNotFoundException(cafeId.toString(), ErrorCode.CAFE_NOT_FOUND_ERROR);
        });
        Pageable pageable = PageRequest.of(0, request.getLimit());

        try {
            return reviewRepository.searchByCafeId(cafeId, request.getTimestamp(), pageable);
        } catch (Exception e) {
            log.error(e.toString());
            throw new UnexpectedServerException("findReviews 에러", ErrorCode.UNEXPECTED_ERROR);
        }
    }

    @Cacheable(value = "reviews", key = "'allReviews:timestamp:' + #request.getTimestamp()",
            condition = "#request.getSort()=='NEW' and #request.getTagIds()==null and #request.getRating()==null")
    public List<ShowReviewResponse> findReviews(ShowReviewRequest request) {
        Pageable pageable = PageRequest.of(0, request.getLimit());
        return reviewRepository.search(request.getSort(), request.getTagIds(), request.getRating(), request.getTimestamp(), pageable);
    }

    public List<ShowReviewResponse> findUserReviews(String username, ShowUserReviewRequest request) {
        User user = userRepository.findByUsername(username).orElseThrow(() ->{
            throw new UserNotFoundException(username, ErrorCode.USER_NOT_FOUND_ERROR);
        });
        Pageable pageable = PageRequest.of(0, request.getLimit());
        return reviewRepository.searchByUser(user, request.getTimestamp(), pageable);
    }

    public List<ShowReviewResponse> findUserReviews(Long userId, ShowUserReviewRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new UserNotFoundException(userId.toString(), ErrorCode.USER_NOT_FOUND_ERROR);
        });
        Pageable pageable = PageRequest.of(0, request.getLimit());
        return reviewRepository.searchByUser(user, request.getTimestamp(), pageable);
    }

    public ShowReviewResponse findReview(Long reviewId){
        ShowReviewResponse showReviewResponse = reviewRepository.findShowReviewResponseById(reviewId).orElseThrow(() -> {
            throw new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND_ERROR);
        });
        return showReviewResponse;
    }

    // 본인의 임시저장 리뷰인지 검사 & DraftReview 리턴
    private DraftReview validateIdentityWithDraftReview(String username, Long draftReviewId) {
        DraftReview draftReview = draftReviewRepository.findById(draftReviewId).orElseThrow(() -> {
            throw new DraftReviewNotFoundException(draftReviewId.toString(), ErrorCode.DRAFT_REVIEW_NOT_FOUND_ERROR);
        });
        // 해당 리뷰가 본인의 리뷰가 맞는지 확인
        if (!draftReview.getUser().getUsername().equals(username)) {
            throw new UserNotAuthenticatedException(ErrorCode.USER_NOT_AUTH_ERROR);
        }
        return draftReview;
    }

    // 본인의 리뷰인지 검사 & Review 리턴
    private Review validateIdentityWithReview(String username, Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> {
            throw new ReviewNotFoundException(reviewId.toString(), ErrorCode.REVIEW_NOT_FOUND_ERROR);
        });
        // 해당 리뷰가 본인의 리뷰가 맞는지 확인
        if (!review.getUser().getUsername().equals(username)) {
            throw new UserNotAuthenticatedException(ErrorCode.USER_NOT_AUTH_ERROR);
        }
        return review;
    }

    private List<Tag> saveTags(List<Integer> tagIds, Review savedReview) {
        List<Tag> tagList = new ArrayList<>();
        if (tagIds == null) return new ArrayList<>();

        for (Integer tagId : tagIds) {
            tagList.add(Tag.builder()
                    .tagId(tagId)
                    .review(savedReview)
                    .build());
        }
        if (tagList.isEmpty()) {
            return new ArrayList<>();
        } else {
            return tagRepository.saveAll(tagList);
        }
    }

    private void updateTags(List<Integer> tagIds, Review review) {
        // 태그 엔티티 모두 삭제후 재생성
        List<Tag> tags = tagRepository.findAllByReview(review);
        tagRepository.deleteAll(tags);
        saveTags(tagIds, review);
    }

    private void deleteAllTagsInReview(Review review){
        List<Tag> tags = tagRepository.findAllByReview(review);
        tagRepository.deleteAll(tags);
    }

    // 이미 파일 저장된 이미지를 ReviewImage 객체로도 저장
    private List<String> saveReviewImage(List<UUID> imageIds, Review review) {
        List<String> reviewImageIds = new ArrayList<>();
        for (UUID imageId : imageIds) {
            // 이미지 파일 경로 변경
            String imageIdStr = imageId.toString();
            ImageUtil.renameCompressedImage(ImageUtil.DRAFT_REVIEW_IMAGE_PATH+imageIdStr, ImageUtil.REVIEW_IMAGE_PATH+imageIdStr);

            ReviewImage reviewImage = reviewImageRepository.save(ReviewImage.builder()
                    .id(imageId)
                    .review(review)
                    .build());
            reviewImageIds.add(reviewImage.getId().toString());
        }
        return reviewImageIds;
    }

    // 특정 리뷰의 모든 리뷰 이미지 삭제
    private void deleteAllReviewImageInReview(Review review){
        List<ReviewImage> images = reviewImageRepository.findAllByReview(review);
        for (ReviewImage reviewImage : images) {
            try {
                ImageUtil.deleteCompressedImage(ImageUtil.REVIEW_IMAGE_PATH, reviewImage.getId().toString());
            } catch (Exception e) {
                log.error("deleteAllReviewImageInReview 메소드 에러 발생");
            }
            if (reviewImage != null) reviewImageRepository.delete(reviewImage);
        }
    }


    public List<ShowReviewResponse> findFollowingUsersReviews(String username, ShowUserReviewRequest request) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username, ErrorCode.USER_NOT_FOUND_ERROR));

        Pageable pageable = PageRequest.of(0, request.getLimit());
        return reviewRepository.searchByFollowingUsers(currentUser.getId(), request.getTimestamp(), pageable);
    }
}