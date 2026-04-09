from __future__ import annotations


def _section_prompt(role_name: str, task: str, context: str, expected: str) -> str:
    return (
        f"You are acting as {role_name}.\n"
        "Follow the repository constraints in the provided context.\n\n"
        "=== TASK ===\n"
        f"{task}\n\n"
        "=== CONTEXT ===\n"
        f"{context}\n\n"
        "=== OUTPUT REQUIREMENTS ===\n"
        f"{expected}\n"
    )


def _call_metagpt_sdk(prompt: str) -> str:
    """
    MetaGPT SDK API differs across versions.
    Keep ALL adaptation inside this function.

    Pinned MetaGPT commit (user): 11cdf466d042aece04fc6cfd13b28e1a70341b1f
    """
    try:
        # Most common entrypoint style seen in MetaGPT repos
        from metagpt.llm import LLM  # type: ignore

        llm = LLM()
        if hasattr(llm, "ask"):
            return str(llm.ask(prompt))  # type: ignore
        if hasattr(llm, "aask"):
            out = llm.aask(prompt)  # type: ignore
            return str(out)
        raise AttributeError("LLM has no ask/aask method")
    except Exception as e:
        raise RuntimeError(
            "MetaGPT SDK entrypoint mismatch.\n"
            "Edit metagpt_team/roles.py::_call_metagpt_sdk to match your installed MetaGPT commit.\n"
            f"Original error: {e!r}"
        )


def build_team_and_run(task: str, context: str) -> dict[str, str]:
    pm_expected = (
        "Write a PRD in Markdown:\n"
        "- Goals / Non-goals\n"
        "- User stories\n"
        "- Functional requirements\n"
        "- Non-functional requirements\n"
        "- Acceptance criteria\n"
        "- Risks & open questions\n"
    )

    arch_expected = (
        "Write architecture in Markdown:\n"
        "- Modules/components\n"
        "- Key flows (sequence)\n"
        "- Data model notes (align with repo SQL)\n"
        "- API outline (align with docs/API.md)\n"
        "- Tradeoffs\n"
        "- What existing docs/files to update in this repo\n"
    )

    qa_expected = (
        "Write QA test plan in Markdown:\n"
        "- Smoke tests\n"
        "- API tests (happy path + negative)\n"
        "- Edge cases\n"
        "- Minimal test data suggestions\n"
        "- What to automate later\n"
    )

    dev_expected = (
        "Write dev plan in Markdown:\n"
        "- Concrete task breakdown (small PRs/commits)\n"
        "- Suggested file paths\n"
        "- Implementation steps\n"
        "- Checkpoints and verification steps\n"
        "Avoid pasting huge code.\n"
    )

    skeleton_expected = (
        "Provide CODE SKELETON suggestions in Markdown:\n"
        "- Proposed directory tree\n"
        "- Key file list\n"
        "- Key interfaces/classes/functions signatures\n"
        "- Pseudocode where helpful\n"
        "- Notes on configuration/env vars\n"
        "Do NOT dump full implementation.\n"
    )

    pm = _call_metagpt_sdk(_section_prompt("PM", task, context, pm_expected))
    arch = _call_metagpt_sdk(_section_prompt("Architect", task, context, arch_expected))
    qa = _call_metagpt_sdk(_section_prompt("QA Engineer", task, context, qa_expected))
    dev = _call_metagpt_sdk(_section_prompt("Developer", task, context, dev_expected))
    skeleton = _call_metagpt_sdk(_section_prompt("Developer (Code Skeleton)", task, context, skeleton_expected))

    return {
        "pm_prd": pm,
        "architecture": arch,
        "qa_testplan": qa,
        "dev_plan": dev,
        "code_skeleton": skeleton,
    }
