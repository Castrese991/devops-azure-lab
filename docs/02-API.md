# API v0.1

Tutte le chiamate passano da `http://localhost:8080`.

| Metodo e path | Input / comportamento | Risultato |
|---|---|---|
| POST /api/auth/login | username, password | 200: accessToken, tokenType, expiresIn |
| POST /api/customers | name, email | 201: cliente e header Location |
| GET /api/customers/{id} | ID numerico | 200 oppure 404 |
| GET /api/customers?page=0&size=20 | lista, massimo 100 elementi | 200: array |
| POST /api/orders | customerId, description, amount | 201: ordine e Location |
| GET /api/orders/{id} | ID numerico | 200 oppure 404 |
| GET /api/orders?page=0&size=20 | lista, massimo 100 elementi | 200: array |
| GET /actuator/health/readiness | readiness gateway | 200 oppure 503 |

Le API cliente/ordine richiedono `Authorization: Bearer <token>` con scope `lab`.
Errori: 400 input non valido, 401 credenziali/token invalidi, 403 scope insufficiente,
404 risorsa assente, 409 email duplicata, 422 cliente ordine inesistente,
503 customer non disponibile durante creazione ordine.

## Esecuzione manuale con curl

Dalla root del progetto, su Bash. Le credenziali generate hanno formato shell-safe.
Non usare `set -x`: stamperebbe i segreti nei log del terminale.

```bash
set -a
source .env
set +a

LOGIN_JSON=$(python3 -c 'import os,json; print(json.dumps({"username":os.environ["BOOTSTRAP_USERNAME"],"password":os.environ["BOOTSTRAP_PASSWORD"]}))')
TOKEN=$(printf '%s' "$LOGIN_JSON" | curl --fail-with-body -sS \
  -H 'Content-Type: application/json' --data-binary @- \
  http://localhost:8080/api/auth/login \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["accessToken"])')
unset LOGIN_JSON

curl -i -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Mario Rossi","email":"mario.rossi@example.com"}' \
  http://localhost:8080/api/customers
```

Prendi l'ID della risposta (non supporre che sia sempre 1), quindi:

```bash
CUSTOMER_ID=1 # sostituisci con il valore realmente ricevuto
curl -i -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"customerId\":$CUSTOMER_ID,\"description\":\"Ordine di prova\",\"amount\":49.90}" \
  http://localhost:8080/api/orders

curl -i -H "Authorization: Bearer $TOKEN" \
  'http://localhost:8080/api/orders?page=0&size=20'
unset TOKEN BOOTSTRAP_PASSWORD
```

Non inviare veri dati personali nel laboratorio. Il token scade dopo 15 minuti:
esegui nuovamente il login. Questa versione non ha refresh token.
