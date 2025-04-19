package cafeLogProject.cafeLog.api.cafe.dto;

import cafeLogProject.cafeLog.domains.cafe.domain.Cafe;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CafeInfoRes {

    private String cafeName;
    private String address;
    private String roadAddress;
    private String mapx;
    private String mapy;
    private String link;
    private double avgRating;
    @JsonProperty("isScrap")
    private boolean isScrap;

    public static CafeInfoRes from(Cafe cafe) {
        return CafeInfoRes.builder()
                .cafeName(cafe.getCafeName())
                .address(cafe.getAddress())
                .roadAddress(cafe.getRoadAddress())
                .mapx(cafe.getMapx())
                .mapy(cafe.getMapy())
                .link(cafe.getLink())
                .build();
    }
}
