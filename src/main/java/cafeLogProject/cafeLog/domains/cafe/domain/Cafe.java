package cafeLogProject.cafeLog.domains.cafe.domain;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Table(name = "cafe_db", uniqueConstraints = @UniqueConstraint(columnNames = {"cafe_name", "mapx", "mapy"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cafe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cafe_id")
    private Long id;

    @Column(name = "cafe_name")
    private String cafeName;
    private String mapx;
    private String mapy;
    private String address;
    private String roadAddress;
    private String link;

    @Builder
    public Cafe(String cafeName, String address, String roadAddress, String mapx, String mapy, String link) {
        this.cafeName = cafeName;
        this.address = address;
        this.roadAddress = roadAddress;
        this.mapx = mapx;
        this.mapy = mapy;
        this.link = link;
    }
}
