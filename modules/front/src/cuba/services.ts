import { CubaApp, FetchOptions } from "@cuba-platform/rest";

export var restServices = {
  platform_RegistrationService: {
    register: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (params: any) => {
      return cubaApp.invokeService(
        "platform_RegistrationService",
        "register",
        params,
        fetchOpts
      )
    }
  },
  platform_CameraService: {
    getStatus: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (params: any) => {
      return cubaApp.invokeService(
        "platform_CameraService",
        "getStatus",
        params,
        fetchOpts
      )
    },

    init: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => () => {
      return cubaApp.invokeService(
        "platform_CameraService",
        "init",
        fetchOpts
      )
    },

    update: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (params: any) => {
      return cubaApp.invokeService(
        "platform_CameraService",
        "update",
        params,
        fetchOpts
      )
    },

    write: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (params: any) => {
      return cubaApp.invokeService(
        "platform_CameraService",
        "write",
        params,
        fetchOpts
      )
    },

    stop: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (params: any) => {
      return cubaApp.invokeService(
        "platform_CameraService",
        "stop",
        params,
        fetchOpts
      )
    }
  }
};