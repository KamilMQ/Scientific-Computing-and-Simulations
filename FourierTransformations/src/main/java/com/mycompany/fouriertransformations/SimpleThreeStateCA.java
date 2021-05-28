package com.mycompany.fouriertransformations;
import java.awt.*;
import javax.swing.*;


// 0 - Resting
// 2 - Excited 
// 1 - Recovering

public class SimpleThreeStateCA {

    final static int N = 50;
    final static int CELL_SIZE = 5;
    final static int DELAY = 250;

    static int[][] state = new int[N][N];

    static boolean[][] excitedNeighbour = new boolean[N][N];

    static Display display = new Display();

    public static void main(String args[]) throws Exception {


         int x = N/2;
		 state[x][x] = 2;
		 state[x][x+1] = 1;
		 state[x-1][x] = 1;

        display.repaint();
        pause();

        // Main update loop.
        int iter = 0;
        while (true) {

            System.out.println("iter = " + iter++);

            // Calculate which cells have excited neighbnours.
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {

                    // find neighbours...
                    int ip = Math.min(i + 1, N - 1);
                    int im = Math.max(i - 1, 0);

                    int jp = Math.min(j + 1, N - 1);
                    int jm = Math.max(j - 1, 0);

                    excitedNeighbour[i][j]
                            = state[i][jp] == 2
                            || state[i][jm] == 2
                            || state[ip][j] == 2
                            || state[im][j] == 2;
                }
            }

            // Update state.
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    switch (state[i][j]) {
                        case 0:
                            if (excitedNeighbour[i][j]) {
                                state[i][j] = 2;
                            }
                            break;
                        case 2:
                            state[i][j] = 1;
                            break;
                        default: // 1
                            state[i][j] = 0;
                            break;
                    }
                }
            }

            display.repaint();
            pause();
        }
    }

    static class Display extends JPanel {

        final static int WINDOW_SIZE = N * CELL_SIZE;

        Display() {

            setPreferredSize(new Dimension(WINDOW_SIZE, WINDOW_SIZE));

            JFrame frame = new JFrame("Minimal excitable media model");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(this);
            frame.pack();
            frame.setVisible(true);
        }

        public void paintComponent(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, WINDOW_SIZE, WINDOW_SIZE);
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (state[i][j] > 0) {
                        if (state[i][j] == 2) {
                            g.setColor(Color.BLACK);
                        } else {
                            g.setColor(Color.GRAY);
                        }
                        g.fillRect(CELL_SIZE * i, CELL_SIZE * j,
                                CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }
    }

    static void pause() {
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
