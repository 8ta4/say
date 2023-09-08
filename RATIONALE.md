# Rationale

## Just Say It

Whether you want to capture a fleeting thought, a raw emotion, or an unfiltered idea, just say it. Chronicle every essence of who you are with `say`.

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

## Hardware

### Ubiquity

Chances are, you have an old phone lying around that you can use to test `say`. If not, you can always use your current device.

### Connectivity

Your mobile phone is equipped with Wi-Fi, which allows `say` to automatically transfer your recordings.

### Setup

You're already familiar with how to use your phone, so getting started is easy.

### Cost

Acquiring a second-hand phone to use with `say` is an inexpensive option.

### Portability

While `say` is primarily designed to be stationary and plugged in, it's easy to move it.

## Software

### Segmentation

#### File

Daily segmentation speeds up loading times when using Neovim.

You can easily access recent transcriptions by opening "today".

If you need weekly or monthly transcriptions, you can simply concatenate the daily transcriptions.

This also makes it easy to search for a specific date.

#### Paragraph

`say` uses topic segmentation to divide the text. When the subject matter of the text changes, `say` creates a new paragraph.

You can move between paragraphs in Neovim by using its keyboard shortcut.

#### Line

`say` displays each sentence on a separate line.

You can navigate through sentences using Neovim's keyboard shortcuts.

`say` doesn't prioritize readability for human readers.

Instead, `say` is designed for interaction with the LLM, which doesn't require traditional readability structures.

### Local Storage

`say` prioritizes user control, so your recordings and transcriptions will be stored locally on your desktop.

### Backup

`say` gives you the freedom to choose your preferred backup or cloud storage method.

### Encryption

We encourage you to use device-level encryption to protect your data.

### Voice Activity Detection

To make the most of your storage space, `say` uses voice activity detection to eliminate silent periods.

