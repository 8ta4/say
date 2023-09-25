# say

## Just Say It

> What's the purpose of this tool?

`say` is always on, recording and transcribing your voice 24/7. Whenever inspiration strikes, just say it.

## Setup

> How do I set up this tool?

`say` might configure some third-party tools during its initial run. Just keep an eye on the log if you're curious.

1. Make sure you're using a Mac.

1. Be somewhere in North America.

1. Check that you have an internet connection with an upload bandwidth of at least 1 Gbps.

1. Grab [Homebrew](https://brew.sh/#install).

1. Enter this command in your terminal:

   ```sh
   brew install 8ta4/say/say
   ```

1. To launch `say`, type:

   ```sh
   say
   ```

1. Head over to the [Deepgram website](https://deepgram.com/) and snag a Deepgram API key.

1. When `say` asks for the API key, paste it in. 

## Cost

> What's the cost?

`say` is free to use, but it uses the Deepgram API, which might cost you some money. You can check out the prices on [Deepgram’s pricing page](https://deepgram.com/pricing).

> Will I be charged all the time?

To save some bucks, `say` uses voice activity detection (VAD) to cut down on unnecessary usage. But VAD may not be flawless.

## Usage

> How do I access recent transcriptions?

To access today's transcriptions, hit `Shift + Space`.

Think of it like this: "Say", "Shift", "Space" all start with "S". Just like you hit space at the end of a sentence, `Shift + Space` signals the end of your thought.

> How are my transcriptions organized?

`say` organizes your transcripts in a specific directory structure:

```
.local/share/say/
│
├── YYYY
│   ├── MM
│   │   ├── DD.txt
```

Each day's transcript is organized by year (`YYYY`), month (`MM`), and day (`DD`).

> Can I modify the transcript files?

`DD.txt` is read-only. Sure, you could edit it, but let's not go there.

If you want to annotate or edit something, copy the content to a new file and work on that.

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
