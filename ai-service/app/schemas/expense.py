from pydantic import BaseModel, Field, field_serializer, model_validator
from datetime import datetime

class ExpenseEvent(BaseModel):
    amount: float = Field(..., description="Transaction amount")
    merchant: str = Field(..., description="Merchant name")
    currency: str = Field(..., description="Currency code like INR")
    created_at: datetime | None = Field(None, description="Transaction timestamp, null if not mentioned")
    user_id: int | None = Field(None, description="User ID")

    @model_validator(mode="after")
    def set_default_date(self) -> "ExpenseEvent":
        if self.created_at is None:
            self.created_at = datetime.now()
        return self

    @field_serializer('created_at')
    def serialize_dt(self, v: datetime) -> str:
        return v.isoformat()