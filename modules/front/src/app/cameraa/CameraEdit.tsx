import * as React from "react";
import {ChangeEvent, FormEvent} from "react";
import {Alert, Button, Card, Form, Input, message} from "antd";
import { observer } from "mobx-react";
import { CameraManagementa } from "./CameraManagementa";
import { FormComponentProps } from "antd/lib/form";
import { Link, Redirect } from "react-router-dom";
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
  MultilineText, Msg, FormField, collection, injectMainStore, MainStoreInjected
} from "@cuba-platform/react";

import "../../app/App.css";

import { Camera } from "../../cuba/entities/platform_Camera";
import {User} from "../../cuba/entities/base/sec$User";

type Props = FormComponentProps & EditorProps;

type EditorProps = {
  entityId: string;
};

@injectMainStore
@observer
class CameraEditComponent extends React.Component<
  Props & WrappedComponentProps & MainStoreInjected
> {
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
  formChange = (e: FormEvent) => {
    this.address = "";

    let fieldsValue = this.props.form.getFieldsValue(this.fields);

    const protocol = "rtsp://";

    this.address += protocol;
    this.appendIfNotEmpty(this.getFieldValue("camera.name"), ":");
    this.appendIfNotEmpty(this.getFieldValue("camera.password"), "@");
    this.appendIfNotEmpty(this.getFieldValue("camera.url_address"), "");
    if(this.getFieldValue("camera.port") != undefined){
      this.address += ":";
      this.address += this.getFieldValue("camera.port");
    }
    this.address += "/";
    this.appendIfNotEmpty(this.getFieldValue("camera.path"), "");
    if(this.getFieldValue("camera.options") != undefined && this.getFieldValue("camera.options") != ""){
      this.address += "?";
      String(this.getFieldValue("camera.options")).split("\n").forEach((option) => {
        this.address += option;
        this.address += "&";
      });
      this.address = this.address.substring(0, this.address.length - 1);
    }
  };

  getFieldValue(p: string){
    return this.props.form.getFieldValue(this.props.intl.formatMessage({id: p}));
  }

  appendIfNotEmpty = (value: string, addedValue: string) => {
    if(value != undefined && value != ""){
      this.address += value;
      this.address += addedValue;
    }
  };

  handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    this.props.form.validateFields((err, values) => {
      if (err) {
        message.error(
          this.props.intl.formatMessage({
            id: "management.editor.validationError"
          })
        );
        return;
      }
      this.dataInstance
        .update({
          "address": this.address,
          "urlAddress": this.getFieldValue("camera.url_address"),
          "port": this.getFieldValue("camera.port"),
          "name": this.getFieldValue("camera.cameraName"),
          "height": this.getFieldValue("camera.height"),
          "weight": this.getFieldValue("camera.width"),
          "frameRate": this.getFieldValue("camera.frameRate"),
          "path": this.getFieldValue("camera.path"),
          "user": this.userCollection.items[0]
        })
        .then(() => {
          message.success(
            this.props.intl.formatMessage({ id: "management.editor.success" })
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
              this.props.intl.formatMessage({ id: "management.editor.error" })
            );
          }
        });
    });
  };

  render() {
    if (this.updated) {
      return <Redirect to={CameraManagementa.PATH} />;
    }

    const { status } = this.dataInstance;

    return (
      <Card className="narrow-layout">
        <Form onSubmit={this.handleSubmit} layout="vertical" onChange={this.formChange} >

          <Field
            entityName={Camera.NAME}
            propertyName="urlAddress"
            form={this.props.form}
            formItemOpts={{ style: { marginBottom: "12px" } }}
            getFieldDecoratorOpts={{
              rules: [{ required: true }]
            }}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={this.props.intl.formatMessage({id: "camera.port"})}
            form={this.props.form}
            formItemOpts={{ style: { marginBottom: "12px" } }}
            getFieldDecoratorOpts={{}}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={this.props.intl.formatMessage({id: "camera.cameraName"})}
            form={this.props.form}
            formItemOpts={{ style: { marginBottom: "12px" } }}
            getFieldDecoratorOpts={{
              rules: [{ required: true }]
            }}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={this.props.intl.formatMessage({id: "camera.name"})}
            form={this.props.form}
            formItemOpts={{ style: { marginBottom: "12px" } }}
            getFieldDecoratorOpts={{
              rules: [{ required: true }]
            }}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={this.props.intl.formatMessage({id: "camera.password"})}
            form={this.props.form}
            formItemOpts={{ style: { marginBottom: "12px" } }}
            getFieldDecoratorOpts={{
              rules: [{ required: true }]
            }}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={this.props.intl.formatMessage({id: "camera.height"})}
            form={this.props.form}
            formItemOpts={{ style: { marginBottom: "12px" } }}
            getFieldDecoratorOpts={{
              rules: [{ required: true }]
            }}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={this.props.intl.formatMessage({id: "camera.width"})}
            form={this.props.form}
            formItemOpts={{ style: { marginBottom: "12px" } }}
            getFieldDecoratorOpts={{
              rules: [{ required: true }]
            }}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={this.props.intl.formatMessage({id: "camera.frameRate"})}
            form={this.props.form}
            formItemOpts={{ style: { marginBottom: "12px" } }}
            getFieldDecoratorOpts={{
              rules: [{ required: true }],
            }}

          />

          <Field
            entityName={Camera.NAME}
            propertyName={this.props.intl.formatMessage({id: "camera.path"})}
            form={this.props.form}
            formItemOpts={{ style: { marginBottom: "12px"} }}
            getFieldDecoratorOpts={{}}
          />

          <Field
            entityName={Camera.NAME}
            propertyName={this.props.intl.formatMessage({id: "camera.options"})}
            form={this.props.form}
            formItemOpts={{ style: { marginBottom: "12px" } }}
            getFieldDecoratorOpts={{

            }}
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
              message={<MultilineText lines={toJS(this.globalErrors)} />}
              type="error"
              style={{ marginBottom: "24px" }}
            />
          )}

          <Form.Item style={{ textAlign: "center" }}>
            <Link to={CameraManagementa.PATH}>
              <Button htmlType="button">
                <FormattedMessage id="management.editor.cancel" />
              </Button>
            </Link>
            <Button
              type="primary"
              htmlType="submit"
              disabled={status !== "DONE" && status !== "ERROR"}
              loading={status === "LOADING"}
              style={{ marginLeft: "8px" }}
            >
              <FormattedMessage id="management.editor.submit" />
            </Button>
          </Form.Item>
        </Form>
      </Card>
    );
  }

  componentDidMount() {
    if (this.props.entityId !== CameraManagementa.NEW_SUBPATH) {
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
      }
    );
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
