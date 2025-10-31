// File: src/main/java/com/mycompany/graphicalmazegameenhanced/SaveLoadManager.java
package com.mycompany.graphicalmazegameenhanced;

import javax.swing.*;
import java.io.*;

public class SaveLoadManager {
    private static final String SAVE_FILE = "maze_save.txt";

    public void saveGame(GraphicalMazeGameEnhanced game) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_FILE))) {
            writer.println(game.getCurrentLevel());
            writer.println(game.getPlayerX() + "," + game.getPlayerY() + "," + game.getPlayerFacing());
            writer.println(game.hasObjectiveItem());
            writer.println(0);

            char[][] maze = game.getMaze();
            int countM = 0;
            if (maze != null) {
                for (char[] row : maze)
                    for (char c : row)
                        if (c == 'M' || c == 'B') countM++;
            }
            writer.println(countM);

            if (maze != null) {
                for (int i = 0; i < maze.length; i++) {
                    for (int j = 0; j < maze[i].length; j++) {
                        if (maze[i][j] == 'M' || maze[i][j] == 'B')
                            writer.println(i + "," + j + ",2");
                    }
                }
            }

            if (maze != null) {
                for (char[] row : maze) {
                    for (char c : row) writer.print(c);
                    writer.println();
                }
            }

            writer.println("Saved Game");
            JOptionPane.showMessageDialog(null, "Game saved successfully!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error saving: " + ex.getMessage());
        }
    }

    public void loadGame(GraphicalMazeGameEnhanced game) {
        try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE))) {
            int level = Integer.parseInt(reader.readLine());
            String[] playerData = reader.readLine().split(",");
            int px = Integer.parseInt(playerData[0]);
            int py = Integer.parseInt(playerData[1]);
            int pf = Integer.parseInt(playerData[2]);
            boolean hasItem = Boolean.parseBoolean(reader.readLine());
            int sageStage = Integer.parseInt(reader.readLine());
            int monsterCount = Integer.parseInt(reader.readLine());
            for (int i = 0; i < monsterCount; i++) reader.readLine();

            char[][] maze = new char[GraphicalMazeGameEnhanced.ROWS][GraphicalMazeGameEnhanced.COLS];
            for (int i = 0; i < GraphicalMazeGameEnhanced.ROWS; i++) {
                String line = reader.readLine();
                if (line == null) throw new IOException("Save file corrupted");
                maze[i] = line.toCharArray();
            }

            game.loadLevel(level);
            game.setHasObjectiveItem(hasItem);
            JOptionPane.showMessageDialog(null, "Game loaded. Level: " + level);
            game.getStoryManager().appendToLog("Game loaded. Level: " + level + ".\n");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Load failed: " + ex.getMessage());
        }
    }

    public boolean hasSaveFile() {
        return new File(SAVE_FILE).exists();
    }
}