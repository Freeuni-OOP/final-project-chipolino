package RoadReport.entities;

import RoadReport.enums.VoteType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "report_id"})}
)

public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteType type;

    @Column(nullable = false)
    private LocalDateTime createDate;

    @PrePersist
    private void creation(){
        createDate = LocalDateTime.now();
    }
}
