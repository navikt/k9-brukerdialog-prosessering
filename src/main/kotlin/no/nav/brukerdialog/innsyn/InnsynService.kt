package no.nav.brukerdialog.innsyn

import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.api.domene.Endringsmelding
import org.springframework.stereotype.Service

@Service
class InnsynService(private val k9SakInnsynApiService: K9SakInnsynApiService) {

    fun hentSøknadsopplysningerForBarn(
        endringsmelding: Endringsmelding
    ): K9SakInnsynSøknad {
        return k9SakInnsynApiService.hentSøknadsopplysninger()
            .firstOrNull { k9SakInnsynSøknad: K9SakInnsynSøknad ->
                k9SakInnsynSøknad.barn.identitetsnummer == endringsmelding.ytelse.barn.personIdent.verdi
            } ?: throw IllegalStateException("Søknadsopplysninger inneholdt ikke riktig barn.")
    }
}
