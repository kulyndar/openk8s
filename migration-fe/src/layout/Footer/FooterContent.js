import React, {PureComponent} from "react";
import {GithubOutlined, MailOutlined} from "@ant-design/icons";
import {Col, Row} from "antd";

export default class FooterContent extends PureComponent {

    render() {
        return (<div className='footer'>
            <Row>
                <Col span={16} offset={4}>
                    <div className='description'>This project was created as a part of the Master's Thesis.</div>
                    <div className='copyright'>ČVUT FEL 2021 &copy; Created by Daria Kulynychenko</div>
                </Col>
                <Col className="colLinks">
                    <div className='links'><a href="https://github.com/kulyndar/openk8s" target="_blank"><GithubOutlined /> GitHub</a> </div>
                    <div className='links'><a href="mailto:kulyndar@fel.cvut.cz" target="_blank"><MailOutlined /> kulyndar@fel.cvut.cz</a> </div>
                </Col>
            </Row>


        </div>)
    }
}