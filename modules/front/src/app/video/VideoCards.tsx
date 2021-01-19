import * as React from "react";
import { observer } from "mobx-react";
import { Video } from "../../cuba/entities/platform_Video";
import {Card, Icon, message} from "antd";
import {collection, EntityProperty, getCubaREST} from "@cuba-platform/react";
import ReactPlayer from "react-player";
import {cubaREST} from "../../index";

@observer
export class VideoCards extends React.Component {
  dataCollection = collection<Video>(Video.NAME, {
    view: "video-view",
    sort: "-updateTs"
  });
  fields = ["name", "parentName", "status", "message"];

  render() {
    const { status, items } = this.dataCollection;

    if (status === "LOADING") {
      return <Icon type="spin" />;
    }

    let request = new XMLHttpRequest();
    request.open("GET", "http://localhost:8081/app/rest/myapi/hello");
    request.send();
    request.addEventListener("load", () => {
      message.info(request.responseText);
    })

    return (
      <div className="narrow-layout">
        {items.map(e => (
          <Card
            title={e.name}
            key={e.id}
            style={{ marginBottom: "12px" }}
          >
            {this.fields.map(p => (
              <EntityProperty
                entityName={Video.NAME}
                propertyName={p}
                value={e[p]}
                key={p}
              />
            ))}
            <ReactPlayer url={cubaREST.apiUrl + "myapi/video?videoId=" + e.id} controls/>
          </Card>
        ))}
      </div>
    );
  }
}
