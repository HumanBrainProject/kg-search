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
import React, { useEffect, useRef, useState } from "react";
import mermaid from "mermaid";
import { uniqueId } from "lodash";
import * as d3 from "d3";
import LinkedInstance from "../../pages/Instance/LinkedInstance";

import "./Mermaid.css";

mermaid.initialize({ startOnLoad: true, securityLevel: "loose" });

const attachCallback = (data, children, callbackFunctionName) => {
  if (children && children instanceof Object) {
    Object.keys(children).forEach(key => {
      data += `\n click ${key} ${callbackFunctionName}`;
    });
  }
  return data;
};

const zoomGraph = () => {
  const svg_container = d3.select(".mermaid");
  const svg = svg_container.select("svg");
  const inner_svg = svg.select("g");
  const zoom = d3.zoom().on("zoom", function (e) {
    inner_svg.attr("transform", e.transform);
  });
  svg_container.call(zoom);
};

const MermaidGraph = ({ data, details, callbackFunctionName }) => {
  const ref = useRef();
  const graph = attachCallback(data, details, callbackFunctionName);

  useEffect(() => {
    if(!ref.current?.firstChild.tagName) {
      ref.current?.removeAttribute("data-processed");
      mermaid.contentLoaded();
    }
    zoomGraph();
  }, [callbackFunctionName]);

  return <div ref={ref} className="mermaid">{graph}</div>;
};

const Detail = ({ details, callbackFunctionName }) => {
  const [detailId, setDetailId] = useState();
  useEffect(() => {
    window[callbackFunctionName] = id => {
      setDetailId(id);
    };
    return () => {
      delete window[callbackFunctionName];
    };
  }, [callbackFunctionName]);

  const detail = detailId && details[detailId];

  if (!detailId || !detail) {
    return null;
  }

  return (
    <div className="kgs-mermaid__detail">
      <div className="kgs-mermaid__detail-title">{detail.title?detail.title:detailId}</div>
      <LinkedInstance data={detail} type={detail.type?.value} />
    </div>
  );
};

const Mermaid = ({ data, details }) => {
  const callbackFunctionName = uniqueId("mermaidCallback");

  return (
    <div className="kgs-mermaid">
      <MermaidGraph
        data={data}
        details={details}
        callbackFunctionName={callbackFunctionName}
      />
      <i className="kgs-mermaid__advise">
        Select the items of the graph to get more details about the individual elements.
      </i>
      <Detail details={details} callbackFunctionName={callbackFunctionName} />
    </div>
  );
};

export default Mermaid;
