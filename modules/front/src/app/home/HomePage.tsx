import * as React from "react";
import { FormattedMessage } from "react-intl";
import {restServices} from "../../cuba/services";
import {cubaREST} from "../../index";

class HomePage extends React.Component {
  render() {
    restServices.platform_CameraService.init(cubaREST)();
    return (
      <div>
        <FormattedMessage id="home.welcome" /> Platform!
      </div>
    );
  }
}

export default HomePage;
