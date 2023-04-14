package com.example.prueba2.ui.login

import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.PathInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.prueba2.databinding.ActivityLoginBinding
import com.example.prueba2.R
import com.example.prueba2.data.model.LoginRequest
import org.json.JSONException
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginRequest: LoginRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val interpolator = PathInterpolator(0.0f, 0.0f, 1.0f, 1.0f)
        loading.interpolator = interpolator

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }


            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())

                jsonConverter(username.text.toString(), password.text.toString())
            }
        }
    }

    private fun jsonConverter(username: String, password: String){
        val queue = Volley.newRequestQueue(this)
        loginRequest = LoginRequest(
            username_ = username,
            password_ = password
        )

        val jsonObject = JSONObject()
        jsonObject.put("username", loginRequest.username_)
        jsonObject.put("password", loginRequest.password_)
//        val gson = Gson()
//        val json = gson.toJsonTree(loginRequest).getAsJsonObject()

        val url = "http://192.168.0.2/anas/nfqgest/api/login.php"

        Log.d("Login Response1 ->", jsonObject.toString())
        // aquí puede escribir el código que desea ejecutar después de 1 segundo
        val jsonRequest = JsonObjectRequest(

            Request.Method.POST, url, jsonObject,
            { response ->
                // Procesar la respuesta del servidor
                val jsonResponse = response.toString()
                if(jsonResponse.isEmpty()){

                    Toast.makeText(
                        applicationContext,
                        "Algo salio mal.",
                        Toast.LENGTH_LONG
                    ).show()
                }else{
                    Log.d("Login Response ->", jsonResponse)
                    Toast.makeText(
                        applicationContext,
                        jsonResponse,
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            { error ->
                // Manejar el error de la solicitud
                error.printStackTrace()
            })
        queue.add(jsonRequest)

    }


    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}