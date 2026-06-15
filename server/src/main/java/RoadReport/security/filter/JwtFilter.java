package RoadReport.security.filter;

import RoadReport.security.service.JwtService;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.security.service.RoadUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final RoadUserDetailsService userDetailsService;

    /**
     * Inspects the incoming request for a JWT token, performs validation, and sets up
     * the security authentication context if the token is valid.
     * If an exception occurs during token parsing or validation (e.g., an expired or tampered token),
     * the filter aborts the normal filter chain execution and delegates to {@link #handleException}
     * to return a 401 Unauthorized response.
     *
     * @param request     the incoming HTTP request
     * @param response    the outgoing HTTP response
     * @param filterChain the remaining filter chain to execute
     * @throws IOException      if an I/O error occurs during processing
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain) throws IOException {
        try {
            String token = extractToken(request);

            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtService.extractUsername(token);
                RoadUserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handleException(response);
        }
    }


    /**
     * Extracts the JWT token from the incoming HTTP request.
     * <p>
     * The method first attempts to extract the token using the {@code Bearer } prefix.
     * If the header is missing or malformed, it falls back to inspecting the request's
     * cookies for a cookie explicitly named {@code "jwt"}.
     *
     * @param request the current HTTP request
     * @return the raw JWT token string if found in the header or cookies; {@code null} otherwise
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if(header != null  && header.startsWith("Bearer ")){
            return header.substring(7);
        }

        if(request.getCookies() != null){
            return Arrays.stream(request.getCookies()).
                    filter(cookie -> cookie.getName().equals("jwt"))
                    .map(Cookie::getValue)
                    .findFirst().orElse(null);
        }

       return null;
    }

    /**
     *  This method manually enforces a {@code 401 Unauthorized} status code
     *
     * @param response the current HTTP response to modify
     * @throws IOException if an error occurs while writing to the response body
     */
    private void handleException(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{" +
                        "\"error\": \"Unauthorized\"" +
                        "}"
        );
    }
}
