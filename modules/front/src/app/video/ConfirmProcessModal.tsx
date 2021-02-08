import {observer} from "mobx-react";
import * as React from "react";
import {injectMainStore, MainStoreInjected} from "@cuba-platform/react";
import {injectIntl, WrappedComponentProps} from "react-intl";
import {action, observable} from "mobx";
import {ChangeEvent} from "react";
import {Form, Icon, Input} from "antd";

@injectMainStore
@observer
class ConfirmProcessModal extends React.Component<MainStoreInjected & WrappedComponentProps>{
  @observable
  login: string;

  @observable
  password: string;

  @action
  changeLogin = (e: ChangeEvent<HTMLInputElement>) => {
    this.login = e.target.value;
  }

  render() {
    return (
      <Form>
        <Form.Item>
          <Input
            id="input_login"
            placeholder={this.props.intl.formatMessage({
              id: "login.placeholder.login"
            })}
            value={this.login}
            onChange={this.changeLogin}
            prefix={<Icon type="user" style={{color: "rgba(0,0,0,.25)"}}/>}
            size="large"
          />;
        </Form.Item>
        <Form.Item>
          <Input
            id="input_password"
            placeholder={this.props.intl.formatMessage({
              id: "login.placeholder.password"
            })}
            type="password"
            prefix={<Icon type="lock" style={{color: "rgba(0,0,0,.25)"}}/>}
            size="large"
          />
        </Form.Item>
      </Form>
    )
  }
}

export default injectIntl(ConfirmProcessModal)