from fastapi import APIRouter, FastAPI
from routes.performance import router as performance_entity_router
from routes.assets import router as asset_router
from routes.results import router as results_router
from routes.auth import router as auth_router
from routes.maintenance import router as maintenance_cost_router

app = FastAPI()

# defining all th3 routes
app.include_router(asset_router, prefix='/assets', tags=['assets'])
app.include_router(performance_entity_router,
                   prefix='/performance', tags=['performance'])
app.include_router(results_router, prefix='/results', tags=['results'])
app.include_router(auth_router, prefix='/auth', tags=['Authentication'])
app.include_router(maintenance_cost_router,
                   prefix="/maintenance", tags=["Maintenance_cost"])


@app.get("/")
async def read_root():
    return {"message": "Asset Performance Analytics Dashboard!"}
