import * as React from "react";
import {observer} from "mobx-react";
import {Link} from "react-router-dom";

import {observable} from "mobx";

import {Modal, Button, Table, message, Spin, Tooltip, Row} from "antd";

import {cubaREST} from "../../index";

import {
  collection,
  injectMainStore,
  MainStoreInjected,
  DataTable, DataCollectionStore
} from "@cuba-platform/react";

import {Camera} from "../../cuba/entities/platform_Camera";
import {SerializedEntity} from "@cuba-platform/rest";
import {CameraManagement} from "./CameraManagement";
import {
  FormattedMessage,
  injectIntl,
  WrappedComponentProps
} from "react-intl";
import {restServices} from "../../cuba/services";

import {CheckCircleOutlined, CheckCircleTwoTone, CloseCircleTwoTone, VideoCameraTwoTone} from "@ant-design/icons";
import {render} from "react-dom";
import {StandardEntity} from "../../cuba/entities/base/sys$StandardEntity";
import {showDeletionDialog} from "../App";
import ReactPlayer, {ReactPlayerProps} from "react-player";
import cuba from "@cuba-platform/rest/dist-browser/cuba";
import {ReactNode} from "react";
import {Simulate} from "react-dom/test-utils";

@injectMainStore
@observer
class CameraListComponent extends React.Component<MainStoreInjected & WrappedComponentProps> {
  dataCollection: DataCollectionStore<Camera>;

  @observable
  cameras: Camera[] = [];

  @observable
  isLoaded: boolean = false;

  constructor(props: any) {
    super(props);
    this.getCameras();
  }

  getCameras() {
    cubaREST.loadEntities<Camera>('platform_Camera').then((cameras) => {
      if (cameras.length == 0) {
        this.isLoaded = true;
      }
      let count = 0;
      cameras.forEach((camera) => {
        restServices.platform_CameraService.getStatus(cubaREST)({camera: camera}).then((result) => {
          camera.status = String(result);
          this.cameras.push(camera);
          count++;
        })
          .then((result) => {
            if (count == cameras.length) {
              this.isLoaded = true;
            }
          });
      });
    });
  }

  // @ts-ignore
  // @ts-ignore
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
      render: (status: string) => {
        let id;
        let element;
        if (status == "\"CONNECTED\"") {
          id = 'connected';
          element = <CheckCircleTwoTone twoToneColor="#29e70b"/>;
        } else if (status == "\"RECORDING\"") {
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
    },

    {
      title: this.props.intl.formatMessage({id: "start"}),
      dataIndex: '',
      key: 'start',
      render: (text: string, record: Camera) => (
        <>
          {
            record.status == "\"CONNECTED\"" ? <Button
              onClick={
                (event) => {
                  const key = 'recording';
                  message.loading({content: this.props.intl.formatMessage({id: "recording_is_starting"}), key}, 0);
                  record.status = null;
                  restServices.platform_CameraService.write(cubaREST)({camera: record})
                    .then((result) => {
                      restServices.platform_CameraService.getStatus(cubaREST)({camera: record})
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
            >{this.props.intl.formatMessage({id: "start"})}</Button> : <></>
          }
        </>
      )
    },
    {
      title: this.props.intl.formatMessage({id: "stop"}),
      dataIndex: '',
      key: 'stop',
      render: (text: string, record: Camera) => (
        <>
          {
            record.status == "\"RECORDING\"" ? <Button
              onClick={
                (event) => {
                  const key = 'recording';
                  message.loading({content: this.props.intl.formatMessage({id: "recording_is_stopping"}), key}, 0);
                  record.status = null;
                  restServices.platform_CameraService.stop(cubaREST)({camera: record})
                    .then((result) => {
                      restServices.platform_CameraService.getStatus(cubaREST)({camera: record})
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
            >{this.props.intl.formatMessage({id: "stop"})}</Button> : <></>
          }
        </>
      )
    },
    {
      title: this.props.intl.formatMessage({id: "management.browser.edit"}),
      dataIndex: '',
      key: 'edit',
      render: (text: string, camera: Camera) => (
        <>
          {
            <Link to={CameraManagement.PATH + "/" + camera.id} key="edit">
              <Button
                htmlType="button"
                style={{margin: "0 12px 12px 0"}}
                type="default"
              >
                <FormattedMessage id="management.browser.edit"/>
              </Button>
            </Link>
          }
        </>
      )
    },
    {
      title: this.props.intl.formatMessage({id: "management.browser.remove"}),
      dataIndex: '',
      key: 'delete',
      render: (text: string, camera: Camera) => (
        <>
          {
            <Button
              htmlType="button"
              style={{margin: "0 12px 12px 0"}}
              onClick={() => showDeletionDialog(this.props, camera, 'platform_Camera', (result) => {
                  if (this.cameras.length == 1) {
                    this.cameras = [];
                  } else if (this.cameras.length != 0) {
                    this.cameras = this.deleteCamera(camera);
                  }
                }
              )}
              key="remove"
              type="default"
            >
              <FormattedMessage id="management.browser.remove"/>
            </Button>
          }
        </>
      )
    },
    {
      title: 'a',
      dataIndex: '',
      key: 'stream',
      render: (text: string, camera: Camera) => {
        let url = document.location.href.split('/');
        camera.status = null;
        return (
          <div id="player">
            <Button onClick={() => {
              restServices.platform_StreamService.startStream(cubaREST)({camera: camera})
                .then((result) => {
                  render(
                    <ReactPlayer
                      url={url[0].concat('//').concat(url[2]).concat('/file/' + camera.name + ".m3u8")}
                      controls={true} playing={true}>
                    </ReactPlayer>,
                    document.getElementById("player")
                  );
                })
            }}>
              Go
            </Button>
          </div>
        )
      }
    }
  ];

  deleteCamera(camera: Camera) {
    let result: Camera [] = [];
    this.cameras.forEach((value) => {
      if (camera.id != value.id) {
        result.push(value);
      }
    });

    return result;
  }

  statusToIcon = {}

  @observable selectedRowKey: string | undefined;

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
          .then((result) => {
            message.success({content: this.props.intl.formatMessage({id: "deleting_has_finished"}), key}, 0);
            if (this.cameras.length == 1) {
              this.cameras = [];
            } else if (this.cameras.length != 0) {
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
    console.log(this.cameras);
    const buttons = [
      <Link
        to={CameraManagement.PATH + "/" + CameraManagement.NEW_SUBPATH}
        key="create"
      >
        <Button
          htmlType="button"
          style={{margin: "0 12px 12px 0"}}
          type="primary"
          icon="plus"
        >
          <span>
            <FormattedMessage id="management.browser.create"/>
          </span>
        </Button>
      </Link>,
      <Link to={CameraManagement.PATH + "/" + this.selectedRowKey} key="edit">
        <Button
          htmlType="button"
          style={{margin: "0 12px 12px 0"}}
          disabled={!this.selectedRowKey}
          type="default"
        >
          <FormattedMessage id="management.browser.edit"/>
        </Button>
      </Link>,
      <Button
        htmlType="button"
        style={{margin: "0 12px 12px 0"}}
        disabled={!this.selectedRowKey}
        onClick={this.deleteSelectedRow}
        key="remove"
        type="default"
      >
        <FormattedMessage id="management.browser.remove"/>
      </Button>
    ];

    if (!this.isLoaded) {
      return 'Loading';
    }

    return (
      /*<DataTable
        dataCollection={this.dataCollection}
        fields={this.fields}
        onRowSelectionChange={this.handleRowSelectionChange}
        hideSelectionColumn={true}
        buttons={buttons}

      />

       */
      <div>
        <Row>
          <Link
            to={CameraManagement.PATH + "/" + CameraManagement.NEW_SUBPATH}
            key="create"
          >
            <Button
              htmlType="button"
              style={{margin: "0 12px 12px 0"}}
              type="primary"
              icon="plus"
            >
          <span>
            <FormattedMessage id="management.browser.create"/>
          </span>
            </Button>
          </Link>
        </Row>
        <Table<Camera>
          dataSource={this.cameras}
          columns={this.fields}
        >
        </Table>
      </div>
    );
  }

  getRecordById(id: string): SerializedEntity<Camera> {
    const record:
      | SerializedEntity<Camera>
      | undefined = this.dataCollection.items.find(record => record.id === id);

    if (!record) {
      throw new Error("Cannot find entity with id " + id);
    }

    return record;
  }

  handleRowSelectionChange = (selectedRowKeys: string[]) => {
    this.selectedRowKey = selectedRowKeys[0];
  };

  deleteSelectedRow = () => {
    this.showDeletionDialog(this.getRecordById(this.selectedRowKey!));
  };
}

const CameraList = injectIntl(CameraListComponent);

export default CameraList;