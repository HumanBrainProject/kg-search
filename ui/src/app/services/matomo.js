
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

  setCustomUrl(url) {
    if (this.reactPiwik && url) {
      ReactPiwik.push(["setCustomUrl", url]);
    } else {
      console.info("%cMatomo:setCustomUrl", style, url);
    }
  }

  trackPageView() {
    if (this.reactPiwik) {
      ReactPiwik.push(["trackPageView"]);
    } else {
      console.info("%cMatomo:trackPageView", style);
    }
  }

  trackEvent(category, name, value) {
    if (this.reactPiwik) {
      ReactPiwik.push(["trackEvent", category, name, value]);
    } else {
      console.info("%cMatomo:trackEvent", style, category, name, value);
    }
  }

  trackLink(category, name) {
    if (this.reactPiwik) {
      ReactPiwik.push(["trackLink", category, name]);
    } else {
      console.info("%cMatomo:trackLink", style, category, name);
    }
  }
}

export default new Matomo();
