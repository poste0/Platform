import * as React from "react";
import { observer } from "mobx-react";
import { Video } from "../../cuba/entities/platform_Video";
import {Button, Card, Icon, message, Row} from "antd";
import {collection, EntityProperty, getCubaREST, injectMainStore, MainStoreInjected} from "@cuba-platform/react";
import ReactPlayer from "react-player";
import {cubaREST} from "../../index";
import {SerializedEntity} from "@cuba-platform/rest";
import {showDeletionDialog} from "../App";
import {FormattedMessage, injectIntl, WrappedComponentProps} from "react-intl";
import {observable} from "mobx";
import ButtonGroup from "antd/es/button/button-group";

@injectMainStore
@observer
class VideoCardsComponent extends React.Component<MainStoreInjected & WrappedComponentProps> {
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
            title={
              <Row>
                {e.name}
                <ButtonGroup style={
                  {marginLeft: '50%'}
                }>
                  <Button
                    onClick={() => {
                      showDeletionDialog(this.props, e, "platform_Video", (result) => {
                        this.dataCollection.load();
                      });
                    }}
                  >
                    <FormattedMessage id="management.browser.remove"/>
                  </Button>
                </ButtonGroup>
              </Row>
            }
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

export default injectIntl(VideoCardsComponent)
