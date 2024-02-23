from fastapi import APIRouter, HTTPException
from config.db import performance_metrics_collection
from models.performance import performance
from schemas.performance import performance_metric_entity, performance_metrics_entity
from datetime import datetime
router = APIRouter()

# GET ALL THE PERFORMANCE_METRICS


@router.get('/')
async def find_all_performance_metrics():
    try:
        performance_metrics = performance_metrics_collection.find()
        return performance_metrics_entity(performance_metrics)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# CREATE PERFORMANCE_METRICS


@router.post('/')
async def create_performance_metric(performance_metric: performance):
    try:
        if performance_metric.downtime_start and performance_metric.downtime_end:
            start_time = datetime.strptime(
                performance_metric.downtime_start, "%Y-%m-%d %H:%M:%S")
            end_time = datetime.strptime(
                performance_metric.downtime_end, "%Y-%m-%d %H:%M:%S")
            # Convert seconds to hours
            downtime_duration = (end_time - start_time).total_seconds() / 3600
            performance_metric.downtime = downtime_duration

            # uptime calculation
            total_uptime = performance_metric.total_time - performance_metric.downtime
            performance_metric.Uptime = total_uptime
        inserted_performance_metric = performance_metrics_collection.insert_one(
            performance_metric.dict())
        return {'performance_metric_id': str(inserted_performance_metric.inserted_id)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# GET ANY PARTICULAR METRIC BY ID


@router.get('/{performance_metric_id}')
async def find_performance_metric_by_id(performance_metric_id: str):
    try:
        performance_metric = performance_metrics_collection.find_one(
            {'_id': performance_metric_id})
        if performance_metric:
            return performance_metric_entity(performance_metric)
        raise HTTPException(
            status_code=404, detail="Performance metric not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# UPDATE ANY EXISTING METRIC BY ID


@router.put('/{performance_metric_id}')
async def update_performance_metric(performance_metric_id: str, performance_metric: performance):
    try:
        updated_performance_metric = performance_metrics_collection.replace_one(
            {'_id': performance_metric_id}, performance_metric.dict())
        if updated_performance_metric.modified_count == 1:
            return {"message": "Performance metric updated successfully"}
        raise HTTPException(
            status_code=404, detail="Performance metric not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# DELETE ANY EXISTING METRIC


@router.delete('/{performance_metric_id}')
async def delete_performance_metric(performance_metric_id: str):
    try:
        deleted_performance_metric = performance_metrics_collection.delete_one(
            {'_id': performance_metric_id})
        if deleted_performance_metric.deleted_count == 1:
            return {"message": "Performance metric deleted successfully"}
        raise HTTPException(
            status_code=404, detail="Performance metric not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
