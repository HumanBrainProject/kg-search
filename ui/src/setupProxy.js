const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function(app) {
  app.use(
    "/api",
    createProxyMiddleware({
      //target: "https://kg.ebrains.eu",
      //target: "https://kg-int.humanbrainproject.eu",
      //target: "https://kg-dev.humanbrainproject.eu",
      target:"http://localhost:8080",
      secure:false,
      changeOrigin: true
    })
  );
};