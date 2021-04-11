import {PureComponent} from "react";
import {Form, Input, Button, Checkbox, Select, message} from 'antd';
import { trackPromise } from 'react-promise-tracker';
const { Option } = Select;

const layout = {
    labelCol: { span: 8 },
    wrapperCol: { span: 10 },
};
const tailLayout = {
    wrapperCol: { offset: 11, span: 16 },
};

export default class KubernetesForm extends PureComponent {

    constructor(props) {
        super(props);
        this.state = {
            authType: 'boot'
        }
    }

    handleAuthChange = (value) => {
        this.setState({authType: value});
    };


    render() {
        const {authType} = this.state;
        const onFinish = (values) => {
            trackPromise(fetch("http://localhost:8080/init/kubernetes", {
                method: 'PUT',
                mode: "cors",
                credentials: 'include',
                headers: {
                    "Connection": "keep-alive", // request content type,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(values)
            }).then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    this.props.onError();
                }
            })
                .then(response => {
                    if (response) {
                        if (response.success) {
                            this.props.onSuccess(response);
                        } else {
                            this.props.onError(response);
                        }
                    }
                }).catch(error => {
                    console.log(error);
                    this.props.onError()
            })
            );

        };

        const onFinishFailed = (errorInfo) => {
            console.log('Failed:', errorInfo);
        };

        return (
            <Form
                {...layout}
                name="basic"

                onFinish={onFinish}
                onFinishFailed={onFinishFailed}
            >
                <Form.Item
                    label="Kubernetes cluster IP"
                    name="kubeip"
                    rules={[
                        {
                            required: true,
                            message: 'Please input cluster IP!',
                        },
                        {
                            type: 'url',
                            message: 'Please enter valid url address'
                        }
                    ]}
                >
                    <Input placeholder='Cluster IP' />
                </Form.Item>

                <Form.Item
                    label="Authentication type"
                    name="authType"
                    initialValue='boot'
                >
                    <Select style={{ width: 300 }} onChange={this.handleAuthChange}>
                        <Option value="boot">Bootstrap Token</Option>
                        <Option value="token">Token</Option>
                    </Select>
                </Form.Item>

                {
                    authType === 'boot' &&
                    <Form.Item
                        label="Token ID"
                        name="tokenId"
                        rules={[
                            {
                                required: true,
                                message: 'Please input your bootstrap Token ID!',
                            }
                        ]}
                    >
                        <Input placeholder='Token ID' />
                    </Form.Item>
                }
                {
                    authType === 'boot' &&
                    <Form.Item
                        label="Token Secret"
                        name="tokeSecret"
                        rules={[
                            {
                                required: true,
                                message: 'Please input your Token Secret!',
                            }
                        ]}
                    >
                        <Input.Password placeholder='Token Secret' />
                    </Form.Item>
                }
                {
                    authType === 'token' &&
                    <Form.Item
                        label="Service Account Token"
                        name="token"
                        rules={[
                            {
                                required: true,
                                message: 'Please input your token!',
                            }
                        ]}
                    >
                        <Input.Password placeholder='Token' />
                    </Form.Item>
                }

                <Form.Item {...tailLayout}>
                    <Button type="primary" htmlType="submit">
                        Next
                    </Button>
                </Form.Item>
            </Form>
        );
    }

}