# Copilot Instructions

## Build, Test, and Run

```bash
# Build
./gradlew clean build

# Test (all)
./gradlew test

# Test (single class)
./gradlew test --tests "no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.PleiepengerSyktBarnSøknadKonsumentTest"

# Test (single method)
./gradlew test --tests "no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.PleiepengerSyktBarnSøknadKonsumentTest.methodName"

# Run locally (with tests)
./gradlew clean build && docker build --tag k9-brukerdialog-prosessering-local . && docker-compose up --build

# Run locally (skip tests)
./gradlew clean build -x test && docker build --tag k9-brukerdialog-prosessering-local . && docker-compose up --build
```

Tests use a 6 GB JVM heap and spin up Kafka/GCS containers via TestContainers — they are slow to start but fast once running.

## Architecture

This application receives benefit applications (søknader) from 12 upstream user-dialog services, generates PDFs, and journals them via dokarkiv. All processing is driven by **Kafka Streams**.

Every benefit type (ytelse) has an identical **3-stage Kafka Streams pipeline**:
1. **Preprosessering** — validate K9 format, generate PDF, virus-scan attachments
2. **Journalføring** — journal the document in dokarkiv
3. **Cleanup** — delete temporary files from GCP Storage

Each stage is a separate `StreamsBuilderFactoryBean` with its own dedicated topic. The stages are independent Spring beans and consume/produce typed `TopicEntry<T>` messages.

Inbound REST endpoints accept submissions, validate, enrich with person data, write documents to GCP Storage, then publish to the first Kafka topic — returning `202 Accepted` immediately. The rest happens asynchronously through the pipeline.

## Package Structure

```
src/main/kotlin/no/nav/brukerdialog/
├── ytelse/              # One subpackage per benefit type, each containing:
│   └── {type}/
│       ├── api/         # REST controller + request models
│       ├── kafka/       # 3-stage pipeline + topology config + SerDes
│       └── pdf/         # PDF template data models
├── domenetjenester/
│   ├── innsending/      # InnsendingService (REST ingestion), DuplikatInnsendingSjekker
│   └── mottak/          # PreprosesseringsService, JournalføringsService, CleanupService
├── integrasjon/         # Clients: dokarkiv, familiepdf, clamav, gcpstorage, k9-oppslag, etc.
├── kafka/               # Shared Kafka infrastructure: Topics, SerDes base, process() helper
├── config/              # Spring configs: Security, Jackson, TokenX/Azure clients
└── mellomlagring/       # Encrypted temporary GCP document storage
```

## Key Conventions

### Adding a new ytelse

Follow the structure of an existing ytelse (e.g., `opplæringspenger`). Each ytelse needs:
- A controller in `ytelse/{type}/api/` annotated with `@RequiredIssuers` (TokenX, Level4)
- Three Kafka processor components (`Preprosessering`, `Journalføring`, `Cleanup`) in `ytelse/{type}/kafka/`
- A `TopologyConfiguration` with three `StreamsBuilderFactoryBean` beans and three `Topic` beans
- Custom SerDes extending the base `SerDes<T>` class
- Data model classes: `MottattSøknad`, `PreprosessertSøknad`, and `Cleanup` wrapper

### Kafka Streams pattern

```kotlin
@Bean
fun preprosessering(
    @Qualifier("myYtelsePreprosesseringStreamsBuilder") builder: StreamsBuilder
): KafkaStream {
    builder.stream(mottattTopic.name, mottattTopic.consumed)
        .processValues(ProcessorSupplier {
            LoggingToMDCProcessor { entry ->
                process(
                    name = KafkaStreamName.MY_SØKNAD_PREPROSESSERING,
                    entry = entry,
                    retryTemplate = retryTemplate,
                    logger = logger,
                    block = { preprosesseringsService.prosesser(entry.data) }
                )
            }
        })
        .to(preprosestertTopic.name, preprosestertTopic.produced)
    return builder.build()
}
```

The `process()` helper in `kafka/processors/` handles retry via `RetryTemplate` and wraps the suspending `block` in `runBlocking`.

### REST controller pattern

```kotlin
@RestController
@RequestMapping("/my-ytelse")
@RequiredIssuers(ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"]))
class MyYtelseController(private val innsendingService: InnsendingService, ...) {
    @PostMapping("/innsending", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun innsending(
        @RequestHeader(NavHeaders.BRUKERDIALOG_GIT_SHA) gitSha: String,
        @RequestBody søknad: MyYtelseSøknad,
    ) = runBlocking { innsendingService.registrer(søknad, ...) }
}
```

### Testing pattern

Integration tests extend `AbstractIntegrationTest` (annotated `@KafkaIntegrationTest`) and test the full 3-stage pipeline end-to-end. Use MockK for mocking and Awaitility for asserting async Kafka results.

```kotlin
@KafkaIntegrationTest
@AutoConfigureMockMvc
class MyYtelseSøknadKonsumentTest : AbstractIntegrationTest() {
    @MockkBean lateinit var myExternalClient: MyExternalClient

    @Test
    fun `should process søknad through pipeline`() {
        // POST to REST endpoint, then await Kafka cleanup message
    }
}
```

### Naming

| Concept | Pattern |
|---|---|
| Kafka topology class | `{Ytelse}TopologyConfiguration` |
| Kafka stage component | `{Ytelse}Søknad{Preprosessering\|Journalføring\|Cleanup}` |
| Received model | `{Ytelse}MottattSøknad` |
| Processed model | `{Ytelse}PreprosessertSøknad` |
| SerDes | `{Ytelse}Serdes` |
| Test class | `{ClassName}Test` or `{ClassName}KonsumentTest` |

### Configuration

External service URLs and secrets are injected via environment variables (see `nais/prod-gcp.json` and `naiserator.yaml`). All application config is in `src/main/resources/application.yml`. Kafka topics follow the `dusseldorf.*` prefix convention.

## External Integrations

| Service | Purpose |
|---|---|
| `dokarkiv` | Journal documents (Azure AD auth) |
| `familiepdf` | Generate PDFs from søknad data |
| `clamav` | Virus scan uploaded attachments |
| `k9-selvbetjening-oppslag` | Look up person/employer data |
| `ung-deltakelse-opplyser` | Youth program participation data |
| `ung-brukerdialog-api` | Update task/notification status |
| GCP Storage Bucket | Temporary encrypted document storage (auto-deleted after processing) |
