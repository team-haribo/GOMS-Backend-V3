package com.example.team.haribo.goms.domain.studentcouncil.service.impl

import com.example.team.haribo.goms.domain.common.enums.Role
import com.example.team.haribo.goms.domain.discord.entity.DiscordAccountLink
import com.example.team.haribo.goms.domain.discord.repository.DiscordAccountLinkRepository
import com.example.team.haribo.goms.domain.member.entity.Member
import com.example.team.haribo.goms.domain.member.repository.MemberRepository
import com.example.team.haribo.goms.domain.studentcouncil.exception.InvalidInternalSecretException
import com.example.team.haribo.goms.fixture.MemberFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder

class DiscordStudentCouncilApplyServiceImplTest : DescribeSpec({

    val memberRepository = mockk<MemberRepository>()
    val discordAccountLinkRepository = mockk<DiscordAccountLinkRepository>()
    val secret = "test-secret"
    val service = DiscordStudentCouncilApplyServiceImpl(
        memberRepository,
        discordAccountLinkRepository,
        secret
    )

    // DiscordAccountLink 를 간단히 만드는 헬퍼
    fun link(member: Member, discordUserId: String) =
        DiscordAccountLink(member, discordUserId, "username-$discordUserId")

    describe("DiscordStudentCouncilApplyService.apply") {

        context("Secret 검증") {
            it("Secret이 일치하지 않으면 InvalidInternalSecretException이 발생하고 Repository는 호출되지 않는다") {
                shouldThrow<InvalidInternalSecretException> {
                    service.apply(internalSecret = "wrong-secret", discordUserIds = listOf("d1"))
                }
                verify(exactly = 0) { memberRepository.resetTemporaryStudentCouncilRole() }
                verify(exactly = 0) { discordAccountLinkRepository.findAllByDiscordUserIdIn(any()) }
            }
        }

        context("모든 ID가 계정 연결됨 (일반 회원)") {
            val member = MemberFixture.student(id = 1L) // role = ROLE_STUDENT
            every { memberRepository.resetTemporaryStudentCouncilRole() } returns 0
            every { discordAccountLinkRepository.findAllByDiscordUserIdIn(any()) } returns
                listOf(link(member, "d1"))

            it("success=true, 회원 role이 ROLE_STUDENT_COUNCIL로 변경되고 syncedUsers에 이름이 담긴다") {
                val response = service.apply(secret, listOf("d1"))

                response.success shouldBe true
                response.syncedCount shouldBe 1
                response.syncedUsers shouldContainExactly listOf(member.name)
                response.failedCount shouldBe 0
                response.failedUsers shouldContainExactly emptyList()
                member.role shouldBe Role.ROLE_STUDENT_COUNCIL
            }
        }

        context("isFixedStudentCouncil 회원") {
            val fixedMember = MemberFixture.student(id = 2L).also {
                it.isFixedStudentCouncil = true
                it.role = Role.ROLE_STUDENT_COUNCIL// 기존 role(ROLE_STUDENT)을 유지하는지 보기 위함
            }
            every { memberRepository.resetTemporaryStudentCouncilRole() } returns 0
            every { discordAccountLinkRepository.findAllByDiscordUserIdIn(any()) } returns
                listOf(link(fixedMember, "d2"))

            it("role은 변경되지 않고 그대로 유지되며 syncedUsers에는 포함된다") {
                val response = service.apply(secret, listOf("d2"))

                response.success shouldBe true
                response.syncedUsers shouldContainExactly listOf(fixedMember.name)
                fixedMember.role shouldBe Role.ROLE_STUDENT_COUNCIL // 변경 안 됨
            }
        }

        context("일부 ID가 연결되지 않음") {
            val member = MemberFixture.student(id = 3L)
            every { memberRepository.resetTemporaryStudentCouncilRole() } returns 0
            every { discordAccountLinkRepository.findAllByDiscordUserIdIn(any()) } returns
                listOf(link(member, "linked")) // "missing"은 연결 없음

            it("success=false, 연결 안 된 ID가 failedUsers에 담긴다") {
                val response = service.apply(secret, listOf("linked", "missing"))

                response.success shouldBe false
                response.syncedCount shouldBe 1
                response.syncedUsers shouldContainExactly listOf(member.name)
                response.failedCount shouldBe 1
                response.failedUsers shouldContainExactly listOf("missing")
            }
        }

        context("입력 정규화 (trim, blank 제거, distinct)") {
            val member = MemberFixture.student(id = 4L)
            val captured = slot<Collection<String>>()
            every { memberRepository.resetTemporaryStudentCouncilRole() } returns 0
            every { discordAccountLinkRepository.findAllByDiscordUserIdIn(capture(captured)) } returns
                listOf(link(member, "d1"))

            it("공백 제거·blank 제거·중복 제거된 ID 목록으로 Repository를 조회한다") {
                service.apply(secret, listOf(" d1 ", "d1", "", "  ", "d2"))

                captured.captured.toList() shouldContainExactly listOf("d1", "d2")
            }
        }

        context("호출 순서") {
            val member = MemberFixture.student(id = 5L)
            every { memberRepository.resetTemporaryStudentCouncilRole() } returns 0
            every { discordAccountLinkRepository.findAllByDiscordUserIdIn(any()) } returns
                listOf(link(member, "d1"))

            it("임시 권한 초기화가 계정 연결 조회보다 먼저 호출된다") {
                service.apply(secret, listOf("d1"))

                verifyOrder {
                    memberRepository.resetTemporaryStudentCouncilRole()
                    discordAccountLinkRepository.findAllByDiscordUserIdIn(any())
                }
            }
        }
    }
})
