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

import React, { useMemo } from 'react';
import Citations from '../../components/Field/Citation/Citations';
import DynamicCitation from '../../components/Field/Citation/DynamicCitation';
import MarkDownCitation from '../../components/Field/Citation/MarkDownCitation';
import { FieldLabel } from '../../components/Field/FieldLabel';
import {
  ListField,
  PrintViewListField
} from '../../components/Field/ListField';
import Mermaid from '../../components/Field/Mermaid';
import NeuralActivityVisualizer from '../../components/Field/NeuralActivityVisualizer';
import ObjectField from '../../components/Field/ObjectField';
import TableField from '../../components/Field/TableField';
import FilePreview from '../../components/FilePreview/FilePreview';
import { Hint } from '../../components/Hint/Hint';
import Text from '../../components/Text/Text';
import AsyncHierarchicalFiles from './HierarchicalFiles/AsyncHierarchicalFiles';
import HierarchicalFiles from './HierarchicalFiles/HierarchicalFiles';
import HierarchicalTree from './HierarchicalTree/HierarchicalTree';
import { ValueField, PrintViewValueField } from './ValueField/ValueField';

import './Field.css';

//TODO: remove getFileRepositoryIdFromUrl after next index (incremental) update
const filesUrlRegex = /^\/api\/groups\/.+\/repositories\/(.+)\/files$/;
const liveFilesUrlRegex = /^\/api\/repositories\/(.+)\/files\/live$/;
const getFileRepositoryIdFromUrl = url => {
  if (!url || typeof url !== 'string') {
    return null;
  }

  if (liveFilesUrlRegex.test(url)) {
    const [, repositoryId] = url.match(liveFilesUrlRegex);
    return repositoryId;
  }
  if (filesUrlRegex.test(url)) {
    const [, repositoryId] = url.match(filesUrlRegex);
    return repositoryId;
  }
  return url; // url is a repositoryId
};

const FieldBaseComponent = ({
  name,
  layout,
  style,
  className,
  label,
  inlineLabel = true,
  labelCounter = null,
  hint,
  value,
  Component
}) => {
  if (!Component) {
    return null;
  }

  const fieldClassName = name ? `kgs-field__${name}` : '';
  const layoutClassName = layout ? `kgs-field__layout-${layout}` : '';

  return (
    <span
      style={style}
      className={`kgs-field ${fieldClassName} ${layoutClassName} ${className}`}
    >
      <FieldLabel value={label} counter={labelCounter} inline={inlineLabel} />
      <Hint value={hint} />
      <Component {...value} />
    </span>
  );
};

const getFieldProps = (
  name,
  data,
  mapping,
  type,
  renderUserInteractions = true
) => {
  if (!mapping || !data) {
    return null;
  }

  const ListFieldComponent = renderUserInteractions
    ? ListField
    : PrintViewListField;
  const ValueFieldComponent = renderUserInteractions
    ? ValueField
    : PrintViewValueField;
  const FieldComponent = renderUserInteractions ? Field : PrintViewField;

  let className = '';
  let labelCounter = null;
  let valueProps = null;
  let valueComponent = null;
  let label = mapping.label ? mapping.label : null;

  if (mapping.isGroupedLinks) {
    // Grouped Links

    const groupedLinksSize = Object.keys(data).length;
    const isSingleGroupedLinks = groupedLinksSize === 1;

    if (isSingleGroupedLinks) {
      // Single Grouped Links

      const singleGroupedLinksLabel = Object.keys(data)[0];
      const singleGroupedLinks = Object.values(data)[0];

      if (label) {
        label = `${label} in ${singleGroupedLinksLabel}`;
      } else {
        label = singleGroupedLinksLabel;
      }

      valueProps = {
        items: singleGroupedLinks,
        mapping: mapping,
        type: type,
        fieldComponent: FieldComponent,
        valueFieldComponent: ValueFieldComponent
      };
      valueComponent = ListFieldComponent;
    } else {
      // Multiple Grouped Links

      const multipleGroupedLinksMapping = {
        ...mapping,
        enforceList: true,
        children: Object.keys(data).reduce((acc, service) => {
          acc[service] = { label: service, enforceShowMore: true };
          return acc;
        }, {})
      };

      valueProps = {
        data: data,
        mapping: multipleGroupedLinksMapping,
        type: type,
        fieldComponent: FieldComponent
      };
      valueComponent = ObjectField;
    }
  } else if (mapping.isFilePreview && data.url) {
    // File Preview

    valueProps = {
      url: data.url,
      title: 'data descriptor'
    };
    valueComponent = FilePreview;
  } else if (mapping.isCitation) {
    // Citation

    if (Array.isArray(data)) {
      valueProps = {
        data: data
      };
      valueComponent = Citations;
    } else {
      if (name === 'customCitation') {
        // MarkDown Citation

        valueProps = {
          text: data.value
        };
        valueComponent = MarkDownCitation;
      } else {
        // Dynamic Citation

        valueProps = {
          doi: data.value
        };
        valueComponent = DynamicCitation;
      }
    }
  } else if (mapping.isHierarchical) {
    // Hierarchical

    className = 'kgs-field__hierarchical';

    valueProps = {
      mapping: mapping,
      type: type,
      data: data
    };
    valueComponent = HierarchicalTree;
  } else if (mapping.isHierarchicalFiles) {
    // Hierarchical Files

    className = 'kgs-field__hierarchical-files';

    const repositoryId = mapping.isAsync ? data : null;

    if (repositoryId) {
      // Async Hierarchical Files

      valueProps = {
        mapping: mapping,
        type: type,
        repositoryId: getFileRepositoryIdFromUrl(repositoryId),
        nameFieldPath: 'title.value',
        urlFieldPath: 'iri.url'
      };
      valueComponent = AsyncHierarchicalFiles;
    } else {
      // Old Hierarchical Files

      valueProps = {
        data: data,
        mapping: mapping,
        type: type,
        nameFieldPath: 'value',
        urlFieldPath: 'url'
      };
      valueComponent = HierarchicalFiles;
    }
  } else if (mapping.isTable) {
    // Table

    className = 'kgs-field__table';

    valueProps = {
      items: data,
      mapping: mapping,
      type: type,
      fieldComponent: FieldComponent
    };
    valueComponent = TableField;
  } else if (mapping.isMermaid) {
    valueProps = {
      data: data.value,
      details: data.details,
      mapping: mapping
    };
    valueComponent = Mermaid;
  } else if (mapping.isNeuralActivityVisualizer) {
    valueProps = {
      data: data,
      mapping: mapping
    };
    valueComponent = NeuralActivityVisualizer;
  } else {
    if (Array.isArray(data) && data.length === 1) {
      data = data[0];
    }

    if (Array.isArray(data)) {
      // List

      if (mapping.layout === 'group') {
        labelCounter = data.length;
      }

      valueProps = {
        items: data,
        mapping: mapping,
        type: type,
        fieldComponent: FieldComponent,
        valueFieldComponent: ValueFieldComponent
      };
      valueComponent = ListFieldComponent;
    } else if (mapping.children) {
      // Object

      valueProps = {
        items: data,
        mapping: mapping,
        type: type,
        fieldComponent: FieldComponent
      };
      valueComponent = ObjectField;
    } else {
      // Value

      valueProps = {
        data: data,
        mapping: mapping,
        type: type
      };
      valueComponent = ValueFieldComponent;
    }
  }

  return {
    name: name,
    layout: ['header', 'summary'].includes(mapping.layout)
      ? mapping.layout
      : null,
    style:
      mapping.order && !renderUserInteractions
        ? { order: mapping.order }
        : null,
    className: className,
    label:
      label && (!mapping.hideLabel || !renderUserInteractions) ? label : null,
    inlineLabel: !mapping.tagIcon,
    labelCounter: labelCounter,
    hint: renderUserInteractions && mapping.hint ? mapping.hint : null,
    value: valueProps,
    Component: valueComponent
  };
};

export const FieldBase = (renderUserInteractions = true) => {
  const Component = ({ name, data, mapping, type }) => {
    const fieldProps = useMemo(
      () => getFieldProps(name, data, mapping, type, renderUserInteractions),
      [name, data, mapping, type]
    );

    if (!fieldProps) {
      return null;
    }

    return <FieldBaseComponent {...fieldProps} />;
  };

  return Component;
};

export const Title = ({ text }) => {
  if (!text) {
    return null;
  }
  return (
    <span className="kgs-field kgs-field__title kgs-field__layout-header ">
      <div className="field-value">
        <Text content={text} />
      </div>
    </span>
  );
};

export const Field = FieldBase(true);
Field.displayName = 'Field';
export const PrintViewField = FieldBase(false);
PrintViewField.displayName = 'PrintViewField';
