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

export const getTitle = (data, type, id) => {
  if (data && data._type && data._id) {
    if (data._source && data._source.title && data._source.title.value) {
      return data._source.title.value;
    }
    if (data._type && data._id) {
      return `${data._type} ${data._id}`;
    }
  }
  if (!type || !id) {
    return "Knowledge Graph Search";
  }
  return `${type} ${id}`;
};

const getField = (group, type, name, data, mapping) => {
  switch (name) {
  case "type":
    return {
      name: "type",
      data: { value: type },
      mapping: { visible: true },
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

//const getPreviews = (data, mapping, idx=0) => {
const getPreviews = (data, mapping) => {
  if (Array.isArray(data)) {
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
      .forEach(({ data, mapping }, idx) => previews.push(...getPreviews(data, mapping, idx)));
    return previews;
  } else if (data && data.staticImageUrl && (typeof data.staticImageUrl === "string" || typeof data.staticImageUrl.url === "string")) {
    return [{
      staticImageUrl: data.staticImageUrl && (typeof data.staticImageUrl === "string" ? data.staticImageUrl : data.staticImageUrl.url),
      previewUrl: data.previewUrl,
      label: data.value ? data.value : null
    }];
    /*
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
    */
  }
  return [];
};

export const mapStateToProps = (state, props) => {

  const { data } = props;

  const indexReg = /^kg_(.*)$/;
  const source = data && !(data.found === false) && data._type && data._source;
  const mapping = (source && state.definition && state.definition.typeMappings && state.definition.typeMappings[data._type])?state.definition.typeMappings[data._type]:{};
  const group = (data && indexReg.test(data._index)) ? data._index.match(indexReg)[1] : state.groups.group;
  return {
    id: data && data._id,
    type: data && data._type,
    group: group,
    hasNoData: !source,
    hasUnknownData: !mapping,
    header: {
      group: (group !== state.groups.defaultGroup)?group:null,
      icon: getField(group, data && data._type, "icon", { value: data && data._type, image: { url: source && source.image && source.image.url } }, { visible: true, type: "icon", icon: mapping && mapping.icon }),
      type: getField(group, data && data._type, "type"),
      title: getField(group, data && data._type, "title", source && source["title"], mapping && mapping.fields && mapping.fields["title"])
    },
    previews: getPreviews(source, { children: mapping.fields }),
    main: getFields(group, data && data._type, source, mapping, (type, name) => name !== "title"),
    summary: getFields(group, data && data._type, source, mapping, (type, name, data, mapping) => mapping.layout === "summary" && name !== "title"),
    groups: getFields(group, data && data._type, source, mapping, (type, name, data, mapping) => mapping.layout === "group" && name !== "title")
  };
};
