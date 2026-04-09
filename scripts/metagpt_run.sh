#!/usr/bin/env bash
# scripts/metagpt_run.sh
#
# One-click MetaGPT runner for metaGptMall.
#
# Usage:
#   bash scripts/metagpt_run.sh "your task text"
#   bash scripts/metagpt_run.sh --task-file METAGPT_TASK.md
#   bash scripts/metagpt_run.sh --task-file METAGPT_TASK.md --out-dir /tmp/out
#
# Required env:
#   OPENAI_API_KEY   Your LLM API key (do NOT commit!)
#
# Optional env:
#   OPENAI_MODEL     Model name (default: not set; MetaGPT uses its own default)
#   METAGPT_CMD      MetaGPT CLI executable (default: metagpt)
#   PYTHON_CMD       Python executable (default: python3 or python)
#
# See docs/METAGPT_USAGE.md for full documentation.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# ---------------------------------------------------------------------------
# Usage
# ---------------------------------------------------------------------------
usage() {
  grep '^#' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//' | head -20
  exit 1
}

# ---------------------------------------------------------------------------
# Validate environment
# ---------------------------------------------------------------------------
if [ -z "${OPENAI_API_KEY:-}" ]; then
  echo "ERROR: OPENAI_API_KEY is not set."
  echo "  Export it before running:  export OPENAI_API_KEY='your-key'"
  echo "  See docs/METAGPT_USAGE.md for details."
  exit 1
fi

# ---------------------------------------------------------------------------
# Detect Python
# ---------------------------------------------------------------------------
PYTHON_CMD="${PYTHON_CMD:-}"
if [ -z "${PYTHON_CMD}" ]; then
  if command -v python3 &>/dev/null; then
    PYTHON_CMD="python3"
  elif command -v python &>/dev/null; then
    PYTHON_CMD="python"
  else
    echo "ERROR: Python not found. Install Python 3.10+ and re-run."
    exit 1
  fi
fi

# ---------------------------------------------------------------------------
# Ensure metagpt_tools deps are installed (best-effort)
# ---------------------------------------------------------------------------
REQ_FILE="${ROOT_DIR}/metagpt_tools/requirements.txt"
if [ -f "${REQ_FILE}" ]; then
  "${PYTHON_CMD}" -m pip install -q -r "${REQ_FILE}" || true
fi

# ---------------------------------------------------------------------------
# Delegate to metagpt_tools/run.py
# ---------------------------------------------------------------------------
exec "${PYTHON_CMD}" "${ROOT_DIR}/metagpt_tools/run.py" "$@"
