package io.github.tare99.paymentprocessor.security;

import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record UserPrincipal(String userId) implements UserDetails {

  @Override
  @NonNull
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  @NonNull
  public String getUsername() {
    return userId;
  }
}
