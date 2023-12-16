{ pkgs, ... }:

{
  # https://devenv.sh/basics/
  env.GREET = "devenv";

  # https://devenv.sh/packages/
  packages = [
    pkgs.git
    pkgs.gitleaks

    # https://github.com/NixOS/nixpkgs/issues/253198
    pkgs.purescript

    # https://github.com/electron-userland/electron-builder/blob/47e66ca64a89395a49300e8b2da1d9baeb93825a/docs/index.md?plain=1#L33
    pkgs.yarn
  ];

  # https://devenv.sh/scripts/
  scripts.hello.exec = "echo hello from $GREET";

  # https://github.com/electron-userland/electron-builder/blob/47e66ca64a89395a49300e8b2da1d9baeb93825a/docs/index.md?plain=1#L93
  scripts.dist.exec = ''
    build
    ./node_modules/.bin/electron-builder
  '';
  scripts.build.exec = ''
    ${pkgs.yarn}/bin/yarn install
    ./node_modules/.bin/spago build
  '';

  # https://github.com/electron-userland/electron-builder/blob/47e66ca64a89395a49300e8b2da1d9baeb93825a/docs/index.md?plain=1#L92
  scripts.pack.exec = ''
    build
    ./node_modules/.bin/electron-builder --dir
  '';
  scripts.run.exec = ''
    ./node_modules/.bin/nodemon --watch output --exec './node_modules/.bin/electron .'
  '';
  scripts.watch.exec = ''
    ./node_modules/.bin/spago build --watch
  '';

  enterShell = ''
    hello
    git --version
    build
  '';

  # https://devenv.sh/languages/
  # languages.nix.enable = true;

  # https://devenv.sh/pre-commit-hooks/
  # pre-commit.hooks.shellcheck.enable = true;
  pre-commit.hooks = {
    gitleaks = {
      enable = true;
      # https://github.com/gitleaks/gitleaks/blob/8de8938ad425d11edb0986c38890116525a36035/.pre-commit-hooks.yaml#L4C10-L4C54
      entry = "${pkgs.gitleaks}/bin/gitleaks protect --verbose --redact --staged";
    };
    nixpkgs-fmt.enable = true;
    prettier.enable = true;
    purs-tidy.enable = true;
    # https://github.com/cachix/pre-commit-hooks.nix/issues/31#issuecomment-744657870
    trailing-whitespace = {
      enable = true;
      # https://github.com/pre-commit/pre-commit-hooks/blob/4b863f127224b6d92a88ada20d28a4878bf4535d/.pre-commit-hooks.yaml#L201-L207
      entry = "${pkgs.python3Packages.pre-commit-hooks}/bin/trailing-whitespace-fixer";
      types = [ "text" ];
    };
  };

  # https://devenv.sh/processes/
  # processes.ping.exec = "ping example.com";

  # See full reference at https://devenv.sh/reference/options/
}
