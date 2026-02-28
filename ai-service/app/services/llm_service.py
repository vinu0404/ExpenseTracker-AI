import logging
from openai import OpenAI
from app.schemas.expense import ExpenseEvent
from config import settings

logger = logging.getLogger(__name__)

client = OpenAI(api_key=settings.OPENAI_API_KEY)

def generate_expense_summary(message: str, user_id: int) -> ExpenseEvent | None:
    try:
        logger.info("Calling LLM for userId=%s, model=%s", user_id, settings.MODEL_NAME)
        prompt = (
        f"Extract the expense details from the following message and return it in JSON format "
        f"with keys: amount, merchant, currency, created_at. "
        f"If the date or time is not mentioned in the message, set created_at to null. "
        f"Message: {message}"
        )
        response = client.beta.chat.completions.parse(
            model=settings.MODEL_NAME,
            messages=[{"role": "user", "content": prompt}],
            response_format=ExpenseEvent
        )
        parsed: ExpenseEvent = response.choices[0].message.parsed
        parsed.user_id = user_id
        logger.info("LLM response: amount=%s, merchant=%s, currency=%s", parsed.amount, parsed.merchant, parsed.currency)
        return parsed

    except Exception as e:
        logger.error("LLM call failed for userId=%s: %s", user_id, str(e))
        return None