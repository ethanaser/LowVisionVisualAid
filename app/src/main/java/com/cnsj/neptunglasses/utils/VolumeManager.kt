package com.cnsj.neptunglasses.utils

import android.content.Context
import android.media.AudioManager
import android.util.Log

/**
 * Created by MaFanwei on 2020/3/31.
 */
open class VolumeManager(private val mContext: Context) {
    private val mAudioManager: AudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val min: Int
    private val max: Int
    private var now: Int

    init {
        min = mAudioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        Log.d("TAG", "volume: "+max)
        now = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 7, 0)
    }

    fun resetVolume() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 9, 0)
    }

    fun getNowVolume(): String {
        now = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return "${now / 3}"
    }

    fun setVolumeFromOffset(offset: Int, flag: Int = 0): String {
        now = offset * 3
        if (now >= max) {
            now = max
        } else if (now <= min) {
            now = min
            playNoOperateSound()
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, now, flag)
        return "${now / 3}"
    }

    fun playNoOperateSound() {
        mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
    }
}