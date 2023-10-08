package com.mayur1512.encryption_rsa

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.security.KeyPair

class MainActivity : AppCompatActivity() {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var keyPairMap: KeyPair

    private lateinit var edtEncrypt: EditText
    private lateinit var txtEncrypt: TextView
    private lateinit var txtDecrypt: TextView
    private lateinit var btnEncrypt: Button
    private lateinit var btnDecrypt: Button

    private var encryptedText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rsa)

        biometricPrompt = createBiometricPrompt()
        promptInfo = createBiometricPromptInfo()

        keyPairMap = RSAHelper.generateKeyPair()

        Log.e("PUBLICKEY", "${RSAHelper.printPublicKey(keyPairMap)}")

        edtEncrypt = findViewById(R.id.edt_encrypt)
        txtEncrypt = findViewById(R.id.txt_encrypted)
        txtDecrypt = findViewById(R.id.txt_decrypted)
        btnEncrypt = findViewById(R.id.btn_encrypt)
        btnDecrypt = findViewById(R.id.btn_decrypt)

        btnEncrypt.setOnClickListener {
            val textToEncrypt = edtEncrypt.text.toString().trim()

            if (textToEncrypt.isNotEmpty()) {
                encryptedText = RSAHelper.encrypt(
                    textToEncrypt = textToEncrypt,
                    publicKey = keyPairMap.public
                )

                txtEncrypt.text = encryptedText
            } else {
                showToast("Please enter text to encrypt")
            }
        }

        btnDecrypt.setOnClickListener {
            if (encryptedText.isNotEmpty()) {
                biometricPrompt.authenticate(promptInfo)
            } else {
                showToast("Please encrypt a text first")
            }
        }
    }

    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                val decryptedString = RSAHelper.decrypt(
                    encryptedText,
                    keyPairMap.private
                )

                txtDecrypt.text = decryptedString
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                this@MainActivity.showToast("Biometric authentication error: $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                this@MainActivity.showToast("Biometric authentication failed.")
            }
        }
        return BiometricPrompt(this, executor, callback)
    }

    private fun createBiometricPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setDescription("Place your finger on the fingerprint sensor to authenticate.")
            .setNegativeButtonText("Cancel")
            .build()
    }
}

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}