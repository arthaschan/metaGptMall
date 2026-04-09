from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read_text(p: Path) -> str:
    try:
        return p.read_text(encoding="utf-8")
    except FileNotFoundError:
        return f"[WARN] missing file: {p}\n"


def load_context_list(list_file: Path | None = None) -> list[Path]:
    if list_file is None:
        list_file = ROOT / "metagpt_tools" / "context_files.txt"

    lines = [ln.strip() for ln in read_text(list_file).splitlines()]
    files: list[Path] = []
    for ln in lines:
        if not ln or ln.startswith("#"):
            continue
        files.append((ROOT / ln).resolve())
    return files


def build_context_markdown(files: list[Path]) -> str:
    parts: list[str] = []
    parts.append("# Context (auto-composed)\n\n")
    for p in files:
        rel = p.relative_to(ROOT) if p.exists() else p
        parts.append(f"\n---\n\n## FILE: {rel}\n\n")
        parts.append(read_text(p))
        parts.append("\n")
    return "".join(parts)
