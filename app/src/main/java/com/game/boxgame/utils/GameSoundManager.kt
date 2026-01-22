package com.game.boxgame.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import com.game.boxgame.R

class GameSoundManager(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .build()

    private val tapSound = soundPool.load(context, R.raw.tap, 1)

    private var introPlayer: MediaPlayer? =
        MediaPlayer.create(context, R.raw.game_start)

    private var gameOverPlayer: MediaPlayer? =
        MediaPlayer.create(context, R.raw.game_start)

    fun playTap(enabled: Boolean) {
        if (enabled) {
            soundPool.play(tapSound, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playIntro(enabled: Boolean) {
        if (enabled) {
            introPlayer?.start()
        }
    }

    fun playGameOver(enabled: Boolean) {
        if (enabled) {
            gameOverPlayer?.start()
        }
    }

    fun release() {
        soundPool.release()
        introPlayer?.release()
        gameOverPlayer?.release()
    }
}
