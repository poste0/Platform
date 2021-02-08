import { CubaApp, FetchOptions } from "@cuba-platform/rest";

export type queries_Camera_getCameras_params = {
  id: string;
};

export var restQueries = {
  Camera: {
    getCameras: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (
      params: queries_Camera_getCameras_params
    ) => {
      return cubaApp.query("platform_Camera", "getCameras", params, fetchOpts);
    },
    getCamerasCount: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (
      params: queries_Camera_getCameras_params
    ) => {
      return cubaApp.queryCount(
        "platform_Camera",
        "getCameras",
        params,
        fetchOpts
      );
    },
    getCamerasWithCount: (cubaApp: CubaApp, fetchOpts?: FetchOptions) => (
      params: queries_Camera_getCameras_params
    ) => {
      return cubaApp.queryWithCount(
        "platform_Camera",
        "getCameras",
        params,
        fetchOpts
      );
    }
  }
};
