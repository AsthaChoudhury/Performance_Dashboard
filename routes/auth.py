from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from jose import JWTError, jwt
from datetime import datetime, timedelta
from typing import Optional
from models.auth import User
from schemas.auth import fake_users_db, pwd_context, SECRET_KEY, ALGORITHM, oauth2_scheme, ACCESS_TOKEN_EXPIRE_MINUTES

router = APIRouter()


def verify_password(normalpassword, hashedpassword):
    try:
        return pwd_context.verify(normalpassword, hashedpassword)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def authenticate_user(email: str, password: str):
    try:
        user = fake_users_db.get(email)
        if not user:
            return False
        if not verify_password(password, user["hashed_password"]):
            return False
        return user
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def create_access_token(data: dict, expires_delta: timedelta = None):
    try:
        to_encode = data.copy()
        if expires_delta:
            expire = datetime.utcnow() + expires_delta
        else:
            expire = datetime.utcnow() + timedelta(minutes=15)
        to_encode.update({"exp": expire})
        encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
        return encoded_jwt
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def get_current_user(token: str = Depends(oauth2_scheme)):
    try:
        credentials_exception = HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid authentication credentials",
            headers={"WWW-Authenticate": "Bearer"},
        )
        try:
            payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
            email: str = payload.get("sub")
            if email is None:
                raise credentials_exception
        except JWTError:
            raise credentials_exception
        user = fake_users_db.get(email)
        if user is None:
            raise credentials_exception
        return user
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


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
        return {"access_token": access_token, "token_type": "bearer"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/protected")
async def protected_route(current_user: User = Depends(get_current_user)):
    try:
        return {"message": "Protected route"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
