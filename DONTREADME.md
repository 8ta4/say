# say

## Goals

### 24/7

> What's wrong with writing or typing?

`say` is for those moments when note-taking is a hassle, like when you're busy, when you're in bed, or when you're busy in bed.

Writing or typing can be slow. You might lose your train of thought.

It's automatic, so say goodbye to keeping a journal.

> What's wrong with other dictation apps?

Most dictation apps require you to press a button or say a command before you start speaking. That can interrupt your flow.

`say` is always on. Whether it's a business idea or comedic line, just say it.

> Isn't recording all the time a privacy concern?

Absolutely. That's why `say` is designed for home use. Just be aware it might pick up others' voices if they're around.

### Accuracy

> Is there a Word Error Rate goal?

Word Error Rate can be misleading. It counts every mistake equally, regardless of how significant or trivial it is.

Interaction with Large Language Models (LLMs) takes the front seat here. `say` is built with the expectation that LLMs will use its transcripts, even if it means sacrificing readability.

What matters is whether LLMs can understand what you are saying and respond accordingly.

Not all words are created equal. Getting named entities and trendy terms right is crucial.

### Latency

> How low is the latency goal?

`say` is shooting for sub-one-second latency.

> Why keep latency below one second?

"[1.0 second is about the limit for the user's flow of thought to stay uninterrupted](https://www.nngroup.com/articles/response-times-3-important-limits/#:~:text=1.0%20second%20is%20about%20the%20limit%20for%20the%20user's%20flow%20of%20thought%20to%20stay%20uninterrupted)".

With `say`, you can turn speech into text in a flash, ready for whatever you need it for - writing, editing, sharing.

`say`’s user interface is optimized to reduce perceived latency.

> Why not aim for latency below 0.1 seconds?

"[[D]uring delays of more than 0.1 but less than 1.0 second... the user does lose the feeling of operating directly on the data](https://www.nngroup.com/articles/response-times-3-important-limits/#:~:text=during%20delays%20of%20more%20than%200.1%20but%20less%20than%201.0%20second%2C%20but%20the%20user%20does%20lose%20the%20feeling%20of%20operating%20directly%20on%20the%20data)".

But that's OK. Operating directly on the data can be distracting. You can focus on speaking now and working on the data later.

### Budget

> How much does say cost per month?

`say` is aiming for less than $100/month because that's the sweet spot for most personal productivity tools.

Some folks might say that a higher price makes you use the tool more. You know, the whole sunk cost fallacy thing. But let's just say it: that's just a trick to get you to spend more money.

## Setup

> How do I set up this tool's dev environment?

1. Make sure you're using a Mac.

1. Get [git](https://formulae.brew.sh/formula/git#default) installed.

1. Get [devenv](https://devenv.sh/getting-started/) installed too.

1. Get [direnv](https://formulae.brew.sh/formula/direnv#default) installed as well.

1. Run the following commands:

   ```sh
   git clone git@github.com:8ta4/say.git
   cd say
   direnv allow
   ```

The `devenv.nix` file has got all the scripts you need.

> Why does this tool mess with my third-party tools when I first run this tool?

I could've asked you to edit your `.skhdrc` file to make a shortcut for this tool. But that would be a hassle.

And I couldn't do it for you, because that would break [Homebrew's policy](https://docs.brew.sh/Homebrew-and-Python#:~:text=homebrew%20has%20a%20strict%20policy%20never%20to%20write%20stuff%20outside%20of%20the%20brew%20--prefix).

## Architecture

`say` functions as a monolithic macOS background process. It's just a `brew install` away, eliminating the need for any additional devices.

The uniformity of the macOS ecosystem simplifies both development and testing.

This setup cuts down on cloud dependence and gives your privacy a boost.

## Name Overlap

This tool intentionally replaces the `say` command built-in with macOS.

If you run the default `say` command on macOS while this tool is active, it may interfere with the recording and transcription process.

I've chosen to keep the name of this tool as `say` to ensure a smooth installation experience.



## API Key

I've chosen not to store the API key. This approach keeps the codebase simple and reduces potential security vulnerabilities.

Designed for continuous operation, `say` ideally requires the API key to be entered just once.

## Transcription

`say` taps into a powerful API that can transcribe speech accurately and fast. `say` doesn't reinvent the wheel.

`say` doesn't send any previous speech to the API for context. Sending partial context may not help much with accuracy. Sending full context may help, but it may also increase cost, latency, and complexity.

## Trigger

### Manual

The manual trigger is `Shift + Space`. It's easy to press and it doesn't clash with anything.

`⌘ + Space` is used by Spotlight or other launchers. `Ctrl + Space` triggers auto-suggestions in IDEs, like VS Code.

### Automatic

The automatic trigger is based on voice activity. `say` keeps track of how much you talk without transcribing. When you reach 1 minute of untranscribed speech, `say` waits for a pause and then sends the audio for transcription.

This trategy is designed to strike a balance between accuracy and latency.

Longer speech gives more context and improves accuracy.

And waiting for a pause helps capture your whole thought and avoid cutting off mid-sentence.

This strategy also helps the manual trigger meet the sub-second latency goal. 

From my experience, if the audio is 1 minute or less, the transcription API usually responds in under a second. But if the audio is 2 minutes or longer, the latency can extend to 1 second or more. This was tested on a North American gigabit connection.

There is a natural delay when you switch from talking to reading your transcript. This delay might offset any extra speech when you surpass 1 minute.

## Data Retention

`say` doesn't keep a record of the audio once it has been transcribed.

The act of storing audio can amplify the observer effect, potentially making you more self-aware and uncomfortable.

By choosing not to store audio, `say` ensures that sounds like your snoring, farting, or moaning aren't kept on record.

Being arrested and having your own audio used against you in a court of law isn't a pleasant experience.

Even the sound of typing can sometimes be enough to decipher what you're typing. This becomes a security risk if you're entering sensitive information like passwords while speaking.

If you're interested in documenting everything, you might consider recording video. However, this comes with its own risks, such as accidentally capturing footage when you're nude.

## Segmentation

### Line

Each sentence in `say` gets its own line. That way, you can easily move up and down with `j` and `k`.

### Paragraph

`say` starts a new paragraph when you request transcription. This helps you keep track of what you have already used and what you need next. You can jump to the latest chunk of text with `Shift + [` and `Shift + ]`.

### File

Daily segmentation speeds up loading times.

If you need weekly or monthly transcriptions, you can simply concatenate the daily transcriptions.

This also makes it easy to search for a specific date.

## Updating the Transcript File

1. `say` makes a copy of the `DD.txt` file and creates a temporary file in the system's temp directory. This ensures that the application directory doesn't get cluttered.

1. Any updates made to the file will modify the temporary file, preserving the read-only nature of the original `DD.txt` file.

1. `say` uses the atomic `rename` operation to replace the original `DD.txt` file with the modified temporary file. This atomic operation ensures that the `DD.txt` file is always consistent.

## Text Editor

I've selected Visual Studio Code, and here's why:

- It's got this cool [auto-reload feature](https://stackoverflow.com/questions/30078077/visual-studio-code-auto-refresh-file-changes#:~:text=vscode%20will%20never%20refresh%20the%20file%20if%20you%20have%20changes%20in%20that%20file%20that%20are%20not%20saved%20to%20disk.%20however%2C%20if%20the%20file%20is%20open%20and%20does%20not%20have%20changes%2C%20it%20will%20replace%20with%20the%20changes%20on%20disk%2C%20that%20is%20true.) built right in.

- It can be set up to [honor read-only files](https://code.visualstudio.com/docs/getstarted/settings#:~:text=//%20Marks%20files%20as%20readonly%20when%20their%20file%20permissions%20indicate%20as%20such.%20This%20can%20be%20overridden%20via%20%60files.readonlyInclude%60%20and%20%60files.readonlyExclude%60%20settings.%0A%20%20%22files.readonlyFromPermissions%22%3A%20false%2C).

- It's got a [Neovim plugin](https://github.com/vscode-neovim/vscode-neovim#neovim-configuration) that plays nice with your existing Neovim config.

## Encryption

I've decided to skip application-level encryption, and let me tell you why:

Adding an encryption layer might mean you'd have to juggle a decryption key or passphrase.

I want you to be able to access your transcription with any software you fancy.

You can totally use device-level encryption.

A forgotten key or an encryption hiccup could lead to data loss.
