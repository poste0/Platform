import * as React from "react";
import { RouteComponentProps } from "react-router";
import { observer } from "mobx-react";
import NodeEdit from "./NodeEdit";
import NodeList from "./NodeList";

type Props = RouteComponentProps<{ entityId?: string }>;

@observer
export class NodeManagement extends React.Component<Props> {
  static PATH = "/nodeManagement";
  static NEW_SUBPATH = "new";

  render() {
    const { entityId } = this.props.match.params;
    return <>{entityId ? <NodeEdit entityId={entityId} /> : <NodeList />}</>;
  }
}
