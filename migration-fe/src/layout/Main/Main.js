import React, {PureComponent} from 'react';
import {Button, Layout, Typography} from 'antd';
import Wizard from "../Wizard/Wizard";
import FooterContent from "../Footer/FooterContent";
const { Content, Footer } = Layout;
const { Paragraph, Text, Title } = Typography;

export default class Main extends PureComponent {

    constructor (props) {
        super(props);
        this.state = {
            showWizard: false
        }
    }

    toggleShowWizard = () => {
        this.setState({showWizard: !this.state.showWizard})
    };

    render() {
        const {showWizard} = this.state;
        return  (<div>
            {showWizard ? <Wizard onClose={this.toggleShowWizard} /> :
                <Layout className="layout">
                    <Content className="main-page-content">
                        <Title>Hello, cloud users!</Title>
                        <Title level={3}>Welcome to <Text mark>opek8s</Text> migration app.</Title>
                        <Paragraph>
                            This small app was developed to help you migrate your work from <Text mark>Kubernetes</Text> to <Text mark>OpenShift</Text> platforms.
                            In a few clicks you will be able to select object you want to move.
                        </Paragraph>
                        <Paragraph>
                            Please, notice that app will need to have appropriate roles in your clusters. For Kubernetes cluster, please ensure, that application account has capabilities to read all objects. For OpenShift, the app has to have ability to list namespaces and to create new objects.
                        </Paragraph>
                        <Paragraph>
                            To start tour journey, please click on the button below.
                        </Paragraph>
                        <Button type="primary" onClick={this.toggleShowWizard}>Start</Button>
                    </Content>
                    <Footer>
                        <FooterContent />
                    </Footer>
                </Layout> }
            </div>
        )
    }

}