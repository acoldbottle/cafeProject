package cafeLogProject.cafeLog.domains.review.exception;

import cafeLogProject.cafeLog.common.exception.CafeAppException;
import cafeLogProject.cafeLog.common.exception.ErrorCode;

public class ReviewNotFoundException extends CafeAppException {
    public ReviewNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReviewNotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
