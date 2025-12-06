package edu.farmingdale.threadsexample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FibonacciDemoNoBgThrd() {
    var answer by remember { mutableStateOf("") }
    var textInput by remember { mutableStateOf("40") }

    Column {
        Row {
            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Number?") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
            Button(onClick = {
                val num = textInput.toLongOrNull() ?: 0
                val fibNumber = fibonacci(num)        // runs on main thread
                answer = NumberFormat.getNumberInstance(Locale.US).format(fibNumber)
            }) {
                Text("Fibonacci")
            }
        }

        Text("Result (no bg thread): $answer")
    }
}

@Composable
fun FibonacciDemoWithCoroutine() {
    var answer by remember { mutableStateOf("") }
    var textInput by remember { mutableStateOf("40") }
    var isCalculating by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column {
        Row {
            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Number?") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
            Button(onClick = {
                val num = textInput.toLongOrNull() ?: 0
                isCalculating = true
                answer = ""

                // Heavy work moved off the main thread
                scope.launch(Dispatchers.Default) {
                    val fibNumber = fibonacci(num)
                    val formatted = NumberFormat.getNumberInstance(Locale.US).format(fibNumber)

                    withContext(Dispatchers.Main) {
                        answer = formatted
                        isCalculating = false
                    }
                }
            }) {
                Text(if (isCalculating) "Working..." else "Fibonacci (Coroutine)")
            }
        }

        if (isCalculating) {
            Text("Calculating in background…")
        }

        Text("Result (coroutine): $answer")
    }
}

fun fibonacci(n: Long): Long {
    return if (n <= 1) n else fibonacci(n - 1) + fibonacci(n - 2)
}
