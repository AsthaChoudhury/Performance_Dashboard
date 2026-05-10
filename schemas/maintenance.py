def maintenance_cost_entity(item) -> dict:
    return {
        "maintenance_date": item["maintenance_date"],
        "maintenance_price": item["maintenance_price"]
    }


def maintenance_costs_entity(entity) -> list:
    return [maintenance_cost_entity(item) for item in entity]
