import * as React from "react";
import { RouteComponentProps } from "react-router";
import { observer } from "mobx-react";
import CameraEdit from "./CameraEdit";
import CameraList from "./CameraList";

type Props = RouteComponentProps<{ entityId?: string }>;

@observer
export class CameraManagementa extends React.Component<Props> {
  static PATH = "/cameraManagementa";
  static NEW_SUBPATH = "new";

  render() {
    const { entityId } = this.props.match.params;
    return (
      <>{entityId ? <CameraEdit entityId={entityId} /> : <CameraList />}</>
    );
  }
}
