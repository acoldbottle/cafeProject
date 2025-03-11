package cafeLogProject.cafeLog.api.review.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShowReviewRequest {
    String sort;

    @Min(value = 1, message = "최소 리뷰 개수는 1입니다.")
    @Max(value = 20, message = "최대 리뷰 개수는 20입니다.")
    Integer limit;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
    LocalDateTime timestamp;

    @Size(max = 5, message = "최대 태그 개수는 5입니다.")
    List<Integer> tagIds;

    @Min(value = 1, message = "별점값은 1 이상이어야 합니다.")
    @Max(value = 5, message = "별점값은 5 이하여야 합니다.")
    Integer rating;

    public ShowReviewRequest() {
        sort = "NEW";
        limit = 10;
        timestamp = LocalDateTime.of(3000,1,1,0,0,0,0);
    }

    @Builder
    public ShowReviewRequest (String sort, Integer limit, LocalDateTime timestamp, List<Integer> tagIds, Integer rating) {
        this.sort = sort;
        this.limit = limit;
        this.timestamp = timestamp;
        this.tagIds = tagIds;
        this.rating = rating;
    }

}
