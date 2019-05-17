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
import { connect } from "react-redux";
import API from "../services/API";
import { ImagePreviews } from "./ImagePreviews";
import { ImagePopup } from "../components/ImagePopup";
import { Field } from "./Field";
import { FieldsPanel } from "./FieldsPanel";
import { FieldsTabs } from "./FieldsTabs";
import "./Instance.css";

class InstanceBase extends PureComponent {
  constructor(props) {
    super(props);
    this.state = {
      preview: null
    };
  }
  handleShowPreview = preview => {
    this.setState({preview: preview});
  };
  handleClosePreview = () => {
    this.setState({preview: null});
  }
  componentWillUnmount() {
    this.setState({preview: null});
  }
  render() {
    const {type, hasNoData, hasUnknownData, header, previews, main, summary, groups} = this.props;

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
      <div className={`kgs-instance kgs-instance__grid ${(previews && previews.length)?"kgs-instance__with-previews":""}`} data-type={type}>
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
        <ImagePreviews className="kgs-instance__previews" width="300px" images={previews} onClick={this.handleShowPreview} />
        <FieldsPanel className="kgs-instance__main" fields={main} fieldComponent={Field} />
        <FieldsPanel className="kgs-instance__summary" fields={summary} fieldComponent={Field} />
        <FieldsTabs className="kgs-instance__groups" fields={groups} />
        <ImagePopup className="kgs-instance__image_popup" src={this.state.preview && this.state.preview.previewUrl && (typeof this.state.preview.previewUrl === "string"?this.state.preview.previewUrl:this.state.preview.previewUrl.url)} label={(this.state.preview && this.state.preview.label)?this.state.preview.label:""} onClick={this.handleClosePreview} />
      </div>
    );
  }
}

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
  } else if (data && typeof data.staticImageUrl === "string") {
    return [{
      staticImageUrl: data.staticImageUrl  && (typeof data.staticImageUrl === "string"?data.staticImageUrl:data.staticImageUrl.url),
      previewUrl: data.previewUrl,
      label: data.value?data.value:null
    }];
  } else if (data && typeof data.url === "string" && /^https?:\/\/.+\.cscs\.ch\/.+$/.test(data.url)) {
    const cats = [
      "https://cdn2.thecatapi.com/images/2pb.gif",
      "http://lorempixel.com/output/cats-q-c-640-480-1.jpg",
      "http://lorempixel.com/output/cats-q-c-640-480-2.jpg",
      "https://cdn2.thecatapi.com/images/18f.gif",
      "http://lorempixel.com/output/cats-q-c-640-480-3.jpg",
      "https://cdn2.thecatapi.com/images/dbt.gif",
      "https://cdn2.thecatapi.com/images/d5k.gif",
      "http://lorempixel.com/output/cats-q-c-640-480-4.jpg",
      "http://lorempixel.com/output/cats-q-c-640-480-5.jpg",
      "http://lorempixel.com/output/cats-q-c-640-480-6.jpg",
      "http://lorempixel.com/output/cats-q-c-640-480-7.jpg",
      "http://lorempixel.com/output/cats-q-c-640-480-8.jpg",
      "http://lorempixel.com/output/cats-q-c-640-480-9.jpg",
      "http://lorempixel.com/output/cats-q-c-640-480-10.jpg"
    ];
    const dogs = [
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
      staticImageUrl: dogs[idx % (dogs.length -1)],
      previewUrl: (Math.round(Math.random() * 10) % 2)?{
        url: cats[idx % (cats.length -1)],
        isAnimated: /^.+\.gif$/.test(cats[idx % (cats.length -1)])
      }:undefined,
      label: data.value?data.value:null
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