from pydantic import BaseModel

# defining performance_metrics variables


class performance(BaseModel):
    Uptime: float
    downtime: float
    Efficiency: float
    failurerate: float
    downtime_start: str
    downtime_end: str
    downtime_reason: str
    total_time: float
