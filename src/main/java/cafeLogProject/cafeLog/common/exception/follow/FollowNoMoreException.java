package cafeLogProject.cafeLog.common.exception.follow;

import cafeLogProject.cafeLog.common.exception.CafeAppException;
import cafeLogProject.cafeLog.common.exception.ErrorCode;

public class FollowNoMoreException extends CafeAppException {
    public FollowNoMoreException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FollowNoMoreException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
