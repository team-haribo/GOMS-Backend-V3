package com.example.team.haribo.goms.GomsServerV3.global.exception

enum class ErrorCode(
    val status: Int,
    val message: String
) {

    // AUTH
    EXPIRED_TOKEN(401, "토큰이 만료되었습니다."),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    UNAUTHORIZED(401, "인증 정보가 유효하지 않습니다."),

    // COMMON
    INTERNAL_SERVER_ERROR(500, "예기치 못한 서버 에러가 발생했습니다."),
    INVALID_REQUEST(400, "요청 정보가 유효하지 않습니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),
    NOT_FOUND(404, "요청한 리소스를 찾을 수 없습니다."),

    // MEMBER
    NOT_FOUND_MEMBER(404, "해당 사용자를 찾을 수 없습니다."),
    INVALID_MEMBER_PRINCIPAL(401, "현재 인증된 사용자의 정보가 유효하지 않습니다."),

    // OUTING
    CANNOT_OUTING(403, "외출이 금지된 사용자입니다."),

    // QR
    QR_EXPIRED(400, "QR 코드가 만료되었습니다.");

}
