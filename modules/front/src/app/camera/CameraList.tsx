import * as React from "react";
import {observer} from "mobx-react";
import {Link} from "react-router-dom";

import {observable} from "mobx";

import {Modal, Button, Table, message, Tooltip, Row} from "antd";

import {cubaREST} from "../../index";

import {
  injectMainStore,
  MainStoreInjected
} from "@cuba-platform/react";

import {Camera} from "../../cuba/entities/platform_Camera";
import {CameraManagement} from "./CameraManagement";
import {
  FormattedMessage,
  injectIntl,
  WrappedComponentProps
} from "react-intl";
import {restServices} from "../../cuba/services";

import {CheckCircleTwoTone, CloseCircleTwoTone, VideoCameraTwoTone} from "@ant-design/icons";
import {render} from "react-dom";
import {deleteFromDataSource, getAll, showDeletionDialog} from "../App";
import ReactPlayer from "react-player";
import {ReactElement} from "react";

@injectMainStore
@observer
class CameraListComponent extends React.Component<MainStoreInjected & WrappedComponentProps> {
  @observable
  cameras: Camera[] = [];

  @observable
  isLoaded: boolean = false;

  @observable
  selectedRowKey: string | undefined;

  constructor(props: any) {
    super(props);

    getAll<Camera>(restServices.platform_CameraService.getCameras)
      .then((result: Camera []) => {
        let cameras: Camera [] = result;
        let count = 0;
        if(cameras.length === 0){
          this.isLoaded = true;
          return;
        }

        cameras.forEach((camera) => {
          restServices.platform_CameraService.getStatus(cubaREST)({camera: camera}).then((result) => {
            camera.status = String(result);
            this.cameras.push(camera);
            count++;
          })
            .then((result) => {
              if(count === cameras.length){
                this.isLoaded = true;
              }
            });
        });
      });
  }

  fields = [
    {
      title: this.props.intl.formatMessage({id: "url_address"}),
      dataIndex: 'urlAddress',
      key: 'urlAddress'
    },

    {
      title: this.props.intl.formatMessage({id: "port"}),
      dataIndex: 'port',
      key: 'port'
    },

    {
      title: this.props.intl.formatMessage({id: "name"}),
      dataIndex: 'name',
      key: 'name'
    },

    {
      title: this.props.intl.formatMessage({id: "status"}),
      dataIndex: 'status',
      key: 'status',
      render: (text: string, camera: Camera) => {
        if(camera != null) {
          let status = camera.status;
          let id;
          let element;

          if (status === "\"CONNECTED\"") {
            id = 'connected';
            element = <CheckCircleTwoTone twoToneColor="#29e70b"/>;
          } else if (status === "\"RECORDING\"") {
            id = 'recording';
            element = <VideoCameraTwoTone/>
          } else {
            id = 'not_connected';
            element = <CloseCircleTwoTone twoToneColor="#ff0000"/>;
          }

          return (
            <>
              {
                <Tooltip placement="topLeft" title={this.props.intl.formatMessage({id})}>
                  {element}
                </Tooltip>
              }
            </>
          )
        }
        return <></>
      }
    },

    {
      title: '',
      dataIndex: '',
      key: 'stream',
      render: (text: string, camera: Camera) => {
        let url = this.getUrlToNginx();
        let cameraCopy = Camera.copy(camera);

        let player: ReactElement [] = [
          <ReactPlayer
          url={url.concat('/file/' + camera.name + ".m3u8")}
          playing={true}>
          </ReactPlayer>,

          <Button onClick={() => {
            restServices.platform_StreamService.stopStream(cubaREST)({camera: cameraCopy})
              .then((result) => {
                render(
                  button,
                  document.getElementById("player")
                );
              })
          }}>
            {this.getFormattedText("stop")}
          </Button>
        ];

        let button: ReactElement = <Button onClick={() => {
          restServices.platform_StreamService.startStream(cubaREST)({camera: cameraCopy})
            .then((result) => {
              render(
                player,
                document.getElementById("player")
              );
            })
        }}>
          {this.getFormattedText("live")}
        </Button>;

        return (
          <div id="player">
            {button}
          </div>
        );
      }
    }
  ];

  getFormattedText(key: string){
    return this.props.intl.formatMessage({id: key});
  }

  getUrlToNginx() {
    const delimiter = '/';
    const urlParts = document.location.href.split(delimiter);
    return urlParts[0].concat('//').concat(urlParts[2]);
  }

  showDeletionDialog = (e: Camera) => {
    Modal.confirm({
      title: this.props.intl.formatMessage(
        {id: "management.browser.delete.areYouSure"},
        {instanceName: e.name}
      ),
      okText: this.props.intl.formatMessage({
        id: "management.browser.delete.ok"
      }),
      cancelText: this.props.intl.formatMessage({
        id: "management.browser.delete.cancel"
      }),
      onOk: () => {
        this.selectedRowKey = undefined;
        const key = 'deleting';

        message.loading({content: this.props.intl.formatMessage({id: "deleting_is_starting"}), key}, 0);
        cubaREST.deleteEntity('platform_Camera', e.id)
          .then(() => {
            message.success({content: this.props.intl.formatMessage({id: "deleting_has_finished"}), key}, 0);
            if (this.cameras.length === 1) {
              this.cameras = [];
            } else if (this.cameras.length !== 0) {
              this.cameras = this.cameras.splice(this.cameras.indexOf(e), 1);
            }
          })
          .catch((error) => {
            message.error({content: this.props.intl.formatMessage({id: "management.editor.error"}), key}, 0);
          });
      }
    });
  };

  render() {
    const buttons = [
      <Link
        to={CameraManagement.PATH + "/" + CameraManagement.NEW_SUBPATH}
        key="create">
        <Button
          htmlType="button"
          style={{margin: "0 12px 12px 0"}}
          type="primary"
          icon="plus">
          <span>
            <FormattedMessage id="management.browser.create"/>
          </span>
        </Button>
      </Link>,

      <Link to={CameraManagement.PATH + "/" + this.selectedRowKey} key="edit">
        <Button
          htmlType="button"
          style={{margin: "0 12px 12px 0"}}
          type="default"
          disabled={!this.selectedRowKey}>
          <FormattedMessage id="management.browser.edit"/>
        </Button>
      </Link>,

      <Button
        htmlType="button"
        style={{margin: "0 12px 12px 0"}}
        onClick={() => showDeletionDialog(this.props, this.getRecordById(this.selectedRowKey!), 'platform_Camera', (result) => {
            if (this.cameras.length === 1) {
              this.cameras = [];
            } else if (this.cameras.length !== 0) {
              this.cameras = deleteFromDataSource(this.getRecordById(this.selectedRowKey!), this.cameras);
            }
            this.selectedRowKey = undefined;
          }
        )}
        key="remove"
        type="default"
        disabled={!this.selectedRowKey}>
        <FormattedMessage id="management.browser.remove"/>
      </Button>,

      <Button
        htmlType="button"
        style={{margin: "0 12px 12px 0"}}
        type="default"
        onClick={() => {
            const record = this.getRecordById(this.selectedRowKey!);
            const recordCopy = Camera.copy(record);
            const key = 'recording';

            message.loading({content: this.props.intl.formatMessage({id: "recording_is_starting"}), key}, 0);
            restServices.platform_CameraService.write(cubaREST)({camera: recordCopy})
              .then(() => {
                restServices.platform_CameraService.getStatus(cubaREST)({camera: recordCopy})
                  .then((result) => {
                    record.status = String(result);
                    message.success({
                      content: this.props.intl.formatMessage({id: "recording_has_started"}),
                      key
                    }, 0);
                    this.setState({});
                  })
                  .catch((error) => {
                    throw new Error('Status error');
                  });
              })
              .catch((error) => {
                message.error(error.toString());
              })
          }
        }
        disabled={!this.selectedRowKey || this.getRecordById(this.selectedRowKey!).status !== "\"CONNECTED\""}>
        {this.props.intl.formatMessage({id: "start"})}
      </Button>,

      <Button
        htmlType="button"
        style={{margin: "0 12px 12px 0"}}
        type="default"
        onClick={
          (event) => {
            const record = this.getRecordById(this.selectedRowKey!);
            const recordCopy = Camera.copy(record);
            const key = 'recording';

            message.loading({content: this.props.intl.formatMessage({id: "recording_is_stopping"}), key}, 0);
            restServices.platform_CameraService.stop(cubaREST)({camera: recordCopy})
              .then(() => {
                restServices.platform_CameraService.getStatus(cubaREST)({camera: recordCopy})
                  .then((result) => {
                    record.status = String(result);
                    message.success({
                      content: this.props.intl.formatMessage({id: "recording_has_stopped"}),
                      key
                    }, 0);
                    this.setState({});
                  })
                  .catch((error) => {
                    throw new Error('Status error');
                  })
              })
              .catch((error) => {
                message.error(error.toString());
              })
          }
        }
        disabled={!this.selectedRowKey || this.getRecordById(this.selectedRowKey).status === "\"CONNECTED\""}>
        {this.props.intl.formatMessage({id: "stop"})}
      </Button>
    ];

    if (!this.isLoaded) {
      return 'Loading';
    }

    return (
      <div>
        <Row>
          {buttons}
        </Row>
        <Table<Camera>
          dataSource={this.cameras}
          columns={this.fields}
          rowSelection={
            {
              type: "radio",
              onChange: (selectedRowKeys: string[] | number[], selectedRows: Camera[]) => {
                this.handleRowSelectionChange([String(selectedRows[0].id)]);
              }
            }
          }>
        </Table>
      </div>
    );
  }

  getRecordById(id: string): Camera {
    const record: Camera | undefined = this.cameras.find(record => record.id === id);

    if (!record) {
      return new Camera();
    }

    return record;
  }

  handleRowSelectionChange = (selectedRowKeys: string[]) => {
    this.selectedRowKey = selectedRowKeys[0];
  };

  deleteSelectedRow = () => {
    this.showDeletionDialog(this.getRecordById(this.selectedRowKey!));
  };

  isStartEnabled(): boolean {
    if(!this.selectedRowKey){
      return false;
    }

    const record = this.getRecordById(this.selectedRowKey!);
    if(record.status === "\"CONNECTED\""){
      return true;
    }
    return false;
  };
}

const CameraList = injectIntl(CameraListComponent);

export default CameraList;