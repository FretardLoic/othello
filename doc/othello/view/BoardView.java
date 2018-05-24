package othello.view;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import othello.model.AI;
import othello.model.IOthello;
import othello.model.Othello;
import othello.util.Color;
import othello.util.Coord;

public class BoardView {

	// ATTRIBUTS

    public final int BORDER_SIZE = 1;
	private static final Dimension DEFAULT_PREFERED_DIMENSION = new Dimension(30,30);
	private static final java.awt.Color BORDER_COLOR = java.awt.Color.BLACK;
	
    private IOthello model;
    private CellView[][] cells;
    private CellListener[][] cellListeners;
    private PropertyChangeListener turnListener;
    private PropertyChangeListener aiListener;
    private JFrame mainFrame;
    private JLabel currentPlayer;
    private JLabel whiteScore;
    private JLabel whitePlayer;
    private JLabel blackPlayer;
    private JLabel blackScore;
    private Set<Coord> possibilities;
    
    // CONSTRUCTEUR
    public BoardView(IOthello model) {
        createModel(model);
        createView();
        placeComponents();
        createController();
        if (model.getCurrentPlayer().getClass() == AI.class){
        	new Thread(new Runnable() {
				public void run() {
					model.turn(null);
				}
			}).start();
        }
    }
    
    // REQUETES
    public IOthello getModel() {
        return model;
    }
    
    // COMMANDES
    
    /**
     * affiche la partie d'othello du mod�le.
     */
    public void display() {
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
    
    public void setModel(IOthello model) {
        this.model = model;
        refresh();
    }
    
    //OUTILS
    private void createModel(IOthello model) {
        this.model = model;
        possibilities = model.getBoard().getValidMoves(model.getCurrentPlayer().getColor());
    }
    
    private void createView() {
        cells = new CellView[model.getBoard().getSize()][model.getBoard().getSize()];
        for(int i = 0 ; i < cells.length; ++i) {
        	for(int j = 0 ; j < cells[i].length; ++j) {
        		Coord coord = new Coord(i,j);
        		Color c = getModel().getBoard().getColor(coord);
        		if (c == Color.BLACK) {
                    cells[i][j] = new CellView(DrawableCell.BLACK);
        		} else if (c == Color.WHITE) {
                    cells[i][j] = new CellView(DrawableCell.WHITE);
        		} else if (possibilities.contains(coord)) {
                    cells[i][j] = new CellView(DrawableCell.VALID_MOVE);
        		} else {
                    cells[i][j] = new CellView(DrawableCell.INVALID_MOVE);
        		}
            }
        }
        mainFrame = new JFrame("Othello");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setResizable(false);
        currentPlayer = new JLabel("Joueur " 
        		+ model.getCurrentPlayer().getColor() + " doit jouer.", JLabel.CENTER);
        int width = currentPlayer.getWidth();
        int height = currentPlayer.getHeight() + 50;
        currentPlayer.setPreferredSize(new Dimension(width, height));
        currentPlayer.setFont(new Font("Serif", Font.PLAIN, 22));
        whiteScore = new JLabel(model.getBoard().getPointsPlayer(Color.WHITE) + "");
        whitePlayer = new JLabel(Color.WHITE.toString() + " : ");
        
        blackScore = new JLabel(model.getBoard().getPointsPlayer(Color.BLACK) + "");
        blackPlayer = new JLabel(Color.BLACK.toString() + " : ");
    }
    
    private void placeComponents() {
    	JPanel row = new JPanel(new GridLayout(model.getBoard().getSize(), 1)); {
			for(int i = 0 ; i < cells.length; ++i) {
				JLabel headerText = new JLabel((i +  1) +"", JLabel.CENTER);
				headerText.setForeground(java.awt.Color.WHITE);
				JPanel header = new JPanel();
				header.setPreferredSize(DEFAULT_PREFERED_DIMENSION);
				header.setBackground(new java.awt.Color(139,69,19));
				header.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, BORDER_SIZE));
				header.add(headerText);
				row.add(header);
	        }
		}
        JPanel p = new JPanel(new GridLayout(model.getBoard().getSize(), 
        		model.getBoard().getSize())); {
			for(int i = 0 ; i < cells.length; ++i) {
	        	for(int j = 0 ; j < cells[i].length; ++j) {
	            	p.add(cells[i][j]);
	            }
	        }
		}
		JPanel col = new JPanel(new GridLayout(1, model.getBoard().getSize())); {
			for(int i = 0 ; i < cells.length; ++i) {
				JLabel headerText = new JLabel((char) ('a' + i) + "", JLabel.CENTER);
				headerText.setForeground(java.awt.Color.WHITE);
				JPanel header = new JPanel();
				header.setPreferredSize(DEFAULT_PREFERED_DIMENSION);
				header.setBackground(new java.awt.Color(139,69,19));
				header.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, BORDER_SIZE));
				header.add(headerText);
				col.add(header);
	        }
		}
		
		JPanel board = new JPanel();
        board.setLayout(new GridBagLayout());
	    GridBagConstraints gbc = new GridBagConstraints();
			
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.gridheight = 3;
	    gbc.gridwidth = 1;
	    board.add(new JPanel(), gbc);
	    
	    gbc.gridx = 1;
	    gbc.gridheight = 1;
	    board.add(new JPanel(), gbc);
	    
	    gbc.gridx = 2;
        board.add(col, gbc);
        
        gbc.gridx = 3;
	    gbc.gridheight = 4;
	    board.add(new JPanel(), gbc);
	    
        gbc.gridx = 1;
	    gbc.gridy = 1;
	    gbc.gridheight = 1;
        board.add(row, gbc);
        
        gbc.gridx = 2;
        board.add(p, gbc);
        
        gbc.gridx = 0;
	    gbc.gridy = 2;
	    gbc.gridwidth = 3;
	    board.add(new JPanel(), gbc);
	    
        mainFrame.add(board, BorderLayout.CENTER);
        p = new JPanel(new GridLayout(2 ,1)); {
        	JPanel q = new JPanel(new GridLayout(1 ,2)); {
        		p.add(whitePlayer);
            	p.add(whiteScore);
        	}
        	p.add(q);
        	q = new JPanel(new GridLayout(1 ,2)); {
        		p.add(blackPlayer);
            	p.add(blackScore);
        	}
        	p.add(q);
		}
        mainFrame.add(p, BorderLayout.EAST);
        mainFrame.add(currentPlayer, BorderLayout.NORTH);
    }
    
    private void createController() {
    	cellListeners = new CellListener[model.getBoard().getSize()][model.getBoard().getSize()];
    	for(int i = 0 ; i < cells.length; ++i) {
        	for(int j = 0 ; j < cells[i].length; ++j) {
        		cellListeners[i][j] = new CellListener(new Coord(i,j));
            	cells[i][j].addMouseListener(cellListeners[i][j]);
            }
        }

        turnListener = new PropertyChangeListener() {
 			@Override
 			public void propertyChange(PropertyChangeEvent evt) {
 				refresh();
 			}
 		};
        model.addPropertyChangeListener(IOthello.TURN, turnListener);

 		aiListener = new PropertyChangeListener() {
 			@Override
 			public void propertyChange(PropertyChangeEvent evt) {
 				if ((boolean) evt.getNewValue()) {
 					for(int i = 0 ; i < cells.length; ++i) {
 			        	for(int j = 0 ; j < cells[i].length; ++j) {
 			            	cells[i][j].removeMouseListener(cellListeners[i][j]);
 			            }
 			        }
 				} else {
 					for(int i = 0 ; i < cells.length; ++i) {
 			        	for(int j = 0 ; j < cells[i].length; ++j) {
 			            	cells[i][j].addMouseListener(cellListeners[i][j]);
 			            }
 			        }
 				}
 			}
 		};
        model.addPropertyChangeListener(IOthello.AI_PLAY, aiListener);
    }
    
    /**
     * rafra�chis la vue.
     */
    private void refresh() {
        possibilities = model.getBoard().getValidMoves(model.getCurrentPlayer().getColor());
	    for(int i = 0 ; i < cells.length; ++i) {
	    	for(int j = 0 ; j < cells[i].length; ++j) {
	    		Coord coord = new Coord(i,j);
	    		Color c = getModel().getBoard().getColor(coord);
	    		if (c == Color.BLACK) {
	                cells[i][j].setDrawableCell(DrawableCell.BLACK);
	    		} else if (c == Color.WHITE) {
	                cells[i][j].setDrawableCell(DrawableCell.WHITE);
	    		} else if (possibilities.contains(coord)) {
	                cells[i][j].setDrawableCell(DrawableCell.VALID_MOVE);
	    		} else {
	                cells[i][j].setDrawableCell(DrawableCell.INVALID_MOVE);
	    		}
	    	}
	    }
    	whiteScore.setText(model.getBoard().getPointsPlayer(Color.WHITE) + "");
        blackScore.setText(model.getBoard().getPointsPlayer(Color.BLACK) + "");
    	currentPlayer.setText("Joueur " 
        		+ model.getCurrentPlayer().getColor().toString() + " doit jouer.");
    	if (model.isGameOver()) {
    		model.removePropertyChangeListener(IOthello.AI_PLAY, aiListener);
    		model.removePropertyChangeListener(IOthello.TURN, turnListener);
    		new PopUpResult(model);
    		mainFrame.dispose();
		}
    }
    
    //CLASSES INTERNES
    
    /**
     * Un Listener qui joue un coup lorsqu'un �v�nement lui est notifi�. 
     */
    class CellListener extends MouseAdapter {
    	
    	private Coord c;
    	
    	public CellListener(Coord c) {
    		this.c = c;
    	}
    	
    	public void mouseClicked(MouseEvent e) {
    		if (possibilities.contains(c)) {

    			new Thread(new Runnable() {
    				public void run() {
    					model.turn(c);
    				}
    			}).start();
    		}
    	}
    }

    
    // TEST
    public static void main(String[] args) {
    	IOthello model = new Othello();
        BoardView bv = new BoardView(model);
        bv.display();
    }
}
