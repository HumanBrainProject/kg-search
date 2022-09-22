module.exports = {
  "settings": {
    "react": {
      "pragma": "React",
      "version": "17.0.2"
    }
  },
  "env": {
    "browser": true,
    "es2021": true,
    "node": true,
    "jest/globals": true
  },
  "extends": [
    "eslint:recommended",
    "plugin:react/recommended",
    "plugin:jest/recommended",
    "plugin:react-hooks/recommended",
    "prettier"
  ],
  "parserOptions": {
    "ecmaFeatures": {
      "jsx": true
    },
    "ecmaVersion": "latest",
    "sourceType": "module"
  },
  "plugins": [
    "react"
  ],
  "rules": {
    "indent": [
      "warn",
      2
    ],
    "linebreak-style": [
      "error",
      "unix"
    ],
    "quotes": [
      "warn",
      "double"
    ],
    "semi": [
      "error",
      "always"
    ],
    "curly": [
      "error",
      "all"
    ],
    "react/no-unknown-property": "warn",
    "no-trailing-spaces": "error",
    "no-throw-literal": "error",
    "strict": 0,
    "react/prop-types": [
      0
    ]
  }
};
