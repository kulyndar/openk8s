import React, {PureComponent} from "react";
import {Steps, Button, Layout, message, Result, Typography, Space, Popconfirm} from "antd";
import {CloseOutlined}  from '@ant-design/icons';
import KubernetesForm from "../KubernetesForm/KubernetesForm";
import KubernetesClusterStructure from "../KubernetesClusterStructure/KubernetesClusterStructure";
import OpenShiftForm from "../OpenShiftForm/OpenShiftForm";
import {
    callApi,
    POST,
    ROUTE_OPENSHIFT_CLEAR_ROLLBACK,
    ROUTE_OPENSHIFT_MIGRATE,
    ROUTE_OPENSHIFT_ROLLBACK
} from "../../routes/API";
import CloseCircleOutlined from "@ant-design/icons/lib/icons/CloseCircleOutlined";
import FooterContent from "../Footer/FooterContent";
import CheckCircleOutlined from "@ant-design/icons/lib/icons/CheckCircleOutlined";

const { Step } = Steps;
const { Header, Content, Footer } = Layout;
const { Paragraph, Text } = Typography;

export default class Main extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {
            current: 0,
            errors: []
        }
    }

    next = () => {
        const {current} = this.state;
        this.setCurrent(current + 1);
        const errors = [...this.state.errors];
        errors[current] = false;
        this.setState({errors: errors})
    };

    prev = () => {
        const {current} = this.state;
        this.setCurrent(current - 1);
    };

    error = (status) => {
        const errors = [...this.state.errors];
        errors[this.state.current] = true;
        this.setState({errors: errors});
        if (status) {
            message.error(<span><b>{status.cause}</b>{status.message}</span>)
        } else {
            message.error(<span><b>Unexpected error occurred!</b></span>)
        }

    };

    success = (status) => {
        if (status) {
            message.success(<span><b>{status.cause}</b>{status.message}</span>)
        }
        this.next();
    };

    setCurrent = (newCurrent) => {
        this.setState({current: newCurrent});
    };

    onSubmit = (selectedItems) => {
        callApi(ROUTE_OPENSHIFT_MIGRATE, POST, this.handleMigrationSuccess, this.error, JSON.stringify(selectedItems))
    };

    handleMigrationSuccess = (response) => {
        this.setState({showResult: true, migrationResult: response});
    };

    handleRollbackSuccess = (response) => {
        this.setState({showRollbackResult: true, showResult: false,  rollbackResult: response});
    };

    onRollback = () => {
        callApi(ROUTE_OPENSHIFT_ROLLBACK, POST, this.handleRollbackSuccess, this.error);
    };

    onClose = () => {
        callApi(ROUTE_OPENSHIFT_CLEAR_ROLLBACK, POST, () => {}, this.error, null, (r) => r.text());
        this.props.onClose();
    };

    prepareSteps = () => {
        return [
            {
                title: 'Connect to Kubernetes',
                content: (<KubernetesForm onNext={this.next} onError={this.error} onSuccess={this.success} />),
            },
            {
                title: 'Connect to OpenShift',
                content: (<OpenShiftForm onNext={this.next} onError={this.error} onSuccess={this.success} onPrev={this.prev} />),
            },
            {
                title: 'Select data',
                content: (<KubernetesClusterStructure onNext={this.next} onError={this.error} onSuccess={this.success} onPrev={this.prev} onSubmit={this.onSubmit}/>),
            },
        ];
    };

    renderResultOk = (messageOK) => {
        return messageOK && messageOK.length > 0 &&
            (<div className="desc">
                <Paragraph>
                    <Text
                        strong
                        style={{
                            fontSize: 16,
                        }}
                    >
                        Following items were successfully migrated:
                    </Text>
                </Paragraph>
                {
                    messageOK.map(result => {
                        let message;
                        if (result.kind && result.name) {
                            message =  <span>{result.kind} {result.name} was migrated. </span>
                        }
                        return (
                            <Paragraph>
                                <CheckCircleOutlined className="site-result-ok-icon" /> {message}
                            </Paragraph>
                        );
                    })
                }
            </div>);
    };

    renderResultErrors = (messageErrors) => {
        return messageErrors && messageErrors.length > 0 &&
            (<div className="desc">
                <Paragraph>
                    <Text
                        strong
                        style={{
                            fontSize: 16,
                        }}
                    >
                        Following error occurred during migration:
                    </Text>
                </Paragraph>
                {
                    messageErrors.map(result => {
                        let message;
                        if (result.kind && result.name) {
                            message =  <span>{result.kind} {result.name} was not migrated. Reason:
                                <b>{result.cause} </b>{result.message}</span>
                        } else {
                            message = <span><b>{result.cause} </b>{result.message}</span>
                        }
                        return (
                            <Paragraph>
                                <CloseCircleOutlined className="site-result-error-icon" /> {message}
                            </Paragraph>
                        );
                    })
                }
                <Paragraph>
                    <Text
                        strong
                        style={{
                            fontSize: 16,
                        }}
                    >
                        To check result run the following command in your cluster for each object:
                    </Text>
                </Paragraph>
                <Paragraph>
                    <Text code>oc get -n [namespace] [type] [name]</Text>
                </Paragraph>
            </div>);
    };

    renderResult = () => {
        const {migrationResult} = this.state;
        const migrationOk = migrationResult && migrationResult.filter(item => item.success);
        const migrationErrors = migrationResult && migrationResult.filter(item => !item.success);
        if (!migrationErrors || migrationErrors.length === 0) {
            return (<Result
                status="success"
                title="Cluster was successfully migrated!"
                subTitle="Click the button bel return to the home page"
                extra={[
                    <Space>
                        <Button type="primary" key="goHome" onClick={this.onClose}>
                            Finish
                        </Button>
                        <Popconfirm title="Are you sure？ All migrated items except namespaces will be deleted." okText="Yes" cancelText="No" onConfirm={this.onRollback}>
                            <Button type="danger" key="rollback">
                            Rollback changes
                        </Button>
                        </Popconfirm>
                    </Space>
                ]}
            >
                {this.renderResultOk(migrationOk)}
            </Result>);
        } else {
            return (<Result
                status="warning"
                title="Some problems occurred during cluster migration."
                subTitle="Some items were not migrated. See reasons below. Please, repair cluster and try again."
                extra={[
                    <Space>
                    <Button type="primary" key="goHome" onClick={this.onClose}>
                        Finish
                    </Button>
                        <Popconfirm title="Are you sure？ All migrated items except namespaces will be deleted." okText="Yes" cancelText="No" onConfirm={this.onRollback}>
                            <Button type="danger" key="rollback">
                                Rollback changes
                            </Button>
                        </Popconfirm>
                    </Space>
                        ]}
            >
                {this.renderResultErrors(migrationErrors)}
                {this.renderResultOk(migrationOk)}
            </Result>);
        }
    };

    renderRollbackResult = () => {
        const {rollbackResult} = this.state;
        if (!rollbackResult || rollbackResult.length === 0) {
            return (<Result
                status="success"
                title="Changes were successfully rollbacked!"
                subTitle="Click the button bel return to the home page"
                extra={[
                    <Button type="primary" key="goHome" onClick={this.props.onClose}>
                        Finish
                    </Button>
                ]}
            />);
        } else {
            return (<Result
                status="warning"
                title="Some problems occurred during rollback operation."
                subTitle="Some items were not rollbacked. See reasons below. Please, repair cluster manually."
                extra={[
                    <Button type="primary" key="goHome" onClick={this.props.onClose}>
                        Finish
                    </Button>
                ]}
            >
                <div className="desc">
                    <Paragraph>
                        <Text
                            strong
                            style={{
                                fontSize: 16,
                            }}
                        >
                            Following error occurred during rollback:
                        </Text>
                    </Paragraph>
                    {
                        rollbackResult.map(result => {
                            let message;
                            if (result.kind && result.name) {
                                message =  <span>{result.kind} {result.name} was not migrated. Reason:
                                <b>{result.cause} </b>{result.message}</span>
                            } else {
                                message = <span><b>{result.cause} </b>{result.message}</span>
                            }
                            return (
                                <Paragraph>
                                    <CloseCircleOutlined className="site-result-error-icon" /> {message}
                                </Paragraph>
                            );
                        })
                    }
                </div>
            </Result>);
        }
    };

    render() {
        const {current, showResult, showRollbackResult} = this.state;
        const steps = this.prepareSteps();
        return (
            <div>
            {showResult &&
                <Layout className='layout'>
                    <Content>
                        {this.renderResult()}
                    </Content>
                    <Footer>
                        <FooterContent />
                    </Footer>
                </Layout>}
            {showRollbackResult &&
            <Layout className='layout'>
                <Content>
                    {this.renderRollbackResult()}
                </Content>
                <Footer>
                    <FooterContent />
                </Footer>
            </Layout>
            }
            {!showResult && !showRollbackResult &&
            <Layout className="layout">
                <Header className="header">
                    <Steps current={current}>
                        {steps.map((item, idx) => (
                            <Step key={item.title} title={item.title} status={this.state.errors[idx] ? 'error' : undefined} />
                        ))}
                    </Steps>
                    <CloseOutlined onClick={this.props.onClose} className="closeButton" />
                </Header>
                <Content>
                    <div className={"steps-content steps-content-" + current}>{steps[current].content}</div>
                </Content>
                <Footer>
                    <FooterContent />
                </Footer>
            </Layout>
            }
            </div>
        );
    };
}