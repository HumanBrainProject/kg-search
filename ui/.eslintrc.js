module.exports = {
  parser: "@typescript-eslint/parser",
  env: {
      "browser": true,
      "es2021": true,
      "node": true,
      "jest": true
  },
  extends: [
      "eslint:recommended",
      "plugin:@typescript-eslint/recommended",
      "plugin:react/recommended",
      "plugin:react-hooks/recommended",
      "prettier",
  ],
  parserOptions: {
      "ecmaFeatures": {
          "jsx": true
      },
      "ecmaVersion": "latest",
      "sourceType": "module"
  },
  plugins: [
      "react", "@typescript-eslint"
  ],
  rules: {
    "react/prop-types": [
        0
      ],
      "@typescript-eslint/no-namespace": "off"
  }
}