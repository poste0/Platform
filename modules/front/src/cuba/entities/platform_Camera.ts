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
  status?: string | null;
}
export type CameraViewName = "_minimal" | "_local" | "_base";
export type CameraView<V extends CameraViewName> = V extends "_local"
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
      | "status"
    >
  : V extends "_base"
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
      | "status"
    >
  : never;
