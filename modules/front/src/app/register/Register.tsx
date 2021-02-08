import {injectMainStore, MainStoreInjected} from "@cuba-platform/react";
import {injectIntl, WrappedComponentProps} from "react-intl";
import {observer} from "mobx-react";
import React, {ChangeEvent, FormEvent} from "react";
import {action, observable} from "mobx";
import {Button, Form, Input, message} from "antd";
import {restServices} from "../../cuba/services";
import {cubaREST} from "../../index";
import Login from "../login/Login";

@injectMainStore
@observer
export default class Register extends React.Component<MainStoreInjected & WrappedComponentProps> {
  @observable
  register_login: string;

  @observable
  register_password: string;

  @observable
  register_name: string;

  @action
  changeLoginRegister = (e: ChangeEvent<HTMLInputElement>) => {
    this.register_login = e.target.value;
  };

  @action
  changePasswordRegister = (e: ChangeEvent<HTMLInputElement>) => {
    this.register_password = e.target.value;
  };

  @action
  changeNameRegister = (e: ChangeEvent<HTMLInputElement>) => {
    this.register_name = e.target.value;
  };

  @action
  doRegister = (e: FormEvent) => {
    restServices.platform_RegistrationService.register(cubaREST)
    ({login: this.register_login, password: this.register_password, name: this.register_name})
      .then((result) => {
        message.success("");
        Login.processLogin(this.register_login, this.register_password, this.props.mainStore!);
      })
      .catch((result) => {
        message.error(this.props.intl.formatMessage({id: "register.failed"}));
      });
  };

  render() {
    return (
      <Form onSubmit={this.doRegister} layout="vertical">
        <Form.Item>
          <Input
            id="input_login_register"
            onChange={this.changeLoginRegister}
            value={this.register_login}
            placeholder={this.props.intl.formatMessage({
              id: "login.placeholder.login"
            })}/>
        </Form.Item>
        <Form.Item>
          <Input
            id="input_password_register"
            type="password"
            onChange={this.changePasswordRegister}
            value={this.register_password}
            placeholder={this.props.intl.formatMessage({
              id: "login.placeholder.password"
            })}/>
        </Form.Item>
        <Form.Item>
          <Input
            id="input_name"
            onChange={this.changeNameRegister}
            value={this.register_name}
            placeholder={this.props.intl.formatMessage({
              id: "register.name"
            })}/>
        </Form.Item>
        <Form.Item>
          <Button
            type="primary"
            htmlType="submit">
            Register
          </Button>
        </Form.Item>
      </Form>
    );
  }
}