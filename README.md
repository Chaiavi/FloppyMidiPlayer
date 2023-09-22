# FloppyMidiPlayer

Scans your **Floppy drive** and plays the Midi files existing on it (Originally was created for the Raspberry Pie)

## Main Features

- Plays automatically, no need to do anything
- As simple as it gets
- Supports Jre11 and up in order to be played even on Raspberry pies
- Simple controls
  - Use the left/right arrow keys to jump forward +-5 seconds
  - Use the up/down arrow keys to jump to next/previous midi file
  - Use the spacebar to pause the current Midi file
  - Use ESC in order to shutdown the player
- Supports swapping of the diskette while playing in order to play midi files from the new diskette
- Uses the excellent [midi4j library](https://github.com/Chaiavi/midi4j)

## Note

In order to capture the keyboard controls, a JFrame is created and captures keyboard controls  
This was done as a hack as there is no simple way of capturing single key keyboard I/O in Java!

Scanner captures only full lines  
Console doesn't work on the IDE
JLine is a too-big-library for such a task and is way too complicated for this simple task.