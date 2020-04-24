const proxy = require("http-proxy-middleware");

module.exports = function(app) {
  app.use(proxy("/search/proxy", {
    target:"http://localhost:9000",
    pathRewrite: { "^/search/proxy": "/proxy" },
    secure:false,
    changeOrigin: true }));
  app.use(proxy("/search/auth", {
    target:"http://localhost:9000",
    pathRewrite: { "^/search/auth": "/auth" },
    secure:false,
    changeOrigin: true }));
  app.use(proxy("/query", {
    target: "https://kg-dev.humanbrainproject.eu",
    changeOrigin: true,
    ws: true }));
};