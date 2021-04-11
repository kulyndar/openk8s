import {PureComponent} from "react";
import {Form, Input, Button, Select} from 'antd';
import {callApi, PUT, ROUTE_KUBERNETES_INIT} from "../../routes/API";
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

    handleSuccess = (response) => {
        if (response) {
            if (response.success) {
                this.props.onSuccess(response);
            } else {
                this.props.onError(response);
            }
        }
    };

    onError = () => {
        this.props.onError();
    };


    render() {
        const {authType} = this.state;
        const onFinish = (values) => callApi(ROUTE_KUBERNETES_INIT, PUT, this.handleSuccess, this.onError, JSON.stringify(values));

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