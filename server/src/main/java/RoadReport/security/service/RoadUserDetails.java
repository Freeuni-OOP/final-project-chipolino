package RoadReport.security.service;

import RoadReport.entities.User;
import RoadReport.enums.Role;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@RequiredArgsConstructor
@Builder
public class RoadUserDetails implements UserDetails {
    private final User user;

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRoles().name()));
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    public @NonNull Long getId() {
        return user.getId();
    }

    public @NonNull String getEmail() {
        return user.getEmail();
    }

    public @NonNull Role getRole() {
        return user.getRoles();
    }

    public @NonNull Integer getReputationScore() {
        return user.getReputationScore();
    }

    public @NonNull Boolean getBanned() {
        return user.getBanned();
    }

    public @NonNull java.time.LocalDateTime getBanExpiration() {
        return user.getBanExpiration();
    }

    public @NonNull java.time.LocalDateTime getCreateDate() {
        return user.getCreateDate();
    }

    @Override
    public @NonNull String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
