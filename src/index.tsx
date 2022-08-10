import {
  requireNativeComponent,
  UIManager,
  Platform,
  ViewStyle,
  NativeModules,
  NativeEventEmitter
} from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-awesome-module' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

type AwesomeModuleProps = {
  color: string;
  style: ViewStyle;
};

const ComponentName = 'AwesomeModuleView';

export const AwesomeModuleView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<AwesomeModuleProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

export const getConstants = () =>
  NativeModules.GCMeetPermissions.getConstants();

export interface IParamsConnection {
  roomId: string;
  displayName: string;
  isAudioOn: boolean;
  isVideoOn: boolean;
  isModerator: boolean;
  clientHostName: string;
  blurSigma: number;
}

export const openConnection = (params: IParamsConnection) =>
  NativeModules.GCMeetService.openConnection(params);
export const closeConnection = () =>
  NativeModules.GCMeetService.closeConnection();

export const authorizeForVideo = () =>
  NativeModules.GCMeetPermissions.authorizeForVideo();
export const authorizeForAudio = () =>
  NativeModules.GCMeetPermissions.authorizeForAudio();

export const enableVideo = () => NativeModules.GCMeetService.enableVideo();
export const disableVideo = () => NativeModules.GCMeetService.disableVideo();
export const switchCamera = () => NativeModules.GCMeetService.toggleCamera();
export const enableAudio = () => NativeModules.GCMeetService.enableAudio();
export const disableAudio = () => NativeModules.GCMeetService.disableAudio();

export const PeersListener = (handler: (event: any) => void) => {
  const peersEventEmitter = new NativeEventEmitter(
    NativeModules.RNEventEmitter
  );
  const peersEventListener = peersEventEmitter.addListener('onPeers', handler);
  return peersEventListener.remove;
};
