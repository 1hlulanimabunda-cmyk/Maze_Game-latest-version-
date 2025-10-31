// File: src/com/mycompany/graphicalmazegameenhanced/StoryManager.java
package com.mycompany.graphicalmazegameenhanced;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class StoryManager {
    private final JTextArea storyLog = new JTextArea();
    private String activeSpeech = "";
    private boolean showingSpeech = false;

    public StoryManager() {
        storyLog.setEditable(false);
        storyLog.setWrapStyleWord(true);
        storyLog.setLineWrap(true);
        storyLog.setFont(new Font("Serif", Font.PLAIN, 14));
    }

    public JScrollPane createLogScrollPane() {
        return new JScrollPane(storyLog);
    }

    public void appendToLog(String text) {
        SwingUtilities.invokeLater(() -> {
            storyLog.append(text);
            storyLog.setCaretPosition(storyLog.getDocument().getLength());
        });
    }

    public void showSpeechBubble(String text) {
        activeSpeech = text;
        showingSpeech = true;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                showingSpeech = false;
                activeSpeech = "";
            }
        }, 3000);
    }

    public boolean hasActiveSpeech() {
        return showingSpeech;
    }

    public void drawSpeechBubble(Graphics2D g2d, int playerX, int playerY, int cellSize) {
        if (!showingSpeech) return;
        int bubbleX = playerY * cellSize + 20;
        int bubbleY = playerX * cellSize - 60;
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(bubbleX, bubbleY, 200, 50, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(bubbleX, bubbleY, 200, 50, 20, 20);
        g2d.setFont(new Font("Serif", Font.PLAIN, 14));
        FontMetrics fm = g2d.getFontMetrics();
        int tx = bubbleX + (200 - fm.stringWidth(activeSpeech)) / 2;
        int ty = bubbleY + 30;
        g2d.drawString(activeSpeech, tx, ty);
    }
}