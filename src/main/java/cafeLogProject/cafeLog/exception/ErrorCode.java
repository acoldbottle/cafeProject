package cafeLogProject.cafeLog.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_CREATE_ERROR(HttpStatus.BAD_REQUEST, "사용자 생성에 실패했습니다."),
    USER_EXTRACT_ERROR(HttpStatus.UNAUTHORIZED, "사용자 정보를 추출하는데 실패했습니다."),

    TOKEN_EXPIRED_ERROR(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    TOKEN_INVALID_ERROR(HttpStatus.UNAUTHORIZED, "토큰이 유효하지 않습니다."),
    TOKEN_NULL_ERROR(HttpStatus.UNAUTHORIZED, "토큰이 비어있습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
