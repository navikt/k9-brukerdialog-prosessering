package no.nav.brukerdialog.mellomlagring.dokument.kryptering

import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class KrypteringUtil(val passphrase: String, private val iv: String) {
    private val key: SecretKey

    private companion object {
        private const val ALGO = "AES/GCM/NoPadding"
    }

    init {
        if (passphrase.isBlank() || iv.isBlank()) {
            throw IllegalArgumentException("Passphrase og iv må settes")
        }
        key = nøkkel(passphrase, iv)
    }

    fun krypter(plainText: String): String {
        try {
            val cipher = Cipher.getInstance(ALGO)
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv.toByteArray()))
            return Base64.getUrlEncoder().encodeToString(cipher.doFinal(plainText.toByteArray()))
        } catch (ex: Exception) {
            throw RuntimeException("Error while encrypting text", ex)
        }
    }

    fun dekrypter(encrypted: String): String {
        try {
            val cipher = Cipher.getInstance(ALGO)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv.toByteArray()))
            return String(cipher.doFinal(Base64.getUrlDecoder().decode(encrypted)))
        } catch (ex: Exception) {
            throw RuntimeException("Error while decrypting text", ex)
        }
    }

    private fun nøkkel(krypteringsnøkkel: String, salt: String): SecretKey {
        try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val passordKarakterer = krypteringsnøkkel.toCharArray()
            val spesifikasjon = PBEKeySpec(passordKarakterer, salt.toByteArray(), 10000, 256)
            val nøkkel = factory.generateSecret(spesifikasjon)
            return SecretKeySpec(nøkkel.encoded, "AES")
        } catch (ex: Exception) {
            throw RuntimeException("Error while generating key", ex)
        }
    }
}
