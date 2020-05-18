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
import { Ribbon } from "../../components/Ribbon/Ribbon";

const getCounter = (data, mapping) => {
  let counter = 0;
  if (mapping && mapping.framed) {
    switch (mapping.framed.aggregation) {
    case "count":
      counter = Array.isArray(data)?data.length:1;
      break;
    default:
      counter = 0;
    }
  }
  return counter;
};

const getSuffix = (counter, mapping) => {
  if (!mapping || !mapping.framed) {
    return null;
  }
  if (Number.isInteger(counter) && counter > 1) {
    return mapping.framed.suffix && mapping.framed.suffix.plural;
  }
  return mapping.framed.suffix && mapping.framed.suffix.singular;
};

export const HitRibbon = ({className, data, mapping}) => {
  if (!mapping || !data) {
    return null;
  }
  const counter = getCounter(data, mapping);
  const suffix = getSuffix(counter, mapping);
  return (
    <Ribbon className={className} icon={mapping.icon} text={mapping.content} counter={counter} suffix={suffix} />
  );
};