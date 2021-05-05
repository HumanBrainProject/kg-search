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
import "./ObjectField.css";

const ObjectFieldBase = (renderUserInteractions = true) => {

  const DefaultList = ({className, children}) => {
    return (
      <ul className={className}>
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

  const ObjectField = ({show, data, mapping, group}) => {
    if (!show || !mapping || !mapping.visible) {
      return null;
    }

    const List = mapping.separator?CustomList:DefaultList;
    const ListItem = mapping.separator?CustomListItem:DefaultListItem;
    const FieldComponent = renderUserInteractions?Field:PrintViewField;

    const fields = Object.entries(mapping.children)
      .filter(([name, mapping]) =>
        mapping
              && (mapping.showIfEmpty || (data && data[name]))
              && mapping.visible
      )
      .map(([name, mapping]) => ({
        name: name,
        data: data && data[name],
        mapping: mapping,
        group: group
      }));

    return (
      <List className="kgs-field__object">
        {
          fields.map(({name, data, mapping, group}, idx) => (
            <ListItem key={name} separator={mapping.separator} isFirst={!idx}>
              <FieldComponent name={name} data={data} mapping={mapping} group={group} />
            </ListItem>
          ))
        }
      </List>
    );
  };

  return ObjectField;
};

export const ObjectField = ObjectFieldBase(true);
export const PrintViewObjectField = ObjectFieldBase(false);