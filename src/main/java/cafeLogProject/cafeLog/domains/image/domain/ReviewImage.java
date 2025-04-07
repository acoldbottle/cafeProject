package cafeLogProject.cafeLog.domains.image.domain;

import cafeLogProject.cafeLog.domains.review.domain.Review;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "review_image_tb")
@NoArgsConstructor
public class ReviewImage {

    @Id
//    @UuidGenerator        // 저장시에 생성됨 (엔티티 생성 -> uuid 사용한 이미지 파일 저장 -> 엔티티 저장 순대로 처리하기 위해 주석처리함)
    @Column(name = "review_image_id", columnDefinition = "BINARY(16)")      // binary(255)대신 binary(16)로 저장하여 JPA 사용시 오류방지를 위함
    private UUID id;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = true)
    private Review review;

    public ReviewImage(UUID id){
        this.id = id;
    }

    @Builder
    public ReviewImage(UUID id, Review review){
        this.id = id;
        this.review = review;
    }

}
