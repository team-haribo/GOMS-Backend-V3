package com.example.team.haribo.goms.domain.member.controller

import com.example.team.haribo.goms.domain.member.dto.request.MemberWithdrawRequest
import com.example.team.haribo.goms.domain.member.dto.response.MyProfileResponse
import com.example.team.haribo.goms.domain.member.dto.response.MyRoleResponse
import com.example.team.haribo.goms.domain.member.dto.response.ProfileImageResponse
import com.example.team.haribo.goms.domain.member.service.MemberWithdrawService
import com.example.team.haribo.goms.domain.member.service.MyProfileQueryService
import com.example.team.haribo.goms.domain.member.service.MyRoleQueryService
import com.example.team.haribo.goms.domain.member.service.ProfileImageCreateService
import com.example.team.haribo.goms.domain.member.service.ProfileImageDeleteService
import com.example.team.haribo.goms.domain.member.service.ProfileImageUpdateService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Member", description = "회원 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@Validated
@RequestMapping("/api/v3/member")
class MemberController(
    private val memberWithdrawService: MemberWithdrawService,
    private val myRoleQueryService: MyRoleQueryService,
    private val myProfileQueryService: MyProfileQueryService,
    private val profileImageCreateService: ProfileImageCreateService,
    private val profileImageUpdateService: ProfileImageUpdateService,
    private val profileImageDeleteService: ProfileImageDeleteService
) {

    @Operation(
        summary = "내 권한 조회",
        description = "현재 로그인한 사용자의 권한(Role)을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = MyRoleResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "인증 실패")
        ]
    )
    @GetMapping("/myrole")
    fun getMyRole(): ResponseEntity<MyRoleResponse> {
        return ResponseEntity.ok(myRoleQueryService.execute())
    }

    @Operation(
        summary = "내 프로필 조회",
        description = "현재 로그인한 사용자의 프로필 정보를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = MyProfileResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "인증 실패")
        ]
    )
    @GetMapping("/profile")
    fun getMyProfile(): ResponseEntity<MyProfileResponse> {
        return ResponseEntity.ok(myProfileQueryService.execute())
    }

    @Operation(
        summary = "프로필 이미지 등록",
        description = "현재 로그인한 사용자의 프로필 이미지를 등록합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "등록 성공",
                content = [Content(schema = Schema(implementation = ProfileImageResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "409", description = "이미 프로필 이미지가 존재함"),
            ApiResponse(responseCode = "413", description = "이미지 파일 크기 초과"),
            ApiResponse(responseCode = "415", description = "지원하지 않는 이미지 형식")
        ]
    )
    @PostMapping("/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createProfileImage(
        @RequestPart("image") image: MultipartFile
    ): ResponseEntity<ProfileImageResponse> {
        return ResponseEntity.status(201).body(profileImageCreateService.execute(image))
    }

    @Operation(
        summary = "프로필 이미지 수정",
        description = "현재 로그인한 사용자의 프로필 이미지를 수정합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = [Content(schema = Schema(implementation = ProfileImageResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "409", description = "프로필 이미지가 존재하지 않음"),
            ApiResponse(responseCode = "413", description = "이미지 파일 크기 초과"),
            ApiResponse(responseCode = "415", description = "지원하지 않는 이미지 형식")
        ]
    )
    @PatchMapping("/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateProfileImage(
        @RequestPart("image") image: MultipartFile
    ): ResponseEntity<ProfileImageResponse> {
        return ResponseEntity.ok(profileImageUpdateService.execute(image))
    }

    @Operation(
        summary = "프로필 이미지 삭제",
        description = "현재 로그인한 사용자의 프로필 이미지를 삭제합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "409", description = "프로필 이미지가 존재하지 않음")
        ]
    )
    @DeleteMapping("/profile-image")
    fun deleteProfileImage(): ResponseEntity<Void> {
        profileImageDeleteService.execute()
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "회원 탈퇴",
        description = "비밀번호 확인 후 회원 탈퇴를 진행합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 실패")
        ]
    )
    @DeleteMapping("/withdraw")
    fun withdraw(
        @Valid @RequestBody request: MemberWithdrawRequest
    ): ResponseEntity<Void> {
        memberWithdrawService.withdraw(request)
        return ResponseEntity.ok().build()
    }
}