from __future__ import annotations

import argparse
import shutil
import subprocess
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


@dataclass
class ValidationResult:
    command: list[str]
    returncode: int
    output: str
    timed_out: bool = False

    @property
    def success(self) -> bool:
        return self.returncode == 0 and not self.timed_out


def ensure_dir(p: Path) -> None:
    p.mkdir(parents=True, exist_ok=True)


def read_text(p: Path) -> str:
    return p.read_text(encoding="utf-8")


def write_text(p: Path, s: str) -> None:
    p.write_text(s, encoding="utf-8")


def resolve_task_file(task_file: str) -> Path:
    task_path = Path(task_file)
    if task_path.is_absolute():
        if not task_path.exists():
            raise SystemExit(f"Task file not found: {task_path}")
        return task_path

    direct = (ROOT / task_path).resolve()
    if direct.exists():
        return direct

    matches = sorted((ROOT / "metagpt_tasks").glob(f"**/{task_path.name}"))
    if len(matches) == 1:
        return matches[0].resolve()
    if len(matches) > 1:
        options = "\n".join(f"  - {p.relative_to(ROOT)}" for p in matches)
        raise SystemExit(
            f"Task file {task_file!r} is ambiguous. Use one of:\n{options}"
        )
    raise SystemExit(f"Task file not found: {task_file}")


def relative_paths(paths: list[Path]) -> list[str]:
    return sorted({p.resolve().relative_to(ROOT).as_posix() for p in paths})


def default_validation_commands(paths: list[Path]) -> list[list[str]]:
    rels = relative_paths(paths)
    commands: list[list[str]] = []
    if any(rel.startswith("server/") for rel in rels):
        commands.append(["mvn", "-f", str(ROOT / "server" / "pom.xml"), "test"])
    if any(rel.startswith("web/") for rel in rels) and (ROOT / "web" / "node_modules").exists():
        commands.append(["npm", "--prefix", str(ROOT / "web"), "run", "build"])
    return commands


def truncate_output(text: str, limit: int = 16000) -> str:
    if len(text) <= limit:
        return text
    head = text[:6000]
    tail = text[-8000:]
    return f"{head}\n\n... OUTPUT TRUNCATED ...\n\n{tail}"


def run_validation_commands(commands: list[list[str]]) -> list[ValidationResult]:
    results: list[ValidationResult] = []
    for command in commands:
        try:
            completed = subprocess.run(
                command,
                cwd=ROOT,
                capture_output=True,
                text=True,
                timeout=600,
                check=False,
            )
            output = (completed.stdout or "") + (completed.stderr or "")
            results.append(
                ValidationResult(
                    command=command,
                    returncode=completed.returncode,
                    output=truncate_output(output),
                )
            )
        except subprocess.TimeoutExpired as exc:
            output = ((exc.stdout or "") + (exc.stderr or "")) if exc.stdout or exc.stderr else ""
            results.append(
                ValidationResult(
                    command=command,
                    returncode=124,
                    output=truncate_output(output or "Command timed out after 600 seconds."),
                    timed_out=True,
                )
            )
    return results


def build_validation_report(results: list[ValidationResult]) -> str:
    parts = ["# Verification Report\n"]
    for idx, result in enumerate(results, start=1):
        status = "PASS" if result.success else "FAIL"
        parts.append(f"\n## Command {idx}: {' '.join(result.command)}\n")
        parts.append(f"- Status: {status}\n")
        parts.append(f"- Exit code: {result.returncode}\n")
        if result.timed_out:
            parts.append("- Timed out: yes\n")
        parts.append("\n```text\n")
        parts.append(result.output.strip())
        parts.append("\n```\n")
    return "".join(parts)


def build_current_files_snapshot(paths: list[Path], max_chars: int = 120000) -> str:
    parts = ["# Current Files\n"]
    used = len(parts[0])
    for path in sorted({p.resolve() for p in paths}):
        rel = path.relative_to(ROOT).as_posix()
        text = read_text(path)
        chunk = f"\n## FILE: {rel}\n\n{text}\n"
        if used + len(chunk) > max_chars:
            parts.append("\n[TRUNCATED] snapshot limit reached.\n")
            break
        parts.append(chunk)
        used += len(chunk)
    return "".join(parts)


def run_impl_auto_fix_loop(
    *,
    task: str,
    context_md: str,
    initial_written: list[Path],
    out_dir: Path,
    max_fix_rounds: int,
) -> tuple[list[Path], list[Path], list[Path], bool | None]:
    from metagpt_team.impl_writer import parse_file_blocks, write_impl_files
    from metagpt_team.roles import build_impl_fix_and_run

    written_map = {p.resolve(): p for p in initial_written}
    validation_logs: list[Path] = []
    fix_logs: list[Path] = []
    commands = default_validation_commands(list(written_map.values()))
    if not commands:
        return list(written_map.values()), validation_logs, fix_logs, None

    for round_idx in range(max_fix_rounds + 1):
        results = run_validation_commands(commands)
        validation_log = out_dir / f"VALIDATION_{round_idx:02d}.md"
        write_text(validation_log, build_validation_report(results))
        validation_logs.append(validation_log)

        if all(result.success for result in results):
            return list(written_map.values()), validation_logs, fix_logs, True

        if round_idx == max_fix_rounds:
            return list(written_map.values()), validation_logs, fix_logs, False

        raw_fix = build_impl_fix_and_run(
            task=task,
            context=context_md,
            current_files=build_current_files_snapshot(list(written_map.values())),
            verification_report=build_validation_report(results),
        )
        fix_log = out_dir / f"IMPL_FIX_{round_idx + 1:02d}_RAW.md"
        write_text(fix_log, raw_fix)
        fix_logs.append(fix_log)

        blocks = parse_file_blocks(raw_fix)
        if not blocks:
            return list(written_map.values()), validation_logs, fix_logs, False

        rewritten = write_impl_files(blocks, repo_root=ROOT, dry_run=False, overwrite=True)
        if not rewritten:
            return list(written_map.values()), validation_logs, fix_logs, False

        for path in rewritten:
            written_map[path.resolve()] = path
        commands = default_validation_commands(list(written_map.values()))

    return list(written_map.values()), validation_logs, fix_logs, False


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
    ap = argparse.ArgumentParser(
        description="MetaGPT team runner for metaGptMall.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=(
            "Modes:\n"
            "  plan  (default) Generate PRD / architecture / test plan / dev plan /\n"
            "                  code skeleton Markdown into metagpt_outputs/ and sync\n"
            "                  to metagpt_artifacts/latest/.\n"
            "  impl            Generate full runnable implementation code (Spring Boot\n"
            "                  3.x backend + Vue 3 frontend) and write source files\n"
            "                  directly into server/ and web/ inside the repository.\n"
        ),
    )
    ap.add_argument("task", nargs="?", default="")
    ap.add_argument("--task-file", default="")
    ap.add_argument("--out-dir", default="")
    ap.add_argument("--context-list", default=str(ROOT / "metagpt_tools" / "context_files.txt"))
    ap.add_argument("--latest-dir", default=str(ROOT / "metagpt_artifacts" / "latest"))
    ap.add_argument(
        "--mode",
        choices=["plan", "impl"],
        default="plan",
        help="Runner mode: 'plan' (default) generates Markdown artifacts; "
             "'impl' generates and writes runnable source code into server/ and web/.",
    )
    ap.add_argument("--dry-run", action="store_true", help="(impl mode) Do not write files; only print what would be written.")
    ap.add_argument("--overwrite", action="store_true", help="(impl mode) Overwrite existing files.")
    ap.add_argument(
        "--auto-fix",
        action="store_true",
        help="(impl mode) After writing files, run local verification commands and ask MetaGPT for bounded fix rounds when validation fails.",
    )
    ap.add_argument(
        "--max-fix-rounds",
        type=int,
        default=1,
        help="(impl mode) Maximum verification-driven fix rounds when --auto-fix is enabled.",
    )
    args = ap.parse_args()

    task = args.task
    if args.task_file:
        task_path = resolve_task_file(args.task_file)
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

    if args.mode == "impl":
        from metagpt_team.roles import build_impl_and_run  # late import for clearer error
        from metagpt_team.impl_writer import parse_file_blocks, write_impl_files

        raw = build_impl_and_run(task=task, context=context_md)
        # Save raw LLM output for audit / replay
        write_text(out_dir / "IMPL_RAW.md", raw)

        blocks = parse_file_blocks(raw)
        written = write_impl_files(blocks, repo_root=ROOT, dry_run=args.dry_run, overwrite=args.overwrite)
        validation_logs: list[Path] = []
        fix_logs: list[Path] = []
        validation_ok: bool | None = None
        if args.auto_fix and not args.dry_run and written:
            written, validation_logs, fix_logs, validation_ok = run_impl_auto_fix_loop(
                task=task,
                context_md=context_md,
                initial_written=written,
                out_dir=out_dir,
                max_fix_rounds=max(args.max_fix_rounds, 0),
            )

        validation_status = (
            "skipped"
            if validation_ok is None
            else "passed"
            if validation_ok
            else "failed"
        )
        write_text(
            outs.summary,
            (
                "# Summary (impl mode)\n\n"
                f"Generated {len(written)} source file(s) into server/ and web/.\n\n"
                "- MetaGPT pinned commit: 11cdf466d042aece04fc6cfd13b28e1a70341b1f\n"
                f"- Raw LLM output: {out_dir / 'IMPL_RAW.md'}\n"
                f"- Auto-fix enabled: {'yes' if args.auto_fix else 'no'}\n"
                f"- Verification status: {validation_status}\n"
                + (
                    "- Validation logs:\n"
                    + "".join(f"  - {p.relative_to(ROOT)}\n" for p in validation_logs)
                    if validation_logs
                    else ""
                )
                + (
                    "- Fix raw outputs:\n"
                    + "".join(f"  - {p.relative_to(ROOT)}\n" for p in fix_logs)
                    if fix_logs
                    else ""
                )
                +
                "- Files written:\n"
                + "".join(f"  - {p.relative_to(ROOT)}\n" for p in written)
            ),
        )

        print(f"[metagpt_team] impl done. {len(written)} file(s) written.")
        print(f"[metagpt_team] raw output: {out_dir / 'IMPL_RAW.md'}")
    else:
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
