package com.reactnativeawesomemodule

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log

import com.facebook.react.bridge.*
import com.facebook.react.bridge.UiThreadUtil.runOnUiThread
import com.facebook.react.modules.core.DeviceEventManagerModule
import gcore.videocalls.meet.GCoreMeet

class GCMeetService(val reactContext: ReactApplicationContext, private val application: Application) : ReactContextBaseJavaModule(reactContext) {

  lateinit var audioManager: AudioManager
  var lastPeer: String? = null

  override fun getName(): String {
    return "GCMeetService";
  }

  init {
    runOnUiThread {
      GCoreMeet.instance.init(application, null, false)
    }
  }

  @ReactMethod
  fun openConnection(roomOptions: ReadableMap) {
    runOnUiThread {
      GCoreMeet.instance.roomManager.peerId = roomOptions.getString("peerId") ?: ""
      GCoreMeet.instance.roomManager.roomId = roomOptions.getString("roomId") ?: ""
      GCoreMeet.instance.roomManager.displayName = roomOptions.getString("displayName") ?: ""
      GCoreMeet.instance.roomManager.isModer = roomOptions.getBoolean("isModerator")

      GCoreMeet.instance.startConnection(reactContext)

      GCoreMeet.instance.roomManager.options.startWithCam = roomOptions.getBoolean("isVideoOn")
      GCoreMeet.instance.roomManager.options.startWithMic = roomOptions.getBoolean("isAudioOn")
      audioManager = reactContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
      audioManager.isSpeakerphoneOn = true

      if(GCoreMeet.instance.roomManager.isClosed())
        GCoreMeet.instance.roomManager.join()
    }
  }

  @ReactMethod
  fun closeConnection() {
    runOnUiThread {
      GCoreMeet.instance.roomManager.destroyRoom()
    }
  }

  @ReactMethod
  fun enableVideo() {
    GCoreMeet.instance.roomManager.enableCam()
  }

  @ReactMethod
  fun disableVideo() {
    GCoreMeet.instance.roomManager.disableCam()
  }

  @ReactMethod
  fun toggleVideo() {
    GCoreMeet.instance.roomManager.disableEnableCam()
  }

  @ReactMethod
  fun enableAudio() {
    if(!GCoreMeet.instance.roomManager.isMicEnabled())
      GCoreMeet.instance.roomManager.enableMic()

    GCoreMeet.instance.roomManager.unmuteMic()
  }

  @ReactMethod
  fun disableAudio() {
    GCoreMeet.instance.roomManager.muteMic()
  }

  @ReactMethod
  fun toggleAudio() {
    if(!GCoreMeet.instance.roomManager.isMicEnabled())
      GCoreMeet.instance.roomManager.enableMic()

    GCoreMeet.instance.roomManager.muteUnMuteMic()
  }

  @ReactMethod
  fun toggleCamera() {
    GCoreMeet.instance.roomManager.changeCam()
    Log.d("qwe", "toggle cam")
  }

  private fun sendEvent(reactContext: ReactContext, eventName: String, params: String?) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  @ReactMethod
  fun addListener(eventName: String) {
    // Set up any upstream listeners or background tasks as necessary
    Log.d("qwe", eventName)
    Handler(Looper.getMainLooper()).post {
      GCoreMeet.instance.getPeers().observeForever { peers ->
        peers?.allPeers?.let {
          if(it.isNotEmpty()){
            Log.d("qwe", eventName + " " + it[0].id)
            lastPeer = it[0].id
            sendEvent(reactContext, "onPeerHandle", it[0].id)
          } else if (it.isEmpty() && lastPeer != null) {
            Log.d("qwe", eventName + " " + lastPeer)
            sendEvent(reactContext, "onPeerClosed", lastPeer)
          }
        }
      }
    }
  }

  @ReactMethod
  fun removeListeners(count: Int) {
    // Remove upstream listeners, stop unnecessary background tasks
  }
}
