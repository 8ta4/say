import appRoot from "app-root-path";
import dayjs from "dayjs";
import { app, BrowserWindow, globalShortcut, ipcMain } from "electron";
import { readFileSync } from "fs";
import { InferenceSession, Tensor } from "onnxruntime-node";
import { Readable } from "stream";

export { default as fixPath } from "fix-path";

export const getAppRootPath = () =>
  // https://github.com/inxilpro/node-app-root-path/blob/baf711a6ec61acf50aeb42fb6e5118e899bcbe4b/README.md?plain=1#L24
  appRoot.toString();

export const createSession = (path) => async () =>
  InferenceSession.create(path);

export const tensor = new Tensor(new Float32Array(2 * 1 * 64), [2, 1, 64]);

const sr = new Tensor(new BigInt64Array([16000n]));

export const run = (session) => (audio) => (h) => (c) => async () => {
  const input = new Tensor(audio, [1, audio.length]);
  const result = await session.run({
    input,
    sr,
    h,
    c,
  });
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
      },
    );
    const { paragraphs } = result.results.channels[0].alternatives[0];
    if (paragraphs) {
      return just(paragraphs);
    }
    return nothing;
  };

export const getCurrentDate = () => {
  const currentDate = dayjs();
  return {
    year: currentDate.format("YYYY"),
    month: currentDate.format("MM"),
    day: currentDate.format("DD"),
  };
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
  // https://www.electronjs.org/docs/latest/tutorial/quick-start#create-a-web-page
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
