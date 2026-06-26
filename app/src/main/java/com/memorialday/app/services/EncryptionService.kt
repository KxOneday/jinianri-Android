// 纪念日 - 加密服务
// 对应 iOS: EncryptionService.swift + KeychainManager
// Android 使用 EncryptedSharedPreferences 替代 iOS Keychain

package com.memorialday.app.services

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/** 加密服务 - AES-256-GCM 本地加密 */
object EncryptionService {

    private const val KEY_ALIAS = "memorial_encryption_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    private var secretKey: SecretKey? = null

    fun init(context: Context) {
        try {
            val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }
            if (keyStore.containsAlias(KEY_ALIAS)) {
                val entry = keyStore.getEntry(KEY_ALIAS, null) as java.security.KeyStore.SecretKeyEntry
                secretKey = entry.secretKey
            } else {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
                )
                keyGenerator.init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                )
                secretKey = keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            // Android KeyStore 不可用时使用内存密钥降级
            val keyBytes = ByteArray(32)
            SecureRandom().nextBytes(keyBytes)
            secretKey = SecretKeySpec(keyBytes, "AES")
        }
    }

    /** 加密数据 (对应 iOS encrypt) */
    fun encrypt(data: ByteArray): ByteArray? {
        val key = secretKey ?: return null
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(data)
            // combined: iv + ciphertext
            iv + encrypted
        } catch (e: Exception) {
            null
        }
    }

    /** 解密数据 (对应 iOS decrypt) */
    fun decrypt(combinedData: ByteArray): ByteArray? {
        val key = secretKey ?: return null
        return try {
            val iv = combinedData.copyOfRange(0, GCM_IV_LENGTH)
            val ciphertext = combinedData.copyOfRange(GCM_IV_LENGTH, combinedData.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            null
        }
    }
}
