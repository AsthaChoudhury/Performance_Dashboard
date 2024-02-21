def performance_metric_entity(item) -> dict:
    return {
        "uptime": item["uptime"],
        "downtime": item["downtime"],
        "efficiency": item["efficiency"],
        "maintenance_costs": item["maintenance_costs"],
        "failure_rate": item["failure_rate"]
    }


def performance_metrics_entity(entity) -> list:
    return [performance_metric_entity(item) for item in entity]
