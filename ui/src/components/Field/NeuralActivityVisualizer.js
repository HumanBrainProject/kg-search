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
import Visualizer from 'neural-activity-visualizer-react';
import React, { useState, useMemo } from 'react';
import { Select } from '../../components/Select/Select';

import './NeuralActivityVisualizer.css';

const Component = ({ source, showSignals, showSpikeTrains }) => {
  if (!source) {
    return null;
  }
  return (
    <Visualizer source={source} showSignals={showSignals} showSpikeTrains={showSpikeTrains} />
  );
};

const NeuralActivityVisualizer = ({ data }) => {

  const [current, setCurrent] = useState(undefined);

  const items = useMemo(() => {
    if (!Array.isArray(data) || data.length === 0) {
      return [];
    }
    return data.reduce((acc, item) => {
      acc.list.push({label: item.name, value: item.url});
      acc.map[item.url] = item;
      return acc;
    }, {list: [{label: 'none', value: ''}], map:{}});
  }, [data]);

  const handleChange = v => setCurrent(v && items.map[v]?items.map[v]:undefined);

  return (
    <div className="kgs-neural-activity-visualizer">
      <div><Select className="kgs-fileFilter" label="Select a file" value={current?.url} list={items.list} onChange={handleChange} /></div>
      <Component key={current?.url} source={current?.url} showSignals={current?.showSignals !== undefined?current.showSignals:undefined} showSpikeTrains={current?.showSpikeTrains !== undefined?current.showSpikeTrains:undefined} />
    </div>
  );
};

export default NeuralActivityVisualizer;
