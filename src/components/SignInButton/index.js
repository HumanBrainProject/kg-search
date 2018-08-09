/*
*   Copyright (c) 2018, EPFL/Human Brain Project PCO
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

import React, { Component } from "react";
import "./styles.css";

export class SignInButton extends Component {
    constructor(props) {
        super(props);
        this.state = {
            style: {
                display: props.show?"block":"none",
                top: "25px",
            }
        };
        this.eventState = {
            didResize: false,
            didOrientationChange: false,
            didMute: false
        };
        this.buttonRef = null;
        this.setButtonRef = element => {
            this.buttonRef = element;
        };
        this.initialized = false;
        this.observer = null;
        this.interval = null;
        this.handleResizeEvent = this.handleResizeEvent.bind(this);
        this.handleOrientationChangeEvent = this.handleOrientationChangeEvent.bind(this);
        this.handleMutationEvent = this.handleMutationEvent.bind(this);
    }
    this.interval = setInterval(this.adjustLayout, 250);
  }
  componentWillUnmount() {
    window.removeEventListener("resize", this.handleResizeEvent);
    window.removeEventListener("orientationchange", this.handleOrientationChangeEvent);
    if (this.observer) {
      this.observer.disconnect();
      delete this.observer;
      this.observer = null;
    }
    clearInterval(this.interval);
    this.interval = null;
  }
  UNSAFE_componentWillReceiveProps(nextProps) {
    if (nextProps.show !== this.props.show) {
      const state = Object.assign({}, this.state, {style: {display: nextProps.show?"block":"none"}});
      this.setState(state);
    }
    UNSAFE_componentWillReceiveProps(nextProps) {
        if (nextProps.show !== this.props.show) {
            const state = Object.assign({}, this.state, {style: {display: nextProps.show?"block":"none"}});
            this.setState(state);
        }
    }
    shouldComponentUpdate(nextProps, nextState) {
        return nextProps.show !== this.props.show || nextState.style.display !== this.state.style.display || nextState.style.top !== this.state.style.top;
    }
    handleResizeEvent() {
        this.eventState.didResize = true;
    }
    handleOrientationChangeEvent() {
        this.eventState.didOrientationChange = true;
    }
    handleMutationEvent() {
        this.eventState.didMute = true;
    }
    render() {
        const { show, onClick } = this.props;
        const button = <a ref={this.setButtonRef} href={onClick} style={this.state.style}>Login</a>;
        return (
           show?<div ref={this.setButtonRef} className="kgs-sign-in" style={this.state.style}>{button}</div>:null
        );

    }
    return (
      <div ref={this.setButtonRef} className="kgs-sign-in" style={this.state.style}>{button}</div>
    );
  }
}