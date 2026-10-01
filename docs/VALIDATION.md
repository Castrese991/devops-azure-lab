# Verifiche della versione v0.1

Data: 30 settembre 2026. Ambiente di preparazione Linux, OpenJDK 17, Maven 3.9.12.
Build finale: `mvn -o -B -ntp verify`, dopo aver scaricato le dipendenze.
Risultato: **BUILD SUCCESS**, quattro JAR Spring Boot prodotti.

| Suite | Test | Failure | Error | Skipped |
|---|---:|---:|---:|---:|
| it.castrese.lab.api.GatewayTest | 3 | 0 | 0 | 0 |
| it.castrese.lab.auth.AuthApiTest | 5 | 0 | 0 | 0 |
| it.castrese.lab.customer.CustomerApiTest | 5 | 0 | 0 | 0 |
| it.castrese.lab.order.CustomerClientTest | 4 | 0 | 0 | 0 |
| it.castrese.lab.order.OrderApiTest | 4 | 0 | 0 | 0 |

Totale: **21 test passati**, zero failure/error/skipped.

Verificato:
- routing gateway su server HTTP e preservazione Authorization;
- login/password, firma RSA reale e validazione issuer/audience/scadenza;
- API clienti, normalizzazione email, vincolo UNIQUE e persistenza H2;
- API ordini, salvataggio dopo verifica cliente e nessun salvataggio su guasto;
- client REST contro server HTTP reale, con 404, 500 e connessione fallita;
- migrazioni Flyway customer/order e validazione Hibernate su H2;
- sintassi Bash/Python, parsing YAML;
- generazione locale chiavi/credenziali, permessi e protezione dal sovrascrivere setup esistente.

Limiti:
- Docker non disponibile nell'ambiente di preparazione: immagini e Compose non eseguiti qui.
- PostgreSQL reale, bootstrap account e migrazioni su PostgreSQL da verificare con
  `python3 scripts/smoke-test.py` dopo l'avvio Compose.
- I test API usano un decoder JWT mock dove indicato; la firma RSA reale viene
  verificata nella suite auth. Lo smoke test usa invece un token vero attraverso tutto lo stack.
- La pipeline GitHub è predisposta ma non eseguita su GitHub; nessun push effettuato.
- Nessun deployment Kubernetes/Azure eseguito, nessun incidente inserito.

I test usano il mock maker subclass: non richiedono self-attachment di agent alla JVM.
Le classi finali non sono oggetto di mock. H2 è una dipendenza esclusivamente di test.
