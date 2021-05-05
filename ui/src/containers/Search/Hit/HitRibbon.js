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

import React from "react";
import { Ribbon } from "../../../components/Ribbon/Ribbon";

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