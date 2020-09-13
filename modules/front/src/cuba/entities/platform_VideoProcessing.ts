import { StandardEntity } from "./base/sys$StandardEntity";
import { Node } from "./platform_Node";
import { Video } from "./platform_Video";
export class VideoProcessing extends StandardEntity {
  static NAME = "platform_VideoProcessing";
  node?: Node | null;
  video?: Video | null;
}
export type VideoProcessingViewName =
  | "_minimal"
  | "_local"
  | "_base"
  | "processing-view";
export type VideoProcessingView<
  V extends VideoProcessingViewName
> = V extends "processing-view"
  ? Pick<
      VideoProcessing,
      | "id"
      | "version"
      | "createTs"
      | "createdBy"
      | "updateTs"
      | "updatedBy"
      | "deleteTs"
      | "deletedBy"
      | "node"
      | "video"
    >
  : never;
