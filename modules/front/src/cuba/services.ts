import { CubaApp, FetchOptions } from "@cuba-platform/rest";

export type platform_CameraService_getStatus_params = {
  camera: any;
};

export type platform_CameraService_stop_params = {
  camera: any;
};

export type platform_CameraService_update_params = {
  camera: any;
};

export type platform_CameraService_write_params = {
  camera: any;
};

export type platform_RegistrationService_register_params = {
  login: any;
  password: any;
  name: any;
};

export type platform_StreamService_startStream_params = {
  camera: any;
};

export type platform_StreamService_stopStream_params = {
  camera: any;
};

export var restServices = {
  platform_CameraService: {
    getCameras: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => () => {
      return cubaApp.invokeService(
        "platform_CameraService",
        "getCameras",
        {},
        fetchOpts
      );
    },
    getStatus: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (
      params: platform_CameraService_getStatus_params
    ) => {
      return cubaApp.invokeService(
        "platform_CameraService",
        "getStatus",
        params,
        fetchOpts
      );
    },
    init: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => () => {
      return cubaApp.invokeService(
        "platform_CameraService",
        "init",
        {},
        fetchOpts
      );
    },
    stop: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (
      params: platform_CameraService_stop_params
    ) => {
      return cubaApp.invokeService(
        "platform_CameraService",
        "stop",
        params,
        fetchOpts
      );
    },
    update: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (
      params: platform_CameraService_update_params
    ) => {
      return cubaApp.invokeService(
        "platform_CameraService",
        "update",
        params,
        fetchOpts
      );
    },
    write: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (
      params: platform_CameraService_write_params
    ) => {
      return cubaApp.invokeService(
        "platform_CameraService",
        "write",
        params,
        fetchOpts
      );
    }
  },
  platform_RegistrationService: {
    register: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (
      params: platform_RegistrationService_register_params
    ) => {
      return cubaApp.invokeService(
        "platform_RegistrationService",
        "register",
        params,
        fetchOpts
      );
    }
  },
  platform_StreamService: {
    init: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => () => {
      return cubaApp.invokeService(
        "platform_StreamService",
        "init",
        {},
        fetchOpts
      );
    },
    startStream: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (
      params: platform_StreamService_startStream_params
    ) => {
      return cubaApp.invokeService(
        "platform_StreamService",
        "startStream",
        params,
        fetchOpts
      );
    },
    stopStream: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (
      params: platform_StreamService_stopStream_params
    ) => {
      return cubaApp.invokeService(
        "platform_StreamService",
        "stopStream",
        params,
        fetchOpts
      );
    }
  },
  platform_NodeService: {
    getCpu: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (params: any) => {
      return cubaApp.invokeService(
        "platform_NodeService",
        "getCpu",
        params,
        fetchOpts
      );
    },
    getGpu: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (params: any) => {
      return cubaApp.invokeService(
        "platform_NodeService",
        "getGpu",
        params,
        fetchOpts
      );
    },
    getStatus: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (params: any) => {
      return cubaApp.invokeService(
        "platform_NodeService",
        "getStatus",
        params,
        fetchOpts
      );
    },
    getNodes: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => () => {
      return cubaApp.invokeService(
        "platform_NodeService",
        "getNodes",
        fetchOpts
      );
    },
  }
};
