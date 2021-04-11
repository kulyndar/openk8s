
import './App.css';
import React from "react";
import Main from "./layout/Main/Main";
import { usePromiseTracker } from "react-promise-tracker";
import {Spin} from "antd";


const LoadingIndicator = props => {
    const { promiseInProgress } = usePromiseTracker();
    return promiseInProgress && (
        <div className="overlap" >
        <Spin size="large" />
        </div>
        );
    };

function App() {
  return (
    <div className="App">

        <Main />
        <LoadingIndicator />

    </div>
  );
}

export default App;
