# GOMS PR 본문 기준

현재 `.github/pull_request_template.md`의 `관련 이슈`, 작업내용, 참고사항, PR 체크리스트 순서를 유지한다. 기존 heading과 checklist 문구를 바꾸거나 template에 없는 heading을 기본으로 추가하지 않는다.

- 해결하는 Issue: `Closes #<issue-number>`
- 단순 연관 Issue: `Related to #<issue-number>`
- `작업내용`: 리뷰에 필요한 핵심 변경만 짧게 작성
- `참고사항`: 운영 영향·호환성·중요한 제약·실제 검증 결과만 작성
- 실행하지 않은 검증은 체크하거나 본문에 기록하지 않음

세부 metadata 규칙은 `AGENTS.md`, 작성 절차는 `.agents/skills/write-pr/SKILL.md`를 따른다.
