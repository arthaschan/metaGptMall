from __future__ import annotations


def _section_prompt(role_name: str, task: str, context: str, expected: str) -> str:
    return (
        f"You are acting as {role_name}.
"
        "Follow the repository constraints in the provided context.
\n"
        "=== TASK ===\n"
        f"{task}\n\n"
        "=== CONTEXT ===\n"
        f"{context}\n\n"
        "=== OUTPUT REQUIREMENTS ===\n"
        f"{expected}\n"
    )


def _classify_llm_error(e: Exception) -> str:
    """Return a user-actionable hint for common MetaGPT/OpenAI failures."""
    msg = repr(e)
    low = msg.lower()

    # Pydantic config validation from MetaGPT (api_key placeholder, etc.)
    if "validationerror" in low and "llm.api_key" in low:
        return (
            "MetaGPT config validation failed: llm.api_key is not set. "
            "Edit MetaGPT config (usually MetaGPT/config/config2.yaml) and set llm.api_key."
        )

    # OpenAI model not found / not accessible
    if "model_not_found" in low or "does not exist" in low:
        return (
            "The configured model name is not available for your API key. "
            "Fix llm.model in MetaGPT config/config2.yaml to a model you have access to (e.g. gpt-4o-mini)."
        )

    # Wrong endpoint: non-chat model used with chat.completions
    if "not a chat model" in low or "v1/chat/completions" in low:
        return (
            "The configured model is not a chat model but MetaGPT is calling the chat.completions endpoint. "
            "Set llm.model to a chat model (e.g. gpt-4o-mini, gpt-4o, gpt-4.1-mini)."
        )

    # Quota / billing / rate limit
    if "insufficient_quota" in low or "you exceeded your current quota" in low:
        return (
            "OpenAI API quota/billing is insufficient (insufficient_quota). "
            "Check OpenAI Billing/limits or switch to a cheaper model (e.g. gpt-4o-mini)."
        )

    if "rate_limit" in low or "error code: 429" in low:
        return (
            "OpenAI rate limit hit (429). Retry later, reduce concurrency, or use a cheaper/smaller model."
        )

    return ""


def _call_metagpt_sdk(prompt: str) -> str:
    """Call MetaGPT LLM via SDK, with best-effort compatibility across versions."""
    try:
        from metagpt.llm import LLM  # type: ignore
        import asyncio
        import inspect

        llm = LLM()

        # Prefer async API if present
        if hasattr(llm, "aask"):
            res = llm.aask(prompt)  # type: ignore
            if inspect.iscoroutine(res):
                return str(asyncio.run(res))
            return str(res)

        # Fallback to sync ask
        if hasattr(llm, "ask"):
            return str(llm.ask(prompt))  # type: ignore

        raise AttributeError("LLM has no ask/aask method")

    except Exception as e:
        hint = _classify_llm_error(e)
        msg = (
            "MetaGPT call failed.\n"
            "This is usually NOT an SDK entrypoint issue; it's commonly a config/model/quota problem.\n"
        )
        if hint:
            msg += f"Hint: {hint}\n"
        msg += f"Original error: {e!r}"
        raise RuntimeError(msg)


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