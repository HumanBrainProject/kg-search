const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function(app) {
  app.use(
    "/search/api",
    createProxyMiddleware({
      //target: "https://kg.ebrains.eu",
      //target: "https://kg-int.humanbrainproject.eu",
      //target: "https://kg-dev.humanbrainproject.eu",
      target:"http://localhost:9000",
      pathRewrite: { "^/search/api": "/api" },
      secure:false,
      changeOrigin: true
    })
  );
};