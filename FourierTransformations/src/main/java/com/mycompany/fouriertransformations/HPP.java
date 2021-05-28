package com.mycompany.fouriertransformations;
import java.awt.* ;
import javax.swing.* ;

public class HPP {


    final static int NX = 80, NY = 60 ;  // Lattice dimensions
    final static int q = 4 ;  // population
     
    final static int NITER = 10000 ;
    final static int DELAY = 500 ;

    final static double DENSITY = 1.0 ;  // initial state, between 0 and 1.0.

    static Display display = new Display() ;

    static boolean [] [] [] fin = new boolean [NX] [NY] [q] ;
    static boolean [] [] [] fout = new boolean [NX] [NY] [q] ;

    public static void main(String args []) throws Exception {

        // initialize - populate a subblock of grid
        for(int i = 0; i < NX/4 ; i++) { 
            for(int j = 0; j < NY/4 ; j++) { 
                boolean [] fin_ij = fin [i] [j] ;
                for(int d = 0 ; d < q ; d++) {
                    if(Math.random() < DENSITY) {
                        fin_ij [d] = true ;
                    }
                }
            }
        }

        // initialize - staged head on collision in x direction.
        // fin [NX/2 + 1] [NY/2] [0] = true ;
        // fin [NX/2 - 1] [NY/2] [1] = true ;


        display.repaint() ;
        Thread.sleep(DELAY) ;

        for(int iter = 0 ; iter < NITER ; iter++) {

            // Collision

            for(int i = 0; i < NX ; i++) { 
                for(int j = 0; j < NY ; j++) { 
                    boolean [] fin_ij = fin [i] [j] ;
                    boolean [] fout_ij = fout [i] [j] ;

                    
                    // please add collisions as per lecture!
                    if(fin_ij [0] && fin_ij [1] && !fin_ij [2] && !fin_ij [3]) {
                        fout_ij [0] = false ;
                        fout_ij [1] = false ;
                        fout_ij [2] = true ;
                        fout_ij [3] = true ;
                    } else if(!fin_ij [0] && !fin_ij [1] && fin_ij [2] && fin_ij [3]){
                        fout_ij [0] = true ;
                        fout_ij [1] = true ;
                        fout_ij [2] = false ;
                        fout_ij [3] = false ;  
                    } else {
                     // default, no collisions case:
                        fout_ij [0] = fin_ij [0] ;
                        fout_ij [1] = fin_ij [1] ;
                        fout_ij [2] = fin_ij [2] ;
                        fout_ij [3] = fin_ij [3] ;
                    }
                }
            }

            // Streaming

            for(int i = 0; i < NX ; i++) { 
                int iP1 = (i + 1) % NX ;
                int iM1 = (i - 1 + NX) % NX ;
                for(int j = 0; j < NY ; j++) { 
                    int jP1 = (j + 1) % NY ;
                    int jM1 = (j - 1 + NY) % NY ;

                    // no streaming case:

                    fin [iM1] [j] [0] = fout [i] [j] [0] ;
                    fin [iP1] [j] [1] = fout [i] [j] [1] ;
                    fin [i] [jM1] [2] = fout [i] [j] [2] ;
                    fin [i] [jP1] [3] = fout [i] [j] [3] ;

                    // please add streaming as per lecture!
            
                }
            }

            System.out.println("iter = " + iter) ;
            display.repaint() ;
      
            Thread.sleep(DELAY) ;
        }
    }

    
    static class Display extends JPanel {

        final static int CELL_SIZE = 14 ; 

        public static final int ARROW_START = 2 ;
        public static final int ARROW_END   = 7 ;
        public static final int ARROW_WIDE  = 3 ;

        Display() {

            setPreferredSize(new Dimension(CELL_SIZE * NX, CELL_SIZE * NY)) ;

            JFrame frame = new JFrame("HPP");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(this);
            frame.pack();
            frame.setVisible(true);
        }
        public float localDensity(boolean[] fin_ij) {
			int pop = 0;
			for (int d = 0; d < q; d++) {
				if (fin_ij[d])
					pop++;
			}
			float localDensity = (float) pop / q;
			return localDensity;
		}

		public float averageDensity(boolean[][][] fin, int x, int y, int distance) {
			int count = 0;
			float totalDensity = 0;

			for (int i = 0; i < distance; i++) {
				int offsetX = x + (i - (distance / 2));
				int fi = Math.floorMod(offsetX, NX);
				for (int j = 0; j < distance; j++) {
					int offsetY = y + (j - (distance / 2));
					int fj = Math.floorMod(offsetY, NY);
					totalDensity += localDensity(fin[fi][fj]);
					count++;
				}
			}
			return totalDensity / count;
		}

        public void paintComponent(Graphics g) {

            g.setColor(Color.WHITE) ;
            g.fillRect(0, 0, CELL_SIZE * NX, CELL_SIZE * NY) ;

            g.setColor(Color.BLUE) ;
            for(int i = 0 ; i < NX ; i++) {
                for(int j = 0 ; j < NY ; j++) {
                    boolean [] fin_ij = fin [i] [j] ;
                    int originX = CELL_SIZE * i + CELL_SIZE/2 ;
                    int originY = CELL_SIZE * j + CELL_SIZE/2 ;
                    if (fin_ij[0] || fin_ij[1] || fin_ij[2] || fin_ij[3]) {
						int values = (int) (255 - (255 * averageDensity(fin, i, j, 5)));
						g.setColor(new Color(values, values, 255));
						g.fillRect(originX, originY, originX + CELL_SIZE, originY + CELL_SIZE);
					}
                }
            } 
        }
    }
}

