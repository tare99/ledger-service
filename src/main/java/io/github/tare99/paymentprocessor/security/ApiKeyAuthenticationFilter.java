package io.github.tare99.paymentprocessor.security;

import io.github.tare99.paymentprocessor.domain.entity.ApiKey;
import io.github.tare99.paymentprocessor.domain.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
  private static final String BEARER_PREFIX = "Bearer ";

  private final ApiKeyService apiKeyService;

  public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
    this.apiKeyService = apiKeyService;
  }

  @Override
  public void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String token = extractBearerToken(request);
      if (token != null && token.startsWith(ApiKeyService.KEY_PREFIX)) {
        Optional<ApiKey> apiKey = apiKeyService.validate(token);
        if (apiKey.isPresent()) {
          String accountNumber = apiKey.get().getAccount().getAccountNumber();
          UserPrincipal principal = new UserPrincipal(accountNumber);
          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    } catch (Exception e) {
      log.warn("Could not authenticate via API key: {}", e.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  private String extractBearerToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
      return header.substring(BEARER_PREFIX.length());
    }
    return null;
  }
}
