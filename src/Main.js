// https://www.electronjs.org/docs/latest/tutorial/quick-start#create-a-web-page
import { app, BrowserWindow, globalShortcut, ipcMain } from "electron";

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

export const appendFloat32Array = (first) => (second) => {
  const combined = new Float32Array(first.length + second.length);
  combined.set(first);
  combined.set(second, first.length);
  return combined;
};

export const memptyFloat32Array = new Float32Array();

export const foo = (buffer) => () => {
  console.log(buffer);
};
