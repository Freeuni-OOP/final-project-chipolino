package RoadReport.entities;

import RoadReport.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role roles = Role.USER;

    @Version
    private Long version;

    @Builder.Default
    private Integer reputationScore = 0;
    @Builder.Default
    private Boolean nonReliable = false;

    @Builder.Default
    private Integer rejectedReportsCount = 0;
    @Builder.Default
    private Boolean banned = false;
    private LocalDateTime banExpiration;

    @Column(nullable = false)
    private LocalDateTime createDate;

    @PrePersist
    private void creation(){
        createDate = LocalDateTime.now();
    }
}
