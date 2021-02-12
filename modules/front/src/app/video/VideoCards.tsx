import * as React from "react";
import {observer} from "mobx-react";
import {Video} from "../../cuba/entities/platform_Video";
import {Button, Card, Icon, Input, message, Modal, Row, Select, Table} from "antd";
import {collection, injectMainStore, MainStoreInjected} from "@cuba-platform/react";
import ReactPlayer from "react-player";
import {cubaREST} from "../../index";
import {showDeletionDialog} from "../App";
import {FormattedMessage, injectIntl, WrappedComponentProps} from "react-intl";
import {action, observable} from "mobx";
import {ChangeEvent, ReactElement} from "react";
import {restServices} from "../../cuba/services";
import {Node} from "../../cuba/entities/platform_Node";
import {Link} from "react-router-dom";

@injectMainStore
@observer
class VideoCardsComponent extends React.Component<MainStoreInjected & WrappedComponentProps> {
  dataCollection = collection<Video>(Video.NAME, {
    view: "video-view",
    sort: "-updateTs",
    filter: {
      conditions: [
        {
          property: "camera.user.login", operator: "=", value: this.props.mainStore!.userName!
        }
      ]
    }
  });

  fields = ["name", "parentName", "status", "message"];

  @observable
  login: string;

  @observable
  password: string;

  @action
  changeLogin = (e: ChangeEvent<HTMLInputElement>) => {
    this.login = e.target.value;
  }

  @action
  changePassword = (e: ChangeEvent<HTMLInputElement>) => {
    this.password = e.target.value;
  }

  nodes: Node [] = [];

  areNodesLoaded: boolean = false;

  constructor(props: any) {
    super(props);
    this.getConnectedNodes();
  }


  private getConnectedNodes() {
    restServices.platform_NodeService.getConnectedNodes(cubaREST)()
      .then((result: string) => {
        let nodes: Node [] = JSON.parse(result);
        nodes.forEach((node) => {
          this.nodes.push(node);
        })
      })
      .then((result) => {
        this.areNodesLoaded = true;
      });
  }

  render() {
    const {status, items} = this.dataCollection;

    if (status === "LOADING" || !this.areNodesLoaded) {
      return <Icon type="spin"/>;
    }

    return (
      <div className="narrow-layout" id="mainDiv">
        {items.map(e => {
          let processButton: ReactElement = e.status === "ready" || e.status === "error" ?
            <Button
              htmlType="button"
              style={{margin: "0 12px 12px 0"}}
              onClick={() => {
                let loginInput: ReactElement<Input> =  <Input
                  id="input_login"
                  placeholder={this.props.intl.formatMessage({
                    id: "login.placeholder.login"
                  })}
                  prefix={<Icon type="user" style={{color: "rgba(0,0,0,.25)"}}/>}
                  size="large"
                  onChange={(e) => {login = e.target.value}}/>;

                let passwordInput: ReactElement<Input> =  <Input
                  id="input_password"
                  placeholder={this.props.intl.formatMessage({
                    id: "login.placeholder.password"
                  })}
                  type="password"
                  prefix={<Icon type="lock" style={{color: "rgba(0,0,0,.25)"}}/>}
                  size="large"
                  onChange={(e) => {password = e.target.value}}/>;

                let nodeSelect: ReactElement<Select> = <Select style={{width: "100%"}} onChange={(e) => {node = e.toString()}} placeholder={this.props.intl.formatMessage({id: "node"})}>
                  {this.nodes.map((node) => {
                    return (
                      <Select.Option value={node.id}>
                        {node.name}
                      </Select.Option>
                    )
                  })}
                </Select>;

                let nodeInput: ReactElement<Input> = <Input.Group>
                  {nodeSelect}
                </Input.Group>;

                let password: string = '';
                let login: string = '';
                let node = '';

                Modal.confirm(
                  {
                    content: [loginInput, passwordInput, nodeInput],
                    okButtonProps: {disabled: this.nodes.length === 0},
                    onOk: () => {
                      if(login === this.props.mainStore!.userName){
                        this.props.mainStore!.login(login, password)
                          .then(() => {
                            restServices.platform_NodeService.processVideo(cubaREST)({nodeId: node, videoId: e.id, login: login, password: password})
                              .then(() => {
                                e.status = "processing";
                              });
                          })
                          .catch(() => {
                            message.error(this.props.intl.formatMessage({id: "login.failed"}));
                          });
                      }
                      else{
                        message.error(this.props.intl.formatMessage({id: "login.failed"}));
                      }
                    },
                  }
                );
              }
              }
            ><FormattedMessage id="process"/></Button> : <></>

          let showProcessingButton: ReactElement<Link> = <Link to={"processing/" + e.id}>
            <Button
              htmlType="button"
              style={{margin: "0 12px 12px 0"}}
            >
              {this.props.intl.formatMessage({id: "show_processings"})}
            </Button>
          </Link>

          return (
            <Card
              title={
                <Row>
                  <Button
                    htmlType="button"
                    style={{margin: "0 12px 12px 0"}}
                    onClick={() => {
                      showDeletionDialog(this.props, e, "platform_Video", (result) => {
                        this.dataCollection.load();
                      });
                    }}>
                    <FormattedMessage id="management.browser.remove"/>
                  </Button>
                  {processButton}
                  {showProcessingButton}
                </Row>
              }
              key={e.id}
              style={{marginBottom: "12px"}}>
              <Table<Video>
                columns={[
                  {
                    title: this.props.intl.formatMessage({id: 'name'}),
                    key: "name",
                    dataIndex: "name"
                  },
                  {
                    title: this.props.intl.formatMessage({id: 'status'}),
                    key: "status",
                    dataIndex: "status",
                    render: (text: string, video: Video) => {
                      return (
                        <>
                          {this.props.intl.formatMessage({id: String(video.status)})}
                        </>
                      )
                    }
                  }

                ]}
                dataSource={[e]}
                pagination={false}>

              </Table>
              <ReactPlayer url={cubaREST.apiUrl + "myapi/video?videoId=" + e.id} controls/>
            </Card>
          )
        })}
      </div>
    );
  }
}

export default injectIntl(VideoCardsComponent)
