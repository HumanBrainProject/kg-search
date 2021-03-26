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

import React, { useState } from "react";
import "./ChevronStack.css";
import { ChevronButton } from "./ChevronButton";
import { ChevronStackPagination } from "./ChevronStackPagination";

export const ChevronStack = ({ items, itemComponent, getKey, onClick }) => {

  const [index, setIndex] = useState(0);

  if (!Array.isArray(items) || !items.length) {
    return null;
  }

  const handlePageChange = page => setIndex(page-1);

  const item = items[index];
  const key = getKey(item);

  return (
    <div className="kgs-chevron-stack" size={items.length>5?5:items.length}>
      <div className="kgs-chevron-stack__total">{items.length} results</div>
      <div className="kgs-chevron-stack__panel">
        <ChevronButton key={key} reference={key} data={item} component={itemComponent} onClick={onClick} />
      </div>
      <ChevronStackPagination page={index+1} totalPages={items.length} onClick={handlePageChange} />
    </div>
  );
};

export default ChevronStack;