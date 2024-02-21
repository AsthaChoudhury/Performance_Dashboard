from pydantic import BaseModel
from typing import Optional


class User(BaseModel):
    email: str
    hashed_password: str
    full_name: Optional[str] = None
    disabled: Optional[bool] = None
