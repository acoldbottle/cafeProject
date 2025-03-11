package cafeLogProject.cafeLog.common.auth.jwt;

public class TokenExpiration {

    public static final long ACCESS_TOKEN_EXPIRATION = 30 * 60 * 1000L; // 30분
    public static final long REFRESH_TOKEN_EXPIRATION = 14 * 24 * 60 * 60 * 1000L; // 14일
}
