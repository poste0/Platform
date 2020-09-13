import { StandardEntity } from "./base/sys$StandardEntity";
import { Node } from "./platform_Node";
import { Image } from "./platform_Image";
export class ImageProcessing extends StandardEntity {
  static NAME = "platform_ImageProcessing";
  node?: Node | null;
  images?: Image[] | null;
}
export type ImageProcessingViewName =
  | "_minimal"
  | "_local"
  | "_base"
  | "imageProcessing-view";
export type ImageProcessingView<
  V extends ImageProcessingViewName
> = V extends "imageProcessing-view"
  ? Pick<
      ImageProcessing,
      | "id"
      | "version"
      | "createTs"
      | "createdBy"
      | "updateTs"
      | "updatedBy"
      | "deleteTs"
      | "deletedBy"
      | "node"
      | "images"
    >
  : never;
