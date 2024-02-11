module.exports = {
  env: {
    browser: true,
    es2021: true,
    node: true,
  },
  extends: ["airbnb-base", "prettier"],
  overrides: [
    {
      env: {
        node: true,
      },
      files: [".eslintrc.{js,cjs}"],
      parserOptions: {
        sourceType: "script",
      },
    },
    {
      files: ["src/**/*.js"],
      rules: {
        "import/prefer-default-export": "off",
      },
    },
  ],
  parserOptions: {
    ecmaVersion: "latest",
    sourceType: "module",
  },
  rules: {
    // https://stackoverflow.com/a/68177043
    "import/extensions": [
      "error",
      {
        js: "ignorePackages",
      },
    ],
    "import/no-unresolved": "off",
    "no-unused-vars": ["error", { argsIgnorePattern: "^_" }],
  },
  settings: {
    "import/core-modules": ["electron"],
  },
};
