import java.awt.event.*;
import java.awt.*;
import java.util.List;
// import java.util.concurrent.ExecutionException;
// import javax.swing.SwingWorker;

public class FifteenPuzzleSwingDemo extends Frame implements ActionListener {
    private static final long serialVersionUID = 1L; // Default Serial ID
    public Graphics g;
    private int PUZZLE_SIZE = 4;
    private int GRID_SIZE = 100;
    private List<FifteenPuzzle> solutions;
    private int CURRENT_INDEX = 0;
    private boolean playNextFlag = false;
    private boolean playPauseFlag = false;
    private boolean playPrevFlag = false;
    Button buttonPrev;
    Button buttonNext;
    Button buttonPlayPause;

    FifteenPuzzleSwingDemo() {
        setVisible(true);
        setLayout(null);
        setSize(570 + 570, 700);
        setLocation(400, 50);
        setFont(new Font("Forte", Font.ITALIC, 40));
        setBackground(Color.WHITE);

        buttonPrev = new Button("<<");
        buttonNext = new Button(">>");
        buttonPlayPause = new Button("Play");
        buttonPrev.setBounds(100, 540, 100, 60);
        buttonPlayPause.setBounds(220, 540, 100, 60);
        buttonNext.setBounds(340, 540, 100, 60);
        add(buttonPrev);
        add(buttonPlayPause);
        add(buttonNext);
        buttonPrev.addActionListener(this);
        buttonPlayPause.addActionListener(this);
        buttonNext.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    FifteenPuzzleSwingDemo(List<FifteenPuzzle> solutions) {
        this();
        this.PUZZLE_SIZE = FifteenPuzzle.PUZZLE_SIZE;
        this.solutions = solutions;
    }

    public void makeFrame(int x, int y, int width_x, int height_y) {
        g.setColor(new Color(208, 241, 242));
        g.fillRoundRect(x, y, width_x, height_y, 20, 20);
    }

    public void makeGrid(int x, int y, int val) {
        if (val != 0) {
            g.setColor(Color.RED);
            g.fillRoundRect(x, y, GRID_SIZE, GRID_SIZE, 60, 60);
            g.setColor(Color.WHITE);

            if (val < 10) {
                g.drawString(Integer.toString(val), x + GRID_SIZE / 2 - 15, y + GRID_SIZE / 2 + 14);
            } else {
                g.drawString(Integer.toString(val), x + GRID_SIZE / 2 - 23, y + GRID_SIZE / 2 + 14);
            }

        } else {
            g.setColor(Color.BLACK);
            g.fillRoundRect(x, y, GRID_SIZE, GRID_SIZE, 60, 60);
        }
    }

    public void makeBoard(FifteenPuzzle sp) {
        int i = 60, k = 60;
        int space_x = 5;
        int space_y = 5;

        for (int j = 0; j < PUZZLE_SIZE * PUZZLE_SIZE; j++) {
            int x = i + space_x;
            int y = k + space_y;
            this.makeGrid(x, y, (sp.tiles[(int) (j / 4)][j % 4]) % 16);
            i += 100;
            space_x += 5;
            if (j % PUZZLE_SIZE == 3 && j != 0) {
                k += 100;
                space_y += 5;
                space_x = 5;
                i = 60;
            }
        }
    }

    public void makeBoard(int i, int k, FifteenPuzzle sp) {
        int temp = i;
        int space_x = 5;
        int space_y = 5;

        for (int j = 0; j < PUZZLE_SIZE * PUZZLE_SIZE; j++) {
            int x = i + space_x;
            int y = k + space_y;
            this.makeGrid(x, y, (sp.tiles[(int) (j / 4)][j % 4]) % 16);
            i += 100;
            space_x += 5;
            if (j % PUZZLE_SIZE == 3 && j != 0) {
                k += 100;
                space_y += 5;
                space_x = 5;
                i = temp;
            }
        }
    }

    public void player() {
        if (solutions != null) {
            for (int i = CURRENT_INDEX + 1; i < solutions.size(); i++) {
                CURRENT_INDEX = i;
                this.makeBoard(solutions.get(i));
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        } else {
            g.setColor(Color.RED);
            g.drawString("Did not solve", 130, 340);
            g.drawString("Sorry!", 200, 380);
        }
    }

    public void paint(Graphics g) {
        super.paint(g);
        this.g = g;
        this.makeFrame(50, 50, 446, 600);
        g.setColor(Color.black);
        g.drawString("Target", 750, 100);
        this.makeFrame(600, 150, 446, 446);
        this.makeBoard(610, 160, solutions.get(solutions.size() - 1));

        if (!playPauseFlag) {
            this.makeBoard(solutions.get(CURRENT_INDEX));
        } else if (playNextFlag) {
            this.makeBoard(solutions.get(CURRENT_INDEX));
        } else if (playPrevFlag) {
            this.makeBoard(solutions.get(CURRENT_INDEX));
        } else {
            // start();
            if (CURRENT_INDEX == solutions.size() - 1) {
                this.makeBoard(solutions.get(CURRENT_INDEX));
            } else {
                this.player();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonPrev) {
            if (CURRENT_INDEX > 0) {
                CURRENT_INDEX--;
                playPrevFlag = true;
                playNextFlag = false;
                playPauseFlag = false;
            }
            repaint();
        }
        if (e.getSource() == buttonPlayPause) {
            playNextFlag = false;
            playPrevFlag = false;
            playPauseFlag = (!playPauseFlag);
            repaint();
        }
        if (e.getSource() == buttonNext) {
            if (solutions != null && CURRENT_INDEX < solutions.size() - 1) {
                CURRENT_INDEX++;
                playNextFlag = true;
                playPrevFlag = false;
                playPauseFlag = false;
            }
            repaint();
        }
    }
}

// private void start() {
// SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
// @Override
// protected Boolean doInBackground() throws Exception {
// // Simulate doing something useful.
// if (solutions != null) {
// for (int i = 1; i < solutions.size(); i++) {
// Thread.sleep(1000);
// System.out.println(i);
// // The type we pass to publish() is determined
// // by the second template parameter.
// publish(i);
// }
// return true;
// } else {
// g.setColor(Color.RED);
// g.drawString("Did not solve", 130, 340);
// g.drawString("Sorry!", 200, 380);
// }
// // Here we can return some object of whatever type
// // we specified for the first template parameter.
// // (in this case we're auto-boxing 'true').
// return false;
// }

// // Can safely update the GUI from this method.
// protected void done() {
// boolean status;
// try {
// // Retrieve the return value of doInBackground.
// status = get();
// if (status == true) {
// System.out.println("Your Puzzle Solved");
// } else {
// System.out.println("Could Not Solve");
// }
// } catch (InterruptedException e) {
// // This is thrown if the thread's interrupted.
// } catch (ExecutionException e) {
// // This is thrown if we throw an exception
// // from doInBackground.
// } catch (Exception e) {
// // Exception
// }
// }

// @Override
// // Can safely update the GUI from this method.
// protected void process(List<Integer> currentSolutionIndexs) {
// // Here we receive the values that we publish().
// // They may come grouped in chunks.
// int mostRecentIndex = currentSolutionIndexs.get(currentSolutionIndexs.size()
// - 1);
// System.out.println("Most Recent Index" + mostRecentIndex);
// makeBoard(solutions.get(mostRecentIndex));
// }
// };
// worker.execute();
// }