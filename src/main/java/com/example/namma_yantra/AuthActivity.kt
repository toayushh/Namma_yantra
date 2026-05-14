package com.example.namma_yantra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser != null) {
            openMain()
            return
        }

        setContentView(R.layout.activity_auth)

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val roleGroup = findViewById<RadioGroup>(R.id.roleGroup)
        val titleText = findViewById<TextView>(R.id.authTitle)
        val subtitleText = findViewById<TextView>(R.id.authSubtitle)
        val actionBtn = findViewById<Button>(R.id.authActionBtn)
        val switchModeBtn = findViewById<Button>(R.id.switchModeBtn)

        fun updateMode() {
            titleText.text = if (isRegisterMode) "Create account" else "Welcome back"
            subtitleText.text = if (isRegisterMode) {
                "Choose your role and join the machinery network"
            } else {
                "Sign in to browse, list, and book farm equipment"
            }
            nameInput.visibility = if (isRegisterMode) android.view.View.VISIBLE else android.view.View.GONE
            roleGroup.visibility = if (isRegisterMode) android.view.View.VISIBLE else android.view.View.GONE
            actionBtn.text = if (isRegisterMode) "Register" else "Login"
            switchModeBtn.text = if (isRegisterMode) "I already have an account" else "Create new account"
        }

        switchModeBtn.setOnClickListener {
            isRegisterMode = !isRegisterMode
            updateMode()
        }

        actionBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.length < 6) {
                Toast.makeText(this, "Enter email and at least 6 character password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            actionBtn.isEnabled = false
            actionBtn.text = if (isRegisterMode) "Creating..." else "Signing in..."

            if (isRegisterMode) {
                if (name.isEmpty()) {
                    actionBtn.isEnabled = true
                    updateMode()
                    Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                register(name, email, password, roleGroup, actionBtn, ::updateMode)
            } else {
                login(email, password, actionBtn, ::updateMode)
            }
        }

        updateMode()
    }

    private fun register(
        name: String,
        email: String,
        password: String,
        roleGroup: RadioGroup,
        actionBtn: Button,
        updateMode: () -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    actionBtn.isEnabled = true
                    updateMode()
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val selectedRole = findViewById<RadioButton>(roleGroup.checkedRadioButtonId).text.toString()
                val profile = UserProfile(
                    uid = user.uid,
                    name = name,
                    email = email,
                    role = selectedRole
                )

                FirebaseRepository.saveUserProfile(
                    profile = profile,
                    onSuccess = {
                        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
                        openMain()
                    },
                    onError = { error ->
                        actionBtn.isEnabled = true
                        updateMode()
                        Toast.makeText(this, "Profile save failed: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
            .addOnFailureListener { error ->
                actionBtn.isEnabled = true
                updateMode()
                Toast.makeText(this, "Registration failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun login(
        email: String,
        password: String,
        actionBtn: Button,
        updateMode: () -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show()
                openMain()
            }
            .addOnFailureListener { error ->
                actionBtn.isEnabled = true
                updateMode()
                Toast.makeText(this, "Login failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
