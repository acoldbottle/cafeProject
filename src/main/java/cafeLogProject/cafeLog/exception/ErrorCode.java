package cafeLogProject.cafeLog.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_CREATE_ERROR(HttpStatus.BAD_REQUEST, "사용자 생성에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
