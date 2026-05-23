package com.example.plannerdiario.data

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Gerencia autenticação com Google e operações de backup no Google Drive.
 *
 * CONFIGURAÇÃO NECESSÁRIA (uma vez por projeto):
 * 1. Acesse https://console.cloud.google.com
 * 2. Crie um projeto e ative a "Google Drive API"
 * 3. Configure a "Tela de consentimento OAuth" (tipo: Externo)
 * 4. Crie credenciais OAuth 2.0 → Android (packageName + SHA-1 do keystore)
 * 5. Não precisa de google-services.json para este fluxo
 */
class DriveBackupManager(private val context: Context) {

    companion object {
        private const val BACKUP_FILENAME = "orgday_backup.json"
        private const val DRIVE_SCOPE    = "https://www.googleapis.com/auth/drive.appdata"
        private const val TOKEN_SCOPE    = "oauth2:$DRIVE_SCOPE"
        private const val DRIVE_API      = "https://www.googleapis.com/drive/v3"
        private const val DRIVE_UPLOAD   = "https://www.googleapis.com/upload/drive/v3"
    }

    /** Client de sign-in configurado com escopo de appData do Drive */
    val signInClient: GoogleSignInClient by lazy {
        val opts = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DRIVE_SCOPE))
            .build()
        GoogleSignIn.getClient(context, opts)
    }

    fun getSignedInAccount(): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)

    fun isSignedInWithDrive(): Boolean {
        val account = getSignedInAccount() ?: return false
        return GoogleSignIn.hasPermissions(account, Scope(DRIVE_SCOPE))
    }

    /** Resultado selado para operações do Drive */
    sealed class DriveResult {
        object Success : DriveResult()
        data class SuccessData(val data: String) : DriveResult()
        data class Error(val message: String) : DriveResult()
        data class NeedsPermission(val intent: Intent) : DriveResult()
        object NotFound : DriveResult()
    }

    // ── Token ─────────────────────────────────────────────────────────────────

    private suspend fun getToken(): String? = withContext(Dispatchers.IO) {
        try {
            val account = getSignedInAccount() ?: return@withContext null
            val gAccount = account.account ?: return@withContext null
            GoogleAuthUtil.getToken(context, gAccount, TOKEN_SCOPE)
        } catch (_: Exception) { null }
    }

    // ── Upload ────────────────────────────────────────────────────────────────

    suspend fun uploadBackup(jsonContent: String): DriveResult = withContext(Dispatchers.IO) {
        try {
            val token = getToken()
                ?: return@withContext DriveResult.Error("not_authenticated")

            val existingId = findBackupFileId(token)
            if (existingId != null) updateFile(token, existingId, jsonContent)
            else createFile(token, jsonContent)
        } catch (e: UserRecoverableAuthException) {
            e.intent?.let { DriveResult.NeedsPermission(it) }
                ?: DriveResult.Error("no_recovery_intent")
        } catch (e: Exception) {
            DriveResult.Error(e.message ?: "unknown_error")
        }
    }

    // ── Download ──────────────────────────────────────────────────────────────

    suspend fun downloadBackup(): DriveResult = withContext(Dispatchers.IO) {
        try {
            val token = getToken()
                ?: return@withContext DriveResult.Error("not_authenticated")
            val fileId = findBackupFileId(token)
                ?: return@withContext DriveResult.NotFound

            val conn = openConnection("$DRIVE_API/files/$fileId?alt=media", "GET", token)
            if (conn.responseCode == 200) {
                DriveResult.SuccessData(conn.inputStream.bufferedReader().readText())
            } else {
                DriveResult.Error("http_${conn.responseCode}")
            }
        } catch (e: UserRecoverableAuthException) {
            e.intent?.let { DriveResult.NeedsPermission(it) }
                ?: DriveResult.Error("no_recovery_intent")
        } catch (e: Exception) {
            DriveResult.Error(e.message ?: "unknown_error")
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun findBackupFileId(token: String): String? {
        val query = URLEncoder.encode("name='$BACKUP_FILENAME'", "UTF-8")
        val conn  = openConnection(
            "$DRIVE_API/files?spaces=appDataFolder&q=$query&fields=files(id,name)",
            "GET", token
        )
        if (conn.responseCode != 200) return null
        val files = JSONObject(conn.inputStream.bufferedReader().readText())
            .getJSONArray("files")
        return if (files.length() > 0) files.getJSONObject(0).getString("id") else null
    }

    private fun createFile(token: String, content: String): DriveResult {
        val boundary = "orgday_mp_boundary"
        val meta     = JSONObject().apply {
            put("name", BACKUP_FILENAME)
            put("parents", JSONArray().apply { put("appDataFolder") })
        }.toString()
        val body = "--$boundary\r\n" +
                "Content-Type: application/json; charset=UTF-8\r\n\r\n$meta\r\n" +
                "--$boundary\r\nContent-Type: application/json\r\n\r\n$content\r\n" +
                "--$boundary--"

        val conn = openConnection("$DRIVE_UPLOAD/files?uploadType=multipart", "POST", token)
        conn.setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
        conn.doOutput = true
        OutputStreamWriter(conn.outputStream).use { it.write(body) }
        return if (conn.responseCode in 200..201) DriveResult.Success
               else DriveResult.Error("create_failed_${conn.responseCode}")
    }

    private fun updateFile(token: String, fileId: String, content: String): DriveResult {
        val conn = openConnection("$DRIVE_UPLOAD/files/$fileId?uploadType=media", "PATCH", token)
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        OutputStreamWriter(conn.outputStream).use { it.write(content) }
        return if (conn.responseCode == 200) DriveResult.Success
               else DriveResult.Error("update_failed_${conn.responseCode}")
    }

    private fun openConnection(urlStr: String, method: String, token: String): HttpURLConnection {
        val conn = (URL(urlStr).openConnection() as HttpURLConnection)
        conn.requestMethod = method
        conn.setRequestProperty("Authorization", "Bearer $token")
        return conn
    }
}

