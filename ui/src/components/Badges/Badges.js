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

import uniqueId from 'lodash/uniqueId';
import React from 'react';
import { Tooltip } from 'react-tooltip';

import './Badges.css';

const Badge = ({ name, title, active }) => {
  if (!active) {
    return null;
  }
  return (
    <span className={`badge rounded-pill kgs-badge kgs-badge-${name}`}>{title}</span>
  );
};

const MetaBadge = ({ name, title, active }) => {
  if (!active) {
    return null;
  }
  const id = encodeURI(uniqueId(`kgs-meta-badge-${name}`));
  return (
    <span className="kgs-meta-badge-wrapper">
      <span id={id} className={`kgs-meta-badge kgs-meta-badge-${name}`} />
      <Tooltip className="kgs-meta-badge-tooltip" anchorSelect={`#${id}`} place="bottom" variant="dark" content={title} />
    </span>
  );
};


const Badges = ({ badges }) => {
  if (!Array.isArray(badges)) {
    return null;
  }
  return (
    <div className="kgs-badges">
      <div>
        <Badge name="new"      title="New"           active={badges.includes('isNew')} />
        <Badge name="trending" title="Top trending"  active={badges.includes('isTrending')} />
      </div>
      <div className="kgs-meta-badges">
        <MetaBadge name="isUsingOthers"               title="EBRAINS dataset/model uses another EBRAINS dataset/model"             active={badges.includes('isUsingOthers')} />
        <MetaBadge name="isUsedByOthers"              title="EBRAINS dataset/model is being used by another EBRAINS dataset/model" active={badges.includes('isUsedByOthers')} />
        <MetaBadge name="isFollowingStandards"        title="Organized according to a formal structure/community standards"        active={badges.includes('isFollowingStandards')} />
        <MetaBadge name="isLinkedToTools"             title="Linked to EBRAINS tools & software"                                   active={badges.includes('isLinkedToTools')} />
        <MetaBadge name="isLearningResourceAvailable" title="Learning Resource Available"                                          active={badges.includes('isLearningResourceAvailable')} />
        <MetaBadge name="isLinkedToImageViewer"       title="Linked to Image Viewer Service"                                       active={badges.includes('isLinkedToImageViewer')} />
        <MetaBadge name="isIntegratedWithAtlas"       title="Integrated with Atlas"                                                active={badges.includes('isIntegratedWithAtlas')} />
        <MetaBadge name="isReplicable"                title="Model Replicability"                                                  active={badges.includes('isReplicable')} />
        <MetaBadge name="isUsedInLivePaper"           title="Data/Model used in a live paper"                                      active={badges.includes('isUsedInLivePaper')} />
        <MetaBadge name="hasInDepthMetaData"          title="Dataset has in-depth metadata"                                        active={badges.includes('hasInDepthMetaData')} />
      </div>
    </div>
  );
};

export default Badges;