package com.example.team.haribo.goms.global.exception

enum class ErrorCode(
    val status: Int,
    val message: String
) {

    // AUTH
    EXPIRED_TOKEN(401, "토큰이 만료되었습니다."),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    UNAUTHORIZED(401, "인증 정보가 유효하지 않습니다."),
    INVALID_MEMBER_PRINCIPAL(404, "존재하지 않는 사용자입니다."),
    INVALID_CREDENTIALS(401, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_PASSWORD_POLICY(400, "비밀번호 정책을 만족하지 않습니다."),
    INVALID_VERIFIED_TOKEN(401, "이메일 인증 정보가 유효하지 않습니다."),
    INVALID_REFRESH_TOKEN(401, "유효하지 않은 리프레시 토큰입니다."),
    VERIFICATION_CODE_MISMATCH(401, "인증번호가 일치하지 않습니다."),
    VERIFICATION_CODE_EXPIRED(410, "인증번호가 만료되었습니다."),


    // COMMON
    INTERNAL_SERVER_ERROR(500, "예기치 못한 서버 에러가 발생했습니다."),
    INVALID_REQUEST(400, "요청 정보가 유효하지 않습니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),
    TOO_MANY_REQUESTS(429, "요청이 너무 많습니다."),

    // MEMBER
    NOT_FOUND_MEMBER(404, "해당 사용자를 찾을 수 없습니다."),
    ALREADY_REGISTERED_EMAIL(409, "이미 가입된 이메일입니다."),
    MEMBER_WITHDRAW_PASSWORD_MISMATCH(403, "회원 탈퇴 비밀번호가 일치하지 않습니다."),

    // OUTING
    CANNOT_OUTING(403, "외출이 금지된 사용자입니다."),
    ALREADY_OUTING(409, "이미 외출 중인 사용자입니다."),
    NOT_OUTING(409, "현재 외출 중이 아닙니다."),
    EMPTY_NAME(400, "name 값 누락 또는 공백입니다."),

    // STUDENT-COUNCIL
    ROLE_CONFLICT(409, "이미 동일한 권한입니다."),
    STATUS_CONFLICT(409, "상태 변경이 불가능합니다."),

    // QR
    QR_EXPIRED(400, "QR 코드가 만료되었습니다."),

    // PLACE
    NOT_FOUND_PLACE(404, "장소를 찾을 수 없습니다."),
    ALREADY_RECOMMENDED_PLACE(409, "이미 추천한 장소입니다."),
    ALREADY_UNRECOMMENDED_PLACE(409, "이미 추천 취소된 장소입니다."),

    // REVIEW
    NOT_FOUND_REVIEW(404, "후기를 찾을 수 없습니다."),
    REVIEW_FORBIDDEN(403, "후기에 대한 권한이 없습니다."),
    REVIEW_CONTENT_EMPTY(400, "후기 내용이 비어있습니다."),
    REVIEW_CONTENT_TOO_LONG(400, "후기 내용이 너무 깁니다."),
    ALREADY_REVIEWED_PLACE(409, "이미 해당 장소에 후기를 작성했습니다."),

    // REPORT
    NOT_FOUND_REPORT(404, "신고를 찾을 수 없습니다."),
    REPORT_FORBIDDEN(403, "신고에 대한 권한이 없습니다."),
    REPORT_CONTENT_EMPTY(400, "신고 내용이 비어있습니다."),
    REPORT_CONTENT_TOO_LONG(400, "신고 내용이 너무 깁니다."),
    ALREADY_REPORTED_REVIEW(409, "이미 해당 후기를 신고했습니다."),
    REPORT_ALREADY_RESOLVED(409, "이미 처리된 신고입니다."),
    INVALID_REPORT_STATUS(400, "유효하지 않은 신고 상태입니다."),

    // DEVICE TOKEN
    NOT_FOUND_DEVICE_TOKEN(404, "해당 디바이스 토큰을 찾을 수 없습니다.");
}
