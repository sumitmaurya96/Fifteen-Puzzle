import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import javax.swing.SwingUtilities;
import java.util.Scanner;
import java.io.*;
// import java.util.Queue;

/**
 * fifteen puzzle using A* Algorithm.
 */
public class FifteenPuzzle {

	private static FifteenPuzzle SOLVED;
	private static boolean INPUT_FROM_FILE = false;
	private static boolean TARGET_FROM_FILE = false;
	private static int SHUFFLE_ITERATION = 0;
	private int display_width;
	private TilePos blank;
	public final static int PUZZLE_SIZE = 4;
	public int[][] tiles;
	public int[][] target;

	private class TilePos {
		public int x;
		public int y;

		public TilePos(int x, int y) {
			this.x = x;
			this.y = y;
		}

	}

	// Constructor to initilize the values of target and input tile
	// Can take Input tile randomly or from file
	// Cant take Target tile default from file
	FifteenPuzzle() {
		int x = PUZZLE_SIZE - 1;
		int y = PUZZLE_SIZE - 1;

		tiles = new int[PUZZLE_SIZE][PUZZLE_SIZE];
		target = new int[PUZZLE_SIZE][PUZZLE_SIZE];
		display_width = Integer.toString(PUZZLE_SIZE * PUZZLE_SIZE).length();

		if (!INPUT_FROM_FILE || !TARGET_FROM_FILE) {
			int cnt = 1;
			for (int i = 0; i < PUZZLE_SIZE; i++) {
				for (int j = 0; j < PUZZLE_SIZE; j++) {
					if (!INPUT_FROM_FILE) {
						tiles[i][j] = (cnt % (PUZZLE_SIZE * PUZZLE_SIZE));
					}
					if (!TARGET_FROM_FILE) {
						target[i][j] = (cnt % (PUZZLE_SIZE * PUZZLE_SIZE));
					}
					cnt++;
				}
			}
		}

		if (INPUT_FROM_FILE || TARGET_FROM_FILE) {
			try {
				BufferedReader in = new BufferedReader(new FileReader("input.txt"));
				String line = null;
				int i = 0;
				int k = 0;
				while ((line = in.readLine()) != null) {

					String cells[] = line.split("\t");

					int cellsSize = cells.length;

					if (i < 4) {
						for (int j = 0; j < cellsSize; j++) {
							if (INPUT_FROM_FILE) {
								tiles[i][j] = Integer.parseInt(cells[j]);
								if (tiles[i][j] == 0) {
									x = i;
									y = j;
								}
							}
						}
					}

					if (i > 5) {

						for (int j = 0; j < cellsSize; j++) {
							if (TARGET_FROM_FILE) {
								target[k][j] = Integer.parseInt(cells[j]);
							}
						}
						k++;
					}
					i++;
				}

				in.close();
			} catch (Exception e) {
				System.out.println("err" + e);
			}
		}

		// Initilize Blank tile, means tile with value 0
		blank = new TilePos(x, y);
		tiles[blank.x][blank.y] = 0;
	}

	FifteenPuzzle(FifteenPuzzle toClone) {
		this(); // chain to basic initialization
		for (TilePos p : allTilePos()) {
			tiles[p.x][p.y] = toClone.tile(p);
		}
		blank = toClone.getBlank();
	}

	public List<TilePos> allTilePos() {
		ArrayList<TilePos> out = new ArrayList<TilePos>();
		for (int i = 0; i < PUZZLE_SIZE; i++) {
			for (int j = 0; j < PUZZLE_SIZE; j++) {
				out.add(new TilePos(i, j));
			}
		}
		return out;
	}

	public int tile(TilePos p) {
		return tiles[p.x][p.y];
	}

	public TilePos getBlank() {
		return blank;
	}

	public TilePos whereIs(int x) {
		for (TilePos p : allTilePos()) {
			if (tile(p) == x) {
				return p;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof FifteenPuzzle) {
			for (TilePos p : allTilePos()) {
				if (this.tile(p) != ((FifteenPuzzle) o).tile(p)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int out = 0;
		for (TilePos p : allTilePos()) {
			out = (out * PUZZLE_SIZE * PUZZLE_SIZE) + this.tile(p);
		}
		return out;
	}

	public void show() {
		System.out.println("-----------------");
		for (int i = 0; i < PUZZLE_SIZE; i++) {
			System.out.print("| ");
			for (int j = 0; j < PUZZLE_SIZE; j++) {
				int n = tiles[i][j];
				String s;
				if (n > 0) {
					s = Integer.toString(n);
				} else {
					s = "";
				}
				while (s.length() < display_width) {
					s += " ";
				}
				System.out.print(s + "| ");
			}
			System.out.print("\n");
		}
		System.out.print("-----------------\n\n");
	}

	// Returns a List containing possible moves as tile objects i.e.. Left, Right,
	// Up, Down
	public List<TilePos> allValidMoves() {
		ArrayList<TilePos> out = new ArrayList<TilePos>();
		for (int dx = -1; dx < 2; dx++) {
			for (int dy = -1; dy < 2; dy++) {
				TilePos tp = new TilePos(blank.x + dx, blank.y + dy);
				if (isValidMove(tp)) {
					out.add(tp);
				}
			}
		}
		return out;
	}

	// Check if move is valid or not
	public boolean isValidMove(TilePos p) {
		// Moving out of boundary not allowed
		if ((p.x < 0) || (p.x >= PUZZLE_SIZE)) {
			return false;
		}
		if ((p.y < 0) || (p.y >= PUZZLE_SIZE)) {
			return false;
		}
		// Diagonal move not allowed means (dx, dy) can't be (1,1), (-1,-1)
		// below pair of (dx,dy) is also not possible
		// (-1,1) ,(1, -1)
		int dx = blank.x - p.x;
		int dy = blank.y - p.y;
		if ((Math.abs(dx) + Math.abs(dy) != 1) || (dx * dy != 0)) {
			return false;
		}
		return true;
	}

	// Move player i.e.. swap zero tile to that position p given in argument
	public void move(TilePos p) {
		if (!isValidMove(p)) {
			throw new RuntimeException("Invalid move");
		}
		assert tiles[blank.x][blank.y] == 0;
		tiles[blank.x][blank.y] = tiles[p.x][p.y];
		tiles[p.x][p.y] = 0;
		blank = p;
	}

	/**
	 * returns a new puzzle with the move applied
	 * 
	 * @param p
	 * @return
	 */
	public FifteenPuzzle moveClone(TilePos p) {
		FifteenPuzzle out = new FifteenPuzzle(this);
		out.move(p);
		return out;
	}

	// Shuffles the given puzzle
	public void shuffle(int howmany) {
		for (int i = 0; i < howmany; i++) {
			List<TilePos> possible = allValidMoves();
			int which = (int) (Math.random() * possible.size());
			TilePos move = possible.get(which);
			this.move(move);
		}
	}

	public int numberMisplacedTiles() {
		int wrong = 0;
		for (int i = 0; i < PUZZLE_SIZE; i++) {
			for (int j = 0; j < PUZZLE_SIZE; j++) {
				if ((tiles[i][j] > 0) && (tiles[i][j] != SOLVED.tiles[i][j])) {
					wrong++;
				}
			}
		}
		return wrong;
	}

	public boolean isSolved() {
		return numberMisplacedTiles() == 0;
	}

	/**
	 * another A* heuristic. Total manhattan distance (L1 norm) from each non-blank
	 * tile to its correct position
	 */

	public int manhattanDistance() {
		int sum = 0;
		for (TilePos p : allTilePos()) {
			int val = tile(p);
			if (val > 0) {
				TilePos correct = SOLVED.whereIs(val);
				sum += Math.abs(correct.x = p.x);
				sum += Math.abs(correct.y = p.y);
			}
		}
		return sum;
	}

	/**
	 * distance heuristic for A*
	 * 
	 */

	public int estimateError() {
		// Slower but optimal
		// return this.numberMisplacedTiles();

		// finds a non-optimal solution faster
		return 100 * this.numberMisplacedTiles();

		// return this.manhattanDistance();
	}

	public List<FifteenPuzzle> allAdjacentPuzzles() {
		ArrayList<FifteenPuzzle> out = new ArrayList<FifteenPuzzle>();
		for (TilePos move : allValidMoves()) {
			out.add(moveClone(move));
		}
		return out;
	}

	/**
	 * returns a list of boards if it was able to solve it, or else null
	 * 
	 * @return
	 */

	public List<FifteenPuzzle> aStarSolve() {
		HashMap<FifteenPuzzle, FifteenPuzzle> predecessor = new HashMap<FifteenPuzzle, FifteenPuzzle>();
		HashMap<FifteenPuzzle, Integer> depth = new HashMap<FifteenPuzzle, Integer>();
		final HashMap<FifteenPuzzle, Integer> score = new HashMap<FifteenPuzzle, Integer>();
		Comparator<FifteenPuzzle> comparator = new Comparator<FifteenPuzzle>() {
			@Override
			public int compare(FifteenPuzzle a, FifteenPuzzle b) {
				return score.get(a) - score.get(b);
			}
		};
		PriorityQueue<FifteenPuzzle> toVisit = new PriorityQueue<FifteenPuzzle>(10000, comparator);

		predecessor.put(this, null);
		depth.put(this, 0);
		score.put(this, this.estimateError());
		toVisit.add(this);
		int cnt = 0;
		while (toVisit.size() > 0) {
			FifteenPuzzle candidate = toVisit.remove();
			cnt++;
			if (cnt % 10000 == 0) {
				System.out.printf("Considered %,d positions. Queue = %,d\n", cnt, toVisit.size());
			}
			if (candidate.isSolved()) {
				System.out.printf("Solution considered %d boards\n", cnt);
				LinkedList<FifteenPuzzle> solution = new LinkedList<FifteenPuzzle>();
				FifteenPuzzle backtrace = candidate;
				while (backtrace != null) {
					solution.addFirst(backtrace);
					backtrace = predecessor.get(backtrace);
				}
				return solution;
			}
			for (FifteenPuzzle fp : candidate.allAdjacentPuzzles()) {
				if (!predecessor.containsKey(fp)) {
					predecessor.put(fp, candidate);
					depth.put(fp, depth.get(candidate) + 1);
					int estimate = fp.estimateError();
					score.put(fp, depth.get(candidate) + 1 + estimate);
					// dont' add to p-queue until the metadata is in place that the comparator needs
					toVisit.add(fp);
				}
			}
		}
		return null;
	}

	private static void showSolution(List<FifteenPuzzle> solution) {
		if (solution != null) {
			System.out.printf("Success!  Solution with %d moves:\n", solution.size());
			for (FifteenPuzzle sp : solution) {
				sp.show();
			}
		} else {
			System.out.println("Did not solve. :(");
		}
	}

	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		String answer;

		System.out.print("\nDo you want to use your file's Puzzle as input	Y//N: ");
		answer = s.nextLine();
		answer = answer.toLowerCase();
		if (answer.charAt(0) == 'y') {
			INPUT_FROM_FILE = true;
		}

		System.out.print("\nDo you want to use your file's Target as input	Y//N: ");
		answer = s.nextLine();
		answer = answer.toLowerCase();
		if (answer.charAt(0) == 'y') {
			TARGET_FROM_FILE = true;
		}

		// Now create object
		FifteenPuzzle p = new FifteenPuzzle();
		// Test code
		SOLVED = new FifteenPuzzle();
		SOLVED.tiles = p.target;

		System.out.print("\nDo you want random shuffle of input Puzzle	Y//N: ");
		answer = s.nextLine();
		answer = answer.toLowerCase();
		if (answer.charAt(0) == 'y') {
			System.out.print("Number of Shuffle: ");
			SHUFFLE_ITERATION = s.nextInt();
			p.shuffle(SHUFFLE_ITERATION); // Number of shuffles is critical -- large numbers (100+) and 4x4 puzzle is
			// hard even for A*.
		}

		// Closing Scanner Leak
		s.close();
		System.out.println("Shuffled board:");
		p.show();

		List<FifteenPuzzle> solution;

		System.out.println("Solving with A*");
		solution = p.aStarSolve();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				new FifteenPuzzleSwingDemo(solution);
			}
		});
		showSolution(solution);
	}

}