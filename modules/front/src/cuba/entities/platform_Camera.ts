import { StandardEntity } from "./base/sys$StandardEntity";
import { User } from "./base/sec$User";
import { Video } from "./platform_Video";
export class Camera extends StandardEntity {
  static NAME = "platform_Camera";
  address?: string | null;
  urlAddress?: string | null;
  port?: number | null;
  name?: string | null;
  user?: User | null;
  height?: number | null;
  weight?: number | null;
  frameRate?: number | null;
  path?: string | null;
  videos?: Video[] | null;
  status?: any | null;

  static copy(camera: Camera): Camera {
    let result: Camera = new Camera();
    result.id = camera.id;
    result.address = camera.address;
    result.urlAddress = camera.urlAddress;
    result.port = camera.port;
    result.name = camera.name;
    result.height = camera.height;
    result.weight = camera.weight;
    result.frameRate = camera.frameRate;
    result.path = camera.path;

    return result;
  }
}
export type CameraViewName = "_base" | "_local" | "_minimal";
export type CameraView<V extends CameraViewName> = V extends "_base"
  ? Pick<
      Camera,
      | "id"
      | "address"
      | "urlAddress"
      | "port"
      | "name"
      | "height"
      | "weight"
      | "frameRate"
      | "path"
    >
  : V extends "_local"
  ? Pick<
      Camera,
      | "id"
      | "address"
      | "urlAddress"
      | "port"
      | "name"
      | "height"
      | "weight"
      | "frameRate"
      | "path"
    >
  : never;
