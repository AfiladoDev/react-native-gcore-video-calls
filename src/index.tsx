import type { PropsWithChildren } from 'react';
import {
  requireNativeComponent,
  StyleProp,
  UIManager,
  Platform,
  ViewStyle,
  NativeModules,
  NativeEventEmitter,
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
  NativeModules.ECVideoCallsPermissions.getConstants();

export interface IParamsConnection {
  roomId: string;
  displayName: string;
  isAudioOn: boolean;
  isVideoOn: boolean;
  isModerator: boolean;
  clientHostName: string;
  role: 'common' | 'moderator';
  userId: string; // Deprecated
  blurSigma: number; // Deprecated
}

export const openConnection = (params: IParamsConnection) =>
  NativeModules.ECVideoCallsService.openConnection(params);
export const closeConnection = () =>
  NativeModules.ECVideoCallsService.closeConnection();

export const authorizeForVideo = async () =>
  await NativeModules.ECVideoCallsPermissions.authorizeForVideo();
export const authorizeForAudio = async () =>
  await NativeModules.ECVideoCallsPermissions.authorizeForAudio();

export const enableVideo = () => NativeModules.ECVideoCallsService.enableVideo();
export const disableVideo = () => NativeModules.ECVideoCallsService.disableVideo();
export const switchCamera = () => NativeModules.ECVideoCallsService.flipCamera();
export const enableAudio = () => NativeModules.ECVideoCallsService.enableAudio();
export const disableAudio = () => NativeModules.ECVideoCallsService.disableAudio();

export const PeersListener = (
  eventName: string,
  handler: (event: any) => void
) => {
  const ECVideoCallsServiceEventEmitter = new NativeEventEmitter(
    NativeModules.ECVideoCallsService
  );
  const ECVideoCallsServiceEventListener = ECVideoCallsServiceEventEmitter.addListener(
    eventName,
    handler
  );
  return ECVideoCallsServiceEventListener.remove;
};

interface ViewProps extends PropsWithChildren<any> {
  style: StyleProp<ViewStyle>;
}

export const ECRemoteView = requireNativeComponent<ViewProps>('ECRemoteView');
export const ECLocalView = requireNativeComponent<ViewProps>('ECLocalView');
