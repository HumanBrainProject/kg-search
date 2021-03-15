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

import React from "react";

import { PaginatedList } from "../../../components/PaginatedList/PaginatedList";
import { FilteredList } from "../../../components/FilteredList/FilteredList";
import { Tree } from "../../../components/Tree/Tree";
import { FacetCheckbox } from "./FacetCheckbox";
import { Item } from "../../../components/List/List";

import "./Facet.css";

export const Facet = ({ facet, onChange, onViewChange }) => {
  let Component = null;
  let parameters = null;
  switch (facet.filterType) {
  case "list":
  {
    if (facet.isHierarchical) {
      const list = facet.keywords.map(keyword => {
        let value = [];
        let checked = true;
        let hasAnyChildChecked = false;
        let children = (keyword.children && keyword.children.keywords)?keyword.children.keywords.map(child => {
          value.push(child.value);
          const childChecked = Array.isArray(facet.value) ? facet.value.includes(child.value) : false;
          if (!childChecked) {
            checked = false;
          } else {
            hasAnyChildChecked = true;
          }
          return {
            name: facet.id,
            label: child.value,
            value: child.value,
            count: child.count,
            checked: childChecked
          };
        }):[];
        return {
          name: facet.id,
          label: keyword.value,
          value: value,
          count: keyword.count,
          checked: checked,
          hasAnyChildChecked: hasAnyChildChecked,
          children: children
        };
      });
      const nullValueIdx = list.findIndex(e => e.label === facet.missingTerm);
      if (nullValueIdx !== -1) {
        const removedItems = list.splice(nullValueIdx, 1);
        list.push(removedItems[0]);
      }
      Component = Tree;
      parameters = {
        tree: list,
        ItemComponent: FacetCheckbox,
        itemUniqKeyAttribute: "value",
        onItemClick: item => onChange(facet.id, !item.checked, item.value)
      };
    } else {
      const list = facet.keywords.map(keyword => ({
        name: facet.id,
        label: keyword.value,
        value: keyword.value,
        count: keyword.count,
        checked: Array.isArray(facet.value) ? facet.value.includes(keyword.value) : false,
      }));
      if (facet.isFilterable) {
        Component = FilteredList;
        parameters = {
          label: facet.fieldLabel,
          items: list,
          ItemComponent: FacetCheckbox,
          itemUniqKeyAttribute: "value",
          onItemClick: item => onChange(facet.id, !item.checked, item.value),
        };
      } else {
        Component = PaginatedList;
        parameters = {
          items: list,
          ItemComponent: FacetCheckbox,
          itemUniqKeyAttribute: "value",
          onItemClick: item => onChange(facet.id, !item.checked, item.value),
          onViewChange: size => onViewChange(facet.id, size),
          size: facet.size,
          defaultSize: facet.defaultSize,
          others: facet.others
        };
      }
    }
    break;
  }
  case "exists":
    Component = Item;
    parameters = {
      item: {
        label: `Has ${facet.fieldLabel}`,
        count: facet.count,
        checked: !!facet.value
      },
      ItemComponent: FacetCheckbox,
      onClick: item => onChange(facet.id, !item.checked)
    };
    break;
  default:
    break;
  }
  if (Component) {
    return ( <div className = "kgs-facet" >
      <div className = "kgs-facet-title" > { facet.fieldLabel } </div>
      <Component {...parameters }/>
    </div>
    );
  }
  return null;
};