import {trackPromise} from "react-promise-tracker";

export const BACKEND_HOST = process.env.REACT_APP_BACKEND_HOST;


export const ROUTE_KUBERNETES_INFO = BACKEND_HOST + "/kubernetes/info/{kind}/{name}";
export const ROUTE_KUBERNETES_CLUSTERINFO = BACKEND_HOST + "/kubernetes/clusterinfo";
export const ROUTE_KUBERNETES_INIT = BACKEND_HOST + "/kubernetes/init";


export const GET = 'GET';
export const POST = 'POST';
export const PUT = 'PUT';


export const callApi = (uri, method, onSuccess, onError, body = null, responseModifier = (response) => {return response.json()}) => {
    trackPromise(fetch(uri, {
            method: method,
            mode: "cors",
            credentials: 'include',
            headers: {
                "Connection": "keep-alive",
                "Content-Type": "application/json"
            },
            body: body
        }).then(response => {
            if (response.ok) {
                return responseModifier(response);
            } else {
                onError(response);
            }
        })
            .then(response => {
                onSuccess(response);
            }).catch(error => {
                console.log(error);
                onError(error);
            })
    );
}