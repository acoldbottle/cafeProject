package cafeLogProject.cafeLog.common.exception.image;

import cafeLogProject.cafeLog.common.exception.CafeAppException;
import cafeLogProject.cafeLog.common.exception.ErrorCode;

public class ImageLoadException extends CafeAppException {
    public ImageLoadException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ImageLoadException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
