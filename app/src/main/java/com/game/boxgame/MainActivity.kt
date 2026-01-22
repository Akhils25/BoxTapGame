package com.game.boxgame

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.boxgame.ui.theme.BoxGameTheme
import com.game.boxgame.utils.GameSoundManager
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoxGameTheme {
                GameApp()
            }
        }
    }
}

@Composable
fun GameApp() {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
    } else {
        TapGameScreen()
    }
}

@Composable
fun SplashScreen() {

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                initialScale = 0.2f,
                animationSpec = tween(1000)
            ) + fadeIn(animationSpec = tween(1000))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.Red, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_box_game),
                        contentDescription = "Game Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Box Game",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun TapGameScreen() {

    val context = LocalContext.current
    val soundManager = remember { GameSoundManager(context) }

    var soundEnabled by remember { mutableStateOf(true) }

    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }
    var gameOver by remember { mutableStateOf(false) }
    var targetColor by remember { mutableStateOf(Color.Red) }
    val animatedColor by animateColorAsState(targetColor)
    var isRunning by remember { mutableStateOf(true) }

    val boxSize = 80.dp
    val density = LocalDensity.current

    var boxX by remember { mutableFloatStateOf(0f) }
    var boxY by remember { mutableFloatStateOf(0f) }

    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    LaunchedEffect(isRunning) {
        if (isRunning && timeLeft == 30) {
            soundManager.playIntro(soundEnabled)
        }
    }

    LaunchedEffect(isRunning) {
        while (isRunning && timeLeft > 0) {
            delay(1000)
            timeLeft--
        }

        if (timeLeft == 0) {
            gameOver = true
            isRunning = false
            soundManager.playGameOver(soundEnabled)
        }
    }
    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }


    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Score: $score", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Time: $timeLeft", fontSize = 18.sp)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sound")
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )
            }
        }

        if (!gameOver) {

            Box(
                modifier = Modifier
                    .offset { IntOffset(boxX.toInt(), boxY.toInt()) }
                    .size(boxSize)
                    .background(animatedColor, RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        score++
                        targetColor = randomColor()
                        soundManager.playTap(soundEnabled)

                        val maxX = screenWidthPx - with(density) { boxSize.toPx() }
                        val maxY = screenHeightPx - with(density) { boxSize.toPx() }

                        boxX = Random.nextFloat() * maxX
                        boxY = Random.nextFloat() * maxY
                    }
            )

        } else {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Game Over",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Final Score: $score",
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = {
                    score = 0
                    timeLeft = 30
                    gameOver = false
                    isRunning = true

                    boxX = Random.nextFloat() * (screenWidthPx - with(density) { boxSize.toPx() })
                    boxY = Random.nextFloat() * (screenHeightPx - with(density) { boxSize.toPx() })
                }) {
                    Text("Play Again")
                }
            }
        }
    }
}

fun randomColor(): Color {
    return Color(
        red = Random.nextFloat(),
        green = Random.nextFloat(),
        blue = Random.nextFloat(),
        alpha = 1f
    )
}


