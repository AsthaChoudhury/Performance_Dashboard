from pydantic import BaseModel


class Maintenance(BaseModel):
    maintenance_date: str
    maintenance_price: float
