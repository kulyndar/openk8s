import React, {PureComponent} from "react";
import {Steps, Button, Layout, message} from "antd";
import {CloseOutlined}  from '@ant-design/icons';
import KubernetesForm from "../KubernetesForm/KubernetesForm";
import KubernetesClusterStructure from "../KubernetesClusterStructure/KubernetesClusterStructure";

const { Step } = Steps;
const { Header, Content, Footer } = Layout;

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

    prepareSteps = () => {
        return [
            {
                title: 'First',
                content: (<KubernetesForm onNext={this.next} onError={this.error} onSuccess={this.success}/>),
            },
            {
                title: 'Second',
                content: (<KubernetesClusterStructure onNext={this.next} onError={this.error} onSuccess={this.success} onPrev={this.prev}/>),
            },
            {
                title: 'Last',
                content: 'Last-content',
            },
        ];
    };

    render() {
        const {current} = this.state;
        const steps = this.prepareSteps();
        return (
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
                {/*<Footer>*/}
                {/*    <div className="steps-action">*/}
                {/*        {current > 0 && (*/}
                {/*            <Button style={{margin: '0 8px'}} onClick={() => this.prev()}>*/}
                {/*                Previous*/}
                {/*            </Button>*/}
                {/*        )}*/}
                {/*        {current < steps.length - 1 && (*/}
                {/*            <Button type="primary" onClick={() => this.next()}>*/}
                {/*                Next*/}
                {/*            </Button>*/}
                {/*        )}*/}
                {/*        {current === steps.length - 1 && (*/}
                {/*            <Button type="primary">*/}
                {/*                Done*/}
                {/*            </Button>*/}
                {/*        )}*/}

                {/*    </div>*/}
                {/*</Footer>*/}

            </Layout>
        );
    };
}