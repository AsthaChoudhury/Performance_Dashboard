from fastapi import HTTPException, status, Depends
from datetime import datetime, timedelta
from jose import JWTError, jwt
from passlib.context import CryptContext
from fastapi.security import OAuth2PasswordBearer
from typing import Optional
SECRET_KEY = "your-secret-key"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")
fake_users_db = {
    "user@example.com": {
        "email": "user@example.com",
        # bcrypt hash of "password"
        "hashed_password": "$2b$12$yiX5kJZb0/OiTXcZx1jyZu7gg3QHGXKmqVlFvJ5vI4JidZ0VwGH3W",
        "full_name": "User",
        "disabled": False,
    }
}


# VERIFYING PASSWORD(PASSWORD AND HASHED PASSWORD COMPARISON)


def verify_password(normalpassword, hashedpassword):
    try:
        return pwd_context.verify(normalpassword, hashedpassword)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# USER-AUTHENTICATION


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

# THE TOKEN IS GENERATED HERE


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

# GET THE DETAILS OF THE CURRENT USER


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
