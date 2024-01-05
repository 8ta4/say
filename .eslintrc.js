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
    "no-unused-vars": ["error", { argsIgnorePattern: "^_" }],
  },
  settings: {
    "import/core-modules": ["electron"],
  },
};
