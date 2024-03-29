from fastapi import APIRouter, HTTPException
from config.db import assets_collection, performance_metrics_collection, maintenance_costs_collection
from schemas.assets import assetEntity

router = APIRouter()

# CALCULATION OF AVERAGE DOWNTIME


@router.get('/average-downtime')
async def calculate_average_downtime():
    try:
        total_downtime = sum(asset['downtime']
                             for asset in performance_metrics_collection.find())
        total_assets = performance_metrics_collection.count_documents({})
        average_downtime = total_downtime / total_assets if total_assets != 0 else 0
        return {"average_downtime": average_downtime}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# CALCULATION OF AVERAGE UPTIME


@router.get('/average-uptime')
async def calculate_average_uptime():
    try:
        total_uptime = sum(asset['Uptime']
                           for asset in performance_metrics_collection.find())
        total_assets = performance_metrics_collection.count_documents({})
        average_uptime = total_uptime / total_assets if total_assets != 0 else 0
        return {"average_uptime": average_uptime}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# CALCULATION OF TOTAL MAINTENANCE COSTS


@router.get('/total-maintenance-costs')
async def calculate_total_maintenance_costs():
    try:
        total_maintenance_costs = sum(
            asset['maintenance_cost'] for asset in maintenance_costs_collection.find())
        return {"total_maintenance_costs": total_maintenance_costs}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# CALCULATION OF ASSETS HAVING FAILURE RATE MORE THAN THRESHOLD


@router.get('/high-failure-rate-assets')
async def find_high_failure_rate_assets(threshold: float = 0.1):
    try:
        high_failure_rate_assets = []
        for asset in performance_metrics_collection.find():
            if asset['failure_rate'] > threshold:
                asset_data = assets_collection.find_one(
                    {'_id': asset['asset_id']})
                high_failure_rate_assets.append(assetEntity(asset_data))
        return {"high_failure_rate_assets": high_failure_rate_assets}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
