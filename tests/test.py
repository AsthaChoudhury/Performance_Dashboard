import pytest
from fastapi.testclient import TestClient
from index import app
from models.assets import Asset
from models.performance import performance
from models.auth import User

client = TestClient(app)


def readmain():
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {
        "message": "Welcome to Asset Performance Analytics Dashboard!"}

# test for assets


def createasset():
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


def createperformance():
    performance_data = {

        "uptime": 8.9,
        "downtime": 11.0,
        "efficiency": 56.5,
        "maintenance_costs": 1980.765,
        "failure_rate": 4.5
    }
    response = client.post("/performance/", json=performance_data)
    assert response.status_code == 200
    assert "performance_id" in response.json()

# authentication test


def login():
    login_data = {
        "email": "user@example.com",
        "hashed_password": "$2b$12$yiX5kJZb0/OiTXcZx1jyZu7gg3QHGXKmqVlFvJ5vI4JidZ0VwGH3W",
        "full_name": "user user",
        "disabled": False
    }
    response = client.post("/auth/token", data=login_data)
    assert response.status_code == 200
    assert "access_token" in response.json()
