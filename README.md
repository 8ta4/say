# say

## Just Say It

> What's the purpose of this tool?

`say` is always on, recording and transcribing your voice 24/7. Whenever inspiration strikes, just say it.

## Setup

> How do I set up this tool?

1. First off, you need Homebrew. If you don't have it already, you can find the instructions on the [Homebrew website](https://brew.sh/).

1. Once you've got Homebrew, enter this command in your terminal:

   ```sh
   brew install 8ta4/say/say
   ```

1. You can do launch `say` by entering:

   ```sh
   say
   ```

1. You'll need a Deepgram API key to use `say`. If you don't have one yet, you can get it from the Deepgram website.

1. When `say` prompts you for the API key, just paste it in.

## Cost

> What's the cost?

`say` is free to use, but keep in mind that it uses the Deepgram API, which might cost you some money. You can check out the prices on [Deepgram’s streaming pricing page](https://deepgram.com/pricing).

> Will I be charged all the time?

To help save money, `say` uses voice activity detection (VAD). This feature tries to cut down on unnecessary usage. But VAD may not be flawless.

## Usage

## Transcription

> How do I access recent transcriptions?

Accessing today's transcriptions is as simple as entering:

   ```sh
   say
   ```

> How are my transcriptions organized?

`say` organizes your transcripts in a specific directory structure to make it easy for you to locate your files. Here's how it works:

```
.local/share/say/
│
├── archive/
│   ├── YYYY
│   │   ├── MM
│   │   │   ├── DD
│   │   │   │   ├── read-only.txt
│   │   │   │   └── read-write.txt
│
└── live/
    ├── read-only.txt
    └── read-write.txt
```

> What is the `archive/` folder?

The `archive/` directory stores all your transcripts. Each day's transcript is organized by year (`YYYY`), month (`MM`), and day (`DD`). The transcript for today is saved every 10 minutes.

> What is the `live/` folder?

The `live/` directory contains the real-time transcript for today.

> Why are there `read-only.txt` and `read-write.txt` files?

The `read-only.txt` file is just that - read-only. While you technically can edit this file, it’s best not to.

Feel free to make edits in the `read-write.txt` file.

> How do I back up my transcripts?

You can consider backing up `archive/` directory. `say` gives you the freedom to choose your preferred backup or cloud storage method.

> How do I secure my transcripts?

You can protect your data using device-level encryption.

### Recording

> How accurate are the transcriptions?

`say` aims to capture your thoughts anytime, but common sense applies. If any human listener cannot understand you, `say` will probably struggle too.

> How can I improve transcription accuracy?

You might need to learn to verbalize every internal monologue and express yourself more coherently.

To avoid contaminating your recordings, you might want to consider living alone or avoiding playing any songs or movies that could interfere with your recording. If you are married, consider getting a divorce.

You can consider accent coaching, simpler diction, or avoiding complex or fancy vocabulary. This is similar to learning to touch-type to type faster.

Prolonged speaking can strain your voice, and continuous speaking may require voice training.
