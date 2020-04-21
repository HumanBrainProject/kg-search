const proxy = require("http-proxy-middleware");

module.exports = function (app) {
  app.use(proxy("/proxy", {
    target: "http://localhost:9000",
    changeOrigin: true,
    ws: true
  }));
  app.use(proxy("/auth", {
    target: "http://localhost:9000",
    changeOrigin: true,
    ws: true
  }));
  app.use(proxy("/query", {
    target: "http://localhost:8600",
    changeOrigin: true,
    ws: true
  }));
};