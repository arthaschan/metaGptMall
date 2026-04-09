from __future__ import annotations

import argparse
import shutil
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path

from metagpt_team.context_loader import ROOT, build_context_markdown, load_context_list


@dataclass
class Outputs:
    out_dir: Path
    pm_prd: Path
    arch: Path
    qa: Path
    dev: Path
    skeleton: Path
    summary: Path
    context: Path


def ensure_dir(p: Path) -> None:
    p.mkdir(parents=True, exist_ok=True)


def read_text(p: Path) -> str:
    return p.read_text(encoding="utf-8")


def write_text(p: Path, s: str) -> None:
    p.write_text(s, encoding="utf-8")


def build_outputs(out_dir: Path) -> Outputs:
    return Outputs(
        out_dir=out_dir,
        pm_prd=out_dir / "PM_PRD.md",
        arch=out_dir / "ARCHITECTURE.md",
        qa=out_dir / "QA_TESTPLAN.md",
        dev=out_dir / "DEV_PLAN.md",
        skeleton=out_dir / "CODE_SKELETON.md",
        summary=out_dir / "SUMMARY.md",
        context=out_dir / "prompt_context.md",
    )


def sync_latest(src_dir: Path, latest_dir: Path, filenames: list[str]) -> None:
    ensure_dir(latest_dir)
    for fn in filenames:
        s = src_dir / fn
        if s.exists():
            shutil.copyfile(s, latest_dir / fn)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("task", nargs="?", default="")
    ap.add_argument("--task-file", default="")
    ap.add_argument("--out-dir", default="")
    ap.add_argument("--context-list", default=str(ROOT / "metagpt_tools" / "context_files.txt"))
    ap.add_argument("--latest-dir", default=str(ROOT / "metagpt_artifacts" / "latest"))
    args = ap.parse_args()

    task = args.task
    if args.task_file:
        task_path = Path(args.task_file)
        if not task_path.is_absolute():
            task_path = (ROOT / task_path).resolve()
        task = read_text(task_path)

    if not task.strip():
        raise SystemExit("Task is empty")

    ts = datetime.now().strftime("%Y%m%d_%H%M%S")
    out_dir = Path(args.out_dir) if args.out_dir else (ROOT / "metagpt_outputs" / ts)
    if not out_dir.is_absolute():
        out_dir = (ROOT / out_dir).resolve()
    ensure_dir(out_dir)

    outs = build_outputs(out_dir)

    context_files = load_context_list(Path(args.context_list))
    context_md = build_context_markdown(context_files)
    write_text(outs.context, context_md)

    from metagpt_team.roles import build_team_and_run  # late import for clearer error

    artifacts = build_team_and_run(task=task, context=context_md)

    write_text(outs.pm_prd, artifacts.get("pm_prd", ""))
    write_text(outs.arch, artifacts.get("architecture", ""))
    write_text(outs.qa, artifacts.get("qa_testplan", ""))
    write_text(outs.dev, artifacts.get("dev_plan", ""))
    write_text(outs.skeleton, artifacts.get("code_skeleton", ""))

    write_text(
        outs.summary,
        (
            "# Summary\n\n"
            "Generated PM/Architect/QA/Dev artifacts.\n\n"
            "- MetaGPT pinned commit: 11cdf466d042aece04fc6cfd13b28e1a70341b1f\n"
            f"- Output dir: {out_dir}\n"
        ),
    )

    latest_dir = Path(args.latest_dir)
    if not latest_dir.is_absolute():
        latest_dir = (ROOT / latest_dir).resolve()

    sync_latest(
        src_dir=out_dir,
        latest_dir=latest_dir,
        filenames=[
            "PM_PRD.md",
            "ARCHITECTURE.md",
            "QA_TESTPLAN.md",
            "DEV_PLAN.md",
            "CODE_SKELETON.md",
            "SUMMARY.md",
        ],
    )

    print(f"[metagpt_team] done. outputs: {out_dir}")
    print(f"[metagpt_team] synced latest: {latest_dir}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
