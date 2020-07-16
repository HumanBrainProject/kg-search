const proxy = require("http-proxy-middleware");

module.exports = function(app) {
  app.use(proxy("/search/api", {
    //target: "https://kg.ebrains.eu",
    //target: "https://kg-int.humanbrainproject.eu",
    //target: "https://kg-dev.humanbrainproject.eu",
    target:"http://localhost:9000",
    pathRewrite: { "^/search/api": "/api" },
    secure:false,
    changeOrigin: true }));
  app.use(proxy("/search/indexer", {
    //target: "https://kg.ebrains.eu",
    //target: "https://kg-int.humanbrainproject.eu",
    //target: "https://kg-dev.humanbrainproject.eu",
    target:"http://localhost:9000",
    pathRewrite: { "^/search/indexer": "/indexer" },
    secure:false,
    changeOrigin: true }));
};