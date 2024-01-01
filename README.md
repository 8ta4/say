# say

## Just Say It

> What's the purpose of this tool?

`say` is always on, recording and transcribing your voice 24/7. Whenever inspiration strikes, just say it.

## Setup

> How do I set up this tool?

1. Make sure you're using a Mac with Apple silicon.

1. Install [Homebrew](https://brew.sh/#install).

1. Enter this command in your terminal:

   ```sh
   brew install 8ta4/say/say
   ```

1. If you have already installed Visual Studio Code not using Homebrew, [add it to your system's PATH](https://code.visualstudio.com/docs/setup/mac#_launching-from-the-command-line).

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

> What's the main source of latency?

Usually, it's the Deepgram API that slows things down.

Here are some hacks to speed it up:

- Stay in the continental US to reduce latency, as "[[a]ll Deepgram data is processed inside of the continental US.](https://help.deepgram.com/hc/en-us/articles/6126293557399-Data-Security-Privacy-FAQ#:~:text=all%20deepgram%20data%20is%20processed%20inside%20of%20the%20continental%20us.)"
- Get an internet connection with an upload bandwidth of at least 1 Gbps.

> Which mic does this tool use by default? (Planned)

`say` defaults to your built-in microphone. It's plug-and-play, no external mic needed.

If there are multiple speakers, consider a fancy mic with:

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

> When does this tool start using my external mic? (Planned)

When you're not in a hideaway and your external mic is plugged in, `say` jumps on it to reduce background noise.

> How do I set up a hideaway? (Planned)

1. Connect to the Wi-Fi or Ethernet network you want to use as your hideaway.

1. Hit `⌘ + Space` to open your launcher.

1. Type `say`.

1. Hit `Enter` when `say` pops up.

1. Click `Add the current network as a hideaway`.

> Which mic does this tool use in a hideaway? (Planned)

`say` sticks with your built-in mic in a hideaway.

> Will this tool use the built-in mic if there's no external mic and I'm not in a hideaway? (Planned)

No. If you're not in a hideaway and there's no external mic, `say` won't use the built-in mic to avoid recording poor audio. You can either connect an external mic or mark your current location as a hideaway if you think the built-in mic is good enough.

> Can I set up multiple hideaways? (Planned)

Yes, you can set up multiple network hideaways.

> Does this tool keep a record of the audio? (Planned)

Nope, `say` doesn't store any audio after it's transcribed.

But it uses Deepgram as the transcription service. And Deepgram say they only hold "[audio data for as long as necessary](https://help.deepgram.com/hc/en-us/articles/6126293557399-Data-Security-Privacy-FAQ#:~:text=Deepgram%20holds%20audio%20data%20for%20as%20long%20as%20necessary)", but who knows what that means.

> Does this tool filter out other voices by default?

Nah, `say` is an equal-opportunity listener. It captures all voices equally by default.

> When does this tool start ignoring other voices? (Planned)

Once you've registered your voice with `say`, it'll start tuning out other voices, unless you're chilling in a hideaway.

> How do I capture my voice sample? (Planned)

1. Find a quiet spot.

1. Hook up your external mic.

1. Hit `⌘ + Space` to open your launcher.

1. Type `say`.

1. Hit `Enter` when `say` pops up.

1. Click `Record a voice sample`.

1. Say something in your usual conversational style.

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

> How does this tool segment each file?

`say` splits each file into handy chunks for easy navigation with your [Neovim plugin](https://github.com/vscode-neovim/vscode-neovim/blob/79beb2c83aaec45e87fcb543b78d8b3a7e8ff9e1/README.md#neovim-configuration).

`say` starts a new paragraph when you ask for transcription. This helps you keep track of what you have already used and what you need next. You can jump to the latest chunk of text with `Shift + [` and `Shift + ]`.

Each sentence in `say` gets its own line. That way, you can easily move up and down with `j` and `k`.

> Can I modify the transcript files? (Planned)

`DD.txt` is read-only. Sure, you could edit it, but let's not go there.

If you want to annotate or edit something, copy the content to a new file and work on that.

You can tweak Visual Studio Code to [respect read-only file settings](https://code.visualstudio.com/docs/getstarted/settings#:~:text=//%20Marks%20files%20as%20read%2Donly%20when%20their%20file%20permissions%20indicate%20as%20such.%20This%20can%20be%20overridden%20via%20%60files.readonlyInclude%60%20and%20%60files.readonlyExclude%60%20settings.%0A%20%20%22files.readonlyFromPermissions%22%3A%20false%2C).

> How do I back up my transcripts?

You can back up the `~/.local/share/say` directory using your preferred method.

> How do I secure my transcripts?

You can protect your data using device-level encryption.
