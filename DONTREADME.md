# say

## Goals

### 24/7

`say` is a tool designed for capturing thoughts and ideas through continuous voice recording. It's a personal reflection-focused tool that avoids any potential legal complications.

Voice is an efficient medium for brain-to-computer communication.

`say` allows you to capture your thoughts in situations where traditional note-taking isn't feasible, such as when you're in bed or when your hands are occupied.

Journaling can have therapeutic effects, and `say` automates this process, potentially providing catharsis.

`say` can boost your creativity and productivity by allowing you to record inspirations as they come. Whether it's a business idea, narrative, or comedic line, just say it.

### Latency

`say` allows you to transcribe your speech into text quickly. You can use the transcription right away for your purposes, such as writing, editing, or sharing.

`say` boasts sub-second latency. To put it in perspective, Jacob Nielsen says "[1.0 second is about the limit for the user's flow of thought to stay uninterrupted](https://www.nngroup.com/articles/response-times-3-important-limits/#:~:text=1.0%20second%20is%20about%20the%20limit%20for%20the%20user's%20flow%20of%20thought%20to%20stay%20uninterrupted)".

`say` isn't designed to provide real-time feedback. It aims to let you express your thoughts without interruption or distraction.

To achieve fast transcription, `say` uses some existing APIs that offer low-latency transcription and topic segmentation. `say` doesn't need to create its own algorithms for these tasks.

`say` has a user interface that is optimized to reduce the perceived latency.

### Budget

Most productivity tools designed for individual users have a lower price than $100/month. I wanted to keep `say` affordable.

A high price point can motivate you to use `say` more often. You might think that since you are paying a lot, you should make the most of it and "just say it".

## Architecture

`say` functions as a monolithic macOS background process and also as a Neovim plugin. It's just a `brew install` away, eliminating the need for any additional devices.

The uniformity of the macOS ecosystem simplifies both development and testing.

By self-hosting `say`, you're enhancing your privacy.

## API Key

I've chosen not to store the API key. This approach keeps the codebase simple and reduces potential security vulnerabilities.

Designed for continuous operation, `say` ideally requires the API key to be entered just once.

## Name Overlap

This tool intentionally replaces the `say` command built-in with macOS.

If you run the default `say` command on macOS while this tool is active, it may interfere with the recording and transcription process.

I've chosen to keep the name of this tool as `say` to ensure a smooth installation and usage experience.

## Transcription

To avoid contaminating the transcription with errors, `say` can drop low-confidence segments that the transcription system isn't sure how to transcribe and only retain the ones with high confidence.

## Segmentation

### Line

`say` displays each sentence on a separate line.

`say` prioritizes interaction with the LLM over traditional readability for human readers.

### Paragraph

The reason `say` starts a new paragraph when you request transcription is to make it easier for you to access the most recent portion of your content. This way, you can separate the previous content that you have already used from the new content that you need.

It's also consistent. You can expect that every time you request transcription, a new paragraph will begin.

### File

Daily segmentation speeds up loading times when using Neovim.

If you need weekly or monthly transcriptions, you can simply concatenate the daily transcriptions.

This also makes it easy to search for a specific date.

## Updating `read-only.txt`

1. `say` makes a copy of the `read-only.txt` file and creates a temporary file in the system's temp directory. This ensures that the application directory doesn't get cluttered.

2. Any updates made to the file will modify the temporary file, preserving the read-only nature of the original `read-only.txt` file.

3. `say` uses the atomic `rename` operation to replace the original `read-only.txt` file with the modified temporary file. This atomic operation ensures that the `read-only.txt` file is always consistent.

## Data Retention

`say` doesn't keep a record of the audio once it has been transcribed.

The act of storing audio can amplify the observer effect, potentially making you more self-aware and uncomfortable.

By choosing not to store audio, `say` ensures that sounds like your snoring, farting, or moaning aren't kept on record.

Being arrested and having your own audio used against you in a court of law isn't a pleasant experience.

Even the sound of typing can sometimes be enough to decipher what you're typing. This becomes a security risk if you're entering sensitive information like passwords while speaking.

If you're interested in documenting everything, you might consider recording video. However, this comes with its own risks, such as accidentally capturing footage when you're nude.
