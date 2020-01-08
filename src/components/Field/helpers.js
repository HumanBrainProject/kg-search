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


export const LIST_SMALL_SIZE_STOP = 5;

const VIEW_LESS_LABEL = "view less";
const VIEW_ALL_LABEL = "view all";


export const getNextSizeStop = (sizeStop, props) => {
  const {items, mapping} = props;

  if (!Array.isArray(items) || (mapping && mapping.separator)) {
    return Number.POSITIVE_INFINITY;
  }

  if (sizeStop === LIST_SMALL_SIZE_STOP) {
    return Number.POSITIVE_INFINITY;
  }

  return LIST_SMALL_SIZE_STOP;
};

export const getFilteredItems = (sizeStop, maxSizeStop, props) => {
  const {items, mapping, group} = props;

  if (!Array.isArray(items)) {
    return [];
  }

  const nbToDisplay = Math.min(maxSizeStop, sizeStop);
  return items
    .filter((item, idx) => {
      return idx < nbToDisplay;
    })
    .map((item, idx) => ({
      isObject: !!item.children,
      key: item.reference?item.reference:item.value?item.value:idx,
      show: true,
      data: item.children?item.children:item,
      mapping: mapping,
      group: group
    }));
};

export const getShowMoreLabel = (sizeStop, maxSizeStop, props) => {
  const {items, mapping} = props;

  if (!Array.isArray(items) || (mapping && mapping.separator)) {
    return null;
  }

  if (sizeStop === LIST_SMALL_SIZE_STOP) {
    return VIEW_ALL_LABEL;
  }

  return VIEW_LESS_LABEL;
};

export const hasMore = (sizeStop, maxSizeStop, props) => {
  const {items, mapping} = props;

  if (!Array.isArray(items) || (mapping && mapping.separator)) {
    return false;
  }

  const nbToDisplay = Math.min(maxSizeStop, sizeStop);
  return maxSizeStop > nbToDisplay;
};