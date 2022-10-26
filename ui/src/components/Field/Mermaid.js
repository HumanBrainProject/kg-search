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
import React, { useEffect, useState } from "react";
import mermaid from "mermaid";
import { uniqueId } from "lodash";
import LinkedInstance from "../../pages/Instance/LinkedInstance";

mermaid.initialize({ startOnLoad: true, securityLevel: "loose" });

const attachCallback = (data, children, callbackFunctionkName) => {
  if (children && children instanceof Object) {
    Object.keys(children).forEach(key => {
      data += `\n click ${key} ${callbackFunctionkName}`;
    });
  }
  return data;
};

const MermaidGraph = ({ data, details, callbackFunctionkName }) => {
  const graph = attachCallback(data, details, callbackFunctionkName);
  useEffect(() => {
    mermaid.contentLoaded();
  }, []);
  return <div className="mermaid">{graph}</div>;
};

const Detail = ({ details, callbackFunctionkName }) => {
  const [detail, setDetail] = useState();
  useEffect(() => {
    window[callbackFunctionkName] = id => {
      setDetail(details[id]);
    };
    return () => {
      delete window[callbackFunctionkName];
    };
  }, []);

  if(!detail) {
    return null;
  }

  return <LinkedInstance data={detail} type={detail.type?.value} />;
};

const Mermaid = ({ data, details }) => {
  const callbackFunctionkName = uniqueId("mermaidCallback");

  return (
    <div>
      <MermaidGraph
        data={data}
        details={details}
        callbackFunctionkName={callbackFunctionkName}
      />
      <Detail details={details} callbackFunctionkName={callbackFunctionkName} />
    </div>
  );
};

export default Mermaid;
