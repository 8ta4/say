{ pkgs, ... }:

{
  # https://devenv.sh/basics/
  env.GREET = "devenv";
  env.PIPENV_VENV_IN_PROJECT = "True";

  # https://devenv.sh/packages/
  packages = [
    pkgs.jdk17
    pkgs.ghcid
    pkgs.git
    pkgs.lame
    pkgs.leiningen
    # I initially used Poetry for managing Python dependencies. However, I encountered a persistent issue when installing the project via Homebrew. The error was related to Poetry's handling of lock files in the PyPI cache directory.
    pkgs.pipenv
    pkgs.portaudio
  ];

  # https://devenv.sh/scripts/
  scripts.hello.exec = "echo hello from $GREET";
  scripts.build.exec = ''
    cd "$DEVENV_ROOT/clj"
    ${pkgs.leiningen}/bin/lein uberjar
    cd "$DEVENV_ROOT/hs"
    ${pkgs.haskellPackages.stack}/bin/stack --local-bin-path . --nix install
    cd "$DEVENV_ROOT"
    tar czf say.tar.gz Pipfile Pipfile.lock clj hs
  '';
  scripts.ghci.exec = ''
    export DEVELOPMENT=1
    cd "$DEVENV_ROOT/hs"
    ${pkgs.ghcid}/bin/ghcid --command="${pkgs.stack}/bin/stack ghci" -T="main" --warnings
  '';
  scripts.run.exec = ''
    export DEVELOPMENT=1
    cd "$DEVENV_ROOT/hs"
    ${pkgs.stack}/bin/stack --nix run
  '';
  scripts.say.exec = ''
    "$DEVENV_ROOT/hs/say"
  '';

  enterShell = ''
    hello
    git --version
    ${pkgs.pipenv}/bin/pipenv install
    cd "$DEVENV_ROOT/clj"
    ${pkgs.leiningen}/bin/lein deps
    cd "$DEVENV_ROOT/hs"
    ${pkgs.haskellPackages.stack}/bin/stack build --fast
  '';

  # https://devenv.sh/languages/
  # languages.nix.enable = true;
  languages.haskell.enable = true;
  languages.python.enable = true;

  # https://devenv.sh/pre-commit-hooks/
  # pre-commit.hooks.shellcheck.enable = true;
  pre-commit.hooks = {
    nixpkgs-fmt.enable = true;
    ormolu.enable = true;
    prettier.enable = true;
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
