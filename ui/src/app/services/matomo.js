
import ReactPiwik from "react-piwik";

const style = "color: #f88900;";

class Matomo {

  constructor() {
    this.reactPiwik = null;
  }

  initialize(settings) {
    if (settings?.url && settings?.siteId && !this.reactPiwik) {
      this.reactPiwik = new ReactPiwik({
        url: settings.url,
        siteId:settings.siteId,
        trackErrors: true
      });
    }
  }

  trackCustomUrl(url) {
    if (this.reactPiwik && url) {
      ReactPiwik.push(["setCustomUrl", url]);
    } else {
      console.info("%ctrackCustomUrl", style, url);
    }
  }

  trackPageView() {
    if (this.reactPiwik) {
      ReactPiwik.push(["trackPageView"]);
    } else {
      console.info("%ctrackPageView", style);
    }
  }

  trackEvent(category, name, value) {
    if (this.reactPiwik) {
      ReactPiwik.push(["trackEvent", category, name, value]);
    } else {
      console.info("%ctrackEvent", style, category, name, value);
    }
  }

  trackLink(category, name) {
    if (this.reactPiwik) {
      ReactPiwik.push(["trackLink", category, name]);
    } else {
      console.info("%ctrackLink", style, category, name);
    }
  }
}

export default new Matomo();
