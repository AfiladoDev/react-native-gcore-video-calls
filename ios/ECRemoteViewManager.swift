import Foundation

@objc(ECRemoteViewManager)
class ECRemoteViewManager: RCTViewManager {

  override func view() -> UIView! {
      ECViewsEnum.remote.contentMode = .scaleAspectFit
      return ECViewsEnum.remote
  }

  @objc override static func requiresMainQueueSetup() -> Bool {
    return true
  }
}
