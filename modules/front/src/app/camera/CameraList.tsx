import * as React from "react";
import {observer} from "mobx-react";
import {Link} from "react-router-dom";

import {observable} from "mobx";

import {Modal, Button, Table, message, Spin, Tooltip} from "antd";

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

import {CheckCircleOutlined, CheckCircleTwoTone, CloseCircleTwoTone} from "@ant-design/icons";
import {render} from "react-dom";

@injectMainStore
@observer
class CameraListComponent extends React.Component<MainStoreInjected & WrappedComponentProps> {
  dataCollection: DataCollectionStore<Camera>;

  @observable
  cameras: Camera[] = [];

  constructor(props: any) {
    super(props);
    cubaREST.loadEntities<Camera>('platform_Camera').then((cameras) => {
      cameras.forEach((camera) => {
        restServices.platform_CameraService.getStatus(cubaREST)({camera: camera}).then((result) => {
          camera.status = String(result);
          this.cameras.push(camera);
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
      render: (status: string) => (
        <>
          {
            status === "\"CONNECTED\"" ? <Tooltip placement="topLeft" title={this.props.intl.formatMessage({id: "connected"})}>
              <CheckCircleTwoTone twoToneColor="#29e70b"/>
            </Tooltip> :
              <Tooltip placement="topLeft" title={this.props.intl.formatMessage({id: "not_connected"})}>
                <CloseCircleTwoTone twoToneColor="#ff0000"/>
              </Tooltip>
          }
        </>
      )
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
                          message.success({content: this.props.intl.formatMessage({id: "recording_has_started"}), key}, 0);
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
                          message.success({content: this.props.intl.formatMessage({id: "recording_has_stopped"}), key}, 0);
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
    }
  ];

  statusToIcon = {}

  @observable selectedRowKey: string | undefined;

  showDeletionDialog = (e: SerializedEntity<Camera>) => {
    Modal.confirm({
      title: this.props.intl.formatMessage(
        {id: "management.browser.delete.areYouSure"},
        {instanceName: e._instanceName}
      ),
      okText: this.props.intl.formatMessage({
        id: "management.browser.delete.ok"
      }),
      cancelText: this.props.intl.formatMessage({
        id: "management.browser.delete.cancel"
      }),
      onOk: () => {
        this.selectedRowKey = undefined;

        return this.dataCollection.delete(e);
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

    if (this.cameras.length == 0) {
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
      <Table<Camera>
        dataSource={this.cameras}
        columns={this.fields}
      >
      </Table>
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