from fastapi import APIRouter, HTTPException
from config.db import assets_collection
from models.assets import Asset
from schemas.assets import assetEntity, assetsEntity

router = APIRouter()

# GET ALL THE ASSETS


@router.get('/')
async def find_assets():
    try:
        assets = assets_collection.find()
        return assetsEntity(assets)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# CREATE ASSETS


@router.post('/')
async def create_asset(asset: Asset):
    try:
        inserted_asset = assets_collection.insert_one(asset.dict())
        return {'asset_id': str(inserted_asset.inserted_id)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# GET ANY PARTICULAR ASSETS


@router.get('/{asset_id}')
async def find_asset_by_id(asset_id: str):
    try:
        asset = assets_collection.find_one({'_id': asset_id})
        if asset:
            return assetEntity(asset)
        raise HTTPException(status_code=404, detail="Asset not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# UPDATE ANY EXISTING ASSET


@router.put('/{asset_id}')
async def update_asset(asset_id: str, asset: Asset):
    try:
        updated_asset = assets_collection.replace_one(
            {'_id': asset_id}, asset.dict())
        if updated_asset.modified_count == 1:
            return {"message": "Updated successfully"}
        raise HTTPException(status_code=404, detail="Asset not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# DELETE ANY EXISTING ASSET


@router.delete('/{asset_id}')
async def delete_asset(asset_id: str):
    try:
        deleted_asset = assets_collection.delete_one({'_id': asset_id})
        if deleted_asset.deleted_count == 1:
            return {"message": "Ddeleted successfully"}
        raise HTTPException(status_code=404, detail="Asset not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
