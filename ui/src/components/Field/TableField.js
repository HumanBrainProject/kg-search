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
import { Field, PrintViewField } from "./Field";
import { ValueField } from "./ValueField";
import { LIST_SMALL_SIZE_STOP,
  getNextSizeStop,
  getFilteredItems,
  getShowMoreLabel } from "./helpers";
import "./TableField.css";

// import { Notification } from "../../containers/Notification/Notification";
import HierarchicalFiles from "../Files/HierarchicalFiles";

const CustomTableRow = ({item, viewComponent}) => {
  let Component = viewComponent;
  return (
    <tr>
      { Array.isArray(item)?
        item.map((i, id) =>
          <th key={`${i.name}-${id}`}>{i.data ?
            <Component name={i.name} data={i.data} mapping={i.mapping} group={i.group} />:"-"}</th>
        ):item.data ?
          <React.Fragment>
            <th className="kg-table__filecol">
              <ValueField show={true} data={item.data} mapping={item.mapping} group={item.group} />
            </th>
            <th>{item.data.fileSize ? item.data.fileSize:"-"}</th>
          </React.Fragment>:<th>-</th>
      }
    </tr>
  );
};

const TableFieldBase = (renderUserInteractions = true) => {

  const TableFieldComponent = ({list, showToggle, toggleHandler, toggleLabel}) => {
    const FieldComponent = renderUserInteractions?Field:PrintViewField;
    const fields = list.map(item =>
    {return item.isObject ?
      Object.entries(item.mapping.children)
        .filter(([, mapping]) =>
          mapping && mapping.visible
        )
        .map(([name, mapping]) => ({
          name: name,
          data: item.data && item.data[name],
          mapping: mapping,
          group: item.group
        })): item;
    }
    );

    const fileData = fields.map(item => item.data);
    // const fileData = [{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Arcuate_Anterior_Left.gii","fileSize":"551 KB","value":"Arcuate_Anterior_Left.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Arcuate_Anterior_Left.gii.minf","fileSize":"290 bytes","value":"Arcuate_Anterior_Left.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Probability_Maps/Arcuate_Anterior_Left.nii.gz","fileSize":"248 KB","value":"Arcuate_Anterior_Left.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Arcuate_Anterior_Right.gii","fileSize":"666 KB","value":"Arcuate_Anterior_Right.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Arcuate_Anterior_Right.gii.minf","fileSize":"290 bytes","value":"Arcuate_Anterior_Right.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Probability_Maps/Arcuate_Anterior_Right.nii.gz","fileSize":"301 KB","value":"Arcuate_Anterior_Right.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Arcuate_Left.gii","fileSize":"616 KB","value":"Arcuate_Left.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Arcuate_Left.gii.minf","fileSize":"290 bytes","value":"Arcuate_Left.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Probability_Maps/Arcuate_Left.nii.gz","fileSize":"279 KB","value":"Arcuate_Left.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Arcuate_Posterior_Left.gii","fileSize":"450 KB","value":"Arcuate_Posterior_Left.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Arcuate_Posterior_Left.gii.minf","fileSize":"289 bytes","value":"Arcuate_Posterior_Left.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Probability_Maps/Arcuate_Posterior_Left.nii.gz","fileSize":"212 KB","value":"Arcuate_Posterior_Left.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Arcuate_Posterior_Right.gii","fileSize":"435 KB","value":"Arcuate_Posterior_Right.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Arcuate_Posterior_Right.gii.minf","fileSize":"289 bytes","value":"Arcuate_Posterior_Right.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Probability_Maps/Arcuate_Posterior_Right.nii.gz","fileSize":"218 KB","value":"Arcuate_Posterior_Right.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Arcuate_Right.gii","fileSize":"311 KB","value":"Arcuate_Right.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Arcuate_Right.gii.minf","fileSize":"289 bytes","value":"Arcuate_Right.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Probability_Maps/Arcuate_Right.nii.gz","fileSize":"159 KB","value":"Arcuate_Right.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Cingulum_Long_Left.gii","fileSize":"542 KB","value":"Cingulum_Long_Left.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Cingulum_Long_Left.gii.minf","fileSize":"290 bytes","value":"Cingulum_Long_Left.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Probability_Maps/Cingulum_Long_Left.nii.gz","fileSize":"225 KB","value":"Cingulum_Long_Left.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Cingulum_Long_Right.gii","fileSize":"570 KB","value":"Cingulum_Long_Right.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Cingulum_Long_Right.gii.minf","fileSize":"290 bytes","value":"Cingulum_Long_Right.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Probability_Maps/Cingulum_Long_Right.nii.gz","fileSize":"221 KB","value":"Cingulum_Long_Right.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Cingulum_Short_Left.gii","fileSize":"775 KB","value":"Cingulum_Short_Left.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Cingulum_Short_Left.gii.minf","fileSize":"290 bytes","value":"Cingulum_Short_Left.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Probability_Maps/Cingulum_Short_Left.nii.gz","fileSize":"337 KB","value":"Cingulum_Short_Left.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Cingulum_Short_Right.gii","fileSize":"823 KB","value":"Cingulum_Short_Right.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Cingulum_Short_Right.gii.minf","fileSize":"290 bytes","value":"Cingulum_Short_Right.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Probability_Maps/Cingulum_Short_Right.nii.gz","fileSize":"333 KB","value":"Cingulum_Short_Right.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Cingulum_Temporal_Left.gii","fileSize":"145 KB","value":"Cingulum_Temporal_Left.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Cingulum_Temporal_Left.gii.minf","fileSize":"288 bytes","value":"Cingulum_Temporal_Left.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Probability_Maps/Cingulum_Temporal_Left.nii.gz","fileSize":"96 KB","value":"Cingulum_Temporal_Left.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Cingulum_Temporal_Right.gii","fileSize":"152 KB","value":"Cingulum_Temporal_Right.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Cingulum_Temporal_Right.gii.minf","fileSize":"288 bytes","value":"Cingulum_Temporal_Right.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Probability_Maps/Cingulum_Temporal_Right.nii.gz","fileSize":"96 KB","value":"Cingulum_Temporal_Right.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/CorticoSpinalTract_Left.gii","fileSize":"274 KB","value":"CorticoSpinalTract_Left.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/CorticoSpinalTract_Left.gii.minf","fileSize":"289 bytes","value":"CorticoSpinalTract_Left.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Probability_Maps/CorticoSpinalTract_Left.nii.gz","fileSize":"150 KB","value":"CorticoSpinalTract_Left.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/CorticoSpinalTract_Right.gii","fileSize":"239 KB","value":"CorticoSpinalTract_Right.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/CorticoSpinalTract_Right.gii.minf","fileSize":"289 bytes","value":"CorticoSpinalTract_Right.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Probability_Maps/CorticoSpinalTract_Right.nii.gz","fileSize":"126 KB","value":"CorticoSpinalTract_Right.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Fornix_Left.gii","fileSize":"133 KB","value":"Fornix_Left.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Fornix_Left.gii.minf","fileSize":"288 bytes","value":"Fornix_Left.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Probability_Maps/Fornix_Left.nii.gz","fileSize":"84 KB","value":"Fornix_Left.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Fornix_Right.gii","fileSize":"124 KB","value":"Fornix_Right.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Fornix_Right.gii.minf","fileSize":"288 bytes","value":"Fornix_Right.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Probability_Maps/Fornix_Right.nii.gz","fileSize":"83 KB","value":"Fornix_Right.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/InferiorFrontoOccipital_Left.gii","fileSize":"301 KB","value":"InferiorFrontoOccipital_Left.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/InferiorFrontoOccipital_Left.gii.minf","fileSize":"289 bytes","value":"InferiorFrontoOccipital_Left.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Probability_Maps/InferiorFrontoOccipital_Left.nii.gz","fileSize":"129 KB","value":"InferiorFrontoOccipital_Left.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/InferiorFrontoOccipital_Right.gii","fileSize":"355 KB","value":"InferiorFrontoOccipital_Right.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/InferiorFrontoOccipital_Right.gii.minf","fileSize":"289 bytes","value":"InferiorFrontoOccipital_Right.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Probability_Maps/InferiorFrontoOccipital_Right.nii.gz","fileSize":"152 KB","value":"InferiorFrontoOccipital_Right.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/InferiorLongitudinal_Left.gii","fileSize":"752 KB","value":"InferiorLongitudinal_Left.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/InferiorLongitudinal_Left.gii.minf","fileSize":"290 bytes","value":"InferiorLongitudinal_Left.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Probability_Maps/InferiorLongitudinal_Left.nii.gz","fileSize":"336 KB","value":"InferiorLongitudinal_Left.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/InferiorLongitudinal_Right.gii","fileSize":"749 KB","value":"InferiorLongitudinal_Right.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/InferiorLongitudinal_Right.gii.minf","fileSize":"290 bytes","value":"InferiorLongitudinal_Right.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Probability_Maps/InferiorLongitudinal_Right.nii.gz","fileSize":"336 KB","value":"InferiorLongitudinal_Right.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/labels_DWM.txt","fileSize":"999 bytes","value":"labels_DWM.txt"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/long-bundles_maxprob.nii.gz","fileSize":"78 KB","value":"long-bundles_maxprob.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Uncinate_Left.gii","fileSize":"494 KB","value":"Uncinate_Left.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Meshes/Uncinate_Left.gii.minf","fileSize":"290 bytes","value":"Uncinate_Left.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/left-hemisphere/Probability_Maps/Uncinate_Left.nii.gz","fileSize":"225 KB","value":"Uncinate_Left.nii.gz"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Uncinate_Right.gii","fileSize":"535 KB","value":"Uncinate_Right.gii"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Meshes/Uncinate_Right.gii.minf","fileSize":"290 bytes","value":"Uncinate_Right.gii.minf"},{"url":"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000339_DeepWhiteMatterBundles_DWM_pub/version2018/right-hemisphere/Probability_Maps/Uncinate_Right.nii.gz","fileSize":"243 KB","value":"Uncinate_Right.nii.gz"}]
    return (
      fields && Array.isArray(fields[0]) ?
        (fields && fields[0] ?
          <table className="table">
            <thead>
              <tr>
                {fields[0].map((el,id) =>
                  <th key={`${el.name}-${id}`}>{el.mapping.value}</th>
                )}
              </tr>
            </thead>
            <tbody>
              {fields.map((item, index) => <CustomTableRow key={`${index}`}  item={item} isFirst={!index} viewComponent={FieldComponent} />)}
              {showToggle && (
                <tr>
                  <th><button className="kgs-field__viewMore-button" onClick={toggleHandler} role="link">{toggleLabel}</button></th>
                </tr>
              )}
            </tbody>
          </table>:null) :
        <>
          <HierarchicalFiles data={fileData} />
          {/* <Notification />
          <table className="table">
            <thead>
              <tr>
                <th>Filename</th>
                <th>Size</th>
              </tr>
            </thead>
            <tbody>
              {fields.map((item, index) => <CustomTableRow key={`${index}`}  item={item} isFirst={!index} viewComponent={FieldComponent} />)}
              {showToggle && (
                <tr>
                  <th><button className="kgs-field__viewMore-button" onClick={toggleHandler} role="link">{toggleLabel}</button></th>
                </tr>
              )}
            </tbody>
          </table> */}
        </>
    );
  };

  class TableField extends React.Component {
    constructor(props) {
      super(props);
      const sizeStop = getNextSizeStop(Number.POSITIVE_INFINITY, this.props);
      this.state = {
        sizeStop: sizeStop,
        items: Array.isArray(this.props.items) ? getFilteredItems(sizeStop, this.maxSizeStop, this.props) : this.getItems(),
        hasShowMoreToggle: this.hasShowMoreToggle,
        showMoreLabel: getShowMoreLabel(sizeStop, this.props)
      };
      this.handleShowMoreClick = this.handleShowMoreClick.bind(this);
    }

    get maxSizeStop() {
      const {items, mapping} = this.props;

      if (!Array.isArray(items)) {
        return 0;
      }

      if (!renderUserInteractions && mapping && mapping.overviewMaxDisplay && mapping.overviewMaxDisplay < items.length) {
        return mapping.overviewMaxDisplay;
      }
      return items.length;
    }

    get hasShowMoreToggle() {
      const {items, mapping} = this.props;
      if (!Array.isArray(items) || (mapping && mapping.separator) || !renderUserInteractions) {
        return false;
      }

      return this.maxSizeStop > LIST_SMALL_SIZE_STOP;
    }

    getItems(){
      const {items, mapping, group} = this.props;
      let convertedItem = [];
      convertedItem.push(items);
      return convertedItem.map((item, idx) => ({
        isObject: !!item.children,
        key: item.reference?item.reference:item.value?item.value:idx,
        show: true,
        data: item.children?item.children:item,
        mapping: mapping,
        group: group
      }));
    }

    handleShowMoreClick() {
      this.setState((state,props) => {
        const nextSizeStop = getNextSizeStop(state.sizeStop, props);
        return {
          sizeStop: nextSizeStop,
          items: getFilteredItems(nextSizeStop, this.maxSizeStop, props),
          hasShowMoreToggle: this.hasShowMoreToggle,
          showMoreLabel: getShowMoreLabel(nextSizeStop, props)
        };
      });
    }

    render() {
      const {show} = this.props;

      return (
        show ? <TableFieldComponent list={this.state.items} showToggle={this.state.hasShowMoreToggle} toggleHandler={this.handleShowMoreClick} toggleLabel={this.state.showMoreLabel} />:null
      );
    }
  }
  return TableField;
};

export const TableField = TableFieldBase(true);
export const PrintViewTableField = TableFieldBase(false);