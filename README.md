# say

## Just Say It

> What's the purpose of this tool?

`say` is always on, recording and transcribing your voice 24/7. Whenever inspiration strikes, just say it.

## Setup

> How do I set up this tool?

<!-- TODO: Clarify that Visual Studio Code needs to be installed using Homebrew or not installed yet -->

1. Make sure you're using a Mac with Apple silicon.

1. Be located in the continental US to minimize latency, as "[[a]ll Deepgram data is processed inside of the continental US](https://help.deepgram.com/hc/en-us/articles/6126293557399-Data-Security-Privacy-FAQ#:~:text=all%20deepgram%20data%20is%20processed%20inside%20of%20the%20continental%20us..)."

1. Check that you have an internet connection with an upload bandwidth of at least 1 Gbps.

1. Install [Homebrew](https://brew.sh/#install).

1. Enter this command in your terminal:

   ```sh
   brew install 8ta4/say/say
   ```

1. If you have already installed Visual Studio Code not using Homebrew, you need to [add it to your system's PATH](https://code.visualstudio.com/docs/setup/mac#_launching-from-the-command-line)..

1. Tweak Visual Studio Code to [respect read-only file settings](https://code.visualstudio.com/docs/getstarted/settings#:~:text=//%20Marks%20files%20as%20read%2Donly%20when%20their%20file%20permissions%20indicate%20as%20such.%20This%20can%20be%20overridden%20via%20%60files.readonlyInclude%60%20and%20%60files.readonlyExclude%60%20settings.%0A%20%20%22files.readonlyFromPermissions%22%3A%20false%2C).

1. Open `Finder`.

1. Go to `Applications`.

1. Control-click `say` and choose Open, as this will [bypass Apple's security](https://support.apple.com/guide/mac-help/open-a-mac-app-from-an-unidentified-developer-mh40616/mac) that might block you.

1. Head over to the [Deepgram website](https://deepgram.com/) and copy a Deepgram API key.

1. Add the API key by entering this command in your terminal:

   ```sh
   pbpaste > ~/.config/say/key
   ```

1. If asked, allow `say` to access your mic.

## Cost

> What's the cost?

`say` is free to use, but it uses the Deepgram API, which might cost you some money. You can check out the prices on [Deepgram's pricing page](https://deepgram.com/pricing).

> Will I be charged all the time?

To save some bucks, `say` uses voice activity detection (VAD) to cut down on unnecessary usage. But VAD may not be flawless.

## Usage

> How do I access recent transcriptions?

To access today's transcriptions, hit `⌘ + ;`.

> How are my transcriptions organized?

`say` organizes your transcripts in a specific directory structure:

```
 ~/.local/share/say/
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

You can back up the `~/.local/share/say/` directory using your preferred method.

> How do I secure my transcripts?

You can protect your data using device-level encryption.

> Does the tool filter out other voices?

No, `say` doesn't possess speaker identification capabilities. If there are multiple speakers, consider a fancy mic with:

- Noise cancellation
- Proximity to the mouth
- Wireless capability
- Ergonomic design
- Long battery life

However, if there is only one speaker, your built-in mic should do the trick, saving you from:

- Noise cancellation glitches
- Connectivity issues
- Discomfort
- Battery life concerns

Here are some pro tips:

- Practice verbalizing every internal monologue. It's like learning a new language!
- Keep your words simple and clear. It's like touch-typing for your voice.
- Consider accent coaching. No offense, but Standard American English is the most recognized. Yeah, it's accentist.
- Look into voice training. You don't want to lose your voice after a long chat, right?
- Try to avoid background noise like music or movies.
- Living alone could be an option... if you're really serious about clean recordings.
- And if all else fails... well, there's always divorce.

> How does the intensity of sound change as the microphone is moved further away?

The sound intensity is inversely proportional to the square of the distance.

> Does this tool keep a record of the audio?

Nope, `say` doesn't store any audio after it's transcribed.

But it uses Deepgram as the transcription service. And Deepgram say they only hold "[audio data for as long as necessary](https://help.deepgram.com/hc/en-us/articles/6126293557399-Data-Security-Privacy-FAQ#:~:text=Deepgram%20holds%20audio%20data%20for%20as%20long%20as%20necessary)", but who knows what that means.
