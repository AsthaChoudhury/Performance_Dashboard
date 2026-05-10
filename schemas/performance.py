def performance_metric_entity(item) -> dict:
    return {
        "uptime": item["uptime"],
        "downtime": item["downtime"],
        "downtime_start": item["downtime_start"],
        "downtime_end": item["downtime_end"],
        "downtime_reason": item["downtime_reason"],
        "efficiency": item["efficiency"],
        "failure_rate": item["failure_rate"],
        "total_time": item["total_time"]
    }


def performance_metrics_entity(entity) -> list:
    return [performance_metric_entity(item) for item in entity]
