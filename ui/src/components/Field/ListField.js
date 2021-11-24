/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

import React from "react";
import { ObjectField, PrintViewObjectField } from "./ObjectField";
import { ValueField, PrintViewValueField } from "./ValueField";
import { LIST_SMALL_SIZE_STOP,
  getNextSizeStop,
  getFilteredItems,
  getShowMoreLabel} from "./helpers";
import "./ListField.css";

const ListFieldBase = (renderUserInteractions = true) => {

  const ObjectFieldComponent = renderUserInteractions?ObjectField:PrintViewObjectField;
  const ValueFieldComponent = renderUserInteractions?ValueField:PrintViewValueField;

  const DefaultList = ({children}) => {
    return (
      <ul>
        {children}
      </ul>
    );
  };

  const CustomList = ({className, children}) => (
    <span className={className}>
      {children}
    </span>
  );

  const DefaultListItem = ({children}) => (
    <li>
      {children}
    </li>
  );

  const CustomListItem = ({isFirst, separator, children}) => (
    <span>
      {isFirst?null:separator}
      {children}
    </span>
  );

  const ListFieldComponent = ({list, sort, separator, showAsTag, showToggle, toggleHandler, toggleLabel}) => {
    const isCustom = separator || showAsTag;
    const List = isCustom?CustomList:DefaultList;
    const ListItem = isCustom?CustomListItem:DefaultListItem;
    const className =  `kgs-field__list ${showAsTag?"items-as-tags":""}`;
    if(sort) {
      list.sort((a, b) => a.data.value.toLowerCase().localeCompare(b.data.value.toLowerCase()));
    }
    return (
      <span className={className}>
        <List>
          {
            list.map(({isObject, key, data, mapping, group}, idx) => (
              <ListItem key={key} separator={separator} isFirst={!idx}>
                {isObject?
                  <ObjectFieldComponent show={true} data={data} mapping={mapping} group={group} />
                  :
                  <ValueFieldComponent show={true} data={data} mapping={mapping} group={group} />
                }
              </ListItem>
            ))
          }
        </List>
        {showToggle && (
          <button className="kgs-field__viewMore-button" onClick={toggleHandler} role="link">{toggleLabel}</button>
        )}
      </span>
    );
  };

  class ListField extends React.Component {
    constructor(props) {
      super(props);
      const sizeStop = getNextSizeStop(Number.POSITIVE_INFINITY, this.props);
      this.state = {
        sizeStop: sizeStop,
        items: this.props.mapping.layout === "summary"?getFilteredItems(sizeStop, this.maxSizeStop, this.props):this.getItems(),
        hasShowMoreToggle: this.hasShowMoreToggle && this.props.mapping.layout === "summary",
        showMoreLabel: getShowMoreLabel(sizeStop, this.props)
      };
    }


    getItems = () => {
      const {items, mapping, group} = this.props;

      if (!Array.isArray(items)) {
        return [];
      }
      return items
        .map((item, idx) => ({
          isObject: !!item.children,
          key: item.reference?item.reference:item.value?item.value:idx,
          show: true,
          data: item.children?item.children:item,
          mapping: mapping,
          group: group
        }));
    };

    get maxSizeStop() {
      const {items, mapping} = this.props;

      if (!Array.isArray(items)) {
        return 0;
      }

      if (!renderUserInteractions && mapping && mapping.overviewMaxDisplay && mapping.overviewMaxDisplay < items.length) {
        return mapping.overviewMaxDisplay;
      }
      return items.length;
    }

    get hasShowMoreToggle() {
      const {items, mapping} = this.props;
      if (!Array.isArray(items) || (mapping && mapping.separator) || !renderUserInteractions) {
        return false;
      }

      return this.maxSizeStop > LIST_SMALL_SIZE_STOP;
    }

    handleShowMoreClick = () => {
      this.setState((state, props) => {
        const nextSizeStop = getNextSizeStop(state.sizeStop, props);
        return {
          sizeStop: nextSizeStop,
          items: getFilteredItems(nextSizeStop, this.maxSizeStop, props),
          hasShowMoreToggle: this.hasShowMoreToggle,
          showMoreLabel: getShowMoreLabel(nextSizeStop, props)
        };
      });
    }

    render() {
      const {show, mapping} = this.props;
      if (!show) {
        return null;
      }

      return (
        <ListFieldComponent list={this.state.items} sort={mapping && mapping.sort} separator={mapping && !mapping.tagIcon && mapping.separator} showAsTag={mapping && !!mapping.tagIcon} showToggle={this.state.hasShowMoreToggle} toggleHandler={this.handleShowMoreClick} toggleLabel={this.state.showMoreLabel} />
      );
    }
  }
  return ListField;
};

export const ListField = ListFieldBase(true);
export const PrintViewListField = ListFieldBase(false);