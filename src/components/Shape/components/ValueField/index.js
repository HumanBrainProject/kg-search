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
import showdown from 'showdown';
/*import FilterXSS from 'xss';*/
import xssFilter from 'showdown-xss-filter';
import { store, dispatch } from "../../../../store";
import * as actions from "../../../../actions";
import './styles.css';

const converter = new showdown.Converter({extensions: [xssFilter]});

export class ValueField extends Component {
    constructor(props) {
        super(props);
        this.state = {
            showDetails: false
        };
        this.toggleDetails = this.toggleDetails.bind(this);
        this.hideDetails = this.hideDetails.bind(this);
        this.showDetails = this.showDetails.bind(this);
    }
    toggleDetails(expand) {
       this.setState({showDetails: expand===false||expand===true?expand:!this.state.showDetails});
    }
    hideDetails() {
       this.toggleDetails(false);
    }
    showDetails() {
       this.toggleDetails(true);
    }
    render() {
        const {value, mapping, showSmartContent} = this.props;

        if (!mapping || !mapping.visible)
        return null;
    
        const handleClick = () => {
            const state = store.getState();
            dispatch(actions.loadHit(value.reference, state.search.index));
        };
    
        let valueTag = null;
        let detailsButton = null;
        let detailsContent = null;
        if (value) {
            if (value.reference && showSmartContent) {
                valueTag = <button onClick={handleClick} role="link">{value.value}</button>;
            } else if (value.url && showSmartContent) {
                if (value.url.substr(0,7).toLowerCase() === "mailto:") {
                    valueTag = <a href={value.url}>{value.value}</a>;
                } else {
                    if (value.detail)
                        valueTag = <a href={value.url} target="_blank">{value.value}</a>;
                    else
                        valueTag = <a href={value.url} target="_blank">{value.value}</a>;
                }
            } else {
                const timestamp = value.value && mapping && mapping.type === "date" && Date.parse(value.value);
                if (timestamp && !isNaN(timestamp)) {
                valueTag = new Date(timestamp).toLocaleDateString();
                } else if (showSmartContent && mapping && mapping.markdown) {
                const html = converter.makeHtml(value.value);
                valueTag = <span className="markdown" dangerouslySetInnerHTML={{__html:html}}></span>;
                } else {
                valueTag = value.value.replace(/<\/?em>/gi,"");
                }
            }
            if (value.detail) {
                const html = converter.makeHtml(value.detail);
            detailsButton = <button className="toggle-details-button" onClick={this.toggleDetails}><i className="fa fa-exclamation-circle"></i>{mapping && mapping.detail_label?<span>{mapping.detail_label}</span>:null}</button>;
                detailsContent = <div className="details-panel">
                    <div className="details-inner-panel">
                        <span className="details-markdown" dangerouslySetInnerHTML={{__html:html}}></span>
                        <button className="collapse-details-button" onClick={this.hideDetails} title="close"><i className="fa fa-2x fa-close"></i></button>
                    </div>
                </div>;
            }
        };
    
        return (
            <span className="field-value" data-showDetail={this.state.showDetails}>{valueTag}{detailsButton}{detailsContent}</span>
        );
    }
}
  