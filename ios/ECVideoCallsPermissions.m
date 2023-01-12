#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(ECVideoCallsPermissions, NSObject)
    RCT_EXTERN_METHOD(
      authorizeForVideo: (RCTPromiseResolveBlock)resolve
      rejecter: (RCTPromiseRejectBlock)reject
    )

    RCT_EXTERN_METHOD(
      authorizeForAudio: (RCTPromiseResolveBlock)resolve
      rejecter: (RCTPromiseRejectBlock)reject
    )
@end
