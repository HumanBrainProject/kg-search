
import { init as SentryInit, captureException as SentryCaptureException, showReportDialog as SentryShowReportDialog } from "@sentry/browser";

class Sentry {

  constructor() {
    this.isInitilized = false;
  }

  initialize(settings) {
    if (settings && !this.isInitilized) {
      this.isInitilized = true;
      SentryInit({
        ...settings,
        autoSessionTracking: false
      });
    }
  }

  captureException(e) {
    if (this.isInitilized) {
      SentryCaptureException(e);
    } else {
      console.error("captureException", e);
    }
  }

  showReportDialog(customSettings) {
    if (this.isInitilized) {
      const defaultSettings = {
        title: "An unexpected error has occured.",
        subtitle2: "We recommend you to save all your changes and reload the application in your browser. The KG team has been notified. If you'd like to help, tell us what happened below.",
        labelEmail: "Email",
        labelName: "Name",
        labelComments: "Please fill in a description of your error use case"
      };
      const settings = {
        ...defaultSettings,
        ...customSettings
      };
      SentryCaptureException(new Error(settings.title)); //We generate a custom error as report dialog is only linked to an error.
      SentryShowReportDialog(settings);
    } else {
      console.debug("showReportDialog", customSettings);
    }
  }
}

export default new Sentry();