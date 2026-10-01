# Percorso locale → Azure

## Gate 1 — baseline locale

Completato solo quando build/test e smoke test PostgreSQL sono verdi sul computer
locale. Conserviamo gli output effettivi, senza inventare risultati. Controlliamo
anche riavvio dei container e persistenza dei dati.

## Gate 2 — GitHub e workflow di team

Usiamo un monorepo `devops-azure-lab`, main protetto e branch brevi. L'archivio non
contiene `.git`: la cronologia inizia sul tuo computer con la tua identità.

```bash
git init -b main
git add .
git diff --cached --stat
git status --short
git commit -m "feat: add local microservices baseline"
```

Prima del commit, controlla che non siano tracciati `.env` e `.secrets`:

```bash
git check-ignore .env .secrets/private.pem
git ls-files .env .secrets
```

Il secondo comando non deve mostrare file. Crea su GitHub un repository vuoto,
senza README/licenza generati, poi usa il SUO URL (non un URL di esempio copiato):

```bash
git remote add origin <URL_REPOSITORY>
git push -u origin main
git switch -c feat/first-observability-improvement
```

Per il primo vero esercizio PR faremo una piccola modifica, test, commit, push e
PR verso main, review e merge. Solo dopo il verde della CI e del test locale:

```bash
git switch main
git pull --ff-only
git tag -a v0.1.0 -m "Verified local baseline"
git push origin v0.1.0
```

La workflow già presente esegue Maven verify e Compose con PostgreSQL, poi build
immagini. Su PR non pubblica. Su push a main/tag pubblica in GHCR tramite
GITHUB_TOKEN e permesso packages:write. Tag immagini: `sha-<SHA completo>` e tag
Git per le release. Configureremo protezioni branch e permessi package su GitHub.
Prima della baseline cloud fisseremo anche le Actions a commit SHA verificati.

## Gate 3 — Kubernetes locale

A baseline confermata sceglieremo kind in base a RAM e CPU disponibili.
Creeremo manifest reali per namespace, Deployment, ClusterIP Service, ConfigMap,
Secret, probe, requests/limits e Ingress con un controller supportato.
Un oggetto Ingress da solo non instrada traffico: richiede il controller.
PostgreSQL nel cluster sarà inizialmente solo per laboratorio, con volume persistente.

Percorso applicativo completo:
Internet → Load Balancer del controller → Ingress → Service gateway → Pod gateway
→ Service applicativo → Pod Spring Boot → database.

Service fornisce endpoint stabile e seleziona Pod tramite label; Deployment gestisce
repliche e rollout. Readiness determina se il Pod riceve traffico; liveness decide
se riavviare il processo. ConfigMap contiene configurazione, Secret dati sensibili;
base64 non è cifratura. Passeremo chiavi e credenziali senza committarle.

## Gate 4 — Azure

Prima del provisioning: subscription scelta, region, budget, costi stimati, quota,
size nodi e piano di spegnimento/rimozione. Niente comandi con subscription inventate.

1. Resource Group dedicato e tagging con az CLI.
2. ACR, push delle immagini e autenticazione AKS tramite identità gestita.
3. AKS con rete esplicita: VNet/subnet, indirizzamento, ingress e uscita.
4. Configurazione namespace e secret, poi deployment dello stesso artefatto verificato.
5. TLS e DNS, account/segreti separati dal locale, limitazione accessi amministrativi.
6. PostgreSQL: prima laboratorio; poi valutazione Azure Database for PostgreSQL
   Flexible Server, rete privata e backup con prova di restore.
7. GitHub OIDC per azioni Azure; niente client secret a lunga durata in repository.
8. Rollout verificato, smoke test, immagini per digest e rollback alla release precedente.

## Gate 5 — osservabilità e gestione

Azure Monitor per salute/risorse, Log Analytics per interrogare log, Application
Insights con strumentazione Java per richieste, dipendenze e tracing distribuito.
Definiamo prima traffico di riferimento e SLI: disponibilità, latenza, error rate.
Non consideriamo operativo un alert finché non ne abbiamo verificato la ricezione.
HPA arriva dopo requests/limits e metriche affidabili; il DB non scala con l'HPA.

## Gate 6 — incidenti

Solo sull'ambiente laboratorio dedicato, dopo baseline e possibilità di ripristino.
Gli incidenti saranno eseguiti durante sessioni concordate, non promessi come
modifiche automatiche in background. Prima verificheremo accesso e ambito del target.
Nessuna modifica a risorse estranee al laboratorio o perdita deliberata di dati.

L'alert iniziale riporta sintomi misurati. La causa resta fissa e non viene rivelata;
nessun hint prima di HINT 1/2/3. I comandi mostrano solo output realmente ottenuto.
Senza accesso al tuo terminale eseguirai il comando e incollerai l'output: non verrà
sostituito da simulazioni. Le patch d'incidente, quando necessarie, saranno consegnate
senza mostrare preventivamente la diff, ma il materiale resta ispezionabile da te.
Dopo diagnosi e fix: post-mortem e valutazione del metodo, non solo della soluzione.
