"""
metagpt_tools/run.py

MetaGPT runner for metaGptMall.

Usage:
    python metagpt_tools/run.py "your task text"
    python metagpt_tools/run.py --task-file METAGPT_TASK.md
    python metagpt_tools/run.py --task-file METAGPT_TASK.md --out-dir /tmp/my_output

Environment variables:
    OPENAI_API_KEY   Required (do NOT commit)
    OPENAI_MODEL     Optional
    METAGPT_CMD      Optional (default: metagpt)

See docs/METAGPT_USAGE.md for full documentation.
"""

from __future__ import annotations

import argparse
import os
import subprocess
import sys
from datetime import datetime
from pathlib import Path

# ---------------------------------------------------------------------------
# Paths
# ---------------------------------------------------------------------------

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CONTEXT_LIST = ROOT / "metagpt_tools" / "context_files.txt"
DEFAULT_OUTPUT_BASE = ROOT / "metagpt_outputs"


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------


def read_text(path: Path) -> str:
    """Read a file as UTF-8 text; return a warning string if missing."""
    try:
        return path.read_text(encoding="utf-8")
    except FileNotFoundError:
        return f"[WARN] missing file: {path}\n"


def load_context_files(list_file: Path) -> list[Path]:
    """Parse context_files.txt and return resolved Path objects."""
    lines = read_text(list_file).splitlines()
    files: list[Path] = []
    for line in lines:
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        files.append((ROOT / line).resolve())
    return files


def build_prompt(task: str, context_files: list[Path]) -> str:
    """Compose the full prompt string from task + context files."""
    parts: list[str] = []
    parts.append(
        "You are assisting an ecommerce MVP repository named metaGptMall.\n"
        "Follow the repository conventions described in the context files below.\n"
        "Do not invent port numbers, passwords, topic names, or Redis keys that\n"
        "differ from those documented; flag any ambiguities explicitly.\n"
    )
    parts.append("\n=== TASK ===\n")
    parts.append(task.strip() + "\n")
    parts.append("\n=== CONTEXT FILES ===\n")
    for p in context_files:
        try:
            rel = p.relative_to(ROOT)
        except ValueError:
            rel = p
        parts.append(f"\n--- FILE: {rel} ---\n")
        parts.append(read_text(p))
        parts.append("\n")
    return "".join(parts)


def ensure_dir(path: Path) -> None:
    path.mkdir(parents=True, exist_ok=True)


def load_dotenv_if_available() -> None:
    """Load .env file if python-dotenv is installed (best-effort)."""
    try:
        from dotenv import load_dotenv  # type: ignore[import-untyped]

        env_path = ROOT / ".env"
        if env_path.exists():
            load_dotenv(dotenv_path=env_path, override=False)
    except ImportError:
        pass


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------


def main() -> int:
    load_dotenv_if_available()

    ap = argparse.ArgumentParser(
        description="Run MetaGPT with metaGptMall repository context.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    ap.add_argument(
        "task",
        nargs="?",
        default="",
        help="Task/requirement text to pass to MetaGPT.",
    )
    ap.add_argument(
        "--task-file",
        metavar="PATH",
        default="",
        help="Read task text from this file instead of the positional argument.",
    )
    ap.add_argument(
        "--context-list",
        metavar="PATH",
        default=str(DEFAULT_CONTEXT_LIST),
        help="Path to the context file list (default: metagpt_tools/context_files.txt).",
    )
    ap.add_argument(
        "--out-dir",
        metavar="DIR",
        default="",
        help="Output directory (default: metagpt_outputs/<timestamp>).",
    )
    ap.add_argument(
        "--metagpt-cmd",
        metavar="CMD",
        default=os.environ.get("METAGPT_CMD", "metagpt"),
        help="MetaGPT CLI executable (default: $METAGPT_CMD or 'metagpt').",
    )
    args = ap.parse_args()

    # Resolve task text
    task = args.task
    if args.task_file:
        task_path = Path(args.task_file)
        if not task_path.is_absolute():
            task_path = (ROOT / task_path).resolve()
        task = read_text(task_path)

    if not task.strip():
        print(
            "ERROR: task is empty. Provide a task string or --task-file.",
            file=sys.stderr,
        )
        return 2

    # Resolve context list
    context_list = Path(args.context_list)
    if not context_list.is_absolute():
        context_list = (ROOT / context_list).resolve()
    context_files = load_context_files(context_list)

    # Resolve output directory
    ts = datetime.now().strftime("%Y%m%d_%H%M%S")
    if args.out_dir:
        out_dir = Path(args.out_dir)
        if not out_dir.is_absolute():
            out_dir = (ROOT / out_dir).resolve()
    else:
        out_dir = DEFAULT_OUTPUT_BASE / ts
    ensure_dir(out_dir)

    # Build prompt and save it for traceability
    prompt = build_prompt(task, context_files)
    prompt_file = out_dir / "prompt.txt"
    prompt_file.write_text(prompt, encoding="utf-8")
    print(f"[metagpt_tools] prompt saved → {prompt_file}")

    # Call MetaGPT via CLI
    # NOTE: The exact CLI interface may differ between MetaGPT versions.
    # This implementation pipes the prompt via stdin and captures stdout/stderr.
    # If your version requires a different interface, update the cmd list below
    # or replace subprocess.run(...) with a MetaGPT SDK call.
    cmd = [args.metagpt_cmd]
    print(f"[metagpt_tools] running: {' '.join(cmd)}")
    print(f"[metagpt_tools] output dir: {out_dir}")

    try:
        result = subprocess.run(
            cmd,
            input=prompt,
            text=True,
            capture_output=True,
            check=False,
            env=os.environ.copy(),
        )
    except FileNotFoundError:
        print(
            "ERROR: MetaGPT CLI not found.\n"
            f"  Tried: {args.metagpt_cmd}\n"
            "  Install MetaGPT:  pip install metagpt\n"
            "  Or set:           export METAGPT_CMD=/path/to/metagpt\n"
            "  See: docs/METAGPT_USAGE.md",
            file=sys.stderr,
        )
        return 127

    # Save outputs
    (out_dir / "stdout.txt").write_text(result.stdout or "", encoding="utf-8")
    (out_dir / "stderr.txt").write_text(result.stderr or "", encoding="utf-8")

    if result.returncode != 0:
        print(
            f"[metagpt_tools] MetaGPT exited with code {result.returncode}",
            file=sys.stderr,
        )
        print(f"[metagpt_tools] stderr → {out_dir}/stderr.txt", file=sys.stderr)
        return result.returncode

    print(f"[metagpt_tools] done. Output → {out_dir}/stdout.txt")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
