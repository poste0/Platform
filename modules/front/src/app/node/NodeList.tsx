import * as React from "react";
import { observer } from "mobx-react";
import { Link } from "react-router-dom";

import { observable } from "mobx";

import {Modal, Button, Table, Row, Radio, message} from "antd";

import {
  collection,
  injectMainStore,
  MainStoreInjected,
  DataTable, DataCollectionStore
} from "@cuba-platform/react";

import { Node } from "../../cuba/entities/platform_Node";
import { NodeManagement } from "./NodeManagement";
import {
  FormattedMessage,
  injectIntl,
  WrappedComponentProps
} from "react-intl";
import {restServices} from "../../cuba/services";
import {cubaREST} from "../../index";
import {deleteFromDataSource, getAll, showDeletionDialog} from "../App";

@injectMainStore
@observer
class NodeListComponent extends React.Component<
  MainStoreInjected & WrappedComponentProps
> {
  appState = this.props.mainStore!;

  /*dataCollection: DataCollectionStore<Node> = collection<Node>(Node.NAME, {
    view: "_local",
    sort: "-updateTs",
    filter: {
      conditions:
        [
          {property: "user.login", operator: "=", value: this.appState.userName!}
        ]
    }
  });

   */

  @observable
  nodes: Node [] = [];

  @observable
  isLoaded: boolean = false;

  constructor(props: any) {
    super(props);
    getAll(restServices.platform_NodeService.getNodes)
      .then((result: Node []) => {
        this.nodes = result;
        this.isLoaded = true;
      })
  }

  getNodes(){
    restServices.platform_NodeService.getNodes(cubaREST)()
      .then((result) => {
        console.log(result);
        let nodes: Node [] = JSON.parse(String(result));
        if(nodes.length == 0){
          this.isLoaded = true;
        }

        let count = 0;
        nodes.forEach((node) => {
          this.nodes.push(node);
          count++;
        });
        if(count == nodes.length){
          this.isLoaded = true;
        }
      });
  }

  fields = [
    {
      title: this.props.intl.formatMessage({id: 'name'}),
      dataIndex: 'name',
      key: 'name'
    },
    {
      title: this.props.intl.formatMessage({id: 'address'}),
      dataIndex: 'address',
      key: 'address'
    },
    {
      title: this.props.intl.formatMessage({id: 'cpu'}),
      dataIndex: 'cpu',
      key:'cpu'
    },
    {
      title: this.props.intl.formatMessage({id: 'gpu'}),
      dataIndex: 'gpu',
      key: 'gpu'
    },
  ];

  @observable selectedRowKey: string | undefined;

  showDeletionDialog = (e: Node) => {
    Modal.confirm({
      title: this.props.intl.formatMessage(
        { id: "management.browser.delete.areYouSure" },
        { instanceName: e.name }
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
        cubaREST.deleteEntity('platform_Node', e.id)
          .then((result) => {
            message.success({content: this.props.intl.formatMessage({id: "deleting_has_finished"}), key}, 0);
            if (this.nodes.length == 1) {
              this.nodes = [];
            } else if (this.nodes.length != 0) {
              this.nodes = this.nodes.splice(this.nodes.indexOf(e), 1);
            }
          })
          .catch((error) => {
            message.error({content: this.props.intl.formatMessage({id: "management.editor.error"}), key}, 0);
          });
      }
    });
  };

  render() {
    if (!this.isLoaded) {
      return 'Loading';
    }

    const buttons = [
      <Link
        to={NodeManagement.PATH + "/" + NodeManagement.NEW_SUBPATH}
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
      <Link to={NodeManagement.PATH + "/" + this.selectedRowKey} key="edit">
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
      </Button>,
      <Button
        htmlType="button"
        style={{ margin: "0 12px 12px 0" }}
        disabled={!this.selectedRowKey}
        key="remove"
        type="default"
        onClick={this.getInfo}
      >
        info
      </Button>
    ];

    return (
      <div>
        <Row>
          {buttons}
        </Row>
        <Table<Node>
          dataSource={this.nodes}
          columns={this.fields}
          rowSelection={
            {
              type: "radio",
              onChange: (selectedRowKeys: React.Key [], selectedRows: Node []) => {
                this.handleRowSelectionChange([String(selectedRows[0].id)]);
              }
            }
          }
        >
        </Table>
      </div>
    );
  }

  getInfo = () => {
    let node: Node = this.getRecordById(this.selectedRowKey!);
    let nodes = this.nodes;
    let index = nodes.indexOf(node);
    let cpu = '';
    let gpu = '';

    const key = 'load';
    message.loading({content: ' ', key: key}, 0);
    restServices.platform_NodeService.getCpu(cubaREST)({node: node})
      .then((result: string) => {
        cpu = result;
      });
    restServices.platform_NodeService.getGpu(cubaREST)({node: node})
      .then((result: string) => {
        gpu = result;
      });

    node.cpu = cpu;
    node.gpu = gpu;

    this.nodes = [];
    nodes[index] = node;
    this.nodes = nodes;
    message.success({content: ' ', key: key}, 0);
  }

  getRecordById(id: string): Node{
    const record:
      | Node
      | undefined = this.nodes.find(record => record.id === id);

    if (!record) {
      throw new Error("Cannot find entity with id " + id);
    }

    return record;
  }

  handleRowSelectionChange = (selectedRowKeys: string[]) => {
    this.selectedRowKey = selectedRowKeys[0];
  };

  deleteSelectedRow = () => {
    const node: Node = this.getRecordById(this.selectedRowKey!);
    showDeletionDialog(this.props, node, "platform_Node", (result) => {
      if (this.nodes.length == 1) {
        this.nodes = [];
      } else if (this.nodes.length != 0) {
        this.nodes = deleteFromDataSource(node, this.nodes);
      }
    });
  };
}

const NodeList = injectIntl(NodeListComponent);

export default NodeList;
