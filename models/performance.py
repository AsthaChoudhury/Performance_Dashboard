from pydantic import BaseModel


class performance(BaseModel):
    Uptime: float
    Downtime: float
    Efficiency: float
    maintenancecosts: float
    failurerate: float
