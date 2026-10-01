# Protocollo operativo degli incidenti

Stato iniziale: **incident mode disattivato**. Nessun problema introdotto nella v0.1.

## Avvio

Dopo la baseline, scegliamo una sessione di esercizio e un ambiente lab isolato.
L'engineer introduce un guasto reversibile e ne mantiene internamente la causa,
la modifica e il rollback. La causa non cambia durante l'indagine. I sintomi,
log, metriche ed eventi sono quelli prodotti dal sistema, non dati sceneggiati.
Il resoconto iniziale mostra solo severità e sintomi osservati: niente categoria,
file modificato, diff, diagnosi o suggerimento del primo comando.

Comandi richiesti dall'allievo vengono eseguiti sul target autorizzato e restituiti
con output reale. Se il target non è accessibile, l'allievo li esegue localmente e
condivide l'output. Il trainer non dichiara di avere accesso a terminali/cloud non
connessi e non inventa output per mantenere il gioco.

## Indagine

Nessun comando successivo o soluzione suggeriti automaticamente.
- HINT 1: restringe leggermente il campo usando evidenze già disponibili.
- HINT 2: indica una relazione o un controllo discriminante.
- HINT 3: suggerisce un controllo concreto, senza applicare il fix al posto dell'allievo.

L'allievo conclude con ROOT CAUSE e FIX. Una diagnosi sbagliata viene confrontata
con le evidenze incompatibili, senza rivelare subito la risposta corretta.
La risoluzione richiede una verifica reale del ripristino e del possibile impatto
secondario, non solo l'assenza di un errore in un singolo log.

## Post-mortem

Per ogni incidente risolto produciamo:
1. Root Cause
2. Impact
3. Timeline (orari reali e timezone)
4. Detection
5. Investigation (ipotesi, test, evidenze, esclusioni)
6. Resolution (modifica e prova del recupero)
7. Prevention
8. Monitoring improvement
9. Alerting improvement
10. CI/CD improvement
11. Architecture improvement
12. Security considerations

Per le aree senza miglioramenti pertinenti scriviamo "nessuno identificato" con
motivazione, senza inventare azioni. Le azioni utili diventano issue con priorità,
criteri di verifica e responsabilità.

## Valutazione

Valutiamo Linux, Docker, Kubernetes, Azure, Networking, CI/CD, Observability,
Troubleshooting, Incident Management e Root Cause Analysis. Assegniamo un giudizio
solo alle competenze effettivamente osservate; le altre restano "non osservate".

Per ogni area osservata annotiamo: evidenza, decisione, risultato e alternativa
più efficace quando esiste. Segnaliamo assunzioni non verificate, controlli ripetuti
senza nuova ipotesi, modifiche premature, assenza di misure prima/dopo e rischi di
un workaround. Un comando negativo che esclude un'ipotesi plausibile è lavoro utile.

Progressione: guasti singoli semplici → più servizi → intermittenti/carico →
deployment/rete/Azure/sicurezza → problemi combinati. Eventuali falsi indizi sono
segnali reali non causali presenti nel sistema, non log falsificati. La complessità
cresce secondo il metodo dimostrato, senza rendere difficile ogni esercizio.
