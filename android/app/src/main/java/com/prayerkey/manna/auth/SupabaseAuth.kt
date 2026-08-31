package com.prayerkey.manna.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.prayerkey.manna.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class AuthSession(val userId: String, val email: String, val accessToken: String, val refreshToken: String)

sealed interface AuthResult {
    data class SignedIn(val session: AuthSession) : AuthResult
    data class ConfirmEmail(val email: String) : AuthResult
    data class Failure(val message: String) : AuthResult
}

class SupabaseAuthClient {
    val configured: Boolean
        get() = BuildConfig.SUPABASE_URL.startsWith("https://") && BuildConfig.SUPABASE_PUBLISHABLE_KEY.isNotBlank()

    suspend fun signIn(email: String, password: String): AuthResult = request(
        path = "/auth/v1/token?grant_type=password",
        body = JSONObject().put("email", email.trim()).put("password", password),
    )

    suspend fun signUp(name: String, email: String, password: String): AuthResult = request(
        path = "/auth/v1/signup",
        body = JSONObject()
            .put("email", email.trim())
            .put("password", password)
            .put("data", JSONObject().put("name", name.trim())),
        signupEmail = email.trim(),
    )

    suspend fun resetPassword(email: String): AuthResult = withContext(Dispatchers.IO) {
        if (!configured) return@withContext AuthResult.Failure("Account service is not connected yet.")
        val response = post("/auth/v1/recover", JSONObject().put("email", email.trim()))
        if (response.first in 200..299) AuthResult.ConfirmEmail(email.trim())
        else AuthResult.Failure(errorMessage(response.second))
    }

    private suspend fun request(path: String, body: JSONObject, signupEmail: String? = null): AuthResult = withContext(Dispatchers.IO) {
        if (!configured) return@withContext AuthResult.Failure("Account service is not connected yet.")
        val (code, raw) = post(path, body)
        if (code !in 200..299) return@withContext AuthResult.Failure(errorMessage(raw))
        val json = runCatching { JSONObject(raw) }.getOrElse { return@withContext AuthResult.Failure("The account service returned an unreadable response.") }
        val access = json.optString("access_token")
        val refresh = json.optString("refresh_token")
        val user = json.optJSONObject("user") ?: json
        val userId = user.optString("id")
        val resolvedEmail = user.optString("email", signupEmail.orEmpty())
        if (access.isBlank()) AuthResult.ConfirmEmail(resolvedEmail.ifBlank { signupEmail.orEmpty() })
        else AuthResult.SignedIn(AuthSession(userId, resolvedEmail, access, refresh))
    }

    private fun post(path: String, body: JSONObject): Pair<Int, String> {
        val base = BuildConfig.SUPABASE_URL.trimEnd('/')
        val connection = (URL(base + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
        }
        return try {
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            code to (stream?.bufferedReader()?.use { it.readText() }.orEmpty())
        } finally {
            connection.disconnect()
        }
    }

    private fun errorMessage(raw: String): String {
        val json = runCatching { JSONObject(raw) }.getOrNull()
        return json?.optString("msg")?.takeIf { it.isNotBlank() }
            ?: json?.optString("message")?.takeIf { it.isNotBlank() }
            ?: json?.optString("error_description")?.takeIf { it.isNotBlank() }
            ?: "We could not complete that request. Please try again."
    }
}

/** Stores Supabase session tokens with an AES key that never leaves Android Keystore. */
class SecureAuthStore(context: Context) {
    private val prefs = context.getSharedPreferences("secure_auth", Context.MODE_PRIVATE)
    private val alias = "prayerkey-auth-session"

    fun save(session: AuthSession) {
        val plain = JSONObject()
            .put("userId", session.userId).put("email", session.email)
            .put("accessToken", session.accessToken).put("refreshToken", session.refreshToken)
            .toString().toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        prefs.edit()
            .putString("value", Base64.encodeToString(cipher.doFinal(plain), Base64.NO_WRAP))
            .putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    fun session(): AuthSession? = runCatching {
        val encrypted = Base64.decode(prefs.getString("value", null), Base64.NO_WRAP)
        val iv = Base64.decode(prefs.getString("iv", null), Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        val json = JSONObject(cipher.doFinal(encrypted).toString(Charsets.UTF_8))
        AuthSession(json.getString("userId"), json.getString("email"), json.getString("accessToken"), json.optString("refreshToken"))
    }.getOrNull()

    fun clear() = prefs.edit().clear().apply()

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
            generateKey()
        }
    }
}
