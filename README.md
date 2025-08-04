# K9 Brukerdialog Prosessering

![CI / CD](https://github.com/navikt/k9-brukerdialog-prosessering/workflows/Build/badge.svg)
![Alerts](https://github.com/navikt/k9-brukerdialog-prosessering/workflows/Alerts/badge.svg)

# Innholdsoversikt

* [1. Kontekst](#1-kontekst)
* [2. Funksjonelle krav](#2-funksjonelle-krav)
* [3. Begrensninger](#3-begrensninger)
* [4. Programvarearkitektur](#4-programvarearkitektur)
* [5. Integrasjoner](#5-integrasjoner)
* [6. Data](#6-data)
* [7. Infrastrukturarkitektur](#7-infrastrukturarkitektur)
* [8. Distribusjon](#8-distribusjon)
* [9. Utviklingsmiljø](#9-utviklingsmiljo)
* [10. Drift og støtte](#10-drift-og-stotte)

# 1. Kontekst

K9 Brukerdialog Prosessering er en backend-applikasjon i NAV for mottak og journalføring av søknader og ettersendinger relatert til ytelser i kapittel 9 i folketrygdloven (som omsorgspenger, pleiepenger, opplæringspenger) og ungdomsprogramytelsen.
Tjenesten mottar data fra ulike brukerdialoger, generer pdf og journalfører det.

# 2. Funksjonelle krav

- Mottak og prosessering av søknader og ettersendinger
- Journalføring og mellomlagring av dokumenter
- Integrasjon med Kafka for hendelsesstrømmer
- Sikkerhet og autentisering via TokenX og Azure AD
- Helse- og statusendepunkter for drift og overvåkning

# 3. Begrensninger

- Innsending og prosessering begrenses til kapittel 9-ytelser (omsorgspenger, pleiepenger, opplæringspenger) og ungdomsprogramytelsen.
- Kun autoriserte applikasjoner kan sende kalle tjenesten (se inbound rules)
- Kun godkjente eksterne og interne tjenester kan motta data (se outbound rules)

# 4. Programvarearkitektur

- Spring Boot
- Kafka Streams
- GCP Storage Bucket for mellomlagring
- Prometheus og Elastic/Loki for observability/logging
- Docker og NAIS for deploy
- Swagger for API-dokumentasjon

# 5. Integrasjoner

## Inngående (inbound)
Applikasjonen mottar data fra følgende applikasjoner:
- omsorgspengesoknad
- ekstra-omsorgsdager-andre-forelder-ikke-tilsyn
- sif-ettersending
- omsorgspengerutbetaling-arbeidstaker-soknad
- omsorgsdager-aleneomsorg-dialog
- omsorgspengerutbetaling-soknad
- pleiepenger-i-livets-sluttfase-soknad
- pleiepengesoknad
- endringsmelding-pleiepenger
- dine-pleiepenger
- opplaringspenger-soknad
- ungdomsytelse-deltaker

## Utgående (outbound)
Applikasjonen kommuniserer med:
- GCP Storage Bucket (mellomlagring av dokumenter)
- k9-brukerdialog-cache (mellomlagring av søknadsdata)
- k9-sak-innsyn-api (innsyn i sak)
- k9-selvbetjening-oppslag (oppslagstjeneste for personopplysninger og arbeidsforhold)
- ung-deltakelse-opplyser (deltakelse i ungdomsprogramytelsen)
- clamav (viruskontroll av opplastede dokumenter)
- dokarkiv (journalføring av dokumenter)

Autentisering og autorisasjon håndteres via TokenX og Azure AD, med detaljerte audience/scope-innstillinger i application.yml.

# 6. Data

- Søknadsdata og ettersendinger lagres midlertidig i GCP Storage Bucket før de journalføres og slettes etter at de er prosessert.
- Dokumenter (søknader og vedlegg) journalføres i dokarkiv.
- Kafka brukes til hendelsesstrømmer for prosessering og oppfølging.

# 7. Infrastrukturarkitektur

- NAIS deploy med naiserator.yaml
- GCP buckets for mellomlagring
- TokenX for autentisering
- Prometheus og Elastic/Loki for logging og metrics

# 8. Distribusjon

Distribusjon av tjenesten er gjort med bruk av Github Actions.
Push/merge til dev-* branch vil teste, bygge og deploye til testmiljø.
Push/merge til master branch vil teste, bygge og deploye til produksjonsmiljø og testmiljø.

# 9. Utviklingsmiljø

## Forutsetninger

* docker
* docker-compose
* Java 21
* Kubectl

## Bygge prosjekt

```shell
./gradlew clean build
```

## Kjøre tester

```shell
./gradlew test
```

## Kjøre prosjektet lokalt

```shell
./gradlew clean build && docker build --tag k9-brukerdialog-prosessering-local . && docker-compose up --build
```

Eller for å hoppe over tester under bygging:

```shell
./gradlew clean build -x test && docker build --tag k9-brukerdialog-prosessering-local . && docker-compose up --build
```

## Miljøvariabler

Se nais/prod-gcp.json og nais/naiserator.yaml for nødvendige miljøvariabler og secrets.

# 10. Drift og støtte

## Feilsøking

For å feilsøke data kan du bruke Swagger-UI.
Swagger-UI er tilgjengelig på /swagger-ui.html dersom SWAGGER_ENABLED er satt til true.

## Logging

Loggene til tjenesten kan leses via Kibana og Grafana (Prometheus/Elastic/Loki).

## Alarmer

Vi bruker nais-alerts for å sette opp alarmer. Disse finner man konfigurert i nais/alerts.yml.

## Metrics

Prometheus metrics eksponeres på /metrics.

## Henvendelser

Spørsmål om kode eller prosjektet kan rettes til:
* [#sif-brukerdialog](https://nav-it.slack.com/archives/CQ7QKSHJR)
