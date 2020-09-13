import * as React from "react";
import { observer } from "mobx-react";
import { Link } from "react-router-dom";

import { observable } from "mobx";

import {Modal, Button, Table} from "antd";

import {
  collection,
  injectMainStore,
  MainStoreInjected,
  DataTable
} from "@cuba-platform/react";

import { Camera } from "../../cuba/entities/platform_Camera";
import { SerializedEntity } from "@cuba-platform/rest";
import { CameraManagementa } from "./CameraManagementa";
import {
  FormattedMessage,
  injectIntl,
  WrappedComponentProps
} from "react-intl";

import{User} from "../../cuba/entities/base/sec$User";
import Column from "antd/es/table/Column";
import {restServices} from "../../cuba/services";
import {cubaREST} from "../../index";

@injectMainStore
@observer
class CameraListComponent extends React.Component<
  MainStoreInjected & WrappedComponentProps
  > {
  appState = this.props.mainStore!;
  dataCollection = collection<Camera>(Camera.NAME, {
    view: "_local",
    sort: "-updateTs",
    filter: {
      conditions:
        [
          {property: "user.login", operator: "=", value: this.appState.userName!}
        ]
    }
  });

  getLocalizedMessageById = (id: string) => (localizedMessage: string) => {
    return this.props.intl.formatMessage({
      id: id
    })
  };

  fields = [
    "urlAddress",

    "port",

    "name"
  ];


  @observable selectedRowKey: string | undefined;

  showDeletionDialog = (e: SerializedEntity<Camera>) => {
    Modal.confirm({
      title: this.props.intl.formatMessage(
        { id: "management.browser.delete.areYouSure" },
        { instanceName: e._instanceName }
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
    const buttons = [
      <Link
        to={CameraManagementa.PATH + "/" + CameraManagementa.NEW_SUBPATH}
        key="create"
      >
        <Button
          htmlType="button"
          style={{ margin: "0 12px 12px 0" }}
          type="primary"
          icon="plus"
        >
          <span>
            <FormattedMessage id="management.browser.create" />
          </span>
        </Button>
      </Link>,
      <Link to={CameraManagementa.PATH + "/" + this.selectedRowKey} key="edit">
        <Button
          htmlType="button"
          style={{ margin: "0 12px 12px 0" }}
          disabled={!this.selectedRowKey}
          type="default"
        >
          <FormattedMessage id="management.browser.edit" />
        </Button>
      </Link>,
      <Button
        htmlType="button"
        style={{ margin: "0 12px 12px 0" }}
        disabled={!this.selectedRowKey}
        onClick={this.deleteSelectedRow}
        key="remove"
        type="default"
      >
        <FormattedMessage id="management.browser.remove" />
      </Button>
    ];

    let dataSource = [];
    for(let i = 0; i < this.dataCollection.items.length; i++){
      let status;
      let camera: Camera = new Camera();
      camera.id = this.dataCollection.items[i].id;
      camera.address = this.dataCollection.items[i].address;
      restServices.platform_CameraService.getStatus(cubaREST)({camera: camera}).then((result => {
        status = result;
      }));
      let element = {
        urlAddress: this.dataCollection.items[i].urlAddress,
        port: this.dataCollection.items[i].port,
        name: this.dataCollection.items[i].name,
        status: status
      }

      dataSource.push(element);
    }

    const columns = [
      {
        title: "Url address",
        dataIndex: "urlAddress",
        key: "urlAddress"
      },
      {
        title: "port",
        dataIndex: "port",
        key: "port"
      },
      {
        title: "name",
        dataIndex: "name",
        key: "name"
      },
      {
        title: "Status",
        dataIndex: "Status",
        key: "Status"
      }
    ]

    console.log(dataSource);


    return (
      /*<DataTable
        dataCollection={this.dataCollection}
        fields={this.fields}
        onRowSelectionChange={this.handleRowSelectionChange}
        hideSelectionColumn={true}
        buttons={buttons}
      />

       */
      <Table dataSource={dataSource} columns={columns}></Table>
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
