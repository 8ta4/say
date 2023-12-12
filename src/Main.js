// https://www.electronjs.org/docs/latest/tutorial/quick-start#create-a-web-page
import { app, BrowserWindow, globalShortcut, ipcMain } from "electron";
import { readFileSync } from "fs";
import { InferenceSession, Tensor } from "onnxruntime-node";
import { Readable } from "stream";

const session = await InferenceSession.create("vad.onnx");

export const tensor = new Tensor(new Float32Array(2 * 1 * 64), [2, 1, 64]);

const sr = new Tensor(new BigInt64Array([16000n]));

export const run = (audio) => (h) => (c) => async () => {
  const input = new Tensor(audio, [1, audio.length]);
  const result = await session.run({ input: input, sr: sr, h: h, c: c });
  return { probability: result.output.data[0], h: result.hn, c: result.cn };
};

export const newReadable = () =>
  new Readable({
    read() {},
  });

export const handleClose = (ffmpeg) => (process) => () =>
  ffmpeg.on("close", () => {
    console.log("ffmpeg process closed");
    process();
  });

export const push = (stream) => (float32Array) => () =>
  stream.push(Buffer.from(float32Array.buffer));

export const end = (stream) => () => stream.push(null);

export { createClient } from "@deepgram/sdk";

export const transcribeImpl =
  (just) => (nothing) => (deepgram) => (filepath) => async () => {
    // https://github.com/deepgram/deepgram-node-sdk/blob/7c416605fc5953c8777b3685e014cf874c08eecf/README.md?plain=1#L194-L199
    const { result } = await deepgram.listen.prerecorded.transcribeFile(
      readFileSync(filepath),
      {
        model: "nova-2",
        smart_format: true,
      }
    );
    const paragraphs = result.results.channels[0].alternatives[0].paragraphs;
    if (paragraphs) {
      return just(paragraphs);
    } else {
      return nothing;
    }
  };

const createWindow = () => {
  const win = new BrowserWindow({
    width: 800,
    height: 600,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false,
    },
  });

  win.loadFile("index.html");
};

export const launch = (record) => (save) => () =>
  app.whenReady().then(() => {
    console.log("App is ready, creating window...");
    createWindow();

    globalShortcut.register("Command+;", () => {
      console.log("Command+; is pressed");
      save();
    });

    ipcMain.on("audio", (_, data) => {
      record(data)();
    });
  });
