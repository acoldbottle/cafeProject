package cafeLogProject.cafeLog.domains.review.exception;

import cafeLogProject.cafeLog.common.exception.CafeAppException;
import cafeLogProject.cafeLog.common.exception.ErrorCode;

public class ReviewSaveException extends CafeAppException {
    public ReviewSaveException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReviewSaveException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
