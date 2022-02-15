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

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React, { useState } from "react";
import { Field } from "./Field";
import "./TableField.css";

const CustomTableCell = ({field, isFirstCell, onCollapseToggle}) => {
  if (!field.data) {
    return field.level === 1 ? <th></th>:<td className={`kg-cell_level_${field.level}`}></td>;
  }

  const handleClick = () => onCollapseToggle(field.collectionIndex, !field.isCollectionCollapsed);

  if (field.level === 1) {
    return (
      <th>
        {isFirstCell && field.isCollectionCollapsible && (
          <button onClick={handleClick}><FontAwesomeIcon icon={field.isCollectionCollapsed?"chevron-right":"chevron-down"} /></button>
        )}
        <Field name={field.name} data={field.data} mapping={field.mapping} group={field.group} />
      </th>
    );
  }
  return (
    <td className={`kg-cell_level_${field.level}`}><Field name={field.name} data={field.data} mapping={field.mapping} group={field.group} /></td>
  );
};

const CustomTableRow = ({row, onCollapseToggle}) => {
  const collapse = row[0].isCollectionCollapsed && row[0].level !== 1;
  return(
    <tr className={collapse?"row-hidden":null}>
      {row.map((field, index) => <CustomTableCell key={`${field.name}-${index}`} isFirstCell={!index} field={field} onCollapseToggle={onCollapseToggle} />)}
    </tr>
  );
};

const normalizeCells = (fields, data, group, level, collectionIndex, isCollectionCollapsed, isCollectionCollapsible) => {
  return Object.entries(fields)
    .filter(([, field]) =>
      field && field.visible
    )
    .map(([name, field]) => ({
      name: name,
      data: data && data[name],
      mapping: {...field, labelHidden:true},
      group: group,
      level: level,
      isCollectionCollapsed: isCollectionCollapsed,
      collectionIndex: collectionIndex,
      isCollectionCollapsible: isCollectionCollapsible
    }));
};

const normalizeRows = (list, collapsedRowIndexes) => {
  return list.reduce((acc, item, index) => {
    const hasChildren = item.data && item.data.children;
    const isCollapsible = hasChildren && item.data.collapsible;
    const isCollectionCollapsed = isCollapsible && !!collapsedRowIndexes[index];
    if (item.isObject) {
      acc.push(normalizeCells(item.mapping.children, item.data, item.group, 1, index, isCollectionCollapsed, isCollapsible));
      if (hasChildren) {
        item.data.children.forEach(child => {
          acc.push(normalizeCells(item.mapping.children, child, item.group, 2, index, isCollectionCollapsed, false));
          if(child.children) {
            child.children.forEach(c => {
              acc.push(normalizeCells(item.mapping.children, c, item.group, 3, index, isCollectionCollapsed, false));
            });
          }
        });
      }
    } else {
      acc.push(item);
    }
    return acc;
  }, []);
};


const filterRows = table => {
  const visibleColumns = table.reduce((acc, row) => {
    row.forEach(cell => {
      if (cell.data) {
        acc[cell.name] = true;
      }
    });
    return acc;
  }, {});
  return table.map(row =>
    row.reduce((acc, cell) => {
      if (visibleColumns[cell.name]) {
        acc.push(cell);
      }
      return acc;
    }, []));
};

const TableFieldComponent = ({list}) => {
  const initialState = list.reduce((acc, _, index) => {
    acc[index] = true;
    return acc;
  }, {});
  const [collapsedRowIndexes, setCollapsedRowIndexes] = useState(initialState);

  const rows = filterRows(normalizeRows(list, collapsedRowIndexes));

  if (!rows.length || !rows[0].length) {
    return null;
  }

  const onCollapseToggle = (index, collapse) => {
    const values = {...collapsedRowIndexes};
    if (collapse) {
      values[index] = true;
    } else {
      delete values[index];
    }
    setCollapsedRowIndexes(values);
  };

  return (
    <table className="table">
      <thead>
        <tr>
          {rows[0].map((el,id) =>
            <th key={`${el.name}-${id}`}>{el.mapping.label}</th>
          )}
        </tr>
      </thead>
      <tbody>
        {rows.map((row, index) => <CustomTableRow key={`${index}`} row={row} onCollapseToggle={onCollapseToggle} />)}
      </tbody>
    </table>
  );
};

class TableField extends React.Component {
  getItems = () => {
    const {items, mapping, group, type} = this.props;
    const convertedItem = Array.isArray(items)?items:[items];
    return convertedItem.map((item, idx) => ({
      isObject: !!item.children,
      key: item.reference?item.reference:item.value?item.value:idx,
      show: true,
      data: item.children?item.children:item,
      mapping: mapping,
      group: group,
      type: type
    }));
  };

  render() {
    const {show} = this.props;
    if(!show) {
      return null;
    }

    return (
      <TableFieldComponent list={this.getItems()} />
    );
  }
}

export default TableField;
