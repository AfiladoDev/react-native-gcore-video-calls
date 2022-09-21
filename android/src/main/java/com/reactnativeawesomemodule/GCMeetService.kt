package com.reactnativeawesomemodule

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.*
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.UiThreadUtil.runOnUiThread
import gcore.videocalls.meet.GCoreMeet
import gcore.videocalls.meet.localuser.LocalUserInfo
import gcore.videocalls.meet.model.DEFAULT_LENGTH_RANDOM_STRING
import gcore.videocalls.meet.model.UserRole
import gcore.videocalls.meet.room.RoomParams
import gcore.videocalls.meet.utils.Utils


class GCMeetService(
  private val application: Application,
  private val reactContext: ReactApplicationContext,
) : ReactContextBaseJavaModule(reactContext) {

  private var lastPeer: String? = null

  init {
    runOnUiThread {
      GCoreMeet.instance.init(application)
    }
  }

  @ReactMethod
  fun closeConnection() {
    runOnUiThread {
      GCoreMeet.instance.close()
    }
  }

  @ReactMethod
  fun disableAudio() {
    GCoreMeet.instance.localUser?.toggleMic(false)
  }

  @ReactMethod
  fun disableVideo() {
    GCoreMeet.instance.localUser?.toggleCam(false)
  }

  @ReactMethod
  fun enableAudio() {
    GCoreMeet.instance.localUser?.toggleMic(true)
  }

  @ReactMethod
  fun enableVideo() {
    GCoreMeet.instance.localUser?.toggleCam(true)
  }

  @ReactMethod
  fun flipCamera() {
    GCoreMeet.instance.localUser?.flipCam()
  }

  @ReactMethod
  fun openConnection(options: ReadableMap) {
    runOnUiThread {

      val userRole = when (options.getString("role") ?: "") {
        "common" -> UserRole.COMMON
        "moderator" -> UserRole.MODERATOR
        else -> UserRole.UNKNOWN
      }
      val userInfo = LocalUserInfo(
        displayName = options.getString("displayName") ?: "User${Utils.getRandomString(3)}",
        role = userRole,
        userId = options.getString("userId") ?: Utils.getRandomString(DEFAULT_LENGTH_RANDOM_STRING)
      )

      val roomParams = RoomParams(
        roomId = options.getString("roomId") ?: "",
        hostName = options.getString("clientHostName") ?: "",
        startWithCam = options.getBoolean("isVideoOn"),
        startWithMic = options.getBoolean("isAudioOn")
      )

      GCoreMeet.instance.setConnectionParams(userInfo, roomParams)
      GCoreMeet.instance.connect()
    }
  }

  override fun getName(): String {
    return "GCMeetService"
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
      GCoreMeet.instance.roomState.remoteUsers.observeForever { remoteUsers ->
        remoteUsers?.list?.let { users ->
          if (users.isNotEmpty()) {
            Log.d("qwe", eventName + " " + users[0].id)
            lastPeer = users[0].id
            sendEvent(reactContext, "onPeerHandle", users[0].id)
          } else if (users.isEmpty() && lastPeer != null) {
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
