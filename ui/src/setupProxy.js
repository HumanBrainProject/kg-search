const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function(app) {
  app.use(
    "/api",
    createProxyMiddleware({
      //target: "https://search.kg.ebrains.eu",
      // target: "https://search.kg-ppd.ebrains.eu",
      //target: "https://search.kg-int.ebrains.eu",
      target: "https://search.kg-dev.ebrains.eu",
      // target:"http://localhost:8080",
      secure:false,
      changeOrigin: true
    })
  );
};