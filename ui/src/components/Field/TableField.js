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
import { Field, PrintViewField } from "./Field";
import { LIST_SMALL_SIZE_STOP,
  getNextSizeStop,
  getFilteredItems,
  getShowMoreLabel } from "./helpers";
import "./TableField.css";

const CustomTableRow = ({item, viewComponent}) => {
  let Component = viewComponent;
  return (
    <tr>
      {item.map((i, id) =>
        <th key={`${i.name}-${id}`}>{i.data ?
          <Component name={i.name} data={i.data} mapping={i.mapping} group={i.group} />:"-"}
        </th>
      )}
    </tr>
  );
};

const TableFieldBase = (renderUserInteractions = true) => {

  const TableFieldComponent = ({list, showToggle, toggleHandler, toggleLabel}) => {
    const FieldComponent = renderUserInteractions?Field:PrintViewField;
    const fields = list.map(item =>
    {return item.isObject ?
      Object.entries(item.mapping.children)
        .filter(([, mapping]) =>
          mapping && mapping.visible
        )
        .map(([name, mapping]) => ({
          name: name,
          data: item.data && item.data[name],
          mapping: mapping,
          group: item.group
        })): item;
    }
    );

    return (
      fields && fields[0] && Array.isArray(fields[0]) ?
        <table className="table">
          <thead>
            <tr>
              {fields[0].map((el,id) =>
                <th key={`${el.name}-${id}`}>{el.mapping.value}</th>
              )}
            </tr>
          </thead>
          <tbody>
            {fields.map((item, index) => <CustomTableRow key={`${index}`}  item={item} isFirst={!index} viewComponent={FieldComponent} />)}
            {showToggle && (
              <tr>
                <th><button className="kgs-field__viewMore-button" onClick={toggleHandler} role="link">{toggleLabel}</button></th>
              </tr>
            )}
          </tbody>
        </table>:null
    );
  };

  class TableField extends React.Component {
    constructor(props) {
      super(props);
      const sizeStop = getNextSizeStop(Number.POSITIVE_INFINITY, this.props);
      this.state = {
        sizeStop: sizeStop,
        items: Array.isArray(this.props.items) ? getFilteredItems(sizeStop, this.maxSizeStop, this.props) : this.getItems(),
        hasShowMoreToggle: this.hasShowMoreToggle,
        showMoreLabel: getShowMoreLabel(sizeStop, this.props)
      };
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

    get hasShowMoreToggle() {
      const {items, mapping} = this.props;
      if (!Array.isArray(items) || (mapping && mapping.separator) || !renderUserInteractions) {
        return false;
      }

      return this.maxSizeStop > LIST_SMALL_SIZE_STOP;
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

    handleShowMoreClick = () => {
      this.setState((state,props) => {
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
      const {show} = this.props;
      if(!show) {
        return null;
      }

      return (
        <TableFieldComponent list={this.state.items} showToggle={this.state.hasShowMoreToggle} toggleHandler={this.handleShowMoreClick} toggleLabel={this.state.showMoreLabel} />
      );
    }
  }
  return TableField;
};

export const TableField = TableFieldBase(true);
export const PrintViewTableField = TableFieldBase(false);