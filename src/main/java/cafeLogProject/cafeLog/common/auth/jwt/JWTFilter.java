package cafeLogProject.cafeLog.common.auth.jwt;

import cafeLogProject.cafeLog.common.auth.exception.TokenExpiredException;
import cafeLogProject.cafeLog.common.auth.exception.TokenNullException;
import cafeLogProject.cafeLog.common.auth.jwt.token.JWTTokenService;
import cafeLogProject.cafeLog.common.auth.oauth2.CustomOAuth2User;
import cafeLogProject.cafeLog.common.exception.UnexpectedServerException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static cafeLogProject.cafeLog.common.auth.common.CookieUtil.*;
import static cafeLogProject.cafeLog.common.exception.ErrorCode.*;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final JWTTokenService tokenService;
    private final String[] whiteList;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (isWhitelisted(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String accessToken = extractToken(request, "access");
        String refreshToken = extractToken(request, "refresh");

        if (!checkRefreshTokenValidity(response, refreshToken)) return;

        if (checkBlackList(response, refreshToken)) return;

        checkNullOrEmpty(accessToken);

        authenticateUser(extractUserInfo(accessToken, refreshToken, response));

        chain.doFilter(request, response);
    }

    private boolean isWhitelisted(String uri) {
        for (String pattern : whiteList) {
            if (pattern.contains("**")) {
                String prefix = pattern.substring(0, pattern.indexOf("**"));
                if (uri.startsWith(prefix)) {
                    return true;
                }
            } else if (uri.equals(pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRefreshTokenValidity(HttpServletResponse response, String refreshToken) throws IOException {

        try {
            validateRefreshToken(refreshToken);
        } catch (TokenExpiredException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            String json = String.format("{\"status\": %d, \"message\": \"%s\"}",
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "리프레시 토큰이 만료되었습니다.");
            response.getWriter().write(json);
            return false;
        }


        return true;
    }

    private boolean checkBlackList(HttpServletResponse response, String refreshToken) throws IOException {
        if (tokenService.isExistInBlacklist(refreshToken)) {
            log.info("refreshToken = {}", refreshToken);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            String json = String.format("{\"status\": %d, \"message\": \"%s\"}",
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "로그아웃 된 사용자입니다.");
            response.getWriter().write(json);
            return true;
        }
        return false;
    }


    /**
     * RefreshToken 이 유효한지 확인
     */
    private void validateRefreshToken(String refreshToken) {

        if (refreshToken == null || jwtUtil.isExpired(refreshToken)) {
            log.warn("Refresh token is expired");
            throw new TokenExpiredException(TOKEN_EXPIRED_ERROR);
        }
    }

    /**
     * AccessToken 이 null 이거나 값이 비어있는지 확인
     */
    private void checkNullOrEmpty(String accessToken) {

        if (accessToken == null || accessToken.isEmpty()) {
            log.debug("token null");
            throw new TokenNullException(TOKEN_NULL_ERROR);
        }
    }

    /**
     * JWTUserDTO 반환  --> access 토큰이 만료되었다면 재발급 후 반환
     */
    private JWTUserDTO extractUserInfo(String accessToken, String refreshToken, HttpServletResponse response) {

        try {
            validateAccessToken(accessToken);

            return tokenService.extractUserInfoFromToken(accessToken);
        } catch (TokenExpiredException e) {

            return reissueToken(refreshToken, response);
        } catch (Exception e) {

            log.error("Unexpected error: {}", e.getMessage());
            throw new UnexpectedServerException(UNEXPECTED_ERROR);
        }
    }

    /**
     * 토큰 재발급 후에 JWTUserDTO 반환
     */
    private JWTUserDTO reissueToken(String refreshToken, HttpServletResponse response) {

        log.debug("Token expired, attempting to reissue");

        String tokens = tokenService.reissue(refreshToken);
        String[] tokenArr = tokens.split(" ");

        ResponseCookie access = createCookie("access", tokenArr[0]);
        ResponseCookie refresh = createCookie("refresh", tokenArr[1]);

        addResponseCookie(response, access);
        addResponseCookie(response, refresh);

        return tokenService.extractUserInfoFromToken(tokenArr[1]);
    }

    /**
     * access 토큰 검사
     */
    private void validateAccessToken(String accessToken) {

        if (accessToken == null || jwtUtil.isExpired(accessToken)) {
            log.debug("Access token is expired, try reissue ...");
            throw new TokenExpiredException(TOKEN_EXPIRED_ERROR);
        }
    }

    /**
     * 생성한 UserDTO -> CustomOAuth2User -> 권한 인증 토큰 발급 -> 권한 인증
     */
    private static void authenticateUser(JWTUserDTO userDTO) {

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
