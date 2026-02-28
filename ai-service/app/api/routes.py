import logging
from fastapi import APIRouter, Header, HTTPException
from pydantic import BaseModel
from app.services.llm_service import generate_expense_summary
from app.services.kafka_producer import publish_event

logger = logging.getLogger(__name__)

router = APIRouter()

class SMSRequest(BaseModel):
    message: str

@router.post("/analyze")
def analyze_sms(request: SMSRequest, user_id: int = Header(...)):
    logger.info("Analyze request: userId=%s, message='%s'", user_id, request.message[:80])
    try:
        expense = generate_expense_summary(request.message, user_id)
        if expense is None:
            logger.error("LLM returned None for userId=%s", user_id)
            raise ValueError("Failed to generate expense summary from message")
        logger.info("LLM parsed: userId=%s, merchant=%s, amount=%s", user_id, expense.merchant, expense.amount)
        publish_event(expense.model_dump())
        logger.info("Expense event published to Kafka for userId=%s", user_id)
        return {
            "status": "success",
            "data": expense
        }
    except Exception as e:
        logger.error("Analyze failed for userId=%s: %s", user_id, str(e))
        raise HTTPException(status_code=500, detail=str(e))