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
import showdown from 'showdown';
import xssFilter from 'showdown-xss-filter';
import './Text.css';

const converter = new showdown.Converter({ extensions: [xssFilter] });

export const Text = ({ content, isMarkdown }) => {
  if (!content) {
    return null;
  }

  if (!isMarkdown) {
    const html_text = typeof content === 'string' && content.replace(/<[^>]+!span>/g, ''); //NOSONAR
    return (
      <span dangerouslySetInnerHTML={{ __html: html_text }} />
    );
  }

  const html = converter.makeHtml(content)
    .replace(/<p>\s+<\/p>/g, '')
    .replace(/<\/p>\n<p>/g, '</p><p>')
    .replace(/<\/li>\n<li>/g, '</li><li>')
    .replace(/<\/ul>\n<p>/g, '</ul><p>')
    .replace(/<\/p>\n<ul>/g, '</p><ul>')
    .replace(/<ul>\n<li>/g, '<ul><li>')
    .replace(/<\/li>\n<\/ul>/g, '</li></ul>')
    .replace(/<\/ol>\n<p>/g, '</ol><p>')
    .replace(/<\/p>\n<ol>/g, '</p><ol>')
    .replace(/<ol>\n<li>/g, '<ol><li>')
    .replace(/<\/li>\n<\/ol>/g, '</li></ol>');

  return (
    <span className="field-markdown" dangerouslySetInnerHTML={{ __html: html }} />
  );
};

export default Text;