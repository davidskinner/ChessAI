import javax.management.RuntimeMBeanException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import static java.lang.Math.max;
import static java.lang.Math.min;

/// Represents the state of a chess game
class ChessState
{
	//an int representing every piece/ mask on the board
	public static final int MAX_PIECE_MOVES = 27;
	public static final int None = 0;
	public static final int Pawn = 1;
	public static final int Rook = 2;
	public static final int Knight = 3;
	public static final int Bishop = 4;
	public static final int Queen = 5;
	public static final int King = 6;
	public static final int PieceMask = 7;
	public static final int WhiteMask = 8;
	public static final int AllMask = 15;
	public static int whiteDepth;
	public static int blackDepth;

	Random rand = new Random();

	int[] m_rows;

	//default constructor
	ChessState()
	{
		//this represents every piece on the board in 8 integers
		m_rows = new int[8];
		resetBoard();
	}

	//copy constructor
	ChessState(ChessState that)
	{
		m_rows = new int[8];
		for (int i = 0; i < 8; i++)
			this.m_rows[i] = that.m_rows[i];
	}

	//no idea
	int getPiece(int col, int row)
	{

		return (m_rows[row] >> (4 * col)) & PieceMask;
	}

	//no idea
	boolean isWhite(int col, int row)
	{

		return (((m_rows[row] >> (4 * col)) & WhiteMask) > 0 ? true : false);
	}

	/// Sets the piece at location (col, row). If piece is None, then it doesn't
	/// matter what the value of white is.
	void setPiece(int col, int row, int piece, boolean white)
	{

		m_rows[row] &= (~(AllMask << (4 * col)));
		m_rows[row] |= ((piece | (white ? WhiteMask : 0)) << (4 * col));
	}

	/// Sets up the board for a new game
	void resetBoard()
	{
		setPiece(0, 0, Rook, true);
		setPiece(1, 0, Knight, true);
		setPiece(2, 0, Bishop, true);
		setPiece(3, 0, Queen, true);
		setPiece(4, 0, King, true);
		setPiece(5, 0, Bishop, true);
		setPiece(6, 0, Knight, true);
		setPiece(7, 0, Rook, true);
		for (int i = 0; i < 8; i++)
			setPiece(i, 1, Pawn, true);
		for (int j = 2; j < 6; j++)
		{
			for (int i = 0; i < 8; i++)
				setPiece(i, j, None, false);
		}
		for (int i = 0; i < 8; i++)
			setPiece(i, 6, Pawn, false);
		setPiece(0, 7, Rook, false);
		setPiece(1, 7, Knight, false);
		setPiece(2, 7, Bishop, false);
		setPiece(3, 7, Queen, false);
		setPiece(4, 7, King, false);
		setPiece(5, 7, Bishop, false);
		setPiece(6, 7, Knight, false);
		setPiece(7, 7, Rook, false);
	}

	/// Positive means white is favored. Negative means black is favored.
	int heuristic(Random rand)
	{
		int score = 0;
		for (int y = 0; y < 8; y++)
		{
			for (int x = 0; x < 8; x++)
			{
				int p = getPiece(x, y);
				int value;
				switch (p)
				{
					case None:
						value = 0;
						break;
					case Pawn:
						value = 10;
						break;
					case Rook:
						value = 63;
						break;
					case Knight:
						value = 31;
						break;
					case Bishop:
						value = 36;
						break;
					case Queen:
						value = 88;
						break;
					case King:
						value = 500;
						break;
					default:
						throw new RuntimeException("what?");
				}
				if (isWhite(x, y))
					score += value;
				else
					score -= value;
			}
		}
		return score + rand.nextInt(3) - 1;
	}

	/// Iterates through all the possible moves for the specified color.
	static class ChessMoveIterator
	{
		int x, y;
		ArrayList<Integer> moves;
		ChessState state;
		boolean white;

		/// Constructs a move iterator
		ChessMoveIterator(ChessState curState, boolean whiteMoves)
		{
			x = -1;
			y = 0;
			moves = null;
			state = curState;
			white = whiteMoves;
			advance();
		}

		private void advance()
		{

			if (moves != null && moves.size() >= 2)
			{
				moves.remove(moves.size() - 1);
				moves.remove(moves.size() - 1);
			}
			while (y < 8 && (moves == null || moves.size() < 2))
			{
				if (++x >= 8)
				{
					x = 0;
					y++;
				}
				if (y < 8)
				{
					if (state.getPiece(x, y) != ChessState.None && state.isWhite(x, y) == white)
						moves = state.moves(x, y);
					else
						moves = null;
				}
			}
		}

		/// Returns true iff there is another move to visit
		boolean hasNext()
		{

			return (moves != null && moves.size() >= 2);
		}

		/// Returns the next move
		ChessState.ChessMove next()
		{

			ChessState.ChessMove m = new ChessState.ChessMove();
			m.xSource = x;
			m.ySource = y;
			m.xDest = moves.get(moves.size() - 2);
			m.yDest = moves.get(moves.size() - 1);
			advance();
			return m;
		}
	}

	/// Returns an iterator that iterates over all possible moves for the specified color ****
	ChessMoveIterator iterator(boolean white)
	{

		return new ChessMoveIterator(this, white);
	}

	/// Returns true iff the parameters represent a valid move
	boolean isValidMove(int xSrc, int ySrc, int xDest, int yDest)
	{

		ArrayList<Integer> possible_moves = moves(xSrc, ySrc);
		for (int i = 0; i < possible_moves.size(); i += 2)
		{
			if (possible_moves.get(i).intValue() == xDest && possible_moves.get(i + 1).intValue() == yDest)
				return true;
		}
		return false;
	}

	/// Print a representation of the board to the specified stream
	void printBoard(PrintStream stream)
	{

		stream.println("  A  B  C  D  E  F  G  H");
		stream.print(" +");
		for (int i = 0; i < 8; i++)
			stream.print("--+");
		stream.println();
		for (int j = 7; j >= 0; j--)
		{
			stream.print(Character.toString((char) (49 + j)));
			stream.print("|");
			for (int i = 0; i < 8; i++)
			{
				int p = getPiece(i, j);
				if (p != None)
				{
					if (isWhite(i, j))
						stream.print("w");
					else
						stream.print("b");
				}
				switch (p)
				{
					case None:
						stream.print("  ");
						break;
					case Pawn:
						stream.print("p");
						break;
					case Rook:
						stream.print("r");
						break;
					case Knight:
						stream.print("n");
						break;
					case Bishop:
						stream.print("b");
						break;
					case Queen:
						stream.print("q");
						break;
					case King:
						stream.print("K");
						break;
					default:
						stream.print("?");
						break;
				}
				stream.print("|");
			}
			stream.print(Character.toString((char) (49 + j)));
			stream.print("\n +");
			for (int i = 0; i < 8; i++)
				stream.print("--+");
			stream.println();
		}
		stream.println("  A  B  C  D  E  F  G  H");
	}

	/// Pass in the coordinates of a square with a piece on it
	/// and it will return the places that piece can move to.
	ArrayList<Integer> moves(int col, int row)
	{

		ArrayList<Integer> pOutMoves = new ArrayList<Integer>();
		int p = getPiece(col, row);
		boolean bWhite = isWhite(col, row);
		int nMoves = 0;
		int i, j;

		//for every type of piece on the board, get the moves
		switch (p)
		{
			case Pawn:
				if (bWhite)
				{
					if (!checkPawnMove(pOutMoves, col, inc(row), false, bWhite) && row == 1)
						checkPawnMove(pOutMoves, col, inc(inc(row)), false, bWhite);
					checkPawnMove(pOutMoves, inc(col), inc(row), true, bWhite);
					checkPawnMove(pOutMoves, dec(col), inc(row), true, bWhite);
				} else
				{
					if (!checkPawnMove(pOutMoves, col, dec(row), false, bWhite) && row == 6)
						checkPawnMove(pOutMoves, col, dec(dec(row)), false, bWhite);
					checkPawnMove(pOutMoves, inc(col), dec(row), true, bWhite);
					checkPawnMove(pOutMoves, dec(col), dec(row), true, bWhite);
				}
				break;
			case Bishop:
				for (i = inc(col), j = inc(row); true; i = inc(i), j = inc(j))
					if (checkMove(pOutMoves, i, j, bWhite))
						break;
				for (i = dec(col), j = inc(row); true; i = dec(i), j = inc(j))
					if (checkMove(pOutMoves, i, j, bWhite))
						break;
				for (i = inc(col), j = dec(row); true; i = inc(i), j = dec(j))
					if (checkMove(pOutMoves, i, j, bWhite))
						break;
				for (i = dec(col), j = dec(row); true; i = dec(i), j = dec(j))
					if (checkMove(pOutMoves, i, j, bWhite))
						break;
				break;
			case Knight:
				checkMove(pOutMoves, inc(inc(col)), inc(row), bWhite);
				checkMove(pOutMoves, inc(col), inc(inc(row)), bWhite);
				checkMove(pOutMoves, dec(col), inc(inc(row)), bWhite);
				checkMove(pOutMoves, dec(dec(col)), inc(row), bWhite);
				checkMove(pOutMoves, dec(dec(col)), dec(row), bWhite);
				checkMove(pOutMoves, dec(col), dec(dec(row)), bWhite);
				checkMove(pOutMoves, inc(col), dec(dec(row)), bWhite);
				checkMove(pOutMoves, inc(inc(col)), dec(row), bWhite);
				break;
			case Rook:
				for (i = inc(col); true; i = inc(i))
					if (checkMove(pOutMoves, i, row, bWhite))
						break;
				for (i = dec(col); true; i = dec(i))
					if (checkMove(pOutMoves, i, row, bWhite))
						break;
				for (j = inc(row); true; j = inc(j))
					if (checkMove(pOutMoves, col, j, bWhite))
						break;
				for (j = dec(row); true; j = dec(j))
					if (checkMove(pOutMoves, col, j, bWhite))
						break;
				break;
			case Queen:
				for (i = inc(col); true; i = inc(i))
					if (checkMove(pOutMoves, i, row, bWhite))
						break;
				for (i = dec(col); true; i = dec(i))
					if (checkMove(pOutMoves, i, row, bWhite))
						break;
				for (j = inc(row); true; j = inc(j))
					if (checkMove(pOutMoves, col, j, bWhite))
						break;
				for (j = dec(row); true; j = dec(j))
					if (checkMove(pOutMoves, col, j, bWhite))
						break;
				for (i = inc(col), j = inc(row); true; i = inc(i), j = inc(j))
					if (checkMove(pOutMoves, i, j, bWhite))
						break;
				for (i = dec(col), j = inc(row); true; i = dec(i), j = inc(j))
					if (checkMove(pOutMoves, i, j, bWhite))
						break;
				for (i = inc(col), j = dec(row); true; i = inc(i), j = dec(j))
					if (checkMove(pOutMoves, i, j, bWhite))
						break;
				for (i = dec(col), j = dec(row); true; i = dec(i), j = dec(j))
					if (checkMove(pOutMoves, i, j, bWhite))
						break;
				break;
			case King:
				checkMove(pOutMoves, inc(col), row, bWhite);
				checkMove(pOutMoves, inc(col), inc(row), bWhite);
				checkMove(pOutMoves, col, inc(row), bWhite);
				checkMove(pOutMoves, dec(col), inc(row), bWhite);
				checkMove(pOutMoves, dec(col), row, bWhite);
				checkMove(pOutMoves, dec(col), dec(row), bWhite);
				checkMove(pOutMoves, col, dec(row), bWhite);
				checkMove(pOutMoves, inc(col), dec(row), bWhite);
				break;
			default:
				break;
		}
		return pOutMoves;
	}

	/// Moves the piece from (xSrc, ySrc) to (xDest, yDest). If this move
	/// gets a pawn across the board, it becomes a queen. If this move
	/// takes a king, then it will remove all pieces of the same color as
	/// the king that was taken and return true to indicate that the move
	/// ended the game.
	boolean move(int xSrc, int ySrc, int xDest, int yDest)
	{

		if (xSrc < 0 || xSrc >= 8 || ySrc < 0 || ySrc >= 8)
			throw new RuntimeException("out of range");
		if (xDest < 0 || xDest >= 8 || yDest < 0 || yDest >= 8)
			throw new RuntimeException("out of range");
		int target = getPiece(xDest, yDest);
		int p = getPiece(xSrc, ySrc);
		if (p == None)
			throw new RuntimeException("There is no piece in the source location: " + String.valueOf(xSrc) + " , " + String.valueOf(ySrc));
		if (target != None && isWhite(xSrc, ySrc) == isWhite(xDest, yDest))
			throw new RuntimeException("It is illegal to take your own piece: " + String.valueOf(xSrc) + " , " + String.valueOf(ySrc) +" to "
					+ String.valueOf(xDest) + " , " + String.valueOf(yDest));
		if (p == Pawn && (yDest == 0 || yDest == 7))
			p = Queen; // a pawn that crosses the board becomes a queen
		boolean white = isWhite(xSrc, ySrc);
		setPiece(xDest, yDest, p, white);
		setPiece(xSrc, ySrc, None, true);
		if (target == King)
		{
			// If you take the opponent's king, remove all of the opponent's pieces. This
			// makes sure that look-ahead strategies don't try to look beyond the end of
			// the game (example: sacrifice a king for a king and some other piece.)
			int x, y;
			for (y = 0; y < 8; y++)
			{
				for (x = 0; x < 8; x++)
				{
					if (getPiece(x, y) != None)
					{
						if (isWhite(x, y) != white)
						{
							setPiece(x, y, None, true);
						}
					}
				}
			}
			return true;
		}
		return false;
	}

	//increment?
	static int inc(int pos)
	{

		if (pos < 0 || pos >= 7)
			return -1;
		return pos + 1;
	}

	//decrement?
	static int dec(int pos)
	{

		if (pos < 1)
			return -1;
		return pos - 1;
	}

	//
	boolean checkMove(ArrayList<Integer> pOutMoves, int col, int row, boolean bWhite)
	{

		if (col < 0 || row < 0)
			return true;
		int p = getPiece(col, row);
		if (p > 0 && isWhite(col, row) == bWhite)
			return true;
		pOutMoves.add(col);
		pOutMoves.add(row);
		return (p > 0);
	}

	boolean checkPawnMove(ArrayList<Integer> pOutMoves, int col, int row, boolean bDiagonal, boolean bWhite)
	{

		if (col < 0 || row < 0)
			return true;
		int p = getPiece(col, row);
		if (bDiagonal)
		{
			if (p == None || isWhite(col, row) == bWhite)
				return true;
		} else
		{
			if (p > 0)
				return true;
		}
		pOutMoves.add(col);
		pOutMoves.add(row);
		return (p > 0);
	}

	/// Represents a possible move
	static class ChessMove
	{
		int xSource;
		int ySource;
		int xDest;
		int yDest;
	}

	//this takes a move from minimaxab
	ChessMove bestState(ChessState currentState, boolean isWhite)
	{
		ChessMove bestMove = new ChessMove();
		ChessMove tempMove;
		int bestVal = Integer.MIN_VALUE;
		int tempVal;

		//gets all the white or black moves
		ChessMoveIterator it = currentState.iterator(isWhite);
		printIteratorSize(currentState,isWhite);

		//go through every move
		while(it.hasNext())
		{
			tempMove = it.next();
			ChessState tempState = new ChessState(currentState);
			tempState.move(tempMove.xSource, tempMove.ySource, tempMove.xDest, tempMove.yDest);
			tempState.printBoard(System.out);

			tempVal = tempState.miniMaxAB(tempState, whiteDepth, Integer.MIN_VALUE, Integer.MAX_VALUE,isWhite,isWhite);

			if(tempVal > bestVal)
			{
				bestMove = tempMove;
				bestVal = tempVal;
			}
		}
		return bestMove;
	}

	boolean hoomanMove(boolean white)
	{

		Scanner scanner = new Scanner(System.in);
		ChessMove hoomanMove = new ChessMove();

		String hoomanEntry = String.valueOf(scanner.next());

		log(String.valueOf(hoomanEntry.length()));
		if (hoomanEntry.length() == 4)
		{
			hoomanMove.xSource = Integer.valueOf(String.valueOf(hoomanEntry.charAt(0)));
			hoomanMove.ySource = Integer.valueOf(String.valueOf(hoomanEntry.charAt(1)));
			hoomanMove.xDest = Integer.valueOf(String.valueOf(hoomanEntry.charAt(2)));
			hoomanMove.yDest = Integer.valueOf(String.valueOf(hoomanEntry.charAt(3)));
		}

		boolean didYouWin = false;

		if (isValidMove(hoomanMove.xSource, hoomanMove.ySource, hoomanMove.xDest, hoomanMove.yDest))
		{
			didYouWin = this.move(hoomanMove.xSource, hoomanMove.ySource, hoomanMove.xDest, hoomanMove.yDest);
		}

		if (didYouWin)
		{
			return true;
		} else
		{
			return false;
		}
	}

	private int miniMaxAB(ChessState state, int depth, int alpha, int beta, boolean maximizingPlayer, boolean isWhite)
	{
		ChessState copiedState = new ChessState(state);
		int value;
		ChessState.ChessMove m;
		ChessMoveIterator it = copiedState.iterator(isWhite);
		printIteratorSize(copiedState, isWhite);

		if (depth == 0)
		{
			return copiedState.heuristic(rand);
		}

		if (maximizingPlayer)
		{
			value = Integer.MIN_VALUE;

			while (it.hasNext())
			{
				m = it.next();
				copiedState.move(m.xSource, m.ySource, m.xDest, m.yDest);
				copiedState.printBoard(System.out);
				value = max(value, copiedState.miniMaxAB(copiedState, depth - 1, alpha, beta, !maximizingPlayer, !isWhite));
				alpha = max(alpha, value);
				if (alpha >= beta)
				{
					break;
				}
			}
			return value;
		} else
		{
			value = Integer.MAX_VALUE;

			while (it.hasNext())
			{
				m = it.next();
				copiedState.move(m.xSource, m.ySource, m.xDest, m.yDest);
				copiedState.printBoard(System.out);
				value = min(value, copiedState.miniMaxAB(copiedState, depth - 1, alpha, beta, !maximizingPlayer, !isWhite));
				beta = min(beta, value);
				if (alpha >= beta)
				{
					break;
				}
			}
			return value;
		}
	}

	public static void printIteratorSize(ChessState state, boolean isWhite)
	{
		ChessMoveIterator testIt = state.iterator(isWhite);
		ChessMove testMove;
		ArrayList<ChessMove> moveList = new ArrayList<>();

		//see the number of possible moves for the initial node
		while(testIt.hasNext())
		{
			testMove = testIt.next();
			moveList.add(testMove);
		}
		log(String.valueOf(moveList.size()));
	}

	public static void printMove(ChessMove move)
	{
		log(Integer.toString(move.xSource));
		log(Integer.toString(move.ySource));
		log(Integer.toString(move.xSource));
		log(Integer.toString(move.ySource));
	}

	public static void log(String v)
	{
		System.out.println(v);
	}

	public static void main(String[] args)
	{
		boolean whiteTurn = true;
		boolean whiteHooman = false;
		boolean blackHooman = false;
		int winner = 0;

		if (args.length != 2)
			throw new RuntimeException("You must enter 2 space-separated integers for depth of tree for black and white");

		//number of moves the white player looks ahead
		whiteDepth = Integer.valueOf(args[0]);

		//number of moves the black player looks ahead
		blackDepth = Integer.valueOf(args[1]);

		log("");
		if (whiteDepth == 0)
		{
			whiteHooman = true;
			log("white is a hooman");
		}
		else
		{
			log("white is a computer");
		}

		if (blackDepth == 0)
		{
			blackHooman = true;
			log("black is a hooman");
		}
		else
		{
			log("black is a computer");
		}
		log("");


		ChessState board = new ChessState();
		board.resetBoard();
		board.printBoard(System.out);

		System.out.println();

		//while game is not over
		//depending on whose turn it is:
		//        board.move(1/*B*/, 0/*1*/, 2/*C*/, 2/*3*/);
		//        board.printBoard(System.out);


		int i = 0;

		while (i <= 1)
		{
			if (whiteTurn)
			{
				//1002
				//is the player hooman or AI?
				if (whiteHooman)
				{
					board.hoomanMove(true);

				} else
				{
					//move a piece from the white side
					ChessState.ChessMove m = board.bestState(board, true);
					board.move(m.xSource, m.ySource, m.xDest, m.yDest);
					board.printBoard(System.out);
					log("");
				}
				board.printBoard(System.out);
				log("");

			} else
			{
				//1705
				//is the player hooman or AI?
				if (blackHooman)
				{
					//take in input from black player
					board.hoomanMove(false);
				} else
				{
					//move a piece from the white side
					ChessState.ChessMove m = new ChessMove();
					m = board.bestState(board, false);
					board.move(m.xSource, m.ySource, m.xDest, m.yDest);
					board.printBoard(System.out);
					log("");
				}

				board.printBoard(System.out);
				log("");
			}

			whiteTurn = false;
			i++;
		}


	}
}


