#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

usage() {
  cat <<'EOF'
Usage:
  bash metagpt_team/metagpt_team_run.sh "your task"
  bash metagpt_team/metagpt_team_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md
  bash metagpt_team/metagpt_team_run.sh --mode impl "implement the checkout flow"
  bash metagpt_team/metagpt_team_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl

Options:
  --task-file FILE   Read task from file instead of positional argument
  --mode MODE        Runner mode: 'plan' (default) or 'impl'
                     plan: generate Markdown artifacts into metagpt_outputs/
                     impl: generate code into server/ and web/
  --dry-run          (impl mode) Do not write files; only print what would be written
  --overwrite        (impl mode) Overwrite existing files
  --auto-fix         (impl mode) Run bounded local validation and ask MetaGPT for fix rounds on failure
  --max-fix-rounds N (impl mode) Maximum repair rounds when --auto-fix is enabled

Env:
  OPENAI_API_KEY  required (MetaGPT reads from your MetaGPT config; this is only used by some setups)
  OPENAI_MODEL    optional
EOF
}

TASK=""
TASK_FILE=""
MODE=""
DRY_RUN=""
OVERWRITE=""
AUTO_FIX=""
MAX_FIX_ROUNDS=""

if [[ $# -eq 0 ]]; then
  usage
  exit 2
fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --task-file)
      TASK_FILE="${2:-}"
      shift 2
      ;;
    --mode)
      MODE="${2:-}"
      shift 2
      ;;
    --dry-run)
      DRY_RUN="1"
      shift
      ;;
    --overwrite)
      OVERWRITE="1"
      shift
      ;;
    --auto-fix)
      AUTO_FIX="1"
      shift
      ;;
    --max-fix-rounds)
      MAX_FIX_ROUNDS="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      # positional task string (only takes the last one if multiple are provided)
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

# OPENAI_API_KEY may come from environment or from MetaGPT's own config file.
if [[ -z "${OPENAI_API_KEY:-}" ]]; then
  echo "WARN: OPENAI_API_KEY is not set in the shell; continuing and relying on MetaGPT config." >&2
fi

PYTHON="${PYTHON:-python3}"

ARGS=()
if [[ -n "${TASK_FILE}" ]]; then
  ARGS+=("--task-file" "${TASK_FILE}")
else
  if [[ -z "${TASK}" ]]; then
    echo "ERROR: task is empty (provide a task string or --task-file)" >&2
    exit 2
  fi
  ARGS+=("${TASK}")
fi

if [[ -n "${MODE}" ]]; then
  ARGS+=("--mode" "${MODE}")
fi
if [[ -n "${DRY_RUN}" ]]; then
  ARGS+=("--dry-run")
fi
if [[ -n "${OVERWRITE}" ]]; then
  ARGS+=("--overwrite")
fi
if [[ -n "${AUTO_FIX}" ]]; then
  ARGS+=("--auto-fix")
fi
if [[ -n "${MAX_FIX_ROUNDS}" ]]; then
  ARGS+=("--max-fix-rounds" "${MAX_FIX_ROUNDS}")
fi

(
  cd "${ROOT_DIR}"
  "${PYTHON}" -m metagpt_team.run_team "${ARGS[@]}"
)
