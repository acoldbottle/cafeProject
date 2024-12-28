package cafeLogProject.cafeLog.common.exception;

import cafeLogProject.cafeLog.common.dto.ApiErrorResponse;
import cafeLogProject.cafeLog.common.exception.CafeAppException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //@Valid 위반 에러 헨들러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> MethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        if (message.equals("")) {
            message = "필수 입력 항목이 누락되었습니다.";
        }
        log.error(message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST, message));
    }

    //요청받은 필드의 값이 잘못된 타입인 에러 핸들러
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> HttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String message;
        if (e.getCause() instanceof MismatchedInputException mismatchedInputException) {
            message = mismatchedInputException.getPath().get(0).getFieldName() + " 필드의 값이 잘못되었습니다.";
        } else {
            message = "잘못된 필드의 값입니다.";
        }
        log.error(message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST, message));
    }

    //커스텀한 예외 핸들러
    @ExceptionHandler(CafeAppException.class)
    public ResponseEntity<ApiErrorResponse> CafeAppException(CafeAppException e) {
        log.error(e.getErrorCode().getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(new ApiErrorResponse(e));
    }


}
