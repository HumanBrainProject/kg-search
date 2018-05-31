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

import React from 'react';
import './styles.css';

export function Summary({data, mapping}) {

    const stats = {};

    Object.entries(mapping.fields)
        .map(([name, mapping]) => ({
            name: name,
            mapping: mapping
        }))
        .forEach(e => checkField(e.name, data, e.mapping, null, stats));

    const results = [];
    /*
    [
        <li key="datasets1"><span className="kgs-shape__summary-label">Datasets</span><span className="kgs-shape__summary-value">6</span></li>,
        <li key="contributors1"><span className="kgs-shape__summary-label">Contributors</span><span className="kgs-shape__summary-value">4</span></li>,
        <li key="species1"><span className="kgs-shape__summary-label">Species</span><span className="kgs-shape__summary-value">Macaca mulatta, Mammalia, Rattus norvegicus</span></li>
    ];
    */
    
    Object.entries(stats)
        .map(([key, stat]) => ({
            key: key,
            name: stat.name,
            arrayCount: stat.arrayCount,
            uniqueCount: stat.uniqueCount,
            list: stat.list
        }))
        .forEach(e => {
            let value = e.arrayCount;
            if (e.uniqueCount)
                value = Object.keys(e.uniqueCount).length;
            if (e.list)
                value = Object.keys(e.list).join(", ");
            if (value)
                results.push(<li key={e.key}><span className="kgs-shape__summary-label">{e.name}</span><span className="kgs-shape__summary-value">{value}</span></li>);
        });

    if (results.length)
        return (
            <span className="kgs-shape__field kgs-shape__summary">
                <ul>
                    {results}
                </ul>
            </span>
        );

    return null;
}

function checkField(name, data, mapping, key, stats) {
    //!mapping.aggregate && (mapping.aggregate = (Math.round(Math.random() * 10) %2)?"count":"list");
    const value = data && name && data[name];
    if (value) {
        key = (key?(key + "."):"") + name;
        if (Array.isArray(value)) {
            calcArrayStats(value, mapping, key, stats);
            checkArray(value, mapping, key, stats);
        } else {
            calcItemStats(value, mapping, key, stats);
            if (mapping && mapping.children)
                checkObject(value.children, mapping, key, stats);
        }
    }
}

function checkObject(data, mapping, key, stats) {
    Object.entries(mapping.children)
        .map(([name, mapping]) => ({
            name: name,
            mapping: mapping
        }))
        .forEach(e => {
            const value = data[e.name];
            if (mapping && value !== null && value !== undefined)
                checkField(e.name, data, e.mapping, key, stats);
        });
  }
  
function checkArray(items, mapping, key, stats) {
    items.forEach((item, index) => {
        if (item.children) {
            checkObject(item.children, mapping, key, stats);
        }
    })
}

function calcItemStats(item, mapping, key, stats) {
    if (mapping.value && mapping.aggregate && item.value !== undefined && item.value !== null && ("" + item.value).trim() !== "") {
        if (mapping.aggregate === "count") {
            
            let itemStat = stats[key];
            if (!itemStat) {
                itemStat =  {name: mapping.value, uniqueCount: {}};
                stats[key] = itemStat;
            }
            itemStat.uniqueCount[item.value] = true;

        } else if (mapping.aggregate === "list") {

            let itemStat = stats[key];
            if (!itemStat) {
                itemStat =  {name: mapping.value, list: {}};
                stats[key] = itemStat;
            }
            itemStat.list[item.value] = true;
        } 
    } 
}

function calcArrayStats(items, mapping, key, stats) {
    if (mapping.value && mapping.aggregate && items.length) {
        if (mapping.aggregate === "count") {
            
            let itemStat = stats[key];
            if (!itemStat) {
                itemStat =  {name: mapping.value, arrayCount: 0};
                stats[key] = itemStat;
            }

            itemStat.arrayCount += items.length;

        } else if (mapping.aggregate === "list") {

            let itemStat = stats[key];
            if (!itemStat) {
                itemStat =  {name: mapping.value, list: {}};
                stats[key] = itemStat;
            }
            
            items.forEach((item, index) => {
                if (item.value !== undefined && item.value !== null && ("" + item.value).trim() !== "")
                    itemStat.list[item.value] = true;
            });
            if (!Object.keys(itemStat.list).length)
                delete stats[key];
        } 
    }
}