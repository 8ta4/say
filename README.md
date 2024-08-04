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

1. Hit `⌘ + Space` to open your launcher.

1. Type `say`.

1. Hit `Enter` when `say` pops up.

1. If asked, allow `say` to access your mic.

1. Copy a Deepgram API key from [their website](https://deepgram.com/).

1. Paste your API key when `say` asks for it.

## Cost

> What's the cost?

`say` is free to use, but it uses the Deepgram API, which might cost you some money. You can check out the prices on [Deepgram's pricing page](https://deepgram.com/pricing).

> Will I be charged all the time?

To save some bucks, `say` uses voice activity detection (VAD) to cut down on unnecessary usage. But VAD may not be flawless.

> Am I going to get arrested for using this tool?

Well, that depends on where you live. Different places have different laws about recording conversations. `say` is meant to help with accessibility, not to act as a recording device. But if someone misuses it, there could be legal trouble.

## Usage

> How do I access recent transcriptions?

Just press `⌘ + ;`. This will open the latest transcription file. If a new transcription is done, `say` will serve it up as soon as it's ready. But `say` won't bug you with any more transcriptions after that.

> Can I close the window and have the app running in the background?

Yup, you can. Closing the window with the close button, `⌘ + w`, or even `⌘ + q` won't stop `say`. It'll keep running in the background, capturing your voice.

> Can I quit the background process?

Technically you can force quit the background process. But `say` is meant to be running 24/7 to capture your voice. It'll try to restart itself if you quit it. If you really want to stop using `say`, the best way is to uninstall it and reboot your computer.

> What's the main source of latency?

Usually, it's the Deepgram API that slows things down.

Here are some hacks to speed it up:

- Stay in the continental US to reduce latency, as "[[a]ll Deepgram data is processed inside of the continental US.](https://help.deepgram.com/hc/en-us/articles/6126293557399-Data-Security-Privacy-FAQ#:~:text=all%20deepgram%20data%20is%20processed%20inside%20of%20the%20continental%20us.)"
- Get an internet connection with an upload bandwidth of at least 1 Gbps.

> Will my Mac automatically sleep while this tool is running?

Nope, your Mac stays awake when `say` is running, even on battery power. After all, `say`'s main gig is to capture voice round-the-clock. But hey, modern Macs have some serious battery stamina. Just find a plug when you can.

> Can I manually make my Mac sleep while this tool is running?

Absolutely! If you care more about saving battery, feel free to put your Mac to sleep.

> Does this tool filter out other voices?

Nah, `say` is an equal-opportunity listener.

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

> Which mic does this tool use by default?

`say` defaults to your built-in mic.

> How does the intensity of sound change as the mic is moved further away?

The sound intensity is inversely proportional to the square of the distance.

> When does this tool start using my external mic?

`say` will use your external mic if:

- You're not in a hideaway.
- You've specified an external mic to use outside of your hideaway.
- Your configured external mic is connected.

> How do I set up a hideaway?

1. Connect to the Wi-Fi or Ethernet network you want to use as your hideaway.

1. Hit `⌘ + Space` to open your launcher.

1. Type `say`.

1. Hit `Enter` when `say` pops up.

1. Click `ENABLE HIDEAWAY`.

> How do I set up an external mic for non-hideaway use?

1. Plug in the external mic you want to use.

1. Hit `⌘ + Space` to bring up your launcher.

1. Type `say`.

1. Hit `Enter` when `say` shows up. You'll see a list of mics.

1. Pick the external mic you want.

> Which mic does this tool use in a hideaway?

`say` sticks with your built-in mic in a hideaway.

> Will this tool use the built-in mic if there's no external mic and I'm not in a hideaway?

No. If you're not in a hideaway and there's no external mic, `say` won't use the built-in mic to avoid recording poor audio. You can either connect an external mic or mark your current location as a hideaway if you think the built-in mic is good enough.

> Can I enable multiple hideaways? (Planned)

Absolutely!

> How do I disable a hideaway?

1. Connect to the Wi-Fi or Ethernet network you want to disable as a hideaway.

2. Hit `⌘ + Space` to open your launcher.

3. Type `say`.

4. Hit `Enter` when `say` pops up.

5. Click `DISABLE HIDEAWAY` to turn off the hideaway for that network.

> Does this tool keep a record of the audio?

Nope, `say` doesn't store any audio after it's transcribed.

But it uses Deepgram as the transcription service. And Deepgram say they only hold "[audio data for as long as necessary](https://help.deepgram.com/hc/en-us/articles/6126293557399-Data-Security-Privacy-FAQ#:~:text=Deepgram%20holds%20audio%20data%20for%20as%20long%20as%20necessary)", but who knows what that means.

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

> What decides the transcript file, the start or end time of my audio?

`say` uses audio end time to pick the right file for your transcript. This makes it easier to find what you said today, even if you started talking before midnight.

> How does this tool segment each file?

`say` splits each file into handy chunks for easy navigation with your [Neovim plugin](https://github.com/vscode-neovim/vscode-neovim/blob/79beb2c83aaec45e87fcb543b78d8b3a7e8ff9e1/README.md#neovim-configuration).

`say` starts a new paragraph when you ask for transcription. This helps you keep track of what you have already used and what you need next. You can jump to the latest chunk of text with `Shift + [` and `Shift + ]`.

Each sentence in `say` gets its own line. That way, you can easily move up and down with `j` and `k`.

> Can I modify the transcript files?

`DD.txt` is designed to be read-only.

Dropbox or similar file syncing services might alter the file attribute and make it writable.

Technically, you could switch it to writable, make your edits, and then switch it back to read-only. But let's not go there.

If you want to annotate or edit something, copy the content to a new file and work on that.

You can tweak Visual Studio Code to [respect read-only file settings](https://code.visualstudio.com/docs/getstarted/settings#:~:text=//%20Marks%20files%20as%20read%2Donly%20when%20their%20file%20permissions%20indicate%20as%20such.%20This%20can%20be%20overridden%20via%20%60files.readonlyInclude%60%20and%20%60files.readonlyExclude%60%20settings.%0A%20%20%22files.readonlyFromPermissions%22%3A%20false%2C).

> How do I back up my transcripts?

You can back up the `~/.local/share/say` directory using your preferred method.

> How do I secure my transcripts?

You can protect your data using device-level encryption.
