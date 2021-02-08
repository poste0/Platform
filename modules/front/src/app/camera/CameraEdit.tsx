import * as React from "react";
import {FormEvent} from "react";
import {Alert, Button, Card, Form, Input, message} from "antd";
import {observer} from "mobx-react";
import {CameraManagement} from "./CameraManagement";
import {FormComponentProps} from "antd/lib/form";
import {Link, Redirect} from "react-router-dom";
import {action, IReactionDisposer, observable, reaction, toJS} from "mobx";
import {
  FormattedMessage,
  injectIntl,
  WrappedComponentProps
} from "react-intl";

import {
  Field,
  instance,
  withLocalizedForm,
  extractServerValidationErrors,
  constructFieldsWithErrors,
  clearFieldErrors,
  MultilineText, collection, MainStoreInjected, injectMainStore
} from "@cuba-platform/react";

import "../../app/App.css";

import {Camera} from "../../cuba/entities/platform_Camera";
import {User} from "../../cuba/entities/base/sec$User";

type Props = FormComponentProps & EditorProps;

type EditorProps = {
  entityId: string;
};

@injectMainStore
@observer
class CameraEditComponent extends React.Component<Props & WrappedComponentProps & MainStoreInjected> {
  dataInstance = instance<Camera>(Camera.NAME, {
    view: "_local",
    loadImmediately: false
  });

  @observable
  updated = false;
  reactionDisposer: IReactionDisposer;

  fields = [
    "address",

    "urlAddress",

    "port",

    "name",

    "height",

    "weight",

    "frameRate",

    "path"
  ];

  @observable
  globalErrors: string[] = [];

  @observable
  address: string;

  appState = this.props.mainStore!;

  userCollection = collection<User>(User.NAME, {
    view: "_local",
    sort: "-updateTs",
    filter: {
      conditions:
        [
          {property: "login", operator: "=", value: this.appState.userName!}
        ]
    }
  });

  @action
  formChange = () => {
    this.address = "";

    const protocol = "rtsp://";

    this.address += protocol;
    this.appendIfNotEmpty(this.props.form.getFieldValue('cameraName'), ":");
    this.appendIfNotEmpty(this.props.form.getFieldValue('password'), "@");
    this.appendIfNotEmpty(this.props.form.getFieldValue('urlAddress'), "");

    if (this.props.form.getFieldValue('port') != undefined) {
      this.address += ":";
      this.address += this.props.form.getFieldValue('port');
    }

    this.address += "/";
    this.appendIfNotEmpty(this.props.form.getFieldValue('path'), "");

    if (this.props.form.getFieldValue('options') != undefined && this.props.form.getFieldValue('options') != "") {
      this.address += "?";
      String(this.props.form.getFieldValue('options')).split("\n").forEach((option) => {
        this.address += option;
        this.address += "&";
      });
      this.address = this.address.substring(0, this.address.length - 1);
    }
  };

  appendIfNotEmpty = (value: string, addedValue: string) => {
    this.address += value;
    this.address += addedValue;
  };

  handleSubmit = (e: FormEvent) => {
    e.preventDefault();

    this.props.form.validateFields((err) => {
      if (err) {
        message.error(
          this.props.intl.formatMessage({
            id: "management.editor.validationError"
          })
        );
        return;
      }

      const cameraJson = {
        "address": this.address,
        "urlAddress": this.props.form.getFieldValue('urlAddress'),
        "port": this.props.form.getFieldValue('port'),
        "name": this.props.form.getFieldValue('name'),
        "height": this.props.form.getFieldValue('height'),
        "weight": this.props.form.getFieldValue('weight'),
        "frameRate": this.props.form.getFieldValue('frameRate'),
        "path": this.props.form.getFieldValue('path'),
        "user": this.userCollection.items[0],
        "CREATED_BY": this.userCollection.items[0].login
      };

      this.dataInstance
        .update(cameraJson)
        .then(() => {
          message.success(
            this.props.intl.formatMessage({id: "management.editor.success"})
          );
          this.updated = true;
        })
        .catch((e: any) => {
          if (e.response && typeof e.response.json === "function") {
            e.response.json().then((response: any) => {
              clearFieldErrors(this.props.form);
              const {
                globalErrors,
                fieldErrors
              } = extractServerValidationErrors(response);
              this.globalErrors = globalErrors;
              if (fieldErrors.size > 0) {
                this.props.form.setFields(
                  constructFieldsWithErrors(fieldErrors, this.props.form)
                );
              }

              if (fieldErrors.size > 0 || globalErrors.length > 0) {
                message.error(
                  this.props.intl.formatMessage({
                    id: "management.editor.validationError"
                  })
                );
              } else {
                message.error(
                  this.props.intl.formatMessage({
                    id: "management.editor.error"
                  })
                );
              }
            });
          } else {
            message.error(
              this.props.intl.formatMessage({id: "management.editor.error"})
            );
          }
        });
    });
  };

  render() {
    if (this.updated) {
      return <Redirect to={CameraManagement.PATH}/>;
    }

    const {status} = this.dataInstance;

    return (
      <Card className="narrow-layout">
        <Form onSubmit={this.handleSubmit} layout="vertical" onChange={this.formChange}>

          <Field
            entityName={Camera.NAME}
            propertyName={'urlAddress'}
            form={this.props.form}
            formItemOpts={{style: {marginBottom: "12px"}, label: this.props.intl.formatMessage({id: "camera.address"})}}
            getFieldDecoratorOpts={{
              rules: [{required: true}],
            }}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={'port'}
            form={this.props.form}
            formItemOpts={{style: {marginBottom: "12px"}, label: this.props.intl.formatMessage({id: "camera.port"})}}
            getFieldDecoratorOpts={{}}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={'name'}
            form={this.props.form}
            formItemOpts={{
              style: {marginBottom: "12px"},
              label: this.props.intl.formatMessage({id: "camera.cameraName"})
            }}
            getFieldDecoratorOpts={{}}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={'cameraName'}
            form={this.props.form}
            formItemOpts={{style: {marginBottom: "12px"}, label: this.props.intl.formatMessage({id: "camera.name"})}}
            getFieldDecoratorOpts={{}}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={'password'}
            form={this.props.form}
            formItemOpts={{
              style: {marginBottom: "12px"},
              label: this.props.intl.formatMessage({id: "camera.password"})
            }}
            getFieldDecoratorOpts={{}}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={'height'}
            form={this.props.form}
            formItemOpts={{style: {marginBottom: "12px"}, label: this.props.intl.formatMessage({id: "camera.height"})}}
            getFieldDecoratorOpts={{
              rules: [{required: true}]
            }}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={'weight'}
            form={this.props.form}
            formItemOpts={{style: {marginBottom: "12px"}, label: this.props.intl.formatMessage({id: "camera.width"})}}
            getFieldDecoratorOpts={{
              rules: [{required: true}]
            }}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={'frameRate'}
            form={this.props.form}
            formItemOpts={{
              style: {marginBottom: "12px"},
              label: this.props.intl.formatMessage({id: "camera.frameRate"})
            }}
            getFieldDecoratorOpts={{
              rules: [{required: true}],
            }}

          />

          <Field
            entityName={Camera.NAME}
            propertyName={'path'}
            form={this.props.form}
            formItemOpts={{style: {marginBottom: "12px"}, label: this.props.intl.formatMessage({id: "camera.path"})}}
            getFieldDecoratorOpts={{}}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={'options'}
            form={this.props.form}
            formItemOpts={{style: {marginBottom: "12px"}, label: this.props.intl.formatMessage({id: "camera.options"})}}
            getFieldDecoratorOpts={{}}
          />

          <Form.Item>
            <Input
              id="address_item"
              value={this.address}
              disabled={true}
              placeholder={this.props.intl.formatMessage({id: "camera.address"})}
            />
          </Form.Item>

          {this.globalErrors.length > 0 && (
            <Alert
              message={<MultilineText lines={toJS(this.globalErrors)}/>}
              type="error"
              style={{marginBottom: "24px"}}
            />
          )}

          <Form.Item style={{textAlign: "center"}}>
            <Link to={CameraManagement.PATH}>
              <Button htmlType="button">
                <FormattedMessage id="management.editor.cancel"/>
              </Button>
            </Link>
            <Button
              type="primary"
              htmlType="submit"
              disabled={status !== "DONE" && status !== "ERROR"}
              loading={status === "LOADING"}
              style={{marginLeft: "8px"}}
            >
              <FormattedMessage id="management.editor.submit"/>
            </Button>
          </Form.Item>
        </Form>
      </Card>
    );
  }

  componentDidMount() {
    if (this.props.entityId !== CameraManagement.NEW_SUBPATH) {
      this.dataInstance.load(this.props.entityId);
    } else {
      this.dataInstance.setItem(new Camera());
    }
    this.reactionDisposer = reaction(
      () => {
        return this.dataInstance.item;
      },
      () => {
        this.props.form.setFieldsValue(
          this.dataInstance.getFieldValues(this.fields)
        );
        if (this.dataInstance.getFieldValues(['address']) != undefined) {
          this.props.form.setFieldsValue({'cameraName': this.getCameraName()});
          this.props.form.setFieldsValue({'password': this.getPassword()});
          this.formChange();
        }
      }
    );
  }

  getCameraName(): string {
    return this.getCameraNameAndPassword()[0];
  }

  getPassword(): string {
    return this.getCameraNameAndPassword()[1];
  }

  getCameraNameAndPassword(): string [] {
    return this.dataInstance.getFieldValues(['address']).address.split('@')[0].split('://')[1].split(':');
  }

  componentWillUnmount() {
    this.reactionDisposer();
  }
}

export default injectIntl(
  withLocalizedForm<EditorProps>({
    onValuesChange: (props: any, changedValues: any) => {
      // Reset server-side errors when field is edited
      Object.keys(changedValues).forEach((fieldName: string) => {
        props.form.setFields({
          [fieldName]: {
            value: changedValues[fieldName]
          }
        });
      });
    }
  })(CameraEditComponent)
);
