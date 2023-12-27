# say

## Just Say It

> What's the purpose of this tool?

`say` is always on, recording and transcribing your voice 24/7. Whenever inspiration strikes, just say it.

## Setup

> How do I set up this tool?

1. Make sure you're using a Mac with Apple silicon.

1. Be located in the continental US to minimize latency, as "[[a]ll Deepgram data is processed inside of the continental US](https://help.deepgram.com/hc/en-us/articles/6126293557399-Data-Security-Privacy-FAQ#:~:text=all%20deepgram%20data%20is%20processed%20inside%20of%20the%20continental%20us..)."

1. Check that you have an internet connection with an upload bandwidth of at least 1 Gbps.

1. Install [Homebrew](https://brew.sh/#install).

1. Enter this command in your terminal:

   ```sh
   brew install 8ta4/say/say
   ```

1. If you have already installed Visual Studio Code not using Homebrew, [add it to your system's PATH](https://code.visualstudio.com/docs/setup/mac#_launching-from-the-command-line).

1. Tweak Visual Studio Code to [respect read-only file settings](https://code.visualstudio.com/docs/getstarted/settings#:~:text=//%20Marks%20files%20as%20read%2Donly%20when%20their%20file%20permissions%20indicate%20as%20such.%20This%20can%20be%20overridden%20via%20%60files.readonlyInclude%60%20and%20%60files.readonlyExclude%60%20settings.%0A%20%20%22files.readonlyFromPermissions%22%3A%20false%2C).

1. Head over to the [Deepgram website](https://deepgram.com/) and copy a Deepgram API key.

1. Paste your API key in `~/.config/say/key`.

1. Hit `⌘ + Space` to open your launcher.

1. Type `say`.

1. Hit `Enter` when `say` pops up.

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
 ~/.local/share/say
│
├── YYYY
│   ├── MM
│   │   ├── DD.txt
```

Each day's transcript is organized by year (`YYYY`), month (`MM`), and day (`DD`).

> Can I modify the transcript files? (Planned)

`DD.txt` is read-only. Sure, you could edit it, but let's not go there.

If you want to annotate or edit something, copy the content to a new file and work on that.

> How do I back up my transcripts?

You can back up the `~/.local/share/say` directory using your preferred method.

> How do I secure my transcripts?

You can protect your data using device-level encryption.

> Does the tool filter out other voices?

No, `say` doesn't possess speaker identification capabilities. If there are multiple speakers, consider a fancy mic with:

- Noise cancellation
- Proximity to the mouth
- Wireless capability
- Comfort fit
- Long battery life
- Secure grip

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
- Wear headphones or earphones when you listen to audio.
- Living alone could be an option... if you're really serious about clean recordings.
- And if all else fails... well, there's always divorce.

> How does the intensity of sound change as the microphone is moved further away?

The sound intensity is inversely proportional to the square of the distance.

> Which mic does this tool use by default? (Planned)

`say` defaults to your built-in microphone. It's plug-and-play, no external mic needed.

> How do I switch mics? (Planned)

To switch mics, set up a hideaway. If you're not in a hideaway, `say` will try to use an external mic to cut down on background noise.

> Which mic does `say` use in a hideaway? (Planned)

`say` sticks with your built-in mic in a hideaway.

> Will this tool use the built-in mic if there's no external mic and I'm not in a hideaway? (Planned)

No. If you're not in a hideaway and there's no external mic, `say` won't use the built-in mic to avoid recording poor audio. You can either connect an external mic or mark your current location as a hideaway if you think the built-in mic is good enough.

> Can I set up multiple hideaways? (Planned)

Yes, you can set up multiple network hideaways.

> Does this tool keep a record of the audio? (Planned)

Nope, `say` doesn't store any audio after it's transcribed.

But it uses Deepgram as the transcription service. And Deepgram say they only hold "[audio data for as long as necessary](https://help.deepgram.com/hc/en-us/articles/6126293557399-Data-Security-Privacy-FAQ#:~:text=Deepgram%20holds%20audio%20data%20for%20as%20long%20as%20necessary)", but who knows what that means.
