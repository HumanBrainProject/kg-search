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

export class StatsHelpers {
  static average(values) {
    if (!(values instanceof Array) || values.length === 0) {
      return 0;
    }
    return values.reduce((sum, value) => sum + value, 0) / values.length;
  }
  static standardDeviation(values) {
    if (!(values instanceof Array) || values.length === 0) {
      return 0;
    }

    const avg = StatsHelpers.average(values);
    const squareDiffs = values.map(value => value - avg).map(val => val * val);
    const avgSquareDiff = StatsHelpers.average(squareDiffs);
    const stdDev = Math.sqrt(avgSquareDiff);

    return stdDev;
  }
}
