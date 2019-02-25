import java.awt.*;
import java.util.ArrayList;
import java.util.*;
import java.util.List;
import pacsim.*;

/**
 *
 * University of Central Florida
 * CAP4630 - Spring 2019
 * Authors: Bailey Brooks and Ross Wagner
 *
 **/

public class PacSimMinimax implements PacAction {

	//	optional: class and instance variables
	int depth;
	boolean firstPass;
	PacCell[][] board;
	static final int win = 1000000;
	static final int lose = -1000000;



	private class PointInt{
		public final Point p;
		public final int i;
		public PointInt(Point p, int i){
			this.p = p;
			this.i = i;
		}
	}


	public PacSimMinimax( int depth, String fname, int te, int gran, int max ) {

		// optional: initialize some variables
		this.depth = depth;

		PacSim sim = new PacSim( fname, te, gran, max );
		firstPass = true;
		board = null;
		sim.init(this);

	}

	public static void main( String[] args ){

		String fname = args[ 0 ];
		int depth = Integer.parseInt(args[ 1 ]);

		int te = 0;
		int gr = 0;
		int ml = 0;

		if( args.length == 5){
			te = Integer.parseInt(args[ 2 ]);
			gr = Integer.parseInt(args[ 3 ]);
			ml = Integer.parseInt(args[ 4 ]);
		}

		new PacSimMinimax( depth, fname, te, gr, ml );

		System.out.println("\nAdversarial Search using Minimax by Bailey Brooks and Ross Wagner:");
		System.out.println("\n     Game board     : " + fname);
		System.out.println("     Search depth : " + depth + "\n");

		if( te > 0){
			System.out.println("     Preliminary runs : " + te
					+ "\n     Granularity     : " + gr
					+ "\n     Max move limit     : " + ml
					+ "\n\nPreliminary run results :\n");
		}
	}


	public void init(){
		firstPass = true;
		board = null;
	}

	/*
		Our evaluation function initially checks for 0 food pellets, as that would imply a win and no scoring is necessary.
		We then locate the nearest food and ghost. A loss is returned in the case that the ghost is <= 1 cell away from PacMan.
		The first score calculation is 100 - remaining food pellets.
		This prioritizes consumption of food pellets as we get closer to the final food.
		We want to increase the score when the nearest food is further away than the closest ghost.
		Subsequently, we want to decrease the score whent the closest ghost is further away than the closest food.
		So as food gets closer we bump up the score while as the ghost gets closer we decrease the score.
	*/
	private int evalFunct(BoardState boardState, Point pac){

		int score = 0;

		int remainingFood = boardState.food.size();

		if (remainingFood == 0)
			return win; // no more food to attain = win

		PointInt nearestFood = nearest(pac, boardState.food);

		PointInt nearestGhost = nearest(pac, new ArrayList<>(
			Arrays.asList(boardState.g1Pos, boardState.g2Pos)));

		if (nearestGhost.i <= 1)
			return lose;


		score += 100-remainingFood;

		score += 2*(nearestFood.i-nearestGhost.i);


		score += 10/nearestFood.i;



		score -= 100/nearestGhost.i;

		return score;


	}


	public PacFace action( Object state ){

		PacCell[][] grid = (PacCell[][]) state;


		board = grid; // get the board structure. Walls and such.
		//List<Point> food = PacUtils.findFood(grid);



		List<Point> food = PacUtils.findFood(grid);
		List<Point> morphs = PacUtils.findMorphs(grid);
		//PacUtils.po
		PacUtils.appendPointList(food,morphs);
		// get ghost and pac positions
		List<Point> ghostPositions = PacUtils.findGhosts(grid);
		Point pacPosition = PacUtils.findPacman(grid).getLoc();

		BoardState boardState = new BoardState(food, pacPosition,ghostPositions.get(0),ghostPositions.get(1));

		// get pacman and ghost positions
		PacFace newFace = null;

		// calc minimax
		newFace = miniMax(boardState,depth);


		return newFace;
	}

	/**
	 * Given a point returns a list of adjacent points. the the point is not a valid move null is appended in its place.
	 * The order of directions in the returned list is N,S,E,W.
	 * */
	private List<Point> getMoves(Point p){
		List<Point> moves = new ArrayList<>();

		// N and South
		for(int dy = -1; dy <= 1; dy= dy+2){
			//temp.setLocation(p.x,p.y+dy);
			if (PacUtils.unoccupied(p.x,p.y+dy, board)){
				// append new point
				moves.add(new Point(p.x,p.y+dy));
			}
			else{
				moves.add(null);
			}
		}

		// E and W
		for(int dx = 1; dx >= -1; dx= dx-2){
			//temp.setLocation(p.x,p.y+dy);
			if (PacUtils.unoccupied(p.x+dx,p.y, board)){
				// append new point
				moves.add(new Point(p.x+dx,p.y));
			}
			else{
				moves.add(null);
			}
		}




		return moves;

	}

	private int max(BoardState boardState, int depth, int alpha, int beta){
		if (depth == 0) {
			// evaluate grid
			return  evalFunct(boardState, boardState.pacPos);
			//return 100 - boardState.food.size();
		}

		List<Point> newFood = PacUtils.clonePointList(boardState.food);

		int maxValue = Integer.MIN_VALUE;
		List<Point> pacMoves = getMoves(boardState.pacPos);
		for(Point move : pacMoves){
			if (move != null){
				// check to see if we ate any food
				if (newFood.contains(move)){
					newFood.remove(move);
				}
				BoardState bs = new BoardState(newFood,move,boardState.g1Pos,boardState.g2Pos);
				int val = min(bs, depth,alpha,beta);
				maxValue = Integer.max(maxValue,val);
				if (maxValue>=beta){
					return maxValue;
				}
				alpha = Integer.max(alpha,maxValue);



			}
		}
		return maxValue;
	}

	private int min(BoardState boardState, int depth, int alpha, int beta){
		//
		int v = Integer.MAX_VALUE;
		//for each combo of ghost positions
		List<Point> g1Moves = getMoves(boardState.g1Pos);
		List<Point> g2Moves = getMoves(boardState.g2Pos);

		int minValue = Integer.MAX_VALUE;
		for (Point g1Move : g1Moves){
			if (g1Move != null){

				for (Point g2move : g2Moves){
					// for each valid combo of moves
					if (g2move != null) {

						BoardState bs = new BoardState(boardState.food, boardState.pacPos, g1Move, g2move);
						int temp = max(bs, depth - 1, alpha, beta);
						minValue = Integer.min(minValue, temp);
						if (minValue <= alpha) {
							return minValue;
						}
						beta = Integer.min(beta, minValue);
					}
				}
			}
		}

		return minValue;

	}

	/**
	 * This function takes the current state of the board (grid) and a depth to search to. Alpha beta pruning is used to
	 * avoid unnecessary calculations. Recursion using functions min() and max() passing grid, depth, and alpha or beta
	 * appears to be the best approach at the time.
	 *
	* */
	private PacFace miniMax(BoardState boardState, int depth){

		// for each move available to pacman, change pac's position in board (N,S,E,W)
		Point pacPos = boardState.pacPos;

		// put possible moves into array
		List<Point> moves;
		List<Point> newFood = PacUtils.clonePointList(boardState.food);
		moves = getMoves(pacPos);
		PacFace face = null;
		//int maxIndex = moves.indexOf(Collections.max(min(moves)));

		int maxIndex = -1;
		int maxVal = Integer.MIN_VALUE;
		int i = 0;
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		for(Point move : moves){

			if (move != null){
				PacCell cell = board[move.x][move.y];
				if (cell instanceof HouseCell){
					continue;
				}
				if (newFood.contains(move)){
					//System.out.printf("%s will eat food\n",move.toString());
					newFood.remove(move);
				}
				BoardState bs = new BoardState(newFood,move,boardState.g1Pos,boardState.g2Pos);
				int val = min(bs, depth,alpha,beta);
				if (val>maxVal){
					maxVal = val;
					maxIndex = i;
				}

				//System.out.printf("%s dir:%d val: %d\n",pacPos.toString(),i,val);

			}
			i++;

		}

		//System.out.printf("Move taken: %s %d\n",moves.get(maxIndex),maxVal);

		//System.exit(0);
		switch(maxIndex){

			case 0:
				face = PacFace.N; // index 0
				break;
			case 1:
				face =PacFace.S; // index 1
				break;
			case 2:
				face = PacFace.E; // index 2
				break;
			case 3:
				face = PacFace.W; // index 3
				break;
			//default:
				//face = PacFace.N;

		}


		return face;
	}

	private PointInt nearest(Point start, List<Point> points){
		// for each point in points calculate bfs distance. retrun point with smallest distance

		Point closest = null;
		int minDist = Integer.MAX_VALUE;

		for (Point p: points){
			int temp = BFSPath.getPath(board,start,p).size();
			if (temp<minDist){
				closest =p;
				minDist = temp;
			}
		}

		PointInt pi = new PointInt(closest,minDist);
		return pi;

	}
}