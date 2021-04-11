import {PureComponent} from "react";
import {
    Form,
    Input,
    Button,
    Checkbox,
    Select,
    message,
    Space,
    Empty,
    Tree,
    Typography,
    Modal,
    Collapse,
    Alert
} from 'antd';
import { trackPromise } from 'react-promise-tracker';
import yaml from 'js-yaml';
import {InfoCircleTwoTone, CheckCircleTwoTone, CloseCircleTwoTone, ExclamationCircleTwoTone}  from '@ant-design/icons';


const { Option } = Select;
const {Panel} = Collapse;


const { Title } = Typography;
const layout = {
    labelCol: { span: 8 },
    wrapperCol: { span: 10 },
};
const tailLayout = {
    wrapperCol: { offset: 11, span: 16 },
};

export default class KubernetesClusterStructure extends PureComponent {

    constructor(props) {
        super(props);
        this.state = {
            clusterStructure: null,
            messages: [],
            selected: []
        }
    }

    handleAuthChange = (value) => {
        this.setState({authType: value});
    };

    componentDidMount() {
        trackPromise(fetch("http://localhost:8080/init/clusterinfo", {
                method: 'GET',
                mode: "cors",
                credentials: 'include',
                headers: {
                    "Connection": "keep-alive",
                }
            }).then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    this.props.onError();
                }
            })
                .then(response => {
                    if (response) {
                        this.setState({ ...this.makeTreeStructureSimple(response.simpleStructure), messages: response.messages, clusterStructureFromBe: response.structure});
                    }
                }).catch(error => {
                    console.log(error);
                    this.props.onError()
                })
        );
    }
    makeTreeStructure = (structure) => {
        let data = [];
        const uidMap = new Map();
        const parentChildUidMap = new Map();
        if (structure) {
            structure.forEach(ns => {
                const namespaceDisables = ns.name && ns.name.startsWith("kube-");
                const namespace = this.createResource(ns, null);
                namespace.disabled = namespaceDisables;
                if (ns.deployments) {
                    ns.deployments.forEach(depl => {
                        if (!parentChildUidMap.has(depl.uid)) {
                            parentChildUidMap.set(depl.uid, []);
                        }
                        const deployment = this.createResource(depl, null);
                        if (depl.replicaSets) {
                            depl.replicaSets.forEach(rs => {
                                const replicaSet = this.createResource(rs, depl);
                                if (!parentChildUidMap.has(rs.uid)) {
                                    parentChildUidMap.set(rs.uid, []);
                                }
                                if (rs.pods) {
                                    rs.pods.forEach(p => {
                                        const pod = this.createResource(p, replicaSet);
                                        replicaSet.children.push(pod);
                                        uidMap.set(p.uid, p);
                                        parentChildUidMap.get(depl.uid).push(p.uid);
                                        parentChildUidMap.get(rs.uid).push(p.uid);
                                    });
                                }
                                deployment.children.push(replicaSet);
                                uidMap.set(rs.uid, rs);
                                parentChildUidMap.get(depl.uid).push(rs.uid);
                            });
                        }
                        namespace.children.push(deployment);
                        uidMap.set(depl.uid, depl);
                    });
                }
                if (ns.pods) {
                    ns.pods.forEach(p => {
                        const pod = this.createResource(p, null);
                        namespace.children.push(pod);
                        uidMap.set(p.uid, p);
                    });
                }
                if (ns.replicaSets) {
                    ns.replicaSets.forEach(rs => {
                        const replicaSet = this.createResource(rs, null);
                        if (!parentChildUidMap.has(rs.uid)) {
                            parentChildUidMap.set(rs.uid, []);
                        }
                        if (rs.pods) {
                            rs.pods.forEach(p => {
                                const pod = this.createResource(p, replicaSet);
                                replicaSet.children.push(pod);
                                uidMap.set(p.uid, p);
                                parentChildUidMap.get(rs.uid).push(p.uid);
                            });
                        }
                        namespace.children.push(replicaSet);
                        uidMap.set(rs.uid, rs);
                    });
                }
                if (ns.services) {
                    ns.services.forEach(s => {
                        const service = this.createResource(s, null);
                        namespace.children.push(service);
                        uidMap.set(s.uid, s);
                    });
                }


                data.push(namespace);
                uidMap.set(ns.uid, ns);
            })
        }
        return {clusterStructure: data, uidMap: uidMap, parentChildUidMap: parentChildUidMap};
    };

    makeTreeStructureSimple = (structure) => {
        let data = [];
        const uidMap = new Map();
        const parentChildUidMap = new Map();
        if (structure) {
            structure.forEach(ns => {
                const namespaceDisables = ns.name && ns.name.startsWith("kube-");
                const namespace = this.createResource(ns, null);
                namespace.disabled = namespaceDisables;
                const childrenRes = this.makeSubtree(ns.children, [], parentChildUidMap, uidMap, null);
                namespace.children.push(...childrenRes);
                data.push(namespace)
            })
        }
        return {clusterStructure: data, uidMap: uidMap, parentChildUidMap: parentChildUidMap};
    };

    makeSubtree = (children, parents, parentChildUidMap, uidMap, parent) => {
        const data = [];
        children.forEach(ch => {
            if (!parentChildUidMap.has(ch.uid)) {
                parentChildUidMap.set(ch.uid, []);
            }
            const child = this.createResource(ch, parent);
            const childrenRes = this.makeSubtree(ch.children, [...parents, child.uid], parentChildUidMap, uidMap, ch);
            child.children.push(...childrenRes);
            parents.forEach(par => parentChildUidMap.get(par).push(ch.uid));
            data.push(child);
            uidMap.set(ch.uid, ch);
        });
        return data;
    };

    createResource = (obj, parentRes) => {
        return  {
            title: obj.name,
            type: obj.kind,
            uid: obj.uid,
            parentUid: parentRes && parentRes.uid,
            children: []
        };
    };

    getTreeData(roots, ns, disabled) {
       const {selected} = this.state;
       let data = [];
       const namespaceDisables = ns.disabled;
       if (roots && roots.length > 0) {
           roots.forEach(root => {
               const dis = namespaceDisables || disabled.includes(root.parentUid) || selected.includes(root.parentUid);
               if (dis) {
                   disabled.push(root.uid);
               }
                const node = {
                    title: <span>{root.type} <b>{root.title}</b></span>,
                    key: root.uid,
                    disableCheckbox: dis,
                    children: this.getTreeData(root.children, ns, disabled)
                };
               data.push(node);
           });
       }
       return data;
    }
    onCheck = (keys) => {
        const {parentChildUidMap} = this.state;
        const oldSelected = [...this.state.selected];
        let newSelected = [...keys.checked];
        const selected = newSelected.filter(item => oldSelected.indexOf(item) < 0);
        const unselected = oldSelected.filter(item => newSelected.indexOf(item) < 0);
        if (selected.length > 0) {
            selected.forEach(item => {
                if (parentChildUidMap && parentChildUidMap.has(item)) {
                    newSelected.push(...parentChildUidMap.get(item));
                }
            })
        }
        if (unselected.length > 0) {
            unselected.forEach(item => {
                if (parentChildUidMap && parentChildUidMap.has(item)) {
                    newSelected = newSelected.filter(uid => parentChildUidMap.get(item).indexOf(uid) < 0);
                }
            })
        }
        this.setState({selected: newSelected})
    };

    getItemDescription = (item) => {
        trackPromise(fetch("http://localhost:8080/init/info" + "/" + item.kind + "/" + item.name  + (item.namespace ? "?namespace=" + item.namespace : ""), {
                method: 'GET',
                mode: "cors",
                credentials: 'include',
                headers: {
                    "Connection": "keep-alive",
                }
            }).then(response => {
                if (response.ok) {
                    return response.text();
                } else {
                    this.props.onError();
                }
            })
                .then(response => {
                    if (response) {
                        this.setState({openInfoModal: true, infoModalItem: item, infoForModal: response});
                    }
                }).catch(error => {
                    console.log(error);
                    this.props.onError()
                })
        );
    };

    onSelect = (key) => {
        const {uidMap} = this.state;
        if (key && key[0])
        if (uidMap.has(key[0])) {
            const item = uidMap.get(key[0]);
            this.getItemDescription(item);
        }
    };

    renderModal = () => {
        const {openInfoModal, infoModalItem, infoForModal} = this.state;
        return openInfoModal && (
            <Modal
                title={(infoModalItem.namespace ? "[" + infoModalItem.namespace + "] " : "") + infoModalItem.kind + " " + infoModalItem.name}
                centered
                visible={openInfoModal}
                onCancel={() => this.setState({openInfoModal: false, infoModalItem: null, infoForModal: null})}
                cancelText={"Close"}
                width={1000}
            >
                <div><pre className="yaml" >{yaml.dump(infoForModal)}</pre></div>

            </Modal>
        );
    };

    createHtml = (text) => {
        console.log(text);
        return {__html: text}
    };

    renderMessages = () => {
        const {messages} = this.state;
        if (!messages || messages.length === 0) {
            return null;
        }
        const infoMessages = messages.filter(m => m.type === 'info');
        const successMessages = messages.filter(m => m.type === 'success');
        const warnMessages = messages.filter(m => m.type === 'warning');
        const errorMessages = messages.filter(m => m.type === 'error');

        return (
            <Collapse expandIconPosition={'left'} ghost>
                {infoMessages.length > 0 && <Panel header={<div><InfoCircleTwoTone twoToneColor='#1890ff' /> <span>{"Information messages (" + infoMessages.length + ")"}</span></div>} key="1">
                    {infoMessages.map(m => {
                        return <Alert message={<div dangerouslySetInnerHTML={this.createHtml(m.message)}/>} type={m.type} />
                    })}
                </Panel>}
                {successMessages.length > 0 && <Panel header={<div><CheckCircleTwoTone twoToneColor='#52c41a' /> <span>{"Success messages (" + successMessages.length + ")"}</span></div>} key="2">
                    {successMessages.map(m => {
                        return <Alert message={<div dangerouslySetInnerHTML={this.createHtml(m.message)}/>} type={m.type} />
                    })}
                </Panel>}
                {warnMessages.length > 0 && <Panel header={<div><ExclamationCircleTwoTone twoToneColor='#faad14' /> <span>{"Warning messages (" + warnMessages.length + ")"}</span></div>} key="3">
                    {warnMessages.map(m => {
                        return <Alert message={<div dangerouslySetInnerHTML={this.createHtml(m.message)}/>} type={m.type} />
                    })}
                </Panel>}
                {errorMessages.length > 0 && <Panel header={<div><CloseCircleTwoTone twoToneColor='#ff4d4f' /> <span>{"Error messages (" + errorMessages.length + ")"}</span></div>} key="4">
                    {errorMessages.map(m => {
                        return <Alert message={<div dangerouslySetInnerHTML={this.createHtml(m.message)}/>} type={m.type} />
                    })}
                </Panel>}
            </Collapse>
        );
    };


    render() {
        const {clusterStructure, messages} = this.state;

        if (!clusterStructure && (!messages || messages.length === 0)) {
            return <Empty />;
        }
        return (<div>
            {this.renderModal()}
            {this.renderMessages()}
            {clusterStructure.map(ns => {
            return (
            <div>
                <Title level={3} >Namespace {ns.title}</Title>
                {ns.children.length > 0 ?
                    <Tree
                        checkable
                        onSelect={this.onSelect}
                        onCheck={this.onCheck}
                        checkedKeys={this.state.selected}
                        checkStrictly={true}
                        treeData={this.getTreeData(ns.children, ns, [])}
                    /> : <Empty /> }
            </div>);
        })}
        <Space>
            <Button type="default" onClick={this.props.onPrev}>
                Back
            </Button>
            <Button type="primary" htmlType="submit">
                Next
            </Button>
        </Space>
        </div>)


    }


}