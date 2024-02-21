def assetEntity(item) -> dict:
    return {
        "assetid": str(item["_id"]),
        "assetname": item["assetname"],
        "assettype": item["assettype"],
        "assetlocation": item["assetlocation"],
        "purchasedate": item["purchasedate"],
        "initialcost": float(item["initialcost"]),
        "operationalstatus": item["operationalstatus"]
    }


def assetsEntity(entity) -> list:
    return [assetEntity(item) for item in entity]
