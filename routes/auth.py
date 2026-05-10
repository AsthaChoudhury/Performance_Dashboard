from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from jose import JWTError, jwt
from datetime import datetime, timedelta
from typing import Optional
from models.auth import User
import logging
from schemas.auth import fake_users_db, pwd_context, SECRET_KEY, ALGORITHM, oauth2_scheme, ACCESS_TOKEN_EXPIRE_MINUTES, get_current_user, create_access_token, authenticate_user
logger = logging.getLogger(__name__)
router = APIRouter()


# THE TOKEN IS AUTHENTICATION


@router.post("/token")
async def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends()):
    try:
        user = authenticate_user(form_data.username, form_data.password)
        if not user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Incorrect username or password",
                headers={"WWW-Authenticate": "Bearer"},
            )
        access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
        access_token = create_access_token(
            data={"sub": user["email"]}, expires_delta=access_token_expires
        )
        logger.info("Generated access token for: ", form_data.username)
        return {"access_token": access_token, "token_type": "bearer"}
    except Exception as e:
        logger.error(e)
        raise HTTPException(status_code=500, detail=str(e))

# THE PROTECTED ROUTE IS ENABLED


@router.get("/protected")
async def protected_route(current_user: User = Depends(get_current_user)):
    try:
        return {"message": "Protected route"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
