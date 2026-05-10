from pydantic import BaseModel


class Asset(BaseModel):
    assetid: str
    assetname: str
    asstetype: str
    assetlocation: str
    Purchasedate: str
    initialcost: str
    operationalstatus: str
