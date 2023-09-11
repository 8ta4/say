# say

## Goals

### 24/7

`say` is a tool designed for capturing thoughts and ideas through continuous voice recording. It's a personal reflection-focused tool that avoids any potential legal complications.

Voice is an efficient medium for brain-to-computer communication. However, it may require some adaptation in your thought process. You might need to learn to verbalize every internal monologue and express yourself more coherently.

Prolonged speaking can strain your voice, and continuous speaking may require voice training. However, future technologies, such as lip reading, could potentially offer more efficient solutions.

`say` allows you to capture your thoughts in situations where traditional note-taking isn't feasible, such as when you're in bed or when your hands are occupied.

To avoid contaminating your recordings, you might want to consider living alone or avoiding playing any songs or movies that could interfere with your recording. If you are married, consider getting a divorce.

Journaling can have therapeutic effects, and `say` automates this process, potentially providing catharsis.

`say` can boost your creativity and productivity by allowing you to record inspirations as they come. Whether it's a business idea, narrative, or comedic line, just say it.

### Accuracy

`say` aims to provide high-quality transcription for any user, but there are some limitations and challenges.

One of the challenges is dealing with low-confidence segments that the transcription system is not sure how to transcribe. To avoid contaminating the transcription with errors, `say` can drop these segments and only retain the ones with high confidence.

Another challenge is handling different accents and dialects. Currently, the most recognized accent is Standard American English, because it has the most data available for training the transcription system. This is a form of accentism.

To improve accuracy, users with non-standard accents can consider accent coaching, simpler diction, or avoiding complex or fancy vocabulary. This is similar to learning to touch-type to type faster. Technology will hopefully improve over time, and this adaptation may become unnecessary.

`say` aims to capture your thoughts anytime and anywhere, but common sense applies. If any human listener cannot understand you, `say` will probably struggle too.

### Latency

`say` allows you to transcribe your speech into text quickly. You can use the transcription right away for your purposes, such as writing, editing, or sharing.

`say` is not designed to provide real-time feedback. It aims to let you express your thoughts without interruption or distraction.

To achieve fast transcription, `say` uses some existing APIs that offer low-latency transcription and topic segmentation. `say` does not need to create its own algorithms for these tasks.

`say` has a user interface that is optimized to reduce the perceived latency.

### Budget

Most productivity tools designed for individual users have a lower price than $100/month. I wanted to keep `say` affordable.

A high price point can motivate you to use `say` more often. You might think that since you are paying a lot, you should make the most of it and "just say it".

## Setup

`say` functions as a monolithic macOS background process and also as a Neovim plugin. It's just a `brew install` away, eliminating the need for any additional devices.

The uniformity of the macOS ecosystem simplifies both development and testing.

By self-hosting `say`, you're enhancing your privacy.

## Voice Activity Detection

To make the most of your storage space, `say` uses voice activity detection to eliminate silent periods.

## Transcription

The Interim Transcript (IT) is a real-time draft of ongoing speech.

Eventually, the IT will be transformed into the Final Transcript (FT).

## Manual Edit

The Manual Edit (ME) feature lets you make changes to the transcript.

ME doesn't affect the IT. The start of the IT is marked by Neovim's extmark. Any changes you make before this mark will be saved, while any changes made after will not be saved. If you delete the extmark, the ME will not be saved.

## Segmentation

### Line

`say` displays each sentence on a separate line, a feature known as Automatic Line Segmentation (ALS).

You can navigate through sentences using `j` and `k`.

`say` prioritizes interaction with the LLM over traditional readability for human readers.

### Paragraph

`say` analyzes topics and divides the last paragraph. When the subject matter of the text changes, `say` creates a new paragraph. This feature is known as Automatic Paragraph Segmentation (APS).

You can move between paragraphs using `{` and `}`.

If the last paragraph is one or two sentences long, APS doesn't activate because you can navigate it using `j` and `k`. APS only activates if the paragraph is three sentences long or longer.

APS uses sentence embedding to split paragraphs. It identifies potential paragraph boundaries using sentence boundaries and then calculates the similarity of two new potential paragraphs using averaged embedding.

Each sentence's embedding is calculated and cached because calculating sentence embedding is computationally expensive.

### File

Daily segmentation speeds up loading times when using Neovim.

You can easily access recent transcriptions by opening "today".

If you need weekly or monthly transcriptions, you can simply concatenate the daily transcriptions.

This also makes it easy to search for a specific date.

## Interaction

FT or ME triggers ALS. Once ALS is activated, it then triggers APS.

If a new FT or ME is introduced, any pending ALS and APS are cancelled because the previous ALS and APS may have been based on an outdated last paragraph.

|     | IT                                             | FT                                             | ALS                                                       | APS                                                       | ME                                                     |
| --- | ---------------------------------------------- | ---------------------------------------------- | --------------------------------------------------------- | --------------------------------------------------------- | ------------------------------------------------------ |
| IT  | Replace IT                                     | Replace IT                                     | Apply ALS excluding IT                                    | Apply APS excluding IT                                    | Apply ME only to the section finalized before ME began |
| FT  | Append IT                                      | Append FT                                      | Apply ALS                                                 | Not Applicable (New FT or ME cancels pending ALS and APS) | Apply ME only to the section finalized before ME began |
| ALS | If IT exists, replace IT; otherwise, append IT | If IT exists, replace IT; otherwise, append FT | Not Applicable (New FT or ME cancels pending ALS and APS) | Apply APS excluding IT                                    | Apply ME only to the section finalized before ME began |
| APS | If IT exists, replace IT; otherwise, append IT | If IT exists, replace IT; otherwise, append FT | Not Applicable (New FT or ME cancels pending ALS and APS) | Not Applicable (New FT or ME cancels pending ALS and APS) | Apply ME only to the section finalized before ME began |
| ME  | If IT exists, replace IT; otherwise, append IT | If IT exists, replace IT; otherwise, append FT | Apply ALS excluding IT                                    | Not Applicable (New FT or ME cancels pending ALS and APS) | Apply ME only to the section finalized before ME began |

## Storage

`say` prioritizes user control, so your recordings and transcriptions will be stored locally on your desktop.

## Encryption

We encourage you to use device-level encryption to protect your data.

## Backup

`say` gives you the freedom to choose your preferred backup or cloud storage method.
