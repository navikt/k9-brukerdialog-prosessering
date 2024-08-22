package no.nav.brukerdialog.oppslag.arbeidsgiver

import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.ArbeidsgivereOppslagsService
import no.nav.brukerdialog.oppslag.TilgangNektetException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ArbeidsgiverService(
    private val arbeidsgivereOppslagsService: ArbeidsgivereOppslagsService,
) {
    private val logger: Logger = LoggerFactory.getLogger(ArbeidsgiverService::class.java)

    suspend fun hentArbedisgivere(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
        skalHentePrivateArbeidsgivere: Boolean,
        skalHenteFrilansoppdrag: Boolean,
    ): ArbeidsgivereDto = try {
        arbeidsgivereOppslagsService.hentArbeidsgivere(
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            skalHentePrivateArbeidsgivere = skalHentePrivateArbeidsgivere,
            skalHenteFrilansoppdrag = skalHenteFrilansoppdrag
        ).arbeidsgivere.somArbeidsgivereDto()
    } catch (cause: Throwable) {
        when (cause) {
            is TilgangNektetException -> throw cause
            else -> {
                logger.error("Feil ved henting av arbeidsgivere, returnerer en tom liste", cause)
                ArbeidsgivereDto(emptyList(), emptyList(), emptyList())
            }
        }
    }

    private fun ArbeidsgivereOppslagDto.somArbeidsgivereDto(): ArbeidsgivereDto {
        return ArbeidsgivereDto(
            organisasjoner = organisasjoner.map {
                OrganisasjonDto(
                    organisasjonsnummer = it.organisasjonsnummer,
                    navn = it.navn,
                    ansattFom = it.ansattFom,
                    ansattTom = it.ansattTom
                )
            },
            privateArbeidsgivere = privateArbeidsgivere?.map {
                PrivatArbeidsgiverDto(
                    offentligIdent = it.offentligIdent,
                    ansattFom = it.ansattFom,
                    ansattTom = it.ansattTom
                )
            } ?: listOf(),
            frilansoppdrag = frilansoppdrag?.map {
                FrilansoppdragDto(
                    type = it.type,
                    organisasjonsnummer = it.organisasjonsnummer,
                    navn = it.navn,
                    offentligIdent = it.offentligIdent,
                    ansattFom = it.ansattFom,
                    ansattTom = it.ansattTom
                )
            } ?: listOf()
        )
    }
}
