package cafeLogProject.cafeLog.domains.review.exception;

import cafeLogProject.cafeLog.common.exception.CafeAppException;
import cafeLogProject.cafeLog.common.exception.ErrorCode;

public class ReviewInvalidSortError extends CafeAppException {
    public ReviewInvalidSortError(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReviewInvalidSortError(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
