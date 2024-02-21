from pymongo import MongoClient
client = MongoClient(
    'mongodb+srv://asthachoudhury0910:asthaaaaa@practise.gkhd6ip.mongodb.net/')
db = client['asset_performance']
assets_collection = db['assets']
performance_metrics_collection = db['performance_metrics']
auth_collection = db['authentication']
