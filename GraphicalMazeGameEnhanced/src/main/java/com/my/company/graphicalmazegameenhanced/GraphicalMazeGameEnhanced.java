// File: src/main/java/com/mycompany/graphicalmazegameenhanced/GraphicalMazeGameEnhanced.java
package com.mycompany.graphicalmazegameenhanced;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GraphicalMazeGameEnhanced extends JFrame implements ActionListener {

    public static final int CELL_SIZE = 50;
    public static final int ROWS = 10;
    public static final int COLS = 10;
    private static final int MONSTER_MOVE_DELAY = 300;
    private static final int GLOW_ANIMATION_SPEED = 80;
    private static final Color GOLD = new Color(255, 215, 0);

    private enum GameState { MENU, PLAYING, PAUSED, WIN }
    private GameState state = GameState.MENU;

    private JPanel menuPanel, winPanel, pausePanel;
    private JButton startButton, resumeButton, newGameButton, resumePauseButton, quitPauseButton;
    private JLabel winLabel;

    private int currentLevel = 1;
    private final int MAX_LEVEL = 4;  // FINAL LEVEL IS 4
    private char[][] maze;
    private int playerX = 1, playerY = 1, playerFacing = 2;

    private MonsterManager monsterManager;
    private StoryManager storyManager;
    private SaveLoadManager saveLoadManager;
    private SoundManager soundManager;

    private boolean hasObjectiveItem = false;
    private int sageInteractionStage = 0;
    private String currentObjective = "Find the Sage for guidance on the curse.";
    private Timer monsterTimer, glowTimer;
    private float glowAlpha = 0.5f;
    private boolean glowIncreasing = true;

    private GamePanel gamePanel;
    private JScrollPane logScrollPane;

    /* --------------------------------------------------------------
       CONSTRUCTOR
       -------------------------------------------------------------- */
    public GraphicalMazeGameEnhanced() {
        setTitle("The Cursed Labyrinth - Final Saga");
        setLayout(new BorderLayout());

        /* ---------- MENU PANEL ---------- */
        menuPanel = new JPanel();
        menuPanel.setBackground(new Color(20, 20, 40));
        menuPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;

        startButton = createStyledButton("START GAME");
        resumeButton = createStyledButton("RESUME");
        newGameButton = createStyledButton("NEW GAME");

        startButton.addActionListener(this);
        resumeButton.addActionListener(this);
        newGameButton.addActionListener(this);

        menuPanel.add(startButton, gbc);
        menuPanel.add(resumeButton, gbc);
        menuPanel.add(newGameButton, gbc);

        /* ---------- WIN PANEL ---------- */
        winPanel = new JPanel();
        winPanel.setBackground(new Color(10, 30, 10));
        winPanel.setLayout(new BorderLayout());

        winLabel = new JLabel(
            "<html><div style='text-align:center; padding:40px;'>" +
            "<h1 style='color:#ffcc00; font-size:40px;'>CURSE SHATTERED!</h1>" +
            "<p style='color:#ffcc00; font-size:22px;'>You defeated the Warden and saved the realm!</p>" +
            "</div></html>",
            SwingConstants.CENTER
        );
        winPanel.add(winLabel, BorderLayout.CENTER);

        JButton backToMenu = createStyledButton("BACK TO MENU");
        backToMenu.addActionListener(e -> showMenu());
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(backToMenu);
        winPanel.add(btnPanel, BorderLayout.SOUTH);

        /* ---------- PAUSE PANEL ---------- */
        pausePanel = new JPanel();
        pausePanel.setBackground(new Color(0, 0, 0, 200));
        pausePanel.setLayout(new GridBagLayout());
        GridBagConstraints pgbc = new GridBagConstraints();
        pgbc.insets = new Insets(15, 15, 15, 15);
        pgbc.gridx = 0;
        pgbc.gridy = GridBagConstraints.RELATIVE;
        pgbc.anchor = GridBagConstraints.CENTER;

        resumePauseButton = createStyledButton("RESUME");
        quitPauseButton = createStyledButton("QUIT TO MENU");

        resumePauseButton.addActionListener(e -> resumeFromPause());
        quitPauseButton.addActionListener(e -> quitToMenu());

        pausePanel.add(resumePauseButton, pgbc);
        pausePanel.add(quitPauseButton, pgbc);

        /* ---------- GAME COMPONENTS ---------- */
        monsterManager = new MonsterManager(this);
        storyManager = new StoryManager();
        saveLoadManager = new SaveLoadManager();
        soundManager = new SoundManager();

        gamePanel = new GamePanel(this, monsterManager, storyManager);
        logScrollPane = storyManager.createLogScrollPane();
        logScrollPane.setPreferredSize(new Dimension(COLS * CELL_SIZE, 150));

        setSize(COLS * CELL_SIZE + 16, ROWS * CELL_SIZE + 150 + 100);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (state == GameState.PLAYING || state == GameState.PAUSED) {
                    handleKeyPress(e);
                }
            }
        });
        setFocusable(true);

        resumeButton.setEnabled(saveLoadManager.hasSaveFile());

        showMenu();
        setVisible(true);
    }

    /* --------------------------------------------------------------
       UI HELPERS
       -------------------------------------------------------------- */
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Serif", Font.BOLD, 22));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 90, 0));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(GOLD, 3));
        btn.setPreferredSize(new Dimension(260, 55));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showMenu() {
        getContentPane().removeAll();
        add(menuPanel, BorderLayout.CENTER);
        state = GameState.MENU;
        resumeButton.setEnabled(saveLoadManager.hasSaveFile());
        revalidate();
        repaint();
    }

    private void showWinScreen() {
        getContentPane().removeAll();
        add(winPanel, BorderLayout.CENTER);
        state = GameState.WIN;
        soundManager.playEvent("win");
        revalidate();
        repaint();
    }

    private void showPauseScreen() {
        getContentPane().removeAll();
        add(gamePanel, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);
        add(pausePanel, BorderLayout.NORTH);
        pausePanel.setPreferredSize(new Dimension(getWidth(), 200));
        revalidate();
        repaint();
    }

    private void hidePauseScreen() {
        getContentPane().remove(pausePanel);
        revalidate();
        repaint();
    }

    private void hideMenu() {
        getContentPane().removeAll();
        add(gamePanel, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);
        revalidate();
        repaint();
    }

    /* --------------------------------------------------------------
       ACTION LISTENER
       -------------------------------------------------------------- */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton || e.getSource() == newGameButton) {
            startNewGame();
        } else if (e.getSource() == resumeButton) {
            resumeGame();
        } else {
            monsterManager.moveMonsters();
            checkStoryTriggers();
            if (isPlayerOnMonster()) loseGame();
            gamePanel.repaint();
        }
    }

    private void startNewGame() {
        hideMenu();
        loadLevel(1);
        startTimers();
        state = GameState.PLAYING;
        resumeButton.setEnabled(true);
    }

    private void resumeGame() {
        hideMenu();
        startTimers();
        state = GameState.PLAYING;
    }

    private void pauseGame() {
        if (state != GameState.PLAYING) return;
        state = GameState.PAUSED;
        stopTimers();
        gamePanel.setPaused(true);
        showPauseScreen();
    }

    private void resumeFromPause() {
        if (state != GameState.PAUSED) return;
        state = GameState.PLAYING;
        startTimers();
        gamePanel.setPaused(false);
        hidePauseScreen();
    }

    private void quitToMenu() {
        stopTimers();
        state = GameState.MENU;
        gamePanel.setPaused(false);
        showMenu();
        resumeButton.setEnabled(saveLoadManager.hasSaveFile());
    }

    private void startTimers() {
        if (monsterTimer == null) {
            monsterTimer = new Timer(MONSTER_MOVE_DELAY, this);
            glowTimer = new Timer(GLOW_ANIMATION_SPEED, ev -> {
                glowAlpha = glowIncreasing ? glowAlpha + 0.07f : glowAlpha - 0.07f;
                if (glowAlpha >= 0.9f) glowIncreasing = false;
                if (glowAlpha <= 0.3f) glowIncreasing = true;
                gamePanel.repaint();
            });
        }
        monsterTimer.start();
        glowTimer.start();
    }

    private void stopTimers() {
        if (monsterTimer != null) monsterTimer.stop();
        if (glowTimer != null) glowTimer.stop();
    }

    /* --------------------------------------------------------------
       KEY HANDLING – FINAL LEVEL 4 LOGIC
       -------------------------------------------------------------- */
    private void handleKeyPress(KeyEvent e) {
        if (state == GameState.PAUSED && e.getKeyCode() == KeyEvent.VK_P) {
            resumeFromPause();
            return;
        }
        if (state != GameState.PLAYING) return;

        int key = e.getKeyCode();
        int newX = playerX, newY = playerY, newFacing = playerFacing;

        switch (key) {
            case KeyEvent.VK_W, KeyEvent.VK_UP -> { newX--; newFacing = 0; }
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> { newY--; newFacing = 3; }
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> { newX++; newFacing = 2; }
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> { newY++; newFacing = 1; }
            case KeyEvent.VK_SPACE -> { interactWithSageOrBoss(); return; }
            case KeyEvent.VK_V -> { saveLoadManager.saveGame(this); return; }
            case KeyEvent.VK_L -> { saveLoadManager.loadGame(this); return; }
            case KeyEvent.VK_H -> { showHelp(); return; }
            case KeyEvent.VK_P -> { pauseGame(); return; }
            default -> { return; }
        }

        if (isValidMove(newX, newY)) {
            char target = maze[newX][newY];

            /* ---------- ITEM PICK-UP (LEVEL 4: 'H') ---------- */
            char requiredItem = switch (currentLevel) {
                case 1 -> 'A';
                case 2 -> 'S';
                case 3 -> 'C';
                case 4 -> 'H';  // FINAL ITEM
                default -> '.';
            };

            if (target == requiredItem) {
                hasObjectiveItem = true;
                maze[newX][newY] = '.';
                String itemName = switch (currentLevel) {
                    case 1 -> "Crystal of Eternity";
                    case 2 -> "Ancient Altar Seal";
                    case 3 -> "Celestial Spire";
                    case 4 -> "Warden’s Heart";
                    default -> "Unknown Item";
                };
                storyManager.appendToLog("You acquired the " + itemName + "!\n");
                currentObjective = "Find the exit to win!";
                soundManager.playEvent("pickup");
                gamePanel.repaint();
            }

            /* ---------- EXIT – WIN ON LEVEL 4 ---------- */
            if (target == 'E') {
                if (hasObjectiveItem) {
                    if (currentLevel < MAX_LEVEL) {
                        loadLevel(currentLevel + 1);
                    } else {
                        showWinScreen();  // WIN!
                    }
                    return;
                } else {
                    storyManager.appendToLog("Exit is sealed without the Warden’s Heart.\n");
                    soundManager.playEvent("locked");
                    return;
                }
            }

            /* ---------- MONSTERS / TRAPS ---------- */
            if (monsterManager.isMonsterAt(newX, newY) || MonsterManager.isTrapAt(maze, newX, newY)) {
                loseGame();
                return;
            }

            /* ---------- MOVE PLAYER ---------- */
            maze[playerX][playerY] = (maze[playerX][playerY] == 'P') ? '.' : maze[playerX][playerY];
            playerX = newX; playerY = newY; playerFacing = newFacing;
            maze[playerX][playerY] = 'P';
            if (isPlayerOnMonster()) loseGame();
            gamePanel.repaint();
        }
    }

    /* --------------------------------------------------------------
       INTERACTION – BOSS ON LEVEL 4
       -------------------------------------------------------------- */
    private void interactWithSageOrBoss() {
        int[] sage = MazeData.getSagePositionForLevel(currentLevel);
        if (Math.abs(playerX - sage[0]) <= 1 && Math.abs(playerY - sage[1]) <= 1 &&
            (playerX != sage[0] || playerY != sage[1])) {
            interactWithSage();
            return;
        }

        if (currentLevel == 4) {
            int[] boss = monsterManager.getBossPosition();
            if (boss != null && Math.abs(playerX - boss[0]) <= 1 && Math.abs(playerY - boss[1]) <= 1 &&
                (playerX != boss[0] || playerY != boss[1])) {
                if (hasObjectiveItem) {
                    monsterManager.killBoss();
                    maze[boss[0]][boss[1]] = '.';
                    storyManager.appendToLog("Warden defeated! The curse is broken!\n");
                    soundManager.playEvent("boss_defeat");
                } else {
                    storyManager.appendToLog("You need the Warden’s Heart to challenge the Warden!\n");
                    soundManager.playEvent("locked");
                }
                return;
            }
        }
        storyManager.appendToLog("Nothing to interact with.\n");
    }

    /* --------------------------------------------------------------
       LOAD LEVEL – FINAL LEVEL 4
       -------------------------------------------------------------- */
    public void loadLevel(int level) {
        currentLevel = level;
        hasObjectiveItem = false;
        sageInteractionStage = 0;
        monsterManager.resetMonstersForLevel(level);
        maze = MazeData.getMazeClone(level);
        playerX = 1; playerY = 1; playerFacing = 2;
        maze[playerX][playerY] = 'P';

        String title = switch (level) {
            case 1 -> "Level 1: The Cursed Labyrinth";
            case 2 -> "Level 2: Enchanted Forest";
            case 3 -> "Level 3: Celestial Ruins";
            case 4 -> "Level 4: Warden’s Vault (FINAL BOSS)";
            default -> "";
        };
        String objective = switch (level) {
            case 1 -> "Find the Crystal of Eternity.";
            case 2 -> "Seal the Ancient Altar.";
            case 3 -> "Place the Celestial Spire.";
            case 4 -> "Steal the Warden’s Heart and escape!";
            default -> "";
        };
        currentObjective = objective;

        storyManager.appendToLog("\n=== " + title + " ===\n" + objective + "\n");
        MazeData.addRandomDecorations(maze, level == 1 ? 5 : level == 2 ? 10 : level == 3 ? 8 : 6);
        gamePanel.repaint();
    }

    private void interactWithSage() {
        soundManager.playEvent("sage");
        sageInteractionStage++;
        String msg = switch (currentLevel) {
            case 1 -> sageInteractionStage == 1 ? "Sage: The Crystal lies deep within. Beware the guardians!" : "Sage: Hurry, the curse grows!";
            case 2 -> sageInteractionStage == 1 ? "Sage: Seal the altar to weaken the curse." : "Sage: The forest hides many eyes.";
            case 3 -> sageInteractionStage == 1 ? "Sage: The Spire awaits your crystal!" : "Sage: The stars align for you.";
            case 4 -> sageInteractionStage == 1 ? "Sage: The Warden’s Heart is the key to ending the curse!" : "Sage: Defeat the Warden and escape!";
            default -> "";
        };
        storyManager.showSpeechBubble(msg);
        storyManager.appendToLog(msg + "\n");
    }

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < ROWS && y >= 0 && y < COLS &&
               maze[x][y] != '#' && maze[x][y] != 'W' && maze[x][y] != 'G';
    }

    private void checkStoryTriggers() {
        char item = switch (currentLevel) {
            case 1 -> 'A'; case 2 -> 'S'; case 3 -> 'C';
            case 4 -> 'H';
            default -> '.';
        };
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                int tx = playerX + i, ty = playerY + j;
                if (tx >= 0 && tx < ROWS && ty >= 0 && ty < COLS &&
                    !hasObjectiveItem && maze[tx][ty] == item) {
                    soundManager.playEvent("glow");
                    return;
                }
            }
        }
    }

    private boolean isPlayerOnMonster() { return monsterManager.isMonsterAt(playerX, playerY); }

    private void showHelp() {
        JOptionPane.showMessageDialog(this,
            "Controls:\nWASD / Arrows: Move\nSPACE: Interact\nP: Pause\nV: Save\nL: Load\nH: Help",
            "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loseGame() {
        soundManager.playEvent("lose");
        JOptionPane.showMessageDialog(this, "Game Over! You were caught.", "Defeat", JOptionPane.ERROR_MESSAGE);
        quitToMenu();
    }

    /* --------------------------------------------------------------
       GETTERS
       -------------------------------------------------------------- */
    public int getCurrentLevel() { return currentLevel; }
    public int getRows() { return ROWS; }
    public int getCols() { return COLS; }
    public int getCellSize() { return CELL_SIZE; }
    public float getGlowAlpha() { return glowAlpha; }
    public boolean hasObjectiveItem() { return hasObjectiveItem; }
    public void setHasObjectiveItem(boolean v) { hasObjectiveItem = v; }
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public int getPlayerFacing() { return playerFacing; }
    public char[][] getMaze() { return maze; }
    public MonsterManager getMonsterManager() { return monsterManager; }
    public StoryManager getStoryManager() { return storyManager; }
    public SoundManager getSoundManager() { return soundManager; }
}