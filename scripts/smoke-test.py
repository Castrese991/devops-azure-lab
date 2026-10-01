#!/usr/bin/env python3
"""Real HTTP smoke test, standard library only. Does not print credentials or tokens."""
import json, os, sys, time, uuid
from pathlib import Path
from urllib.request import Request, urlopen
from urllib.error import HTTPError
base=os.environ.get('BASE_URL','http://localhost:8080')
env={}
p=Path(__file__).resolve().parent.parent/'.env'
if p.exists():
    env=dict(line.split('=',1) for line in p.read_text().splitlines() if line and not line.startswith('#'))
username=os.environ.get('BOOTSTRAP_USERNAME',env.get('BOOTSTRAP_USERNAME'))
password=os.environ.get('BOOTSTRAP_PASSWORD',env.get('BOOTSTRAP_PASSWORD'))
if not username or not password: sys.exit('Run init-local.sh or provide BOOTSTRAP_USERNAME and BOOTSTRAP_PASSWORD')
def call(method,path,expected,body=None,token=None):
    headers={'Content-Type':'application/json','X-Request-ID':'smoke-'+str(uuid.uuid4())}
    if token: headers['Authorization']='Bearer '+token
    req=Request(base+path,data=json.dumps(body).encode() if body is not None else None,headers=headers,method=method)
    try:
        with urlopen(req,timeout=15) as r: status,data=r.status,r.read()
    except HTTPError as e: status,data=e.code,e.read()
    if status!=expected: raise AssertionError(f'{method} {path}: expected {expected}, got {status}; {data.decode()}')
    print(f'PASS {method} {path} -> {status}')
    return json.loads(data) if data else None
call('GET','/actuator/health/readiness',200)
call('GET','/api/customers',401)
call('GET','/api/orders',401,token='invalid-token')
call('POST','/api/auth/login',401,{'username':username,'password':'wrong-password'})
token=call('POST','/api/auth/login',200,{'username':username,'password':password})['accessToken']
call('POST','/api/customers',400,{'name':'','email':'invalid'},token)
body={'name':'Cliente laboratorio','email':f'lab-{uuid.uuid4()}@example.com'}
customer=call('POST','/api/customers',201,body,token)
call('POST','/api/customers',409,body,token)
call('GET',f'/api/customers/{customer["id"]}',200,token=token)
call('POST','/api/orders',422,{'customerId':9223372036854775807,'description':'Missing customer','amount':1},token)
call('POST','/api/orders',400,{'customerId':customer['id'],'description':'Invalid','amount':-1},token)
order=call('POST','/api/orders',201,{'customerId':customer['id'],'description':'Ordine laboratorio','amount':49.90},token)
read=call('GET',f'/api/orders/{order["id"]}',200,token=token)
assert read['customerId']==customer['id'] and read['status']=='CREATED'
call('GET','/api/orders?page=0&size=10',200,token=token)
print('SMOKE TEST PASSED. Customer and order intentionally remain in the lab database.')
