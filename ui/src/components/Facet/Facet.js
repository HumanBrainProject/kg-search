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

import React from 'react';
import { FilteredList } from '../FilteredList/FilteredList';
import { Item } from '../List/List';
import { PaginatedList } from '../PaginatedList/PaginatedList';
import Tree from '../Tree/Tree';
import FacetCheckbox from './FacetCheckbox';
import './Facet.css';

const Facet = ({ facet, onChange, onViewChange }) => {
  let Component = null;
  let parameters = null;
  switch (facet.type) {
  case 'list':
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
            name: facet.name,
            label: child.value,
            value: child.value,
            count: child.count,
            checked: childChecked
          };
        }):[];
        return {
          name: facet.name,
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
      Component = list.length?Tree:null;
      parameters = {
        tree: list,
        ItemComponent: FacetCheckbox,
        itemUniqKeyAttribute: 'value',
        onItemClick: item => onChange(facet.name, !item.checked, item.value)
      };
    } else {
      const list = facet.keywords.map(keyword => ({
        name: facet.name,
        label: keyword.value,
        value: keyword.value,
        count: keyword.count,
        checked: Array.isArray(facet.value) ? facet.value.includes(keyword.value) : false,
      }));
      if (facet.isFilterable) {
        Component = list.length?FilteredList:null;
        parameters = {
          label: facet.label,
          items: list,
          ItemComponent: FacetCheckbox,
          itemUniqKeyAttribute: 'value',
          onItemClick: item => onChange(facet.name, !item.checked, item.value),
        };
      } else {
        Component = list.length?PaginatedList:null;
        parameters = {
          items: list,
          ItemComponent: FacetCheckbox,
          itemUniqKeyAttribute: 'value',
          onItemClick: item => onChange(facet.name, !item.checked, item.value),
          onViewChange: size => onViewChange(facet.name, size),
          size: facet.size,
          defaultSize: facet.defaultSize,
          others: facet.others
        };
      }
    }
    break;
  }
  case 'exists':
    Component = Item;
    parameters = {
      item: {
        label: facet.subLabel??`Has ${facet.label}`,
        count: facet.count,
        checked: !!facet.value
      },
      ItemComponent: FacetCheckbox,
      onClick: item => onChange(facet.name, !item.checked)
    };
    break;
  default:
    break;
  }
  if (Component) {
    return ( <div className = "kgs-facet" >
      <div className = "kgs-facet-title" > { facet.title??facet.label } </div>
      <Component {...parameters }/>
    </div>
    );
  }
  return null;
};

export default Facet;