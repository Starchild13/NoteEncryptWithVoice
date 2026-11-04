package com.example.encryption

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Helper object responsible for cryptographic operations using the Android KeyStore system.
 * Handles secure AES key generation, encryption, and decryption with GCM mode off the main thread.
 */
object KeyStoreHelper {

    private const val KEY_ALIAS = "SecureNotesKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG = "KeyStoreHelper"

    /**
     * Retrieves the existing SecretKey from the AndroidKeyStore or generates a new one if it does not exist.
     * Configures AES with GCM block mode and no padding for secure authenticated encryption.
     *
     * @return The [SecretKey] instance stored in the Android KeyStore.
     */
    fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        return keyStore.getEntry(KEY_ALIAS, null)?.let {
            (it as KeyStore.SecretKeyEntry).secretKey
        } ?: run {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    /**
     * Encrypts plain text data using AES/GCM/NoPadding asynchronously on Dispatchers.IO.
     * Prepends the IV to the encrypted ciphertext and encodes the result in Base64.
     *
     * @param data The plain text string to encrypt.
     * @return The Base64-encoded string containing both IV and ciphertext, or null if encryption fails.
     */
    suspend fun encryptData(data: String): String? = withContext(Dispatchers.IO) {
        try {
            val key = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

            val combined = iv + encryptedBytes
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed: ${e.message}", e)
            null
        }
    }

    /**
     * Decrypts a Base64-encoded encrypted data string containing the IV and ciphertext asynchronously on Dispatchers.IO.
     *
     * @param encryptedData The Base64-encoded encrypted payload.
     * @return The decrypted plain text string, or null if decryption fails.
     */
    suspend fun decryptData(encryptedData: String): String? = withContext(Dispatchers.IO) {
        try {
            val key = getOrCreateSecretKey()
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)

            if (combined.size <= IV_SIZE) return@withContext null

            val iv = combined.copyOfRange(0, IV_SIZE)
            val encryptedBytes = combined.copyOfRange(IV_SIZE, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed: ${e.message}", e)
            null
        }
    }
}
