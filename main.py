from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
import mysql.connector
from decimal import Decimal
from datetime import datetime, date

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "root",
    "database": "kharcha",
}

def serialize(val):
    if isinstance(val, Decimal):
        return float(val)
    if isinstance(val, (datetime, date)):
        return str(val)
    return val

def run_query(sql):
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor(dictionary=True)
        cursor.execute(sql)
        rows = cursor.fetchall()
        cursor.close()
        conn.close()
        return [{k: serialize(v) for k, v in row.items()} for row in rows]
    except mysql.connector.Error as e:
        raise Exception(f"MySQL Error: {e}")

def table_exists(table_name):
    try:
        result = run_query(f"""
            SELECT COUNT(*) as cnt 
            FROM information_schema.tables 
            WHERE table_schema = 'kharcha' 
            AND table_name = '{table_name}'
        """)
        return result[0]["cnt"] > 0
    except:
        return False

@app.get("/api/roles")
def get_roles():
    if not table_exists("roles"):
        return []
    return run_query("SELECT * FROM roles ORDER BY role_id")

@app.get("/api/users")
def get_users():
    if not table_exists("users_info"):
        return []
    return run_query("SELECT user_id, name, email, user_name FROM users_info ORDER BY user_id")

@app.get("/api/user-roles")
def get_user_roles():
    if not table_exists("user_roles") or not table_exists("users_info"):
        return []
    return run_query("""
        SELECT 
            u.user_id,
            u.user_name,
            u.name       AS full_name,
            u.email,
            r.role_id,
            r.role_name
        FROM users_info u
        JOIN user_roles ur ON u.user_id = ur.user_id
        JOIN roles r       ON ur.role_id = r.role_id
        ORDER BY u.user_id
    """)

@app.get("/api/tokens")
def get_tokens():
    if not table_exists("tokens"):
        return []
    return run_query("""
        SELECT 
            t.id,
            CONCAT(LEFT(t.token, 20), '...') AS token_preview,
            t.expiry_date,
            u.user_name,
            u.email
        FROM tokens t
        JOIN users_info u ON t.user_id = u.user_id
        ORDER BY t.id DESC
    """)

@app.get("/api/userservice-users")
def get_userservice_users():
    if not table_exists("users"):
        return []
    return run_query("SELECT user_id, name, user_name, email FROM users ORDER BY user_id")

@app.get("/api/expenses")
def get_expenses():
    if not table_exists("expense_info"):
        return []
    return run_query("""
        SELECT 
            id, user_id, amount, merchant, currency,
            description, source, category,
            expense_date, created_at, updated_at
        FROM expense_info
        ORDER BY created_at DESC
    """)

@app.get("/api/stats")
def get_stats():
    def safe_count(table, where=""):
        if not table_exists(table):
            return 0
        q = f"SELECT COUNT(*) AS cnt FROM {table}"
        if where:
            q += f" WHERE {where}"
        return run_query(q)[0]["cnt"]

    def safe_sum(table, col):
        if not table_exists(table):
            return 0
        return run_query(f"SELECT COALESCE(SUM({col}), 0) AS total FROM {table}")[0]["total"]

    return {
        "auth_users":      safe_count("users_info"),
        "service_users":   safe_count("users"),
        "roles":           safe_count("roles"),
        "tokens":          safe_count("tokens"),
        "expenses":        safe_count("expense_info"),
        "total_spent":     safe_sum("expense_info", "amount"),
        "ai_expenses":     safe_count("expense_info", "source='AI'"),
        "manual_expenses": safe_count("expense_info", "source='YOU'"),
    }

@app.get("/")
def index():
    return FileResponse("index.html")