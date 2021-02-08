import {collection, DataCollectionStore} from "@cuba-platform/react";
import React from "react";
import {Icon} from "antd";
import {RouteComponentProps} from "react-router";
import {observer} from "mobx-react";
import {VideoProcessing} from "../../cuba/entities/platform_VideoProcessing";
import {ImageProcessing} from "../../cuba/entities/platform_ImageProcessing";

type Props = RouteComponentProps<{ videoId?: string }>;

@observer
export class Processing extends React.Component<ProcessingProps>{
  videoProcessings: DataCollectionStore<VideoProcessing> = collection<VideoProcessing>(VideoProcessing.NAME,
    {
      view: "processing-view",
      filter: {
        conditions: [
          {
            property: "video.id", operator: "=", value: this.props.videoId
          }
        ]
      },
      sort: "-updateTs"
    }
  );

  imageProcessings: DataCollectionStore<ImageProcessing> = collection<ImageProcessing>(ImageProcessing.NAME,
    {
      view: "imageProcessing-view",
      filter: {
        conditions: [
          {
            property: "images.parentVideo.id", operator: "=", value: this.props.videoId
          }
        ]
      },
      sort: "-updateTs"
    }
  );

  render() {
    const {status, items} = this.videoProcessings;
    const imageStatus = this.imageProcessings.status;
    const imageItems = this.imageProcessings.items;

    if(status === "LOADING" || imageStatus === "LOADING"){
      return <Icon type="spin"/>;
    }
    return (
      <>Not implemented</>
    );
  }
}

export interface ProcessingProps{
  videoId: string;
}

export class ProcessingManagement extends React.Component<Props>{
  render() {
    const {videoId} = this.props.match.params;
    return (videoId ? <Processing videoId={videoId}/> : <></>);
  }
}