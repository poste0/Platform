import { StandardEntity } from "./base/sys$StandardEntity";
import { FileDescriptor } from "./base/sys$FileDescriptor";
import { Camera } from "./platform_Camera";
import { VideoProcessing } from "./platform_VideoProcessing";
export class Video extends StandardEntity {
  static NAME = "platform_Video";
  name?: string | null;
  fileDescriptor?: FileDescriptor | null;
  camera?: Camera | null;
  parentName?: string | null;
  status?: string | null;
  parentVideo?: Video | null;
  videoProcessing?: VideoProcessing | null;
  message?: string | null;
}
export type VideoViewName = "_base" | "_local" | "_minimal" | "video-view";
export type VideoView<V extends VideoViewName> = V extends "_base"
  ? Pick<Video, "id" | "name" | "parentName" | "status" | "message">
  : V extends "_local"
  ? Pick<Video, "id" | "name" | "parentName" | "status" | "message">
  : V extends "video-view"
  ? Pick<
      Video,
      | "id"
      | "name"
      | "parentName"
      | "status"
      | "message"
      | "camera"
      | "fileDescriptor"
      | "parentVideo"
      | "videoProcessing"
    >
  : never;
