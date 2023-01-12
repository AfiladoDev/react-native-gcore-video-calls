package com.reactnativeawesomemodule

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import world.edgecenter.videocalls.ECSession
import world.edgecenter.videocalls.logger.LLog
import world.edgecenter.videocalls.ui.view.remoteuser.RemoteUserVideoView

class ECRemoteViewManager(var mCallerContext: ReactApplicationContext) :
  SimpleViewManager<RemoteUserVideoView>() {

  override fun getName(): String {
    return "ECRemoteView"
  }

  override fun onDropViewInstance(view: RemoteUserVideoView) {
    view.release()
    super.onDropViewInstance(view)
  }

  override fun createViewInstance(reactContext: ThemedReactContext): RemoteUserVideoView {
    val view = RemoteUserVideoView(reactContext.baseContext)

    ECSession.instance.roomState.remoteUsers.observeForever { remoteUsers ->
      remoteUsers?.list?.let { users ->
        if (users.isNotEmpty()) {
          LLog.d("ReactRemoteViewManager", "connected remote user: ${users[0].id}")
          view.connect(users[0].id)
        }
      }
    }
    return view
  }
}
