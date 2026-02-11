# 💾 FloppyMidiPlayer

**Turn your floppy drive into a retro MIDI jukebox.**

Scans your floppy drive for MIDI files and plays them automatically — originally built for the Raspberry Pi.
Uses no UI (Future support for output screens to show Midi Infoo like the song name etc)

---


## ✨ Features

- **Plug & Play** — Just run the JAR file. The player automatically detects and plays MIDI files from your floppy drive.
- **Hot-Swap Support** — Swap diskettes on the fly and the player picks up MIDI files from the new disk.
- **Lightweight & Simple** — Minimal setup, minimal dependencies.
- **Broad Compatibility** — Supports JRE 11+, making it easy to run on Raspberry Pi and other low-resource devices.
- **Powered by [midi4j](https://github.com/Chaiavi/midi4j)** — Built on top of an excellent MIDI library for Java.

## 🎹 Keyboard Controls

| Key | Action |
|-----|--------|
| `←` / `→` | Skip backward / forward 5 seconds |
| `↑` / `↓` | Next / previous MIDI file |
| `Space` | Pause / resume playback |
| `Esc` | Quit the player |

## 🚀 Getting Started

1. Insert a floppy disk containing `.mid` files.
2. Run the player:
   ```bash
   java -jar FloppyMidiPlayer.jar
   ```
3. Enjoy the music! Use the keyboard controls above to navigate.

## 📝 Technical Note

> To capture keyboard input, the player creates a `JFrame` window that listens for key events. This is a pragmatic workaround for Java's limited support for single-key keyboard I/O:
>
> - `Scanner` only captures full lines
> - `Console` doesn't work inside most IDEs
> - `JLine` is overly complex for this use case
