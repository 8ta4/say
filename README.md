# say

## Just Say It

> What's the purpose of this tool?

`say` is always on, recording and transcribing your voice 24/7. Whenever inspiration strikes, just say it.

## Setup

> How do I set up this tool?

1. First off, you need Homebrew. If you don't have it already, check out the [Homebrew website](https://brew.sh/).

1. Once you've got Homebrew, enter this command in your terminal:

   ```sh
   brew install 8ta4/say/say
   ```

1. To launch `say`, type:

   ```sh
   say
   ```

1. You'll need a Deepgram API key. If you don't have one yet, you can get it from the Deepgram website.

1. When `say` asks for the API key, paste it in.

## Cost

> What's the cost?

`say` is free to use, but it uses the Deepgram API, which might cost you some money. You can check out the prices on [Deepgram’s pricing page](https://deepgram.com/pricing).

> Will I be charged all the time?

To save some bucks, `say` uses voice activity detection (VAD) to cut down on unnecessary usage. But VAD may not be flawless.

## Usage

> How do I access recent transcriptions?

To access today's transcriptions, type:

   ```sh
   say
   ```

> How are my transcriptions organized?

`say` organizes your transcripts in a specific directory structure:

```
.local/share/say/
│
├── YYYY
│   ├── MM
│   │   ├── DD
│   │   │   ├── read-only.txt
│   │   │   └── read-write.txt
```

Each day's transcript is organized by year (`YYYY`), month (`MM`), and day (`DD`).

> Why are there `read-only.txt` and `read-write.txt` files?

The `read-only.txt` file is just that - read-only. While you technically can edit this file, it’s best not to.

Feel free to edit the `read-write.txt` file.

> How do I back up my transcripts?

You can back up the `.local/share/say/` directory using your preferred backup or cloud storage method.

> How do I secure my transcripts?

You can protect your data using device-level encryption.

> How accurate are the transcriptions?

`say` aims to capture your thoughts anytime, but it's not magic. If a human can't understand you, `say` will probably struggle too.

Here are some pro tips:
- Practice verbalizing every internal monologue. It's like learning a new language!
- Keep your words simple and clear. It's like touch-typing for your voice.
- Consider accent coaching. No offense, but Standard American English is the most recognized. Yeah, it's accentist.
- Look into voice training. You don't want to lose your voice after a long chat, right?
- Try to avoid background noise like music or movies.
- Living alone could be an option... if you're really serious about clean recordings.
- And if all else fails... well, there's always divorce.
