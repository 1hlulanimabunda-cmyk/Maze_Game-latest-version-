// File: src/com/mycompany/graphicalmazegameenhanced/MonsterManager.java
package com.mycompany.graphicalmazegameenhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MonsterManager {
    private final GraphicalMazeGameEnhanced game;
    private final List<int[]> monsters = new ArrayList<>();
    private final Random random = new Random();
    private int[] boss = null;
    private boolean bossAlive = false;

    public MonsterManager(GraphicalMazeGameEnhanced game) {
        this.game = game;
    }

    public void resetMonstersForLevel(int level) {
        monsters.clear();
        boss = null;
        bossAlive = false;
        char[][] maze = MazeData.getMazeClone(level);

        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                if (maze[i][j] == 'M') {
                    monsters.add(new int[]{i, j, 2});
                } else if (maze[i][j] == 'B') {
                    boss = new int[]{i, j, 2};
                    bossAlive = true;
                }
            }
        }

        char[][] gm = game.getMaze();
        if (gm != null) {
            for (int[] m : monsters) {
                if (gm[m[0]][m[1]] != 'A' && gm[m[0]][m[1]] != 'S' && gm[m[0]][m[1]] != 'C' && gm[m[0]][m[1]] != 'E') {
                    gm[m[0]][m[1]] = 'M';
                }
            }
            if (bossAlive && boss != null) {
                gm[boss[0]][boss[1]] = 'B';
            }
        }
    }

    public void moveMonsters() {
        try {
            char[][] maze = game.getMaze();
            if (maze == null) return;
            int[][] directions = {{-1, 0, 0}, {0, 1, 1}, {1, 0, 2}, {0, -1, 3}};

            for (int[] monster : monsters) {
                if (maze[monster[0]][monster[1]] == 'M') maze[monster[0]][monster[1]] = '.';

                int[] dir = directions[random.nextInt(directions.length)];
                int newX = monster[0] + dir[0];
                int newY = monster[1] + dir[1];
                int newFacing = dir[2];

                if (isValidMove(maze, newX, newY) && !isMonsterAt(newX, newY) && !isPlayerAt(newX, newY) && maze[newX][newY] != 'G') {
                    monster[0] = newX;
                    monster[1] = newY;
                    monster[2] = newFacing;
                }
                if (maze[monster[0]][monster[1]] != 'A' && maze[monster[0]][monster[1]] != 'S' && maze[monster[0]][monster[1]] != 'C' && maze[monster[0]][monster[1]] != 'E') {
                    maze[monster[0]][monster[1]] = 'M';
                }
            }

            if (bossAlive && boss != null) {
                if (maze[boss[0]][boss[1]] == 'B') maze[boss[0]][boss[1]] = '.';
                int[] dir = directions[random.nextInt(directions.length)];
                int newX = boss[0] + dir[0];
                int newY = boss[1] + dir[1];
                if (isValidMove(maze, newX, newY) && !isMonsterAt(newX, newY) && !isPlayerAt(newX, newY)) {
                    boss[0] = newX; boss[1] = newY; boss[2] = dir[2];
                }
                if (maze[boss[0]][boss[1]] != 'A' && maze[boss[0]][boss[1]] != 'S' && maze[boss[0]][boss[1]] != 'C' && maze[boss[0]][boss[1]] != 'E') {
                    maze[boss[0]][boss[1]] = 'B';
                }
            }

        } catch (Exception e) {
            game.getStoryManager().appendToLog("Error moving monsters: " + e.getMessage() + "\n");
        }
    }

    private boolean isValidMove(char[][] maze, int x, int y) {
        return x >= 0 && x < maze.length && y >= 0 && y < maze[0].length && maze[x][y] != '#' && maze[x][y] != 'W' && maze[x][y] != 'G';
    }

    public boolean isMonsterAt(int x, int y) {
        for (int[] m : monsters) if (m[0] == x && m[1] == y) return true;
        return bossAlive && boss != null && boss[0] == x && boss[1] == y;
    }

    public boolean isPlayerAt(int x, int y) {
        return x == game.getPlayerX() && y == game.getPlayerY();
    }

    public static boolean isTrapAt(char[][] maze, int x, int y) {
        return x >= 0 && x < maze.length && y >= 0 && y < maze[0].length && maze[x][y] == 'X';
    }

    public int[] getBossPosition() {
        return bossAlive ? boss : null;
    }

    public void killBoss() {
        bossAlive = false;
        if (boss != null) {
            char[][] maze = game.getMaze();
            if (maze != null) maze[boss[0]][boss[1]] = '.';
        }
    }
}