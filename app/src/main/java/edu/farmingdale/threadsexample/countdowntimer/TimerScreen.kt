package edu.farmingdale.threadsexample.countdowntimer

import android.media.MediaPlayer
import android.util.Log
import android.widget.NumberPicker
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.farmingdale.threadsexample.R
import java.text.DecimalFormat
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    timerViewModel: TimerViewModel = viewModel()
) {
    val context = LocalContext.current

    var hasPlayedFinishSound by remember { mutableStateOf(false) }

    val targetProgress =
        if (timerViewModel.totalMillis > 0L)
            timerViewModel.remainingMillis.toFloat() / timerViewModel.totalMillis.toFloat()
        else
            0f

    val animatedProgress by animateFloatAsState(
        targetValue = if (timerViewModel.isRunning) targetProgress else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "timerProgress"
    )

    LaunchedEffect(timerViewModel.remainingMillis, timerViewModel.isRunning) {
        if (!timerViewModel.isRunning &&
            timerViewModel.totalMillis > 0L &&
            timerViewModel.remainingMillis == 0L &&
            !hasPlayedFinishSound
        ) {
            hasPlayedFinishSound = true
            try {
                val player = MediaPlayer.create(context, R.raw.timer_finished)
                player?.setOnCompletionListener { it.release() }
                player?.start()
            } catch (e: Exception) {
                Log.e("TimerScreen", "Error playing sound", e)
            }
        }

        if (timerViewModel.isRunning) {
            hasPlayedFinishSound = false
        }
    }

    val isLastTenSeconds =
        timerViewModel.isRunning && timerViewModel.remainingMillis in 1_000L..10_000L

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = modifier
                .padding(20.dp)
                .size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            //ToDo 5: visual indicator of time left
            if (timerViewModel.totalMillis > 0L) {
                CircularProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.matchParentSize(),
                    strokeWidth = 8.dp
                )
            }

            // ToDo 4: Make text larger
            Text(
                text = timerText(timerViewModel.remainingMillis),
                fontSize = 64.sp,
                color = if (isLastTenSeconds) Color.Red else Color.Unspecified,
                fontWeight = if (isLastTenSeconds) FontWeight.Bold else FontWeight.Normal
            )
        }

        TimePicker(
            hour = timerViewModel.selectedHour,
            min = timerViewModel.selectedMinute,
            sec = timerViewModel.selectedSecond,
            onTimePick = timerViewModel::selectTime
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.padding(top = 50.dp)
        ) {
            if (timerViewModel.isRunning) {
                Button(
                    onClick = timerViewModel::cancelTimer
                ) {
                    Text("Cancel")
                }
            } else {
                Button(
                    enabled = timerViewModel.selectedHour +
                            timerViewModel.selectedMinute +
                            timerViewModel.selectedSecond > 0,
                    onClick = timerViewModel::startTimer
                ) {
                    Text("Start")
                }
            }

            //ToDo 6: Reset button
            Button(
                onClick = timerViewModel::resetTimer,
                enabled = timerViewModel.totalMillis > 0L,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text("Reset")
            }
        }
    }
}

fun timerText(timeInMillis: Long): String {
    val duration: Duration = timeInMillis.milliseconds
    return String.format(
        Locale.getDefault(), "%02d:%02d:%02d",
        duration.inWholeHours, duration.inWholeMinutes % 60, duration.inWholeSeconds % 60
    )
}

@Composable
fun TimePicker(
    hour: Int = 0,
    min: Int = 0,
    sec: Int = 0,
    onTimePick: (Int, Int, Int) -> Unit = { _: Int, _: Int, _: Int -> }
) {
    // Values must be remembered for calls to onPick()
    var hourVal by remember { mutableIntStateOf(hour) }
    var minVal by remember { mutableIntStateOf(min) }
    var secVal by remember { mutableIntStateOf(sec) }

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Hours")
            NumberPickerWrapper(
                initVal = hourVal,
                maxVal = 99,
                onNumPick = {
                    hourVal = it
                    onTimePick(hourVal, minVal, secVal)
                }
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
        ) {
            Text("Minutes")
            NumberPickerWrapper(
                initVal = minVal,
                onNumPick = {
                    minVal = it
                    onTimePick(hourVal, minVal, secVal)
                }
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Seconds")
            NumberPickerWrapper(
                initVal = secVal,
                onNumPick = {
                    secVal = it
                    onTimePick(hourVal, minVal, secVal)
                }
            )
        }
    }
}

@Composable
fun NumberPickerWrapper(
    initVal: Int = 0,
    minVal: Int = 0,
    maxVal: Int = 59,
    onNumPick: (Int) -> Unit = {}
) {
    val numFormat = NumberPicker.Formatter { i: Int ->
        DecimalFormat("00").format(i)
    }

    AndroidView(
        factory = { context ->
            NumberPicker(context).apply {
                setOnValueChangedListener { _, _, newVal -> onNumPick(newVal) }
                minValue = minVal
                maxValue = maxVal
                value = initVal
                setFormatter(numFormat)
            }
        }
    )
}
