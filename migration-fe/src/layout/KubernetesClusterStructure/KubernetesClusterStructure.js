import {PureComponent} from "react";
import {Alert, Button, Collapse, Empty, message, Modal, Space, Tree, Typography} from 'antd';
import yaml from 'js-yaml';
import {CheckCircleTwoTone, CloseCircleTwoTone, ExclamationCircleTwoTone, InfoCircleTwoTone} from '@ant-design/icons';
import {callApi, GET, ROUTE_KUBERNETES_CLUSTERINFO, ROUTE_KUBERNETES_INFO} from "../../routes/API";


const {Panel} = Collapse;
const { Title } = Typography;


export default class KubernetesClusterStructure extends PureComponent {

    constructor(props) {
        super(props);
        this.state = {
            clusterStructure: null,
            messages: [],
            selected: []
        }
    }

    componentDidMount() {
        callApi(ROUTE_KUBERNETES_CLUSTERINFO, GET, this.onClusterInfoSuccess, this.onError);
    }

    onClusterInfoSuccess = (response) => {
        if (response) {
            this.setState({ ...this.makeTreeStructureSimple(response.simpleStructure), messages: response.messages, clusterStructureFromBe: response.structure});
        }
    };

    makeTreeStructureSimple = (structure) => {
        let data = [];
        const uidMap = new Map();
        const parentChildUidMap = new Map();
        const childParentUidMap = new Map();
        if (structure) {
            structure.forEach(ns => {
                const namespaceDisables = ns.name && ns.name.startsWith("kube-");
                const namespace = this.createResource(ns, null);
                namespace.disabled = namespaceDisables;
                const childrenRes = this.makeSubtree(ns.children, [], parentChildUidMap, uidMap, null, childParentUidMap);
                namespace.children.push(...childrenRes);
                data.push(namespace)
            })
        }
        return {clusterStructure: data, uidMap: uidMap, parentChildUidMap: parentChildUidMap, childParentUidMap: childParentUidMap};
    };

    makeSubtree = (children, parents, parentChildUidMap, uidMap, parent, childParentUidMap) => {
        const data = [];
        children.forEach(ch => {
            if (!parentChildUidMap.has(ch.uid)) {
                parentChildUidMap.set(ch.uid, []);
            }
            if (parents && parents.length > 0) {
                childParentUidMap.set(ch.uid, parents);
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

    getTreeData = (roots, ns, disabled) => {
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
    };

    onSubmit = () => {
        const {childParentUidMap, uidMap, selected} = this.state;
        if (!selected || selected.length === 0) {
            message.error("Please, select some resources to migrate.");
            return;
        }
        const finalSelected = [];
        selected.forEach(item => {
            if (childParentUidMap.get(item)) {
                const parents = childParentUidMap.get(item);
                if (!parents.some(p => selected.includes(p))) {
                    finalSelected.push(uidMap.get(item));
                }
            } else {
                finalSelected.push(uidMap.get(item));
            }
        });
        this.props.onSubmit(finalSelected);
    };

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
        let uri = ROUTE_KUBERNETES_INFO.replace("{kind}", item.kind).replace("{name}", item.name);
        if (item.namespace) {
            uri = uri + "?namespace=" + item.namespace;
        }
        callApi(uri, GET, (resp) => this.onItemDescriptionSuccess(resp, item), this.onError, null, (r) => r.text());
    };

    onItemDescriptionSuccess = (response, item) => {
        if (response) {
            this.setState({openInfoModal: true, infoModalItem: item, infoForModal: response});
        }
    };

    onError = () => {
        this.props.onError();
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
            <Button type="primary" htmlType="submit" onClick={this.onSubmit}>
                Migrate
            </Button>
        </Space>
        </div>)

    }

}