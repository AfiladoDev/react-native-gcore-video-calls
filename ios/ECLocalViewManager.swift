import Foundation

@objc(ECLocalViewManager)
class ECLocalViewManager: RCTViewManager {

  override func view() -> UIView! {
    return ECViewsEnum.local
  }

  @objc override static func requiresMainQueueSetup() -> Bool {
    return true
  }
}
