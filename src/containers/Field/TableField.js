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

import React, { PureComponent } from "react";
import { Field, PrintViewField } from "../Field";
import "./ListField.css";


const CustomTableRow = ({item, viewComponent}) => {
  let Component = viewComponent;
  return (
    <tr>
      {item.map((i, id) =>
        <th key={`${i.name}-${id}`}>{i.data ?
          <Component name={i.name} data={i.data} mapping={i.mapping} group={i.group} />:"-"}</th>
      )}
    </tr>
  );
};

const TableFieldBase = (renderUserInteractions = true) => {

  const LIST_SMALL_SIZE_STOP = 5;
  const LIST_MIDDLE_SIZE_STOP = 10;

  const VIEW_MORE_LABEL = "view more";
  const VIEW_LESS_LABEL = "view less";
  const VIEW_ALL_LABEL = "view all";


  const TableFieldComponent = ({list, showToggle, toggleHandler, toggleLabel}) => {
    const FieldComponent = renderUserInteractions?Field:PrintViewField;

    const fields = list.map(item =>
      Object.entries(item.mapping.children)
        .filter(([, mapping]) =>
          mapping && mapping.visible
        )
        .map(([name, mapping]) => ({
          name: name,
          data: item.data && item.data[name],
          mapping: mapping,
          group: item.group
        }))
    );

    return (
      fields && fields[0] ?
        <table className="table">
          <thead>
            <tr>
              {fields[0].map((el,id) =>
                <th key={`${el.name}-${id}`}>{el.mapping.value}</th>
              )}
            </tr>
          </thead>
          <tbody>
            {fields.map((item, index) => <CustomTableRow key={`${index}`}  item={item} isFirst={!index} viewComponent={FieldComponent}/>)}
            {showToggle && (
              <tr>
                <th><button className="kgs-field__viewMore-button" onClick={toggleHandler} role="link">{toggleLabel}</button></th>
              </tr>
            )}
          </tbody>
        </table>:null
    );
  };

  class TableField extends PureComponent {
    constructor(props) {
      super(props);
      const sizeStop = this.getNextSizeStop(Number.POSITIVE_INFINITY);
      this.state = {
        sizeStop: sizeStop,
        items: Array.isArray(this.props.items) ? this.getFilteredItems(sizeStop) : this.getItems(),
        hasShowMoreToggle: this.hasShowMoreToggle,
        showMoreLabel: this.getShowMoreLabel(sizeStop),
        hasMore: this.hasMore(sizeStop)
      };
      this.handleShowMoreClick = this.handleShowMoreClick.bind(this);
    }

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

    getNextSizeStop(sizeStop) {
      const {items, mapping} = this.props;

      if (!Array.isArray(items) || (mapping && mapping.separator)) {
        return Number.POSITIVE_INFINITY;
      }

      if (sizeStop === LIST_SMALL_SIZE_STOP) {
        return (items.length > LIST_MIDDLE_SIZE_STOP)?LIST_MIDDLE_SIZE_STOP:Number.POSITIVE_INFINITY;
      }

      if (sizeStop === LIST_MIDDLE_SIZE_STOP) {
        return (items && items.length > LIST_MIDDLE_SIZE_STOP)?Number.POSITIVE_INFINITY:LIST_SMALL_SIZE_STOP;
      }

      return LIST_SMALL_SIZE_STOP;
    }

    getFilteredItems(sizeStop) {
      const {items, mapping, group} = this.props;
      if (!Array.isArray(items)) {
        return [];
      }

      const nbToDisplay = Math.min(this.maxSizeStop, sizeStop);
      return items
        .filter((item, idx) => {
          return idx < nbToDisplay;
        })
        .map((item, idx) => ({
          isObject: !!item.children,
          key: item.reference?item.reference:item.value?item.value:idx,
          show: true,
          data: item.children?item.children:item,
          mapping: mapping,
          group: group
        }));
    }

    getItems(){
      const {items, mapping, group} = this.props;
      let convertedItem = [];
      convertedItem.push(items);
      return convertedItem.map((item, idx) => ({
        isObject: !!item.children,
        key: item.reference?item.reference:item.value?item.value:idx,
        show: true,
        data: item.children?item.children:item,
        mapping: mapping,
        group: group
      }));
    }

    getShowMoreLabel(sizeStop) {
      const {items, mapping} = this.props;
      if (!Array.isArray(items) || (mapping && mapping.separator)) {
        return null;
      }

      if (sizeStop === LIST_SMALL_SIZE_STOP) {
        return (this.maxSizeStop > LIST_MIDDLE_SIZE_STOP)?VIEW_MORE_LABEL:VIEW_ALL_LABEL;
      }

      if (sizeStop === LIST_MIDDLE_SIZE_STOP) {
        return (this.maxSizeStop > LIST_MIDDLE_SIZE_STOP)?VIEW_ALL_LABEL:VIEW_LESS_LABEL;
      }

      return VIEW_LESS_LABEL;
    }

    get hasShowMoreToggle() {
      const {items, mapping} = this.props;
      if (!Array.isArray(items) || (mapping && mapping.separator) || !renderUserInteractions) {
        return false;
      }

      return this.maxSizeStop > LIST_SMALL_SIZE_STOP;
    }

    hasMore(sizeStop) {
      const {items, mapping} = this.props;
      if (!Array.isArray(items) || (mapping && mapping.separator)) {
        return false;
      }
      const maxSizeStop = this.maxSizeStop;
      const nbToDisplay = Math.min(maxSizeStop, sizeStop);

      return maxSizeStop > nbToDisplay;
    }

    handleShowMoreClick() {
      this.setState(state => {
        const nextSizeStop = this.getNextSizeStop(state.sizeStop);
        return {
          sizeStop: nextSizeStop,
          items: this.getFilteredItems(nextSizeStop),
          hasShowMoreToggle: this.hasShowMoreToggle,
          showMoreLabel: this.getShowMoreLabel(nextSizeStop),
          hasMore: this.hasMore(nextSizeStop)
        };
      });
    }

    render() {
      const {show} = this.props;

      return (
        show ? <TableFieldComponent list={this.state.items} showToggle={this.state.hasShowMoreToggle} toggleHandler={this.handleShowMoreClick} toggleLabel={this.state.showMoreLabel} />:null
      );
    }
  }
  return TableField;
};

export const TableField = TableFieldBase(true);
export const PrintViewTableField = TableFieldBase(false);