# Come leggere e modificare il progetto

Il codice completo è nei file, non in frammenti da ricomporre. Questa è la sequenza
con cui studiarlo e ricostruire le decisioni, un file alla volta.

1. **pom.xml root**: Java e versioni comuni; i moduli vengono compilati insieme.
   Il BOM Spring Cloud coordina le versioni del gateway con Spring Boot.
2. **pom.xml di ciascun servizio**: dipendenze realmente usate. Solo gateway usa
   WebFlux; gli altri servizi usano MVC/JPA con chiamate bloccanti.
3. **Application.java**: avvio del processo Spring Boot; nessuna business logic.
4. **application.properties**: porte, URL, credenziali e chiavi dall'ambiente.
   Le variabili obbligatorie senza default fanno fallire un avvio mal configurato.
5. **V1__*.sql**: schema iniziale gestito da Flyway. Non modificare una migrazione
   già applicata: crea V2, V3... e considera compatibilità tra release.
6. **Entity e Repository**: persistenza interna. Le Entity non sono le risposte REST.
7. **SecurityConfig**: firma RSA e validatori JWT; health pubblico, API protette.
8. **TokenConfig, BootstrapAccount, AuthController**: chiavi, account iniziale,
   verifica password e token. Il bootstrap usa INSERT ON CONFLICT per poter essere
   rieseguito; non resetta la password a ogni restart.
9. **CustomerController**: DTO, normalizzazione email, inserimento, lettura e lista.
   Il vincolo UNIQUE del DB protegge anche da richieste concorrenti.
10. **CustomerClient**: REST con timeout e propagazione del token e request ID.
    404 remoto significa cliente inesistente (422); indisponibilità remota dà 503.
11. **OrderController**: valida input, verifica cliente e poi salva l'ordine.
    Non teniamo una transazione DB aperta mentre aspettiamo la rete.
12. **ApiErrors**: risposte ProblemDetail uniformi, senza stack trace pubblici.
    I 401/403 della security sono gestiti da Spring Security prima del controller.
13. **RequestLogging**: log di accesso, durata e correlazione, senza credenziali.
    Il gateway genera o valida X-Request-ID e lo propaga; i servizi lo conservano.
14. **Dockerfile**: build Maven multi-stage; JRE finale e processo non-root.
15. **compose.yaml**: DNS dei servizi, dipendenze di avvio, volume PostgreSQL,
    segreti montati come file, healthcheck, limiti CPU/RAM.
16. **Test**: verificano comportamenti osservabili; i mock delimitano cosa non viene
    provato dai test Java. Lo smoke test reale chiude il percorso tra i componenti.
17. **ci.yml**: build e test prima delle immagini pubblicabili.

Non aggiungiamo un livello Service che faccia solo da passaggio al Repository.
Quando compaiono regole di dominio o operazioni transazionali composte, estrarremo
quel comportamento in classi dedicate. CustomerClient è già separato perché rete,
timeout e traduzione degli errori costituiscono una responsabilità distinta.

## Primo esercizio guidato (nessun incidente)

Avvia Compose, esegui lo smoke test, poi apri `docker compose logs order-service`.
Trova la richiesta di creazione ordine e il relativo requestId. Cerca lo stesso
valore nei log del customer. Spiega quale chiamata è partita per prima e perché.
Questa è verifica della baseline: gli incidenti cominceranno solo successivamente.
