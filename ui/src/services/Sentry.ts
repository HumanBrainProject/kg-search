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
import { init, captureException as SentryCaptureException, showReportDialog as SentryShowReportDialog } from '@sentry/browser';
import type { BrowserOptions, ReportDialogOptions} from '@sentry/browser';

class Sentry {
  private _isInitialized: boolean;

  constructor() {
    this._isInitialized = false;
  }

  initialize(settings?: BrowserOptions): void {
    if (settings && !this._isInitialized) {
      this._isInitialized = true;
      if (!window.location.host.startsWith('localhost')) {
        init({
          ...settings,
          autoSessionTracking: false
        } as BrowserOptions);
      }
    }
  }

  showReportDialog(customSettings: ReportDialogOptions) {
    if (this._isInitialized) {
      const defaultSettings = {
        title: 'An unexpected error has occured.',
        subtitle2: 'We recommend you to save all your changes and reload the application in your browser. The KG team has been notified. If you\'d like to help, tell us what happened below.',
        labelEmail: 'Email',
        labelName: 'Name',
        labelComments: 'Please fill in a description of your error use case'
      };
      const settings = {
        ...defaultSettings,
        ...customSettings
      } as ReportDialogOptions;
      SentryCaptureException(new Error(settings.title)); //We generate a custom error as report dialog is only linked to an error.
      SentryShowReportDialog(settings);
    }
  }

}

export default new Sentry();