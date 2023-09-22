package org.chaiware.floppy_midi_player;

import org.chaiware.midi4j.Midi;
import org.chaiware.midi4j.MidiInfo;
import org.chaiware.midi4j.model.MidiMetaMessageType;
import org.tinylog.Logger;
import org.tinylog.configuration.Configuration;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FloppyMidiPlayer {
    final static long FIVE_SECONDS = 5_000_000;
    public static boolean playPreviousMidi = false; // Shameless hack to play the previous midi file

    public static void main(String[] args) throws IOException, InterruptedException {
        Configuration.set("writer.format", "{message}");
        Logger.info("Floppy Midi Player Starting...");

        /* Finding the Floppy Drive */
        File floppyDrive = getFloppyDrive();
        if (floppyDrive == null) {
            Logger.error("No floppy drive found, exiting...");
            System.exit(0);
        }

        /* Playing the MIDI files one after the other, scans the floppy after each song in order to catch a diskette change if occurred */
        while (true) {
            List<File> midiFiles = getListOfMidiFilesFromFloppy(floppyDrive);
            for (int i = 0; i < midiFiles.size(); i++) {
                File currentMidi = midiFiles.get(i);
                Logger.info("Playing: {}", currentMidi.getAbsolutePath());
                Logger.info(showMidiInfo(currentMidi.getAbsolutePath()));
                playMidi(currentMidi.getAbsolutePath());

                if (playPreviousMidi) { // This takes care of the user choice to hear the previous Midi file
                    playPreviousMidi = false;
                    if (i > 0)
                        i -= 2;
                    else
                        i -= 1;
                }

                if (!getListOfMidiFilesFromFloppy(floppyDrive).containsAll(midiFiles)) { // if diskette was changed
                    midiFiles = getListOfMidiFilesFromFloppy(floppyDrive);
                    i = -1;
                    Logger.info("Diskette was changed");
                    Logger.debug("Found the following Midi files ({}): ", midiFiles.size());
                    midiFiles.stream().map(File::getName).forEach(Logger::debug);
                }
            }

            Thread.sleep(2500); // If no disk was found or finished playing all the midi files in the current disette
        }
    }

    /**
     * @return List of Midi files int the floppyDrive, or an empty list if none exist
     */
    private static List<File> getListOfMidiFilesFromFloppy(File floppyDrive) {
        File[] files = floppyDrive.listFiles((dir, name) -> name.toLowerCase().matches("(?i).+\\.(midi|mid)$"));
        return files != null ? List.of(files) : new ArrayList<>();
    }

    /**
     * Finds the System's floppy drive path or null (different methods for Windows or Linux)
     */
    private static File getFloppyDrive() throws IOException {
        Logger.info("Attempting to find the Floppy Drive Automatically");
        if (isWindowsOS()) {
            Logger.info("Identified Windows OS");
            for (File root : File.listRoots()) {
                if (FileSystemView.getFileSystemView().isFloppyDrive(root)) {
                    Logger.info("Floppy Drive Found: {}", root.getAbsolutePath());
                    return root;
                } else {
                    Logger.debug("{} is not a Floppy Drive", root.getAbsolutePath());
                }
            }
        } else { // If is Linux
            Logger.info("Identified NON Windows OS (Linux?)");
            for (FileStore store : FileSystems.getDefault().getFileStores()) {
                long total = store.getTotalSpace() / 1024;
                if (total > 1350 && total < 1500) {
                    return new File(store.toString().split(" \\(")[0]);
                }
            }
        }

        return null;
    }

    private static boolean isWindowsOS() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static void playMidi(String currentMidi) {
        try {
            Midi midi = new Midi(currentMidi);
            midi.play();

            JFrame keyboardController = generateKeyboardController(midi);
            while (midi.isPlaying() || midi.isPaused())
                Thread.sleep(200);

            keyboardController.dispose();
        } catch (Exception ex) {
            Logger.error(ex, "Exception while attempting to play Midi file :: {}", currentMidi);
        }
    }

    /**
     * @return JFrame which controls the keyboard actions <br/>
     * Using here a JFrame in order to capture single character keyboard input
     */
    private static JFrame generateKeyboardController(Midi midi) {
        JFrame keyboardController = new JFrame();
        keyboardController.setVisible(true);
        keyboardController.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();

                switch (keyCode) {
                    case KeyEvent.VK_SPACE:
                    case KeyEvent.VK_NUMPAD5:
                        midi.togglePause();
                        Logger.info("Paused: {}", midi.isPaused());
                        break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_NUMPAD8:
                        midi.stopPlay();
                        Logger.info("Jumping to the next song");
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_NUMPAD2:
                        midi.stopPlay();
                        playPreviousMidi = true;
                        Logger.info("Jumping to the previous song");
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_NUMPAD4:
                    case KeyEvent.VK_Z:
                        midi.jumpToPositionInMS(midi.getCurrentPositionInMS() - FIVE_SECONDS);
                        Logger.info("Navigated to: {}", convertMicrosecondsToTime(midi.getCurrentPositionInMS()));
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_NUMPAD6:
                    case KeyEvent.VK_X:
                        midi.jumpToPositionInMS(midi.getCurrentPositionInMS() + FIVE_SECONDS);
                        Logger.info("Navigated to: {}", convertMicrosecondsToTime(midi.getCurrentPositionInMS()));
                        break;
                    case KeyEvent.VK_ESCAPE:
                    case KeyEvent.VK_DELETE:
                        midi.stopPlay();
                        midi.shutdown();
                        Logger.info("Shutting down, goodbye");
                        System.exit(0);
                }
            }
        });

        return keyboardController;
    }

    private static String showMidiInfo(String currentMidi) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            MidiInfo midiInfo = new MidiInfo(currentMidi);
            stringBuilder.append("Length: ").append(convertMicrosecondsToTime(midiInfo.getMidiLengthInMS())).append("\n\n");
            Map<MidiMetaMessageType, String> allMetaInfo = midiInfo.getAllMetaInfo();
            for (Map.Entry<MidiMetaMessageType, String> entry : allMetaInfo.entrySet())
                stringBuilder.append(entry.getKey().getReadableMetaName()).append("\n").append(entry.getValue()).append("\n\n");

        } catch (Exception ex) {
            Logger.error(ex, "Exception while attempting to show Midi Info :: {}", currentMidi);
        }

        return stringBuilder.toString();
    }

    public static String convertMicrosecondsToTime(long microseconds) {
        long seconds = microseconds / 1000000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        long remainingMicroseconds = microseconds % 1000000;

        // Modify the format string conditionally based on remainingMicroseconds so it won't show the suffix of .000 when there are no microseconds
        String formatString = (remainingMicroseconds == 0) ? "%02d:%02d:%02d" : "%02d:%02d:%02d.%03d";

        return String.format(formatString, hours, minutes, remainingSeconds, remainingMicroseconds / 1000);
    }
}
