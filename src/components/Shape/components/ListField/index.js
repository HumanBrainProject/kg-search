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
import { ObjectField } from '../ObjectField';
import { ValueField } from '../ValueField';
import './styles.css';

export class ListField extends Component {
  constructor(props) {
    super(props);
    this.state = {
      size: 5,
    };
    this.handleShowMoreClick = this.handleShowMoreClick.bind(this);
  }
  handleShowMoreClick() {
    const {items} = this.props;
    let size = 5;
    switch (this.state.size) {
      case 5:
        if (items.length > 10)
          size = 10;
        else
          size = Number.POSITIVE_INFINITY;
        break;
      case 10:
        if (items.length > 10)
          size = Number.POSITIVE_INFINITY;
        else  
          size = 5;
        break;
      default:
        size = 5;
    }
    this.setState({ size: size});
  }
  render() {
    const {items, mapping, showSmartContent} = this.props;
    
    if (!mapping || !mapping.visible)
      return null;

    if (mapping.separator) {
      const keys = {};
      return (
        <span>
          {items.map((item, index) => {
            let value = null;
            if (item.children) {
              value = <ObjectField data={item.children} mapping={mapping} showSmartContent={showSmartContent} />;
            } else {
              value = <ValueField value={item} mapping={mapping} showSmartContent={showSmartContent} />;
            }
            let key = item.value;
            if (key && !keys[key]) {
              keys[key] = true;
            } else {
              key = index;
            }
            return <span key={key}>{index===0?"":mapping.separator}{value}</span>;
          })}
        </span>
      );

    } else {

      let viewMore = null;
      let dotsMore = null;
      if (items.length > 5) {
        if (showSmartContent) {
          if (this.state.size === 5 && items.length > 10) {
            viewMore = <button className="kgs-shape__viewMore-button" onClick={this.handleShowMoreClick} role="link">view more</button>;
          } else if (this.state.size !== Number.POSITIVE_INFINITY) {
            viewMore = <button className="kgs-shape__viewMore-button" onClick={this.handleShowMoreClick} role="link">view all</button>;
          } else {
            viewMore = <button className="kgs-shape__viewMore-button" onClick={this.handleShowMoreClick} role="link">view less</button>;
          }
        } else {
            dotsMore = <li key="-1" className="kgs-shape__more">...</li>;
        }
      }

      const keys = {};
      return (
          <span>
            <ul>
              {items.map((item, index) => {
                if ((this.state.size === Number.POSITIVE_INFINITY || index < this.state.size) && (showSmartContent || index < 5)) {
                  let value = null;
                  if (item.children) {
                    value = <ObjectField data={item.children} mapping={mapping} showSmartContent={showSmartContent} />;
                  } else {
                    value = <ValueField value={item} mapping={mapping} showSmartContent={showSmartContent} />;
                  }
                  let key = item.reference?item.reference:item.value;
                  if (key && !keys[key]) {
                    keys[key] = true;
                  } else {
                    key = index;
                  }
                  return <li key={key}>{value}</li>;
                } else {
                  return null;
                }
              })}
              {dotsMore}
            </ul>
            {viewMore}
          </span>
      );
    }
  }
}