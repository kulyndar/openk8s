import React, {PureComponent} from "react";
import {Steps, Button, Layout, message, Result, Typography} from "antd";
import {CloseOutlined}  from '@ant-design/icons';
import KubernetesForm from "../KubernetesForm/KubernetesForm";
import KubernetesClusterStructure from "../KubernetesClusterStructure/KubernetesClusterStructure";
import OpenShiftForm from "../OpenShiftForm/OpenShiftForm";
import {callApi, POST, ROUTE_OPENSHIFT_MIGRATE} from "../../routes/API";
import CloseCircleOutlined from "@ant-design/icons/lib/icons/CloseCircleOutlined";

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

    renderResult = () => {
        const {migrationResult} = this.state;
        if (!migrationResult || migrationResult.length === 0) {
            return (<Result
                status="success"
                title="Cluster was successfully migrated!"
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
                title="Some problems occurred during cluster migration."
                subTitle="Some items were not migrated. See reasons below. Please, repair cluster and try again."
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
                            Following error occurred during migration:
                        </Text>
                    </Paragraph>
                    {
                        migrationResult.map(result => {
                            let message = "";
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
        const {current, showResult} = this.state;
        const steps = this.prepareSteps();
        return (
            showResult ? this.renderResult() :
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
                    <div className="steps-content">{steps[current].content}</div>
                </Content>
            </Layout>
        );
    };
}