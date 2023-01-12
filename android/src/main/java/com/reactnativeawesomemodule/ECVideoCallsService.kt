package com.reactnativeawesomemodule

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.UiThreadUtil.runOnUiThread
import com.facebook.react.modules.core.DeviceEventManagerModule
import org.webrtc.VideoFrame
import world.edgecenter.videocalls.ECSession
import world.edgecenter.videocalls.localuser.LocalUserInfo
import world.edgecenter.videocalls.model.DEFAULT_LENGTH_RANDOM_STRING
import world.edgecenter.videocalls.model.UserRole
import world.edgecenter.videocalls.network.client.VideoFrameListener
import world.edgecenter.videocalls.room.RoomParams
import world.edgecenter.videocalls.utils.Utils
import world.edgecenter.videocalls.utils.image.VideoFrameConverter
import world.edgecenter.videocalls.utils.image.VideoFrameFaceDetector


class ECVideoCallsService(
  reactContext: ReactApplicationContext,
  private val application: Application,
) : ReactContextBaseJavaModule(reactContext) {

  private var lastPeer: String? = null

  private val frameConverter = VideoFrameConverter()
  private val videoFrameFaceDetector = VideoFrameFaceDetector().also {
    it.faceDetectingFrameInterval = 10
  }

  private val videoFrameListener = object : VideoFrameListener {

    override fun onFrameCaptured(frame: VideoFrame, sink: (frame: VideoFrame) -> Unit) {
        val inputImage = frameConverter.frameToInputImage(frame, frame.rotation)
        val hasFace = videoFrameFaceDetector.hasFace(inputImage)

        val blurredFrame = if (hasFace) {
          frame
        } else {
          frameConverter.blurFrame(frame, 40)
        }

        sink.invoke(blurredFrame)
    }
  }

  init {

    runOnUiThread {
      ECSession.instance.init(application)
    }

    ECSession.instance.videoFrameListener = videoFrameListener
  }

  @ReactMethod
  fun closeConnection() {
    runOnUiThread {
      ECSession.instance.close()
    }
  }

  @ReactMethod
  fun disableAudio() {
    ECSession.instance.localUser?.toggleMic(false)
  }

  @ReactMethod
  fun disableVideo() {
    ECSession.instance.localUser?.toggleCam(false)
  }

  @ReactMethod
  fun enableAudio() {
    ECSession.instance.localUser?.toggleMic(true)
  }

  @ReactMethod
  fun enableVideo() {
    ECSession.instance.localUser?.toggleCam(true)
  }

  @ReactMethod
  fun flipCamera() {
    ECSession.instance.localUser?.flipCam()
  }

  @ReactMethod
  fun openConnection(options: ReadableMap) {
    runOnUiThread {

      val userRole = when (options.getString("role") ?: "") {
        "common" -> UserRole.COMMON
        "moderator" -> UserRole.MODERATOR
        "participant" -> UserRole.PARTICIPANT
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
        startWithMic = options.getBoolean("isAudioOn"),
        isWebinar = false
      )

      ECSession.instance.setConnectionParams(userInfo, roomParams)
      ECSession.instance.connect()
    }
  }

  override fun getName(): String {
      return "ECVideoCallsService"
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
            lastPeer = null
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
