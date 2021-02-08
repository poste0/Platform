import { StandardEntity } from "./base/sys$StandardEntity";
import { FileDescriptor } from "./base/sys$FileDescriptor";
import { Video } from "./platform_Video";
import { ImageProcessing } from "./platform_ImageProcessing";
export class Image extends StandardEntity {
  static NAME = "platform_Image";
  name?: string | null;
  fileDescriptor?: FileDescriptor | null;
  parentVideo?: Video | null;
  imageProcessing?: ImageProcessing | null;
  message?: string | null;
}
export type ImageViewName = "_base" | "_local" | "_minimal" | "image-view";
export type ImageView<V extends ImageViewName> = V extends "_base"
  ? Pick<Image, "id" | "name" | "message">
  : V extends "_local"
  ? Pick<Image, "id" | "name" | "message">
  : V extends "image-view"
  ? Pick<
      Image,
      "id" | "parentVideo" | "fileDescriptor" | "imageProcessing" | "message"
    >
  : never;
