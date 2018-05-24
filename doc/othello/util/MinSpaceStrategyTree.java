package othello.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import othello.model.Board;
import othello.model.IBoard;

public class MinSpaceStrategyTree implements StrategyTree {
	private static final int BITS = 
		(int) Math.ceil(Math.log((Color.values().length + 1)) / Math.log(2.0));
	private static final long BIT_MASK = (long) Math.pow(2, BITS) - 1;
	
	private final NodeComparator nodeComparator;
	private final IBoard board;
	private final MSNode root;
	
	public class MSNode implements Node {
		private final Color playerColor;
		private final long[] boardState; // représentation compact du plateau
		private final Node parent;
		private final int depth;
		private double evaluation;
		private SortedSet<Node> children; // null => enfants non générés
		
		// CONSTRUCTEURS
		public MSNode(MSNode parent, Coord move, double ev) {
			if (parent == null || move == null) {
				throw new AssertionError();
			}
			playerColor = Color.values()[(parent.playerColor.ordinal() + 1) % Color.values().length];
			children = null;
			evaluation = ev;
			boardState = parent.boardState.clone();
			this.parent = parent;
			depth = parent.depth + 1;
			setDisk(move, parent.playerColor);
		}
		
		public MSNode(IBoard b, Color c) {
			if (b == null) {
				throw new AssertionError();
			}
			playerColor = c;
			children = null;
			evaluation = Double.POSITIVE_INFINITY;
			boardState = boardToLong(b);
			parent = null;
			depth = 0;
		}
		
		// REQUETE
		// discutable
		public Iterator<Node> iterator() {
			return children.iterator();
		}
		
		public SortedSet<? extends Node> children() {
			return children;
		}
		
		public double getEval() {
			return evaluation;
		}
		
		public Color getPlayerColor() {
			return playerColor;
		}

		public Node getParent() {
			return parent;
		}
		
		public int getDepth() {
			return depth;
		}
		
		public List<Color> getAllDisks() {
			List<Color> res = new LinkedList<Color>();
			int size = board.getSize();
			
			for (int row = 0; row < size; row++) {
				for (int col = 0; col < size; col++) {
					res.add(getDisk(new Coord(row, col)));
				}
			}
			return res;
		}
		
		public Color getDisk(Coord move) {
			assert(move != null);
			assert(move.isInRect(new Coord(board.getSize(), board.getSize())));
			
			long a = (boardState[shift(move) / Long.SIZE] >> (shift(move) % Long.SIZE))
					& BIT_MASK;
			return a == 0 ? null :
				Color.values()[(int) a - 1];
		}
		
		public String toString() {
			String s = playerColor + ":eval=" + evaluation + ":" + System.lineSeparator();
			for (int row = 0; row < 8; row++) {
				for (int col = 0; col < 8; col++) {
					s += getDisk(new Coord(row, col)) + "\t";
				}
				s += System.lineSeparator();
			}
			return s;
		}
		
		public boolean equals(Object obj) {
			if (obj != null && this.getClass() == obj.getClass()) {
				MSNode c = (MSNode) obj;
				return evaluation == c.evaluation && boardState.equals(c.boardState);		// A DEVELOPPER
			}
			return false;
		}

		// COMMANDES
		public void setEval(double e) {
			evaluation = e;
		}
		
		public void setDisk(Coord move, Color c) {
			if (move != null) {
				updateCell(move, c);
			    for (Coord card : Coord.CARDINALS) {
			        for (Coord x = move.plus(card); getDisk(x) != c && getDisk(x) != null; x = x.plus(card)) {
			            updateCell(x, c);
			        }
			    }
			}
		}
		
		public void generateChildren() {
			children = new TreeSet<Node>(nodeComparator);

			for (Coord d : getDisksOfPlayer()) {
		        for (Coord card : Coord.CARDINALS) {
		            Coord x = d.plus(card);
		            if (board.isValid(x) && getDisk(x) != playerColor && getDisk(x) != null) {
		                x = x.plus(card);
		                while (board.isValid(x) && getDisk(x) != playerColor && getDisk(x) != null) {
		                    x = x.plus(card);
		                }
		                if (board.isValid(x) && getDisk(x) == null) {
		                    children.add(new MSNode(this, x, evaluation));
		                }
		            }
		        }
		    }
		}
		
		// OUTILS
		private void updateCell(Coord move, Color c) {
			assert(move != null && c != null);
			assert(move.isInRect(new Coord(board.getSize(), board.getSize())));

			long a = ((long) (c.ordinal() + 1)) << (shift(move) % Long.SIZE);
			long b = ((long) BIT_MASK) << (shift(move) % Long.SIZE);
			boardState[shift(move) / Long.SIZE] &= ~b;
			boardState[shift(move) / Long.SIZE] |= a;
		}
		
		private int shift(Coord c) {
			return BITS * (c.row() * board.getSize() + c.col());
		}
		
		/**
		 * Conversion d'un IBoard vers un long[] (représentation compact).
		 */
		private long[] boardToLong(IBoard board) {
			assert(board != null);
			
			int size = board.getSize();
			long[] res = new long[(size*size*BITS) / Long.SIZE];
			
			for (int row = size - 1; row >= 0; row--) {
				for (int col = size - 1; col >= 0; col--) {
					Coord coord = new Coord(row, col);
					Color c = board.getColor(coord);
					int ind = shift(coord) / Long.SIZE;
					res[ind] <<= BITS;
					res[ind] |= c == null ? 0 : (c.ordinal() + 1);
				}
			}
			return res;
		}
		
		private Set<Coord> getDisksOfPlayer() {
			int size = board.getSize();
			Set<Coord> res = new HashSet<Coord>();
			
			for (int row = 0; row < size; row++) {
				for (int col = 0; col < size; col++) {
					Coord coord = new Coord(row, col);
					if (getDisk(coord) == playerColor) {
						res.add(coord);
					}
				}
			}
			return res;
		}
		
	}
	
	// CONSTRUCTEURS
	public MinSpaceStrategyTree(IBoard board) {
		this.nodeComparator = new NodeComparator();
		this.board = board;
		this.root = new MSNode(board, Color.BLACK);
	}
	
	// REQUETES
	public IBoard getBoard() {
		return board;
	}
	
	public Node getRoot() {
		return root;
	}

	public static void main(String[] args) {
		IBoard b = new Board(8);
		b.putDisk(new Coord(3,3), Color.BLACK);
		b.putDisk(new Coord(4,4), Color.BLACK);
		b.putDisk(new Coord(3,4), Color.WHITE);
		b.putDisk(new Coord(4,3), Color.WHITE);
		
		MinSpaceStrategyTree t = new MinSpaceStrategyTree(b);
		Node m = t.getRoot();
		m.generateChildren();
		
		System.out.println(m);
		System.out.println("Children size: "+m.children().size()+"\n");
		
		System.out.println("Printing childs: \n");
		for (Node n : m) {
			System.out.println(n);
		}
		
	}
	
}
