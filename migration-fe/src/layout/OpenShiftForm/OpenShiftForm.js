import {PureComponent} from "react";
import {Form, Input, Button, Select} from 'antd';
import {callApi, POST, ROUTE_OPENSHIFT_INIT} from "../../routes/API";
const { Option } = Select;

const layout = {
    labelCol: { span: 8 },
    wrapperCol: { span: 10 },
};
const tailLayout = {
    wrapperCol: { offset: 11, span: 16 },
};

export default class OpenShiftForm extends PureComponent {

    constructor(props) {
        super(props);
        this.state = {
            authType: 'basic'
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
        const onFinish = (values) => callApi(ROUTE_OPENSHIFT_INIT, POST, this.handleSuccess, this.onError, JSON.stringify(values));

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
                    label="OpenShift cluster IP"
                    name="ocIp"
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
                    initialValue='basic'
                >
                    <Select style={{ width: 300 }} onChange={this.handleAuthChange}>
                        <Option value="basic">Basic Auth</Option>
                        <Option value="token">Token</Option>
                    </Select>
                </Form.Item>

                {
                    authType === 'basic' &&
                    <Form.Item
                        label="Username"
                        name="username"
                        rules={[
                            {
                                required: true,
                                message: 'Please input your username',
                            }
                        ]}
                    >
                        <Input placeholder='Username' />
                    </Form.Item>
                }
                {
                    authType === 'basic' &&
                    <Form.Item
                        label="Password"
                        name="password"
                        rules={[
                            {
                                required: true,
                                message: 'Please input your password!',
                            }
                        ]}
                    >
                        <Input.Password placeholder='Password' />
                    </Form.Item>
                }
                {
                    authType === 'token' &&
                    <Form.Item
                        label="Token"
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
                    <Button type="default" onClick={this.props.onPrev}>
                        Back
                    </Button>
                    <Button type="primary" htmlType="submit">
                        Next
                    </Button>
                </Form.Item>
            </Form>
        );
    }

}