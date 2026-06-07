package RoadReport.entities;

import RoadReport.enums.ReportStatus;
import RoadReport.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Builder
@Getter
@Setter
@Table(name = "reports")
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Vote> votes = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double longitude;
    @Column(nullable = false)
    private Double latitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Builder.Default
    private Integer weight = 1;

    @Builder.Default
    private Integer upvotes = 0;
    @Builder.Default
    private Integer downvotes = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> attributes;

    @Version
    private Long version;

    private LocalDateTime expireDate;
    @Column(nullable = false)
    private LocalDateTime createDate;

    @PrePersist
    private void creation(){
        if (createDate == null) {
            createDate = LocalDateTime.now();
        }
        if (expireDate == null) {
            // by default all temporary road reports live for 1 day
            expireDate = createDate.plusDays(1);
        }
    }
}