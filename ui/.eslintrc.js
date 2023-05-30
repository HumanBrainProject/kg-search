module.exports = {
  settings: {
    react: {
      pragma: 'React',
      version: '18.2.0'
    }
  },
  parser: '@typescript-eslint/parser',
  env: {
    browser: true,
    es2021: true,
    node: true,
    jest: true
  },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:jest/recommended',
    'plugin:react/recommended',
    'plugin:react-hooks/recommended',
    'prettier'
  ],
  parserOptions: {
    ecmaFeatures: {
      jsx: true
    },
    ecmaVersion: 'latest',
    sourceType: 'module'
  },
  plugins: ['@typescript-eslint', 'autofix', 'import', 'react', 'react-hooks'],
  rules: {
    '@typescript-eslint/consistent-type-imports': [
      'error',
      {
        prefer: 'type-imports'
      }
    ],
    '@typescript-eslint/no-namespace': 'off',
    'arrow-body-style': ['error', 'as-needed'],
    'autofix/no-unused-vars': [
      'error',
      {
        argsIgnorePattern: '^_',
        ignoreRestSiblings: true,
        destructuredArrayIgnorePattern: '^_'
      }
    ],
    'curly': [
      'error',
      'all'
    ],
    'import/order': [
      'error',
      {
        groups: [
          'builtin',
          'external',
          'parent',
          'sibling',
          'index',
          'object',
          'type'
        ],
        pathGroups: [
          {
            pattern: '@/**/**',
            group: 'parent',
            position: 'before'
          }
        ],
        alphabetize: { order: 'asc' }
      }
    ],
    'indent': [
      'warn',
      2
    ],
    'linebreak-style': [
      'error',
      'unix'
    ],
    // 'no-restricted-imports': [
    //     'error',
    //     {
    //         'patterns': ['../']
    //     }
    // ],
    'no-trailing-spaces': 'error',
    'no-throw-literal': 'error',
    'quotes': [
      'warn',
      'single'
    ],
    'react-hooks/exhaustive-deps': 'error',
    'react/no-unknown-property': 'warn',
    'react/prop-types': [0],
    'react/self-closing-comp': ['error', { component: true, html: true }],
    'semi': [
      'error',
      'always'
    ],
    'strict': 0
  }
};
