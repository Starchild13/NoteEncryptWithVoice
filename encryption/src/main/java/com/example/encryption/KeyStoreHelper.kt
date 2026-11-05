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

    // Alias used to store and identify the secret key in the Android KeyStore
    private const val KEY_ALIAS = "SecureNotesKey"

    // Cryptographic transformation string specifying AES algorithm, GCM block mode, and no padding
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    // Size of the Initialization Vector (IV) in bytes for AES/GCM (12 bytes / 96 bits)
    private const val IV_SIZE = 12

    // Logging tag for debugging cryptographic exceptions
    private const val TAG = "KeyStoreHelper"

    /**
     * Retrieves the existing SecretKey from the AndroidKeyStore or generates a new one if it does not exist.
     * Configures AES with GCM block mode and no padding for secure authenticated encryption.
     *
     * @return The [SecretKey] instance stored in the Android KeyStore.
     */
    fun getOrCreateSecretKey(): SecretKey {
        // Load the Android KeyStore provider
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        // Fetch existing key entry if present
        return keyStore.getEntry(KEY_ALIAS, null)?.let {
            (it as KeyStore.SecretKeyEntry).secretKey
        } ?: run {
            // Generate a new AES secret key inside the hardware-backed Android KeyStore
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
            // Obtain hardware-backed AES secret key
            val key = getOrCreateSecretKey()

            // Initialize Cipher in ENCRYPT_MODE
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)

            // Extract randomly generated Initialization Vector (IV)
            val iv = cipher.iv

            // Encrypt plain text bytes into authenticated ciphertext
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

            // Prepend IV to ciphertext for unified storage
            val combined = iv + encryptedBytes

            // Encode combined byte array as Base64 string for safe database persistence
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
            // Obtain hardware-backed AES secret key
            val key = getOrCreateSecretKey()

            // Decode Base64 payload into raw combined bytes
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)

            // Validate payload length is greater than IV size
            if (combined.size <= IV_SIZE) return@withContext null

            // Extract IV from first 12 bytes
            val iv = combined.copyOfRange(0, IV_SIZE)

            // Extract encrypted ciphertext from remaining bytes
            val encryptedBytes = combined.copyOfRange(IV_SIZE, combined.size)

            // Initialize Cipher in DECRYPT_MODE using GCMParameterSpec with 128-bit authentication tag
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            // Decrypt ciphertext into plain text byte array
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed: ${e.message}", e)
            null
        }
    }
}

