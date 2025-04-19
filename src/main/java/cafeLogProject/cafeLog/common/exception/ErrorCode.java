package cafeLogProject.cafeLog.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_CREATE_ERROR(HttpStatus.BAD_REQUEST, "사용자 생성에 실패했습니다."),
    USER_EXTRACT_ERROR(HttpStatus.UNAUTHORIZED, "사용자 정보를 추출하는데 실패했습니다."),
    USER_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_NOT_AUTH_ERROR(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    USER_NICKNAME_ERROR(HttpStatus.CONFLICT, "이미 사용중인 닉네임입니다"),
    USER_NICKNAME_NULL_ERROR(HttpStatus.BAD_REQUEST, "검색하려는 닉네임이 비어있습니다."),

    TOKEN_EXPIRED_ERROR(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    TOKEN_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "토큰을 찾지 못했습니다.."),
    TOKEN_INVALID_ERROR(HttpStatus.UNAUTHORIZED, "토큰이 유효하지 않습니다."),
    TOKEN_NULL_ERROR(HttpStatus.UNAUTHORIZED, "토큰이 비어있습니다."),
    TOKEN_REISSUE_ERROR(HttpStatus.BAD_REQUEST, "토큰 재발급에 실패했습니다."),

    UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),

    REVIEW_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    REVIEW_INVALID_SORT_ERROR(HttpStatus.BAD_REQUEST, "알 수 없는 정렬 방식입니다."),
    REVIEW_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류에 의해 리뷰를 저장하지 못했습니다."),
    REVIEW_UPDATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류에 의해 리뷰를 저장하지 못했습니다."),
    REVIEW_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류에 의해 리뷰를 삭제하지 못했습니다."),

    DRAFT_REVIEW_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "임시저장 리뷰를 찾을 수 없습니다."),

    TAG_INVALID_ERROR(HttpStatus.BAD_REQUEST, "태그 아이디가 유효하지 않습니다."),


    CAFE_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "카페를 찾을 수 없습니다."),
    CAFE_CATEGORY_ERROR(HttpStatus.BAD_REQUEST, "카테고리가 카페가 아닙니다."),
    CAFE_API_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "네이버 API 응답 중 오류가 발생했습니다."),

    IMAGE_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),
    IMAGE_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류에 의해 이미지를 저장하지 못했습니다."),
    IMAGE_LOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류에 의해 이미지를 불러오지 못했습니다."),
    IMAGE_INVALID_ERROR(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "이미지 파일 형식이 아닙니다."),
    IMAGE_DAMAGED_ERROR(HttpStatus.BAD_REQUEST, "손상된 이미지 파일입니다."),
    IMAGE_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류에 의해 이미지를 삭제하지 못했습니다."),

    FOLLOW_SELF_ERROR(HttpStatus.BAD_REQUEST, "자기 자신을 팔로우 할 수 없습니다."),
    FOLLOW_USER_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    FOLLOW_CURSOR_EXCEPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 커서입니다."),
    FOLLOW_NO_MORE_EXCEPTION(HttpStatus.BAD_REQUEST, "리스트를 모두 조회했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
