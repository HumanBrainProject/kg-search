module.exports = {
  "verbose": true,
  "testMatch": ["<rootDir>/src/**/*.test.js"],
  "transform": {
    "^.+\\.jsx?$": "babel-jest",
    "^.+\\.css$": "<rootDir>/CSSStub.js"
  }
}