// https://www.electronjs.org/docs/latest/tutorial/quick-start#create-a-web-page
import { app, BrowserWindow, globalShortcut, ipcMain } from "electron";
import { InferenceSession, Tensor } from "onnxruntime-node";
import { Readable } from "stream";

const session = await InferenceSession.create("vad.onnx");

export const tensor = new Tensor(new Float32Array(2 * 1 * 64), [2, 1, 64]);

const sr = new Tensor(new BigInt64Array([16000n]));

export const run = (audio) => (h) => (c) => async () => {
  const input = new Tensor(audio, [1, audio.length]);
  const result = await session.run({ input: input, sr: sr, h: h, c: c });
  return { bar: result.output.data[0], h: result.hn, c: result.cn };
};

export const newReadable = () =>
  new Readable({
    read() {},
  });

export const length = (float32Array) => {
  return float32Array.length;
};

export const push = (stream) => (float32Array) => () => {
  stream.push(Buffer.from(float32Array.buffer));
};

export const appendFloat32Array = (first) => (second) => {
  const combined = new Float32Array(first.length + second.length);
  combined.set(first);
  combined.set(second, first.length);
  return combined;
};

export const memptyFloat32Array = new Float32Array();

export const end = (stream) => () => {
  stream.push(null);
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

export const launch = (record) => (process) => () => {
  app.whenReady().then(() => {
    createWindow();

    globalShortcut.register("Command+;", () => {
      console.log("Command+; is pressed");
      process();
    });

    ipcMain.on("audio", (_, data) => {
      record(data)();
    });
  });
};
