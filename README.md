# DevOps Azure Lab — v0.1

Laboratorio reale per Castrese: Java 17, Spring Boot 3.5.16, Spring Cloud
2025.0.3, Maven, PostgreSQL 16. Quattro processi e quattro immagini indipendenti.
Nessun Kafka/RabbitMQ. Nessun incidente inserito.

## Avvio locale (Ubuntu)

Prerequisiti: Docker Engine con permesso di esecuzione, plugin Docker Compose v2
con `--wait`, OpenSSL, Python 3, curl. Consigliati almeno 6 GB di RAM disponibili
per Docker; la prima build richiede rete e tempo per scaricare le immagini.
Non serve installare Java/Maven per usare Compose.

```bash
docker --version
docker compose version
bash scripts/init-local.sh
bash scripts/test.sh
docker compose up -d --build --wait --wait-timeout 240
python3 scripts/smoke-test.py
docker compose ps
```

Lo smoke test fallisce al primo risultato inatteso; crea un cliente e un ordine
con dati di laboratorio e li lascia nel database. Può essere ripetuto.
La directory `.secrets` e `.env` vengono creati una volta e sono esclusi da Git.
Non condividere `.env`, chiavi private o token.

Il gateway è raggiungibile solo da questo computer: http://localhost:8088.
PostgreSQL e i tre servizi non espongono porte sull'host.

```bash
curl -i http://localhost:8088/actuator/health/readiness
docker compose logs --tail=100 order-service
docker compose logs --tail=100 customer-service
docker compose stop
docker compose start
```

`docker compose down` elimina i container e conserva il volume dati.
**`docker compose down -v` cancella tutti i dati del laboratorio.** Non usarlo
per risolvere genericamente un problema. Le credenziali DB vengono applicate
solo all'inizializzazione del volume; cambiare `.env` non cambia le password già
memorizzate in PostgreSQL. Anche l'utente bootstrap viene creato una sola volta.

## Architettura e repository

Monorepo Maven: root aggregatore e un modulo per servizio. Ogni servizio ha
`pom.xml`, `Dockerfile`, `src/main/java`, `src/main/resources`, `src/test`.

| Percorso | Contenuto |
|---|---|
| `api-gateway/` | Spring Cloud Gateway WebFlux, tre route, timeout, Actuator |
| `auth-service/` | Login, account PostgreSQL, BCrypt, firma JWT RSA |
| `customer-service/` | REST clienti, DTO validati, JPA, Flyway |
| `order-service/` | REST ordini, chiamata HTTP autenticata al customer, JPA |
| `infra/postgres/` | Inizializzazione tre ruoli/database |
| `scripts/` | Setup segreti locali, test, smoke test HTTP |
| `.github/workflows/ci.yml` | Build, test, Compose/PostgreSQL, immagini GHCR |
| `docs/` | API, lettura del codice, workflow e percorso Azure |

Il gateway mantiene path e header Authorization. I servizi verificano firma,
issuer, audience, scadenza e scope del token. Non fidarsi della sola presenza del
gateway. Il database auth contiene solo hash BCrypt, non password in chiaro.
Token con durata predefinita 15 minuti; l'account operatore ha scope `lab`.
Questa è una piccola applicazione per operatori interni, non un negozio multiutente:
gli operatori autorizzati possono vedere tutti i clienti e gli ordini.

L'ordine contiene customerId senza foreign key cross-service. Il customer viene
verificato prima del salvataggio; non esiste una transazione distribuita.
La v0.1 non elimina clienti, non espone aggiornamenti né pagamenti.
L'importo è un totale inserito dall'operatore, sempre EUR; non un prezzo catalogo.
Le liste hanno paginazione `page` e `size` (massimo 100), ordinate per ID.
POST ordini non è idempotente: non ripetere automaticamente richieste dopo timeout.

Tre database su una sola istanza PostgreSQL riducono le risorse locali; ogni
servizio accede al proprio database con un ruolo dedicato senza privilegi superuser.
Il ruolo amministrativo serve solo all'inizializzazione. Flyway crea le tabelle;
Hibernate valida lo schema, non lo modifica.

## Health e osservabilità

- Liveness: `/actuator/health/liveness`, verifica stato del processo.
- Readiness: `/actuator/health/readiness`, include DB nei servizi applicativi.
- La liveness non dipende dal DB per evitare restart inutili durante un guasto DB.
- La readiness di order non dipende da customer: gli ordini già salvati restano leggibili.
- Readiness gateway verifica il gateway, non certifica tutto il percorso applicativo.
- Solo health e info esposti da Actuator; niente dettagli DB pubblici.
- Log richieste: requestId, metodo, path, status, durata. Nessun body o token nei log.
- Order propaga X-Request-ID al customer per correlare la chiamata.
- Niente retry automatici dei POST; connect timeout 2 s, read timeout 3 s verso customer.

## Test e limiti di validazione

`mvn -B verify` esegue test JWT/login, API e persistenza H2, routing del gateway e
client REST contro server HTTP effettivo. H2 in modalità PostgreSQL verifica parte
delle migrazioni e della persistenza, ma **non sostituisce PostgreSQL**.
Lo smoke test Compose e la pipeline eseguono il percorso completo con PostgreSQL,
bootstrap credenziali, Flyway e JWT reali. Vedere `docs/VALIDATION.md` per i risultati
effettivamente verificati nell'ambiente di preparazione.

Il Dockerfile salta i test perché la pipeline li esegue prima; una build manuale
con `docker compose up --build` da sola non esegue test. Usare `scripts/test.sh`.

## Versioni e prossime fasi

Boot 3.5 è scelto come baseline didattica coerente con lo stack 3.x; la linea
3.5 ha terminato le release OSS. Prima di esporre il laboratorio su Azure,
aggiorneremo a una linea supportata e controlleremo dipendenze e immagini.
Le immagini base sono fissate per versione/tag ma non per digest: prima della
baseline cloud bloccheremo i digest e automatizzeremo gli aggiornamenti.

La v0.1 non è una produzione esposta a Internet: mancano TLS, gestione credenziali
centralizzata, rate limiting del login, rotazione delle chiavi, backup verificati,
tracing e alert. Questi passaggi sono parte del percorso, non funzioni già presenti.
Nessuna risorsa Azure e nessun repository remoto sono creati da questo archivio.

Inizia da `docs/01-FILE-GUIDE.md`, poi `docs/02-API.md` e `docs/03-ROADMAP.md`.
