import React, {PureComponent} from 'react';
import {Button} from 'antd';
import Wizard from "../Wizard/Wizard";

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
            {showWizard ? <Wizard onClose={this.toggleShowWizard} /> : <Button type="primary" onClick={this.toggleShowWizard}>Start</Button> }
            </div>
        )
    }

}