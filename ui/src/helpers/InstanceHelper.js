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

export const getTags = header => {
  const tags = [];
  if (header) {
    if (header.groupLabel) {
      tags.push(header.groupLabel);
    }
    if (header.type && header.type.data && header.type.data.value) {
      tags.push(header.type.data.value);
    }
  }
  return tags;
};

export const getTitle = (data, id) => {
  if (data && data._id) {
    if (data._source?.title?.value) {
      return `${data._source.title.value}`;
    }
    if (data._source?.type?.value) {
      return `${data._source.type.value} ${data._id}`;
    }
  }
  if (!id) {
    return "Knowledge Graph Search";
  }
  return id;
};

const getField = (group, type, name, data, mapping) => {
  switch (name) {
  case "type":
    return {
      name: "type",
      data: { value: type },
      mapping: { visible: true },
      group: group,
      type: type
    };
  default:
    return {
      name: name,
      data: data,
      mapping: mapping,
      group: group,
      type: type
    };
  }
};

const getHeaderFields = (group, type, data, mapping) => {
  if (!data || !mapping) {
    return [];
  }

  const fields = Object.entries(mapping.fields || {})
    .filter(([name, mapping]) =>
      mapping
      && (mapping.showIfEmpty || (data && data[name]))
      && mapping.layout === "header" && name !== "title"
    )
    .map(([name, mapping]) => getField(group, type, name, data[name], mapping));

  return fields;
};

const getFieldsByGroups = (group, type, data, typeMapping) => {
  if (!data || !typeMapping) {
    return [];
  }

  const overviewFields = [];
  const groups = Object.entries(typeMapping.fields || {})
    .filter(([name, mapping]) =>
      mapping
      && mapping.visible
      && (mapping.showIfEmpty || (data && data[name]))
      && mapping.layout !== "header"
      && !["id", "identifier", "title", "first_release", "last_release", "previewObjects"].includes(name)
    )
    .reduce((acc, [name, mapping]) => {
      const groupName = (!mapping.layout || mapping.layout === "summary") ? null : mapping.layout;
      const field = getField(group, type, name, data[name], mapping);
      if (!groupName) {
        overviewFields.push(field);
      } else {
        if (!acc[groupName]) {
          acc[groupName] = {
            name: groupName,
            fields: []
          };
        }
        acc[groupName].fields.push(field);
      }
      return acc;
    }, {});

  if (overviewFields.length) {
    const previews = getPreviews(data);
    return [
      {
        name: "Overview",
        fields: overviewFields,
        previews: previews
      },
      ...Object.values(groups)
    ];
  }
  return Object.values(groups);
};


export const getPreviews = data => {
  if (Array.isArray(data.previewObjects)) {
    return data.previewObjects.map(item => {
      return {
        staticImageUrl: item.imageUrl,
        previewUrl: {
          url: item.videoUrl ?? item.imageUrl,
          isAnimated: !!item.videoUrl
        },
        label: item?.description,
        link: item?.link
      };
    });
  }
  if (Array.isArray(data.filesOld)) {
    return data.filesOld.map(item => ({
      staticImageUrl: item?.staticImageUrl?.url,
      previewUrl: item.previewUrl,
      label: item?.value
    }));
  }
  return [];
};

const getGroupLabel = (groups, name) => {
  let label = null;
  groups.some(group => {
    if (group.value === name) {
      label = group.label;
      return true;
    }
    return false;
  });
  return label;
};

export const mapStateToProps = (state, props) => {

  const { data } = props;

  const source = data && data._source;
  const type = source?.type?.value;
  const mapping = (source && state.definition?.typeMappings && state.definition.typeMappings[type]) ?? {};
  const group = state.groups.group;
  const version = source && source.version ? source.version : "Current";
  const versions = (source && Array.isArray(source.versions) ? source.versions : []).map(v => ({
    label: v.value ? v.value : "Current",
    value: v.reference
  }));
  const latestVersion = versions && versions.length && version && versions[0];
  const allVersions = source && source.allVersionRef;
  return {
    id: data && data._id,
    type: type,
    group: group,
    hasNoData: !source,
    hasUnknownData: !mapping,
    header: {
      group: (group !== state.groups.defaultGroup) ? group : null,
      groupLabel: (group !== state.groups.defaultGroup) ? getGroupLabel(state.groups.groups, group) : null,
      type: getField(group, type, "type"),
      title: getField(group, type, "title", source && source["title"], mapping && mapping.fields && mapping.fields["title"]),
      fields: getHeaderFields(group, type, source, mapping),
      version: version,
      versions: versions
    },
    latestVersion: latestVersion,
    isOutdated: latestVersion && latestVersion.label !== version,
    allVersions: allVersions,
    groups: getFieldsByGroups(group, type, source, mapping)
  };
};