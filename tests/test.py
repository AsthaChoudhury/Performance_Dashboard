import pytest
from fastapi.testclient import TestClient
from index import app
from models.assets import Asset
from models.performance import performance
from models.auth import User
from models.maintenance import maintenance

client = TestClient(app)


def test_readmain():
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {
        "message": "Welcome to Asset Performance Analytics Dashboard!"}

# test for assets


def test_createasset():
    asset_data = {
        "assetid": "123",
        "assetname": "Asset 1",
        "assettype": "Type 1",
        "assetlocation": "Location 1",
        "purchasedate": "2023-01-01",
        "initialcost": 1000.0,
        "operationalstatus": "Active"
    }
    response = client.post("/assets/", json=asset_data)
    assert response.status_code == 200
    assert "asset_id" in response.json()

# test for performance_metrics


def test_createperformance():
    performance_data = {

        "uptime": 8.9,
        "downtime": 11.0,
        "efficiency": 56.5,
        "failure_rate": 4.5,
        "downtime_start": "2024-02-22 08:00:00",
        "downtime_end": "2024-02-22 09:00:00",
        "downtime_reason": "Scheduled maintenance"
    }
    response = client.post("/performance/", json=performance_data)
    assert response.status_code == 200
    assert "performance_id" in response.json()

# test for maintenance cost


def test_maintenance_cost():
    maintenance_cost_data = {
        "maintenance_date": "09-10-2004",
        "maintenance_price": 89348.0
    }
    response = client.post("/maintenance", data=maintenance_cost_data)
    assert response.status_code == 200
    assert "access_token" in response.json()

# login test


def test_login():
    login_data = {
        "username": "user@example.com",
        "password": "password"
    }
    response = client.post("/auth/token", data=login_data)
    assert response.status_code == 200
    assert "access_token" in response.json()


def test_protected_route():
    # access the token
    login_data = {
        "username": "user@example.com",
        "password": "password"
    }
    login_response = client.post("/auth/token", data=login_data)
    assert login_response.status_code == 200
    access_token = login_response.json()["access_token"]

    #  protected route
    headers = {"Authorization": f"Bearer {access_token}"}
    response = client.get("/auth/protected", headers=headers)
    assert response.status_code == 200
    assert response.json() == {"message": "Protected route"}
