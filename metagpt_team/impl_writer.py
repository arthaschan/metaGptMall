"""
metagpt_team/impl_writer.py

Utilities for the --mode impl codegen pipeline:
  - parse_file_blocks: extract ``file path=...`` fenced blocks from LLM output
  - write_impl_files:  safely write those blocks into server/ and web/
"""

from __future__ import annotations

import re
from pathlib import Path

# Only allow files under these top-level directories to prevent unintended writes.
ALLOWED_PREFIXES = ("server/", "web/")

_BLOCK_RE = re.compile(
    r"^```file\s+path=(\S+)\s*\n(.*?)^```",
    re.MULTILINE | re.DOTALL,
)


def parse_file_blocks(text: str) -> list[tuple[str, str]]:
    """Return [(relative_path, content), ...] for every ``file path=...`` block found."""
    blocks: list[tuple[str, str]] = []
    for m in _BLOCK_RE.finditer(text):
        path_str = m.group(1).strip()
        content = m.group(2)
        blocks.append((path_str, content))
    return blocks


def _validate_path(rel: str, repo_root: Path) -> Path:
    """Resolve and validate that *rel* stays inside an allowed subtree."""
    # Normalise separators and strip leading slashes / dots
    normalised = rel.replace("\\", "/").lstrip("/")
    # Reject any path containing '..' components before resolving
    if any(part == ".." for part in Path(normalised).parts):
        raise ValueError(f"Path {rel!r} contains '..' components — path traversal is not allowed.")
    if not any(normalised.startswith(p) for p in ALLOWED_PREFIXES):
        raise ValueError(
            f"File path {rel!r} must start with one of {ALLOWED_PREFIXES}. "
            "Only server/ and web/ directories are written in impl mode."
        )
    target = (repo_root / normalised).resolve()
    # Double-check against path traversal after resolution (defence in depth)
    try:
        target.relative_to(repo_root.resolve())
    except ValueError:
        raise ValueError(f"Path traversal detected for {rel!r}.")
    return target


def write_impl_files(
    blocks: list[tuple[str, str]],
    repo_root: Path,
) -> list[Path]:
    """Write each (relative_path, content) block to *repo_root*.

    Returns the list of absolute paths that were written.
    Skips blocks whose paths are invalid and prints a warning.
    """
    written: list[Path] = []
    for rel, content in blocks:
        try:
            target = _validate_path(rel, repo_root)
        except ValueError as exc:
            print(f"[impl_writer] SKIP {rel!r}: {exc}")
            continue
        target.parent.mkdir(parents=True, exist_ok=True)
        try:
            target.write_text(content, encoding="utf-8")
        except OSError as exc:
            print(f"[impl_writer] ERROR writing {target}: {exc}")
            continue
        print(f"[impl_writer] wrote {target.relative_to(repo_root)}")
        written.append(target)
    return written
