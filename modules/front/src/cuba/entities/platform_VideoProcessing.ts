import { StandardEntity } from "./base/sys$StandardEntity";
import { Node } from "./platform_Node";
import { Video } from "./platform_Video";
export class VideoProcessing extends StandardEntity {
  static NAME = "platform_VideoProcessing";
  node?: Node | null;
  video?: Video | null;
  resultVideo?: Video | null;
}
export type VideoProcessingViewName =
  | "_base"
  | "_local"
  | "_minimal"
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
      | "resultVideo"
    >
  : never;
