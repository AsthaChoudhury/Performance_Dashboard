from fastapi import APIRouter, HTTPException
from config.db import maintenance_costs_collection
from models.maintenance import Maintenance
from datetime import datetime
from schemas.maintenance import maintenance_cost_entity, maintenance_costs_entity

router = APIRouter()

# GET ALL THE MAINTENANCE


@router.get('/')
async def find_all_maintenance_costs():
    try:
        maintenance_costs = maintenance_costs_collection.find()
        return maintenance_costs_entity(maintenance_costs)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# CREATE PERFORMANCE_METRICS


@router.post('/')
async def create_maintenance_cost(maintenance_cost: Maintenance):
    try:
        inserted_maintenance_cost = maintenance_costs_collection.insert_one(
            maintenance_cost.dict())
        return {'maintenance_cost_id': str(inserted_maintenance_cost.inserted_id)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# GET ANY PARTICULAR METRIC BY ID


@router.get('/{maintenance_cost_id}')
async def find_performance_metric_by_id(maintenance_cost_id: str, date: datetime = None):
    try:
        maintenance_cost = maintenance_costs_collection.find_one(
            {'_id': maintenance_cost_id})
        if maintenance_cost:
            return maintenance_cost_entity(maintenance_cost)
        raise HTTPException(
            status_code=404, detail="maintenance_cost not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# GET ANY MAINTENANCE COST BY DATE


@router.get('/{date}')
async def find_maintenance_by_date(date: str):
    try:
        maintenance = maintenance_costs_collection.find_one({'date': date})
        if maintenance:
            return maintenance_cost_entity(maintenance)
        raise HTTPException(
            status_code=404, detail="Maintenance record not found for the given date")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# UPDATE ANY EXISTING METRIC BY ID


@router.put('/{maintenance_cost_id}')
async def update_maintenance_cost(maintenance_cost_id: str, maintenance_cost: Maintenance):
    try:
        updated_maintenance_cost = maintenance_costs_collection.replace_one(
            {'_id': maintenance_cost_id}, maintenance_cost.dict())
        if updated_maintenance_cost.modified_count == 1:
            return {"message": "maintenance_cost updated successfully"}
        raise HTTPException(
            status_code=404, detail="maintenance_cost not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# UPDATE MAINTENANCE RECORD BY DATE
@router.put('/{date}')
async def update_maintenance_cost_by_date(date: str, maintenance_cost: Maintenance):
    try:
        updated_maintenance_cost = maintenance_costs_collection.update_one(
            {'date': date}, {'$set': maintenance_cost.dict()})
        if updated_maintenance_cost.modified_count == 1:
            return {"message": "updated "}
        raise HTTPException(
            status_code=404, detail="Maintenance record not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# DELETE ANY EXISTING METRIC


@router.delete('/{maintenance_cost_id}')
async def delete_maintenance_cost(maintenance_cost_id: str):
    try:
        deleted_maintenance_cost = maintenance_costs_collection.delete_one(
            {'_id': maintenance_cost_id})
        if deleted_maintenance_cost.deleted_count == 1:
            return {"message": "maintenance_cost deleted successfully"}
        raise HTTPException(
            status_code=404, detail="maintenance_cost not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# DELETE MAINTENANCE BY DATE
@router.delete('/{date}')
async def delete_maintenance_by_date(date: str):
    try:
        deleted_maintenance_cost = maintenance_costs_collection.delete_one({
                                                                           'date': date})
        if deleted_maintenance_cost.deleted_count == 1:
            return {"message": "Maintenance record deleted successfully"}
        raise HTTPException(
            status_code=404, detail="Maintenance record not found for the given date")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
