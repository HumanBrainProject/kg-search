/*  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 *
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
 *
 */
import ReactPiwik, { PiwikOptions } from "react-piwik";

const style = "color: #f88900;";

class Matomo {
  private reactPiwik?: ReactPiwik;

  initialize(settings?: PiwikOptions): void {
    if (settings?.url && settings?.siteId && !this.reactPiwik && !window.location.host.startsWith("localhost")) {
      this.reactPiwik = new ReactPiwik({
        url: settings.url,
        siteId:settings.siteId
      });
    }
  }

  trackPageView(): void {
    if (this.reactPiwik) {
      ReactPiwik.push(["trackPageView"]);
    } else {
      console.info("%c[Matomo] trackPageView", style);
    }
  }

  setCustomUrl(url: string): void {
    if (this.reactPiwik && url) {
      ReactPiwik.push(["setCustomUrl", url]);
    } else {
      console.info(`%c[Matomo] setCustomUrl: ${url}`, style);
    }
  }

  trackEvent(category: string, name: string, value?: string): void {
    if (this.reactPiwik) {
      ReactPiwik.push(["trackEvent", category, name, value]);
    } else {
      console.info(`%c[Matomo] trackEvent: category="${category}", name="${name}", value="${value}"`, style);
    }
  }

  trackLink(category: string, name: string): void {
    if (this.reactPiwik) {
      ReactPiwik.push(["trackLink", category, name]);
    } else {
      console.info(`%c[Matomo] trackLink: category="${category}", name="${name}"`, style);
    }
  }
}

export default new Matomo();
