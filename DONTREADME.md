# say

## Goals

### 24/7

`say` is a tool designed for capturing thoughts and ideas through continuous voice recording. It's a personal reflection-focused tool that avoids any potential legal complications.

Voice is an efficient medium for brain-to-computer communication.

`say` allows you to capture your thoughts in situations where traditional note-taking isn't feasible, such as when you're in bed or when your hands are occupied.

Journaling can have therapeutic effects, and `say` automates this process, potentially providing catharsis.

`say` can boost your creativity and productivity by allowing you to record inspirations as they come. Whether it's a business idea, narrative, or comedic line, just say it.

### Accuracy

`say` is designed with the anticipation that another AI model will utilize its transcriptions.

Interaction with the LLM takes the front seat here, even over traditional readability. It's all about preserving the essence and intent of your words.

Not all words are created equal. Getting named entities right is crucial.

And don't forget those trendy new terms - `say` knows they're crucial to nail down.

### Latency

`say` allows you to transcribe your speech into text quickly. You can use the transcription right away for your purposes, such as writing, editing, or sharing.

`say` aims to keep the latency below one second. "[1.0 second is about the limit for the user's flow of thought to stay uninterrupted](https://www.nngroup.com/articles/response-times-3-important-limits/#:~:text=1.0%20second%20is%20about%20the%20limit%20for%20the%20user's%20flow%20of%20thought%20to%20stay%20uninterrupted)".

`say` isn't designed to provide real-time feedback. It aims to let you express your thoughts without interruption or distraction.

`say` has a user interface that is optimized to reduce the perceived latency.

### Budget

Most productivity tools designed for individual users have a lower price than $100/month. I wanted to keep `say` affordable.

A high price point can motivate you to use `say` more often. You might think that since you are paying a lot, you should make the most of it and "just say it".

## Architecture

`say` functions as a monolithic macOS background process. It's just a `brew install` away, eliminating the need for any additional devices.

The uniformity of the macOS ecosystem simplifies both development and testing.

By self-hosting `say`, you're enhancing your privacy.

## Name Overlap

This tool intentionally replaces the `say` command built-in with macOS.

If you run the default `say` command on macOS while this tool is active, it may interfere with the recording and transcription process.

I've chosen to keep the name of this tool as `say` to ensure a smooth installation experience.

I could have asked you to change your `.skhdrc` file to set up a shortcut for this tool. But that would be a hassle.

And I couldn't do it for you, because that would break [Homebrew's policy](https://docs.brew.sh/Homebrew-and-Python#:~:text=homebrew%20has%20a%20strict%20policy%20never%20to%20write%20stuff%20outside%20of%20the%20brew%20--prefix).

## API Key

I've chosen not to store the API key. This approach keeps the codebase simple and reduces potential security vulnerabilities.

Designed for continuous operation, `say` ideally requires the API key to be entered just once.

## Transcription

`say` taps into a powerful API that can transcribe speech accurately and fast. `say` doesn't reinvent the wheel.

To avoid contaminating the transcription with errors, `say` can drop low-confidence segments that the transcription system isn't sure how to transcribe and only retain the ones with high confidence.

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

From my experience, if the audio is 1 minute or less, the transcription API usually responds in under a second. But if the audio is 2 minutes or longer, the latency can extend to 1 second or more. This was tested on a North American gigabit connection and might vary.

There is a natural delay when you switch from talking to reading your transcript. This delay might offset any extra speech when you surpass 1 minute.

## Segmentation

### Line

Each sentence in `say` gets its own line. That way, you can easily move up and down with `j` and `k`.

### Paragraph

`say` starts a new paragraph when you request transcription. This helps you keep track of what you have already used and what you need next. You can jump to the latest chunk of text with `Shift + [` and `Shift + ]`.

### File

Daily segmentation speeds up loading times.

If you need weekly or monthly transcriptions, you can simply concatenate the daily transcriptions.

This also makes it easy to search for a specific date.

## Updating the transcript file

1. `say` makes a copy of the `DD.txt` file and creates a temporary file in the system's temp directory. This ensures that the application directory doesn't get cluttered.

1. Any updates made to the file will modify the temporary file, preserving the read-only nature of the original `DD.txt` file.

1. `say` uses the atomic `rename` operation to replace the original `DD.txt` file with the modified temporary file. This atomic operation ensures that the `DD.txt` file is always consistent.

## Data Retention

`say` doesn't keep a record of the audio once it has been transcribed.

The act of storing audio can amplify the observer effect, potentially making you more self-aware and uncomfortable.

By choosing not to store audio, `say` ensures that sounds like your snoring, farting, or moaning aren't kept on record.

Being arrested and having your own audio used against you in a court of law isn't a pleasant experience.

Even the sound of typing can sometimes be enough to decipher what you're typing. This becomes a security risk if you're entering sensitive information like passwords while speaking.

If you're interested in documenting everything, you might consider recording video. However, this comes with its own risks, such as accidentally capturing footage when you're nude.
