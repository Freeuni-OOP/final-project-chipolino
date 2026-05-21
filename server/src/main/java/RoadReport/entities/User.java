package RoadReport.entities;

import RoadReport.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
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
    private Role roles = Role.USER;


    private Integer reputationScore = 0;

    private Integer rejectedReportsCount = 0;
    private Boolean banned = false;
    private Boolean nonReliable = false;
    private LocalDateTime banExpiration;

    @Column(nullable = false)
    private LocalDateTime createDate;

    @PrePersist
    private void creation(){
        createDate = LocalDateTime.now();
    }
}
