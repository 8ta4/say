# say

## Goals

### 24/7

> What's wrong with writing or typing?

`say` is for those moments when note-taking is a hassle, like when you're busy, when you're in bed, or when you're busy in bed.

Writing or typing can be slow. You might lose your train of thought.

It's automatic, so say goodbye to keeping a journal.

> What's wrong with other dictation apps?

Most dictation apps require you to press a button or say a command before you start speaking. That can interrupt your flow.

`say` is always on. Whether it's a business idea or comedic line, just say it.

> Isn't recording all the time a privacy concern?

Absolutely. That's why `say` is designed for home use. Just be aware it might pick up others' voices if they're around.

### Accuracy

> Is there a Word Error Rate goal?

Word Error Rate can be misleading. It counts every mistake equally, regardless of how significant or trivial it is.

Interaction with Large Language Models (LLMs) takes the front seat here. `say` is built with the expectation that LLMs will use its transcripts, even if it means sacrificing readability.

What matters is whether LLMs can understand what you are saying and respond accordingly.

Not all words are created equal. Getting named entities and trendy terms right is crucial.

### Latency

> How low is the latency goal?

`say` is shooting for sub-one-second latency.

> Why keep latency below one second?

"[1.0 second is about the limit for the user's flow of thought to stay uninterrupted](https://www.nngroup.com/articles/response-times-3-important-limits/#:~:text=1.0%20second%20is%20about%20the%20limit%20for%20the%20user's%20flow%20of%20thought%20to%20stay%20uninterrupted)".

With `say`, you can turn speech into text in a flash, ready for whatever you need it for - writing, editing, sharing.

`say`'s user interface is optimized to reduce perceived latency.

> Why not aim for latency below 0.1 seconds?

"[[D]uring delays of more than 0.1 but less than 1.0 second... the user does lose the feeling of operating directly on the data](https://www.nngroup.com/articles/response-times-3-important-limits/#:~:text=during%20delays%20of%20more%20than%200.1%20but%20less%20than%201.0%20second%2C%20but%20the%20user%20does%20lose%20the%20feeling%20of%20operating%20directly%20on%20the%20data)".

But that's OK. Operating directly on the data can be distracting. You can focus on speaking now and working on the data later.

### Budget

> How much does say cost per month?

`say` is aiming for less than $100/month because that's the sweet spot for most personal productivity tools.

Some folks might say that a higher price makes you use the tool more. You know, the whole sunk cost fallacy thing. But let's just say it: that's just a trick to get you to spend more money.

## Setup

> How do I set up this tool's dev environment?

1. Follow the [setup steps](https://github.com/8ta4/say?tab=readme-ov-file#setup) in the README.md.

1. Install [devenv](https://github.com/cachix/devenv/blob/dad24416b1ca61a5551db0de863b047765ac0454/docs/getting-started.md#installation).

1. Install [direnv](https://github.com/cachix/devenv/blob/dad24416b1ca61a5551db0de863b047765ac0454/docs/automatic-shell-activation.md#installing-direnv).

1. Run the following commands:

   ```sh
   git clone git@github.com:8ta4/say.git
   cd say
   direnv allow
   ```

The `devenv.nix` file has got all the scripts you need.

## Architecture

> Why not just let a cloud server handle the transcript API calls?

`say` is a macOS background process. This design reduces cloud dependency and gives your privacy a boost.

Plus, it's a breeze to develop and test `say` using Mac.

> Why not use a dedicated recording device?

I wanted to make `say` easy to try out.

> Why not a standalone iPhone app?

Because iOS won't let you do continuous real-time voice detection and transcription in the background, as [iOS's general-purpose background execution has certain limitations](https://developer.apple.com/forums/thread/685525#:~:text=iOS%20puts%20strict,or%20IPC%20request).

## Functionality

### Background Process

> Where does this tool store the API key?

The API key is in `~/.config/say/key`.

> Why not `Application Support`?

`~/.config` is the standard config folder for Unix systems. It's easier to access from the command line.

> When does this tool check if my API key is legit? (Planned)

`say` checks the validity of the API key every time you change it. This way, you get instant feedback if something is wrong.

> Is the API key encrypted?

Nope. It's plain text. This way, you can easily update it as needed.

> Does this tool run automatically after a system reboot? (Planned)

Yes, `say` uses macOS Launch Agents to start itself automatically.

> What happens if this tool stops unexpectedly? (Planned)

`say` also uses macOS Launch Agents to restart itself automatically.

> Why not use Login Items?

Login Items only work when you log in. They won't help you if `say` stops working for some reason.

### Recording

> Why doesn't this tool store my audio after transcribing? (Planned)

Storing audio can amplify the observer effect, potentially making you more self-conscious.

By choosing not to store audio, `say` ensures that sounds like your snoring, farting, or moaning aren't kept on record.

Being arrested and having your own audio used against you in court isn't a pleasant experience.

Even the sound of typing can sometimes reveal what you're typing. This is risky if you're entering sensitive information like passwords while speaking.

If you want to document everything, you might consider recording video. But this has its own risks, such as accidentally capturing yourself naked.

> So, does this tool store any audio at all?

Yep, `say` does temporarily store audio, but it uses the Opus format.

> Why Opus and not MP3?

Opus is pretty cool because it allows real-time compression. MP3 needs the whole audio file to get the best encoding.

Plus, Opus gives you a small file size but still keeps the quality high.

For MP3, it's recommended to use 128 kbps for [audiobooks](<https://support.google.com/books/partner/answer/7504302#file-formats:~:text=mp3%20(cbr%20preferred)%2C%20%3E%3D128kbps%20(mono)>)/[podcasts](https://learn.acast.com/en/articles/3505536-which-audio-file-format-should-i-use-for-my-podcast#sharing:~:text=we%20recommend%20uploading%20mp3%20files%20with%20a%20bitrate%20of%20128kbps.). But if you're using Opus, you can get away with just 24 Kbps for [audiobooks/podcasts](https://wiki.xiph.org/Opus_Recommended_Settings#Recommended_Bitrates:~:text=down%20this%20page.-,Audiobooks%20%2F%20Podcasts,24,-Bitrates%20from%20here).

> How many times larger is a 128 kbps MP3 file than a 24 kbps Opus file?

It's about 5.3333 times larger.

$$\frac{128\text{ kbps}}{24\text{ kbps}} \approx 5.3333$$

> Is latency linearly proportional to file size?

No, latency does not increase linearly with file size. TCP slow start gradually increases data transmission rate. So, even though MP3 file is larger, the actual latency does not increase proportionally.

> What sample rate is used?

`say` uses a sample rate of 16 kHz. Google recommends [a sample rate of at least 16 kHz in the audio files that you use for transcription](https://cloud.google.com/speech-to-text/docs/optimizing-audio-files-for-speech-to-text#sample_rate_frequency_range:~:text=We%20recommend%20a%20sample%20rate%20of%20at%20least%2016%20kHz%20in%20the%20audio%20files%20that%20you%20use%20for%20transcription%20with%20Speech%2Dto%2DText.).

> Does this tool pad voice activity?

Absolutely! `say` adds a 1.5-second padding to make sure it captures everything.

A padding of $\frac{1536}{16000}$ seconds was too short. `say` was missing the start of the speech, resulting in incomplete transcriptions.

For comparison, [Silero VAD uses an even shorter padding of 30 milliseconds](https://github.com/snakers4/silero-vad/blob/94504ece54c8caeebb808410b08ae55ee82dba82/utils_vad.py#L210-L211).

The padding duration matches the 1.5-second pause. This consistency helps `say` capture natural speech patterns, including meaningful pauses.

The 1.5 -second pause was statistically derived from storytelling and interview pause durations.

The choice of a 1.5-second pause was statistically derived from storytelling and interview pause durations. [The mean pause duration in storytelling is 0.94 seconds (standard deviation: 0.23 seconds; sample size: 437), while in interviews it's 0.53 seconds (standard deviation: 0.06 seconds; sample size: 69)](https://www.researchgate.net/profile/Richard-Wiese-2/publication/257239931_The_Use_of_Time_in_Storytelling/links/00b4952d7b579ab2ac000000/The-Use-of-Time-in-Storytelling.pdf#page=11).

I chose the storytelling pause duration.

> Why was storytelling chosen?

I chose storytelling because its pause duration is longer, and I wanted to err on the side of caution to avoid cutting off mid-sentence.

I used this nifty [script](https://stackoverflow.com/a/63706587/21856904) to compute a one-sided tolerance interval with 99% coverage and a 99% confidence level.

> Why not use a confidence interval?

Confidence intervals are great for estimating the mean, but a tolerance interval covers a specified proportion of the population, which is better for making sure `say` captures most of your speech accurately.

> Why 99% and not 95%?

I want to be safe and avoid cutting off your words in the middle.

### Hideaway (Planned)

> How does this tool know when I'm at my hideaway?

`say` uses your network router's MAC address to pinpoint your hideaway.

> Why doesn't this tool use Location Services?

Location Services often need an internet connection to work accurately indoors. But if you're online, `say` can just use your network info to confirm your hideaway.

### Trigger

> Why does this tool use `⌘ + ;` to trigger transcription?

`⌘` is the easiest modifier to reach, and `;` is the only home position key that doesn't clash with major shortcuts.

For example, `⌘ + ;` conflicts with the next spelling and grammar check, which is not very handy without a shortcut to go back.

Also, `⌘ + ;` conflicts with Excel's time insertion, but that's not very common.

> Can this tool just start transcribing on its own?

Absolutely!

> When does this tool start transcribing automatically?

The automatic trigger is based on voice activity. `say` listens to you and waits for a pause before it starts transcribing. It's all thanks to the Silero VAD's ONNX model that detects your voice activity.

> Why does this tool wait for a pause?

Waiting for a pause helps capture your whole thought and avoid cutting off mid-sentence.

To be more specific, `say` waits for 1 minute of untranscribed speech and then looks for a 1.5 second pause.

This 1-minute strategy strikes a balance between accuracy and latency.

Longer speech gives more context and improves accuracy.

But I also want to keep things snappy with the manual trigger aiming for sub-second latency.

From my experience, if the audio is 1 minute or less, the transcription API usually responds in under a second. But if the audio is 2 minutes or longer, the latency can extend to 1 second or more. And that's on a North American gigabit connection.

> Why use ONNX instead of PyTorch?

Speed, my friend! The ONNX model [may even run up to 4-5x faster](https://github.com/snakers4/silero-vad/blob/5e7ee10ee065ab2b98751dd82b28e3c6360e19aa/README.md?plain=1#L37), which means less CPU usage. That's awesome for `say`, since it's always on.

> Can another transcription be triggered while one is already in progress? (Planned)

Absolutely! The likelihood depends on whether the second trigger is manual or automatic.

> If transcription is already happening, is overlap more likely with a manual or automatic second trigger? (Planned)

Overlap is more likely with a manual second trigger.

The automatic trigger is designed to kick in after it accumulates 1 minute of untranscribed speech. Usually, the transcription is done fast, keeping the latency under one second. This quick turnaround makes it pretty unlikely for an automatic second trigger to overlap with the previous trigger.

But a manual trigger can be activated anytime, so it's more likely to overlap.

### Transcription

> Why not do transcription locally?

`say` uses a powerful API that can transcribe speech cheap, fast, and accurately. Deepgram seems to be the best one right now.

With [Deepgram's Pay-as-You-Go at $0.0043/min](https://deepgram.com/pricing#:~:text=Nova%2D2,%240.0043/min), here's a quick math:

An average month has about 30.44 days.

$$\frac{100\text{ dollar/month}}{0.0043\text{ dollar/min} \times 30.44\text{ d/month} \times 60\text{ min/h}} \approx 12.73\text{ h}$$

So, you can transcribe roughly 12.73 hours of voice activity daily.

> Does this tool send any previous speech to the API for context?

`say` doesn't send any previous speech to the API for context. Sending partial context may not help much with accuracy. Sending full context may help, but it may also increase cost, latency, and complexity.

> Why doesn't this tool use streaming transcription?

Streaming transcription seems less accurate and more expensive.

> Can transcription API calls ever fail?

Absolutely, API calls can fail. The usual suspects are network issues.

Error notification depends on whether transcription is triggered manually or automatically.

> Will I get notified if transcription fails when it is triggered manually?

Yup, you will.

> Will I get notified if transcription fails when it is triggered automatically? (Planned)

Nope, you won't. `say` is designed to be non-intrusive.

> Does this tool retry if a transcription API call fails? (Planned)

Yes, indeed! `say` will give it another shot if a transcription API call fails. If any future transcription API call succeeds, it triggers a retry.

### Transcript

> Why does each day gets its own file?

This makes it easy to search for a specific date.

If you need weekly or monthly transcriptions, you can simply concatenate the daily transcriptions.

> What permissions are assigned to each transcription file? (Planned)

Each transcription file is assigned `-r--------` permissions. Only you can read them.

> What permissions are assigned to each transcription directory? (Planned)

Each transcription directory is assigned `drwx------` permissions. Only you can access it.

> How does this tool keep its transcript read-only when it updates it? (Planned)

1. `say` makes a copy of the `DD.txt` file and creates a temporary file in the system's temp directory. This ensures that the application directory doesn't get cluttered.

1. Any updates made to the file will modify the temporary file, preserving the read-only nature of the original `DD.txt` file.

1. `say` uses the atomic `rename` operation to replace the original `DD.txt` file with the modified temporary file. This atomic operation ensures that the `DD.txt` file is always consistent.

> Will my transcript automatically refresh when the transcription is auto-triggered?

Absolutely! Visual Studio Code has this nifty [auto-reload feature](https://stackoverflow.com/questions/30078077/visual-studio-code-auto-refresh-file-changes#:~:text=vscode%20will%20never%20refresh%20the%20file%20if%20you%20have%20changes%20in%20that%20file%20that%20are%20not%20saved%20to%20disk.%20however%2C%20if%20the%20file%20is%20open%20and%20does%20not%20have%20changes%2C%20it%20will%20replace%20with%20the%20changes%20on%20disk%2C%20that%20is%20true.)

> Does this tool do application-level encryption?

No. Encryption would mean dealing with decryption keys or passphrases, and that's no fun.

If you lose your key or something goes wrong with the encryption, you might lose your data.

Plus, I want you to be able to open your transcripts with any tool you like.

But you can still use device-level encryption if you want.
