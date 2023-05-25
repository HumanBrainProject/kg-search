module.exports = {
  parser: "@typescript-eslint/parser",
  env: {
      "browser": true,
      "es2021": true,
      "node": true
  },
  extends: [
      "eslint:recommended",
      "plugin:react/recommended",
      "plugin:react-hooks/recommended",
      "prettier",
      "plugin:@typescript-eslint/recommended"
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