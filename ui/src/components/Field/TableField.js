/* eslint-disable no-unused-vars */
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

const CustomTableCell = ({field, viewComponent}) => {
  if (!field.data) {
    return field.isHeaderRow ? <th>-</th>:<td>-</td>;
  }
  const Component = viewComponent;
  if (field.isHeaderRow) {
    return (
      <th><Component name={field.name} data={field.data} mapping={field.mapping} group={field.group} /></th>
    );
  }
  return (
    <td><Component name={field.name} data={field.data} mapping={field.mapping} group={field.group} /></td>
  );
};

const CustomTableRow = ({row, viewComponent}) => (
  <tr>
    {row.map((field, index) => <CustomTableCell key={`${field.name}-${index}`} field={field} viewComponent={viewComponent} />)}
  </tr>
);

const normalizeCells = (fields, data, group, isHeaderRow) => {
  return Object.entries(fields)
    .filter(([, field]) =>
      field && field.visible
    )
    .map(([name, field]) => ({
      name: name,
      data: data && data[name],
      mapping: {...field, labelHidden:true},
      group: group,
      isHeaderRow: isHeaderRow
    }));
};

const normalizeRows = list => {
  return list.reduce((acc, item) => {
    if (item.isObject) {
      acc.push(normalizeCells(item.mapping.children, item.data, item.group, true));
      if (item.data && item.data.children) {
        item.data.children.forEach(child => acc.push(normalizeCells(item.mapping.children, child, item.group, false)));
      }
    } else {
      acc.push(item);
    }
    return acc;
  }, []);};

const TableFieldBase = (renderUserInteractions = true) => {

  const TableFieldComponent = ({list, showToggle, toggleHandler, toggleLabel}) => {
    const FieldComponent = renderUserInteractions?Field:PrintViewField;

    const rows = normalizeRows(list);
    console.log(rows);

    if (!rows.length || !rows[0].length) {
      return null;
    }

    return (
      <table className="table">
        <thead>
          <tr>
            {rows[0].map((el,id) =>
              <th key={`${el.name}-${id}`}>{el.mapping.value}</th>
            )}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, index) => <CustomTableRow key={`${index}`} row={row} isFirst={!index} viewComponent={FieldComponent} />)}
          {showToggle && (
            <tr>
              <th><button className="kgs-field__viewMore-button" onClick={toggleHandler} role="link">{toggleLabel}</button></th>
            </tr>
          )}
        </tbody>
      </table>
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