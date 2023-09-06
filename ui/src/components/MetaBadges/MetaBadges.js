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

import './MetaBadges.css';

const META_BADGES = [
  { name: 'isUsingOthers',               title: 'EBRAINS dataset/model uses another EBRAINS dataset/model' },
  { name: 'isUsedByOthers',              title: 'EBRAINS dataset/model is being used by another EBRAINS dataset/model' },
  { name: 'isFollowingStandards',        title: 'Organized according to a formal structure/community standards' },
  { name: 'isLinkedToTools',             title: 'Linked to EBRAINS tools & software' },
  { name: 'isLearningResourceAvailable', title: 'Learning Resource Available' },
  { name: 'isLinkedToImageViewer',       title: 'Linked to Image Viewer Service' },
  { name: 'isIntegratedWithAtlas',       title: 'Integrated with Atlas' },
  { name: 'isReplicable',                title: 'Model Replicability' },
  { name: 'isUsedInLivePaper',           title: 'Data/Model used in a live paper' },
  { name: 'hasInDepthMetaData',          title: 'Dataset has in-depth metadata' }
];

const MetaBadge = ({ name, title }) => {
  const id = encodeURI(uniqueId(`kgs-meta-badge-${name}`));
  return (
    <span className="kgs-meta-badge-wrapper">
      <span id={id} className={`kgs-meta-badge kgs-meta-badge-${name}`} />
      <Tooltip className="kgs-meta-badge-tooltip" anchorSelect={`#${id}`} place="bottom" variant="dark" content={title} />
    </span>
  );
};

const MetaBadges = ({ badges }) => {
  const list = Array.isArray(badges)?META_BADGES.filter(badge => badges.includes(badge.name)):[];
  return (
    <div className="kgs-meta-badges">
      {list.map(badge => <MetaBadge key={badge.name} name={badge.name} title={badge.title} />)}
    </div>
  );
};

export default MetaBadges;