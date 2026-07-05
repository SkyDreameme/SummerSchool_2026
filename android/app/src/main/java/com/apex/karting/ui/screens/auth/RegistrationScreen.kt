package com.apex.karting.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apex.karting.data.ApiException
import com.apex.karting.data.ApexRepository
import com.apex.karting.data.MockData
import com.apex.karting.ui.components.ApexPrimaryButton
import com.apex.karting.ui.components.ApexSecondaryButton
import com.apex.karting.ui.theme.ApexError
import com.apex.karting.ui.theme.ApexRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(onLoggedIn: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { ApexRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(1) }
    var phone by remember { mutableStateOf("+7") }
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(0) }
    var showSupport by remember { mutableStateOf(false) }

    // Исправление: отслеживаем изменение номера для сброса таймера
    var currentPhoneForTimer by remember { mutableStateOf("") }

    LaunchedEffect(timerSeconds) {
        if (timerSeconds > 0) {
            delay(1000)
            timerSeconds--
        }
    }

    // Исправление: при смене номера сбрасываем таймер
    LaunchedEffect(phone) {
        if (phone != currentPhoneForTimer) {
            currentPhoneForTimer = phone
            timerSeconds = 0
            // Также сбрасываем ошибку при смене номера
            error = null
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text("Апекс", style = MaterialTheme.typography.displaySmall, color = ApexRed)
        Text("Картинг-центр", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(48.dp))

        if (step == 1) {
            Text("Введите номер телефона", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { value ->
                    val digits = value.filter { it.isDigit() || it == '+' }
                    phone = when {
                        digits.startsWith("+7") -> digits.take(12)
                        digits.startsWith("7") -> "+${digits.take(11)}"
                        digits.startsWith("+") -> "+7${digits.removePrefix("+").take(10)}"
                        else -> "+7${digits.take(10)}"
                    }
                    error = null
                },
                label = { Text("Телефон") },
                placeholder = { Text("+7 (999) 123-45-67") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = ApexError, modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(24.dp))
            ApexPrimaryButton(
                text = "Получить SMS-код",
                loading = loading,
                enabled = phone.matches(Regex("^\\+7\\d{10}$")) && timerSeconds == 0,
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        try {
                            repository.sendSmsCode(phone)
                            step = 2
                            timerSeconds = 60
                            currentPhoneForTimer = phone
                            code = ""
                        } catch (e: ApiException) {
                            error = e.message
                            if (e.retryAfterSeconds != null) {
                                timerSeconds = e.retryAfterSeconds
                            }
                            showSupport = e.code.name.contains("ATTEMPTS")
                        } finally {
                            loading = false
                        }
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Демо-код для входа: ${MockData.DEMO_SMS_CODE}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        } else {
            Text("Введите код из SMS", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it.filter { c -> c.isDigit() }.take(6)
                    error = null
                },
                label = { Text("Код") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = ApexError, modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(8.dp))
            if (timerSeconds > 0) {
                Text("Отправить код повторно через $timerSeconds с", color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            } else {
                ApexSecondaryButton(
                    text = "Отправить код повторно",
                    onClick = {
                        scope.launch {
                            loading = true
                            error = null
                            try {
                                repository.sendSmsCode(phone)
                                timerSeconds = 60
                                currentPhoneForTimer = phone
                            } catch (e: ApiException) {
                                error = e.message
                                if (e.retryAfterSeconds != null) {
                                    timerSeconds = e.retryAfterSeconds
                                }
                            } finally {
                                loading = false
                            }
                        }
                    }
                )
            }
            Spacer(Modifier.height(16.dp))
            ApexPrimaryButton(
                text = "Войти",
                loading = loading,
                enabled = code.length in 4..6,
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        try {
                            repository.verifySmsCode(code)
                            onLoggedIn()
                        } catch (e: ApiException) {
                            error = e.message
                            showSupport = repository.getCodeAttemptsLeft() <= 0
                        } finally {
                            loading = false
                        }
                    }
                }
            )
            if (showSupport) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { /* external link handled in production */ }) {
                    Text("Не приходит код? Свяжитесь с поддержкой")
                }
            }
        }
    }
}