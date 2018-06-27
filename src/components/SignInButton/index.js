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

import React, { Component } from 'react';
import './styles.css';

export class SignInButton extends Component {
    constructor(props) {
        super(props);
        this.state = {
            style: {
                display: "none",
                top: "-9999px",
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
        this.adjustLayout = this.adjustLayout.bind(this);
        this.handleResizeEvent = this.handleResizeEvent.bind(this);
        this.handleOrientationChangeEvent = this.handleOrientationChangeEvent.bind(this);
        this.handleMutationEvent = this.handleMutationEvent.bind(this);
    }
    componentDidMount() {
        window.addEventListener('resize', this.handleResizeEvent);
        window.addEventListener('orientationchange', this.handleOrientationChangeEvent);
        if (window.MutationObserver) {
            this.observer = new MutationObserver(this.handleMutationEvent);
            this.observer.observe(document.body, { attributes: true, childList: true });
        }
        this.interval = setInterval(this.adjustLayout, 250);
    }
    componentWillUnmount() {
        window.removeEventListener('resize', this.handleResizeEvent);
        window.removeEventListener('orientationchange', this.handleOrientationChangeEvent);
        if (this.observer) {
            this.observer.disconnect();
            delete this.observer;
            this.observer = null;
        }
        clearInterval(this.interval);
        this.interval = null;
    }
    componentWillReceiveProps(nextProps) {
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
    adjustLayout() {
        const {relatedElements} = this.props;
        let height = 0;
        const needSizeCalculation = !this.initialized || this.eventState.didResize || this.eventState.didOrientationChange || this.eventState.didMute;
        const eventWasTriggered = needSizeCalculation;
        this.eventState = {
            didResize: false,
            didOrientationChange: false,
            didMute: false
        };
        this.initialized = true;
        let cookieChange = false;
        relatedElements.forEach(e => {
            let doCalc = e.height === undefined || needSizeCalculation;
            if (e.cookieKey && (e.cookieValue === undefined || e.cookieValue === "")) {
                let value = document.cookie;
                if (typeof e.cookieKey === "string") {
                    value = "";
                    const cookie = "; " + document.cookie;
                    const parts = cookie.split("; " + e.cookieKey + "=");
                    if (parts.length === 2)
                        value = parts.pop().split(";").shift();
                }
                if (e.cookieValue !== value) {
                    e.cookieValue = value;
                    cookieChange = true;
                    if (value !== "")
                        e.height = 0;
                    else 
                        doCalc = true;
                }
            }
            if (doCalc) {
                e.height = 0;
                if (!e.conditionQuerySelector || document.querySelector(e.conditionQuerySelector)) {
                    if (!e.nodes || !e.nodes.length)
                        e.nodes = document.querySelectorAll(e.querySelector);
                    e.nodes.forEach(n => {
                        e.height += n.offsetHeight;
                    });
                }
            }
            height += e.height;
        });
        if (this.buttonRef) 
            height -= (this.buttonRef.offsetHeight + 10);
        if (cookieChange || eventWasTriggered) {
            this.setState({style: {top: height + "px"}});
        }
    }
    render() {
        const { show, onClick } = this.props;
        const button = show?<a ref={this.setButtonRef} href={onClick} style={this.state.style}>Login</a>:null;
        if (!show)
            return null;

        return (
            <div ref={this.setButtonRef} className="kgs-sign-in" style={this.state.style}>{button}</div>
        );
    }
}