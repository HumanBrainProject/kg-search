const proxy = require("http-proxy-middleware");

module.exports = function(app) {
  app.use(proxy("/proxy", {
    target: "https://kg.humanbrainproject.eu",
    changeOrigin: true,
    ws: true }));
  app.use(proxy("/search", {
    target: "https://kg.humanbrainproject.eu",
    changeOrigin: true,
    ws: true }));
  app.use(proxy("/auth", {
    target: "https://kg.humanbrainproject.eu",
    changeOrigin: true,
    ws: true }));
  app.use(proxy("/query", {
    target: "https://kg.humanbrainproject.eu",
    changeOrigin: true,
    ws: true }));
};