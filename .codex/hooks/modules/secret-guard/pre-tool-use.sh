#!/usr/bin/env bash
set -euo pipefail

payload="$(cat || true)"
category=""
if printf '%s' "$payload" | grep -Eiq 'AKIA[0-9A-Z]{16}|ASIA[0-9A-Z]{16}'; then category="aws-access-key"; fi
if printf '%s' "$payload" | grep -Eiq 'gh[pousr]_[A-Za-z0-9]{20,}'; then category="github-token"; fi
if printf '%s' "$payload" | grep -Eiq 'sk-[A-Za-z0-9_-]{20,}'; then category="openai-key"; fi
if printf '%s' "$payload" | grep -Eiq 'xox[baprs]-[A-Za-z0-9-]{10,}|discord(app)?\.com/api/webhooks/[0-9]+/[A-Za-z0-9_-]{10,}'; then category="chat-webhook-token"; fi
if printf '%s' "$payload" | grep -Eiq 'BEGIN[[:space:]]+(RSA[[:space:]]+|EC[[:space:]]+|OPENSSH[[:space:]]+)?PRIVATE[[:space:]]+KEY'; then category="private-key"; fi
if printf '%s' "$payload" | grep -Eiq 'Bearer[[:space:]]+[A-Za-z0-9._-]{20,}'; then category="bearer-token"; fi
if printf '%s' "$payload" | grep -Eiq '"private_key"[[:space:]]*:[[:space:]]*"-----BEGIN|firebase.*service[[:space:]_-]*account.*json'; then category="firebase-credential"; fi
if printf '%s' "$payload" | grep -Eiq '(JWT_SECRET|MAIL_PASSWORD|KAKAO_REST_API_KEY|GOMS_INTERNAL_API_SECRET|REDIS_PASSWORD|DB_PASSWORD)[[:space:]]*[=:][[:space:]]*[A-Za-z0-9+/._=-]{12,}'; then
  if ! printf '%s' "$payload" | grep -Eiq '(placeholder|changeme|example|your[_-]?value|<[^>]+>)'; then category="credential-assignment"; fi
fi

if [[ -n "$category" ]]; then
  echo "secret-guard 차단: secret 의심 패턴($category)이 감지되었습니다. 값과 파일 경로는 출력하지 않습니다." >&2
  exit 3
fi
exit 0
