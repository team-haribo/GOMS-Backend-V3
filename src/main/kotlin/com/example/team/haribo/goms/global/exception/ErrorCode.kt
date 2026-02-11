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

    // OUTING
    CANNOT_OUTING(403, "외출이 금지된 사용자입니다."),

    // QR
    QR_EXPIRED(400, "QR 코드가 만료되었습니다.");
}
