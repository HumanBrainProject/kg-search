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
import { connect } from "react-redux";
import { Carousel } from "react-responsive-carousel";
import "react-responsive-carousel/lib/styles/carousel.min.css";
import API from "../services/API";
import { Field } from "./Field";
import { FieldsPanel } from "./FieldsPanel";
import { FieldsTabs } from "./FieldsTabs";
import "./Instance.css";

export const InstanceBase = ({type, hasNoData, hasUnknownData, header, previews, main, summary, groups}) => {

  if (hasNoData) {
    return (
      <div className="kgs-instance" data-type={type}>
        <div className="kgs-instance__no-data">This data is currently not available.</div>
      </div>
    );
  }
  if (hasUnknownData) {
    return (
      <div className="kgs-instance" data-type={type}>
        <div className="kgs-instance__no-data">This type of data is currently not supported.</div>
      </div>
    );
  }
  return (
    <div className={`kgs-instance kgs-instance__grid ${(previews && previews.length)?"kgs-instance__with-carousel":""}`} data-type={type}>
      <div className="kgs-instance__header">
        <h3 className={`kgs-instance__group ${header.group && header.group !== API.defaultGroup?"show":""}`}>Group: <strong>{header.group}</strong></h3>
        <div>
          <Field {...header.icon} />
          <Field {...header.type} />
        </div>
        <div>
          <Field {...header.title} />
        </div>
      </div>
      {previews && !!previews.length && (
        <div className="kgs-instance__carousel">
          <Carousel width="300px" autoPlay interval={3000} infiniteLoop={true} showThumbs={true} showIndicators={false} stopOnHover={true} showStatus={false} >
            {previews.map(({src, legend}) => (
              <div key={src}>
                <img src={src} alt={legend?legend:""}/>
                {legend && (
                  <p className="legend">{legend}</p>
                )}
              </div>
            ))}
          </Carousel>
        </div>
      )}
      <FieldsPanel className="kgs-instance__main" fields={main} fieldComponent={Field} />
      <FieldsPanel className="kgs-instance__summary" fields={summary} fieldComponent={Field} />
      <FieldsTabs className="kgs-instance__groups" fields={groups} />
    </div>
  );
};

const getField = (group, type, name, data, mapping) => {
  switch (name) {
  case "type":
    return {
      name: "type",
      data: {value: type},
      mapping: {visible: true},
      group: group
    };
  default:
    return {
      name: name,
      data: data,
      mapping: mapping,
      group: group
    };
  }
};

const getFields = (group, type, data, mapping, filter) => {
  if (!data || !mapping) {
    return [];
  }

  const fields = Object.entries(mapping.fields || {})
    .filter(([name, mapping]) =>
      mapping
        && (mapping.showIfEmpty || (data && data[name]))
        && (!filter || (typeof filter === "function" && filter(type, name, data[name], mapping)))
    )
    .map(([name, mapping]) => getField(group, type, name, data[name], mapping));

  return fields;
};

const getPreviews = (data, mapping, idx=0) => {
  if (data instanceof Array) {
    const previews = [];
    data.forEach((elt, idx) => previews.push(...getPreviews(elt, mapping, idx)));
    return previews;
  } else if (data && mapping.children) {
    const previews = [];
    Object.entries(mapping.children)
      .filter(([name, mapping]) =>
        mapping
        && (mapping.showIfEmpty || (data && data[name]))
        && mapping.visible
      )
      .map(([name, mapping]) => ({
        data: data && data[name],
        mapping: mapping
      }))
      .forEach(({data, mapping}, idx) => previews.push(...getPreviews(data, mapping, idx)));
    return previews;
  } else if (data && typeof data.previewUrl === "string") {
    return [{
      src: data.previewUrl,
      legend: data.url?data.url:(data.value?data.value:null)
    }];
  } else if (data && typeof data.url === "string" && /^https?:\/\/.+\.cscs\.ch\/.+$/.test(data.url)) {
    const cats = [
      "https://dogtowndogtraining.com/wp-content/uploads/2012/06/300x300-clicker.jpg",
      "https://dogtowndogtraining.com/wp-content/uploads/2012/06/300x300-06.jpg",
      "https://dogtowndogtraining.com/wp-content/uploads/2012/06/300x300-05.jpg",
      "https://dogtowndogtraining.com/wp-content/uploads/2012/06/300x300-03.jpg",
      "https://dogtowndogtraining.com/wp-content/uploads/2012/06/300x300-07.jpg",
      "https://dogtowndogtraining.com/wp-content/uploads/2012/06/300x300-02.jpg",
      "https://dogtowndogtraining.com/wp-content/uploads/2012/06/300x300-04.jpg",
      "https://dogtowndogtraining.com/wp-content/uploads/2012/06/300x300-061-e1340955308953.jpg",
      "https://dogtowndogtraining.com/wp-content/uploads/2012/06/300x300-08.jpg"
    ];
    return [{
      src: cats[idx % (cats.length -1)],
      legend: data.url?data.url:(data.value?data.value:null)
    }];
  }
  return [];
};

export const Instance = connect(
  (state, {data}) => {

    const indexReg = /^kg_(.*)$/;
    const source = data && !(data.found === false) && data._type && data._source;
    const mapping = source && state.definition && state.definition.shapeMappings && state.definition.shapeMappings[data._type];
    const group = (data && indexReg.test(data._index))?data._index.match(indexReg)[1]:API.defaultGroup;

    return {
      type: data && data._type,
      hasNoData: !source,
      hasUnknownData: !mapping,
      header: {
        group: group,
        icon:  getField(group, data && data._type, "icon", {value: data && data._type, image: {url: source && source.image && source.image.url}}, {visible: true, type: "icon", icon: mapping && mapping.icon}),
        type:  getField(group, data && data._type, "type"),
        title: getField(group, data && data._type, "title", source && source["title"], mapping && mapping.fields && mapping.fields["title"])
      },
      previews: getPreviews(source, {children: mapping.fields}),
      main: getFields(group, data && data._type, source, mapping, (type, name) => name !== "title"),
      summary: getFields(group, data && data._type, source, mapping, (type, name, data, mapping) => mapping.layout === "summary" && name !== "title"),
      groups: getFields(group, data && data._type, source, mapping, (type, name, data, mapping) => mapping.layout === "group" && name !== "title")
    };
  }
)(InstanceBase);