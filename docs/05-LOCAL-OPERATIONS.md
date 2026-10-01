# Gestione del laboratorio locale

## Baseline
- Prima versione: v0.1.0.
- Gateway: http://localhost:8088.
- Quattro microservizi e PostgreSQL.
- Build, test Java e smoke test verificati localmente.
- Pipeline GitHub Actions verificata sul push a main.

## Avvio con immagini già presenti
docker compose up -d --no-build --pull never --wait --wait-timeout 240

## Stato dei container
docker compose ps -a

## Verifica applicativa
BASE_URL=http://127.0.0.1:8088 python3 scripts/smoke-test.py

Lo smoke test crea un cliente e un ordine di laboratorio.
Il solo stato healthy non verifica tutte le operazioni applicative.

## Consultazione dei log
docker compose logs --no-color --tail=100 order-service
docker compose logs --no-color --tail=100 customer-service

## Arresto conservando container e dati
docker compose stop

## Note sul primo avvio
La porta host 8080 era occupata: il gateway è stato pubblicato sulla 8088.
La porta interna dei servizi rimane 8080.

Nel runtime locale, no-new-privileges:true impediva l'esecuzione di Java.
L'opzione è stata rimossa; restano utente non-root e cap_drop: ALL.
L'incompatibilità va approfondita prima del deployment Azure.

Non usare docker compose down -v per un normale arresto:
elimina anche il volume PostgreSQL e i dati del laboratorio.
