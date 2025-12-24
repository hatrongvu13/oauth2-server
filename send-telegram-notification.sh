#!/bin/bash

# Thông tin build (có thể lấy từ Git Action environment variables)
GITHUB_SERVER_URL=${GITHUB_SERVER_URL:github.com/hatrongvu13}
REPO_NAME="${GITHUB_REPOSITORY:demo}"
BRANCH_NAME="${GITHUB_REF_NAME:-main}"
COMMIT_HASH="${GITHUB_SHA:0:7}"
COMMIT_MESSAGE="${GITHUB_COMMIT_MESSAGE:-Build completed successfully}"
RUN_ID="${GITHUB_RUN_ID:-0}"
WORKFLOW_NAME="${GITHUB_WORKFLOW:-Build and Deploy}"
RUN_URL="${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID}"

# Emoji và định dạng
SUCCESS_EMOJI="✅"
ROCKET_EMOJI="🚀"
CALENDAR_EMOJI="📅"
BRANCH_EMOJI="🌿"
COMMIT_EMOJI="🔗"

# Tạo message
MESSAGE="*${SUCCESS_EMOJI} BUILD SUCCESSFUL ${SUCCESS_EMOJI}*

*Repository:* \`${REPO_NAME}\`
${BRANCH_EMOJI} *Branch:* \`${BRANCH_NAME}\`
${COMMIT_EMOJI} *Commit:* \`${COMMIT_HASH}\`
${CALENDAR_EMOJI} *Workflow:* ${WORKFLOW_NAME}
*Run ID:* \`#${RUN_ID}\`

*Message:*
\`\`\`
${COMMIT_MESSAGE}
\`\`\`

*Details:* [View in GitHub Actions](${RUN_URL})

${ROCKET_EMOJI} *Deployment completed successfully!* ${ROCKET_EMOJI}"

# Gửi message qua Telegram
curl -s -X POST "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage" \
  -d chat_id="${TELEGRAM_CHAT_ID}" \
  -d text="${MESSAGE}" \
  -d parse_mode="Markdown" \
  -d disable_web_page_preview="true"