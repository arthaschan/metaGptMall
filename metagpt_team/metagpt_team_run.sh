#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

usage() {
  cat <<'EOF'
Usage:
  bash scripts/metagpt_team_run.sh "your task"
  bash scripts/metagpt_team_run.sh --task-file METAGPT_TASK.md

Env:
  OPENAI_API_KEY  required
  OPENAI_MODEL    optional
EOF
}

TASK=""
TASK_FILE=""

if [[ $# -eq 0 ]]; then
  usage
  exit 2
fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --task-file)
      TASK_FILE="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      TASK="$1"
      shift
      ;;
  esac
done

# Optional: load .env if present
if [[ -f "${ROOT_DIR}/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "${ROOT_DIR}/.env"
  set +a
fi

if [[ -z "${OPENAI_API_KEY:-}" ]]; then
  echo "ERROR: OPENAI_API_KEY is not set" >&2
  exit 2
fi

PYTHON="${PYTHON:-python3}"

ARGS=()
if [[ -n "${TASK_FILE}" ]]; then
  ARGS+=("--task-file" "${TASK_FILE}")
else
  ARGS+=("${TASK}")
fi

"${PYTHON}" "${ROOT_DIR}/metagpt_team/run_team.py" "${ARGS[@]}"
