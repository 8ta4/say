{ pkgs, ... }:

{
  # https://devenv.sh/basics/
  env.GREET = "devenv";

  # https://devenv.sh/packages/
  packages = [
    pkgs.ffmpeg
    pkgs.git
    pkgs.gitleaks

    # https://github.com/NixOS/nixpkgs/issues/253198
    # The package spago-0.20.9 is marked as broken in the Nix packages repository, which caused the error.
    # https://github.com/cachix/devenv/blob/7354096fc026f79645fdac73e9aeea71a09412c3/src/modules/languages/purescript.nix#L28-L30
    # https://github.com/cachix/devenv/blob/7354096fc026f79645fdac73e9aeea71a09412c3/src/modules/languages/purescript.nix#L18
    # I am using `yarn` to install `spago` due to issues encountered when trying to use `pkgs.spago` and `languages.purescript.enable`.
    # https://github.com/electron-userland/electron-builder/blob/47e66ca64a89395a49300e8b2da1d9baeb93825a/docs/index.md?plain=1#L33
    pkgs.nodePackages.purescript-language-server
    pkgs.nodePackages.purs-tidy
    pkgs.purescript
    pkgs.purescript-psa
    pkgs.yarn-berry
  ];

  # https://devenv.sh/scripts/
  scripts.hello.exec = "echo hello from $GREET";

  # https://github.com/electron-userland/electron-builder/blob/47e66ca64a89395a49300e8b2da1d9baeb93825a/docs/index.md?plain=1#L93
  scripts.dist.exec = ''
    build
    electron-builder -p never
  '';

  scripts.build.exec = ''
    ${pkgs.yarn-berry}/bin/yarn install
    spago build
  '';

  # https://github.com/electron-userland/electron-builder/blob/47e66ca64a89395a49300e8b2da1d9baeb93825a/docs/index.md?plain=1#L92
  scripts.pack.exec = ''
    build
    electron-builder --dir
  '';
  scripts.run.exec = ''
    nodemon --watch output --exec 'pkill -f "node_modules/electron"; electron .'
  '';
  scripts.watch.exec = ''
    spago build --watch
  '';

  enterShell = ''
    hello
    git --version
    export PATH="$(pwd)/node_modules/.bin:$PATH"
    build
  '';

  # https://devenv.sh/languages/
  # languages.nix.enable = true;
  languages.javascript.enable = true;

  # https://devenv.sh/pre-commit-hooks/
  # pre-commit.hooks.shellcheck.enable = true;
  pre-commit.hooks = {
    eslint.enable = true;
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
