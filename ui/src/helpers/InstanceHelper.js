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
    if (header.category) {
      tags.push(header.category);
    }
  }
  return tags;
};

export const getTitle = (data, id) => {
  if (data?.id) {
    if (data?.title) {
      if (data?.version) {
        return `${data.title} ${data.version}`;
      }
      return `${data.title}`;
    }
    if (data?.type) {
      return `${data.type} ${data.id}`;
    }
  }
  if (!id) {
    return "Knowledge Graph Search";
  }
  return id;
};

const getField = (group, type, name, data, mapping) => {
  if (name === "type") {
    return {
      name: "type",
      data: { value: type },
      mapping: { },
      group: group,
      type: type
    };
  }
  return {
    name: name,
    data: data,
    mapping: mapping,
    group: group,
    type: type
  };
};

const getHeaderFields = (group, type, data, mapping) => {
  if (!data || !mapping) {
    return [];
  }

  return Object.entries(mapping.fields || {})
    .filter(
      ([name, fieldsMapping]) =>
        fieldsMapping &&
        data?.[name] &&
        fieldsMapping.layout === "header" &&
        name !== "title"
    )
    .map(([name, fieldsMapping]) =>
      getField(group, type, name, data[name], fieldsMapping)
    );
};

const getFieldsByTabs = (group, type, data, typeMapping) => {
  if (!data || !typeMapping) {
    return [];
  }

  const overviewFields = [];
  const tabs = Object.entries(typeMapping.fields || {})
    .filter(
      ([name, mapping]) =>
        mapping &&
        data?.[name] &&
        mapping.layout !== "header" &&
        ![
          "id",
          "identifier",
          "category",
          "title",
          "first_release",
          "last_release",
          "previewObjects",
          "disclaimer"
        ].includes(name)
    )
    .reduce((acc, [name, mapping]) => {
      const groupName =
        !mapping.layout || mapping.layout === "summary" ? null : mapping.layout;
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
      ...Object.values(tabs)
    ];
  }
  return Object.values(tabs);
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

  const fields = data?.fields;
  const type = data?.type;
  const mapping =
    (fields &&
      state.definition?.typeMappings &&
      state.definition.typeMappings[type]) ??
    {};
  const group = state.groups.group;
  const version = (data?.version)? data.version : "Current";
  const versions = (
    Array.isArray(data?.versions) ? data.versions : []
  ).map(v => ({
    label: v.value ? v.value : "Current",
    value: v.reference
  }));
  const latestVersion = versions && versions.length && version && versions[0];
  const allVersions = data?.allVersionRef;
  return {
    id: data?.id,
    type: type,
    group: group,
    hasNoData: !fields,
    hasUnknownData: !mapping,
    header: {
      group: group !== state.groups.defaultGroup ? group : null,
      groupLabel:
        group !== state.groups.defaultGroup
          ? getGroupLabel(state.groups.groups, group)
          : null,
      category: data?.category,
      title: data?.title,
      fields: getHeaderFields(group, type, fields, mapping),
      version: version,
      versions: versions
    },
    latestVersion: latestVersion,
    isOutdated: latestVersion && latestVersion.label !== version,
    allVersions: allVersions,
    tabs: getFieldsByTabs(group, type, fields, mapping),
    disclaimer: data?.disclaimer
  };
};
