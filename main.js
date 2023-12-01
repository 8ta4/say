// https://www.electronjs.org/docs/latest/tutorial/quick-start#create-a-web-page
const { app, BrowserWindow, globalShortcut } = require("electron");

const createWindow = () => {
  const win = new BrowserWindow({
    width: 800,
    height: 600,
  });

  win.loadFile("index.html");
};

app.whenReady().then(() => {
  createWindow();

  globalShortcut.register("Command+;", () => {
    console.log("Command+; is pressed");
  });
});
