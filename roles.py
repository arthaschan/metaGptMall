import asyncio

async def _call_metagpt_sdk_async(prompt):
    # Await the LLM call
    return await llm.aask(prompt)

async def _build_team_and_run_async(task, context):
    responses = {}
    # Sequentially awaiting calls for each role
    responses['PM'] = await _call_metagpt_sdk_async(f'PM: {task} {context}')
    responses['Architect'] = await _call_metagpt_sdk_async(f'Architect: {task} {context}')
    responses['QA'] = await _call_metagpt_sdk_async(f'QA: {task} {context}')
    responses['Dev'] = await _call_metagpt_sdk_async(f'Dev: {task} {context}')
    responses['Skeleton'] = await _call_metagpt_sdk_async(f'Skeleton: {task} {context}')
    return responses

def build_team_and_run(task, context):
    return asyncio.run(_build_team_and_run_async(task, context))

# Fixing the escape issue in _section_prompt
_section_prompt = 'This is the first line.\nThis is the second line.\nThis is the third line.'