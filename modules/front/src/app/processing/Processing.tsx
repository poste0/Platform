import {collection, DataCollectionStore} from "@cuba-platform/react";
import {Button, Icon, Table} from "antd";
import {RouteComponentProps} from "react-router";
import {observer} from "mobx-react";
import {VideoProcessing} from "../../cuba/entities/platform_VideoProcessing";
import {ImageProcessing} from "../../cuba/entities/platform_ImageProcessing";
import ReactPlayer from "react-player";
import {observable} from "mobx";
import {cubaREST} from "../../index";
import {Image} from "../../cuba/entities/platform_Image";
import React from "react";

type Props = RouteComponentProps<{ videoId?: string }>;

@observer
export class Processing extends React.Component<ProcessingProps> {
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
            property: "video.id", operator: "=", value: this.props.videoId
          }
        ]
      },
      sort: "-updateTs"
    }
  );

  @observable
  videoUrl: string;

  @observable
  images: Image[];

  render() {
    const {status} = this.videoProcessings;
    const imageStatus = this.imageProcessings.status;
    if (status === "LOADING" || imageStatus === "LOADING") {
      return <Icon type="spin"/>;
    }

    const imageProcessingFields = [
      {title: 'create_ts', dataIndex: 'createTs'},
      {
        title: 'count',
        render: (text: string, imageProcessing: ImageProcessing) => {
          return imageProcessing.images!.length;
        }
      },
      {
        render: (text: string, imageProcessing: ImageProcessing) => {
          return (
            <Button onClick={() => {
              this.images = imageProcessing.images!;
            }}>
              Images
            </Button>
          );
        }
      }
    ];

    const videoProcessingFields = [
      {title: 'create_ts', dataIndex: 'createTs'},
      {
        title: 'video',
        render: (text: string, videoProcessing: VideoProcessing) => {
          return (
            <Button onClick={() => {
              this.videoUrl = cubaREST.apiUrl + "myapi/video?videoId=" + videoProcessing.resultVideo!.id;
            }}>
              Video
            </Button>
          )
        }
      }
    ];

    const imageFields = [
      {
        title: 'image',
        render: (text: string, image: Image) => {
          return (
            <img src={cubaREST.apiUrl + "v2/files/" + image.fileDescriptor!.id + "?access_token=" + cubaREST.restApiToken} alt=""/>
          )
        }
      },
      {
        title: 'message',
        render: (text: string, image: Image) => {
          return image.message;
        }
      }
    ]

    return (
      <div>
        <Table dataSource={this.imageProcessings.items} columns={imageProcessingFields} title={() => "Image processings"}/>
        <Table dataSource={this.videoProcessings.items} columns={videoProcessingFields} title={() => "Video processings"}/>
        <ReactPlayer url={this.videoUrl} controls />
        <Table dataSource={this.images} columns={imageFields}/>
      </div>
    );
  }
}

export interface ProcessingProps {
  videoId: string;
}

export class ProcessingManagement extends React.Component<Props> {
  render() {
    const {videoId} = this.props.match.params;
    return (videoId ? <Processing videoId={videoId}/> : <></>);
  }
}