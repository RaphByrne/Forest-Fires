import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;


public class CAModel {

	
	
	private final static int EMPTY = 0, TREE = 1, ONFIRE = 2;
	
	int[][] lattice;
	int width, height;
	double growthrate;
	double lighteningChance;
	private final int[][] moore = {{0,0},{0,1},{1,1},{1,0},{0,-1},{-1,-1},{-1,0},{1,-1},{-1,1}};
	int numTrees = 0;
	int numFires = 0;
	int numEmpty = 0;
	
	/**
	 * Makes makes an initial random configuration
	 * @param width
	 * @param height
	 * @param numtrees
	 */
	public CAModel(int width, int height, int numtrees, double growthrate) {
		this.width = width;
		this.height = height;
		this.growthrate = growthrate;
		this.lighteningChance = 0;
		lattice = new int[width][height];
		for(int i = 0; i < width; i++)
			for(int j = 0; j < width; j++)
				lattice[i][j] = EMPTY;
		
		long seed = System.currentTimeMillis();
		Random rand = new Random(seed);
		for(int i = 0; i < numtrees; i++) {
			int x = rand.nextInt(width);
			int y = rand.nextInt(height);
			if(lattice[x][y] != TREE) {
				lattice[x][y] = TREE;
			}
		}
		numTrees = numtrees;
		numEmpty = width*height - numTrees;
	}
	
	/**
	 * Makes a new model to the spec of the assignment
	 * @param width the width of the lattice
	 * @param height the hieght of the lattice
	 * @param q the probably that a cell will intially contain a tree
	 * @param growthrate the probably that a new tree will grow in an empty cell
	 * @param lighteningChance probably of a tree catching fire
	 */
	public CAModel(int width, int height, double q, double growthrate, double lighteningChance) {
		this.width = width;
		this.height = height;
		this.growthrate = growthrate;
		this.lighteningChance = lighteningChance;
		lattice = new int[width][height];
		long seed = System.currentTimeMillis();
		Random rand = new Random(seed);
		for(int i = 0; i < width; i++)
			for(int j = 0; j < width; j++)
				if(rand.nextDouble() < q) {
					lattice[i][j] = TREE;
					numTrees++;
				}
				else
					lattice[i][j] = EMPTY;
		numEmpty = width*height - numTrees;
	}
		
		

	/**
 	* Makes a model from an existing lattice
 	*
 	* @param lattice the lattice, a full clone is made
 	* @param growthrate
 	* @param lighteningChance
 	*
 	*/ 	
	public CAModel(int[][] lattice, double growthrate, double lighteningChance) {
		this.lattice = latticeClone(lattice);
		this.width = lattice.length;
		this.height = lattice[0].length;
		this.growthrate = growthrate;
		this.lighteningChance = lighteningChance;
	}


	/**
 	* Performs one iteration of the model
 	*
 	*/ 	
	public void step() {
		Random rand = new Random(System.currentTimeMillis());
		int[][] nextLattice = new int[width][height];
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++) {
				if(lattice[i][j] == ONFIRE) { //FIRE -> EMPTY
					nextLattice[i][j] = EMPTY;
					numFires--;
				} else if(lattice[i][j] == TREE){ //TREE -> FIRE if at least one fire in neighbourhood or randomly
					//here I'm assuming Moore neighbourhoods
					boolean onfire = false;
					for(Integer x : getNeighbourhood(lattice, i , j, moore)) {
						if(x == ONFIRE)
							onfire = true;
					}
					if(rand.nextDouble() < lighteningChance)
						onfire = true;
					if(onfire) {
						nextLattice[i][j] = ONFIRE;
						numFires++;
						numTrees--;
					} else
						nextLattice[i][j] = TREE;
				} else if(lattice[i][j] == EMPTY) { //EMPTY -> TREE with probability p
					if(rand.nextDouble() < growthrate) {
						nextLattice[i][j] = TREE;
						numTrees++;
					} else
						nextLattice[i][j] = EMPTY;
				}
			}
		lattice = nextLattice;
		numEmpty = width*height - numTrees - numFires;
	}
	
	/**
	 * Gets the contents of the neighbourhood cells of a point (i,j) from the lattice 
	 * using the neighbourhood direction vectors dirs
	 * @param dirs
	 * @return
	 */
	public static ArrayList<Integer> getNeighbourhood(int[][] lattice, int i, int j, int[][] dirs) {
		ArrayList<Integer> a = new ArrayList<Integer>();
		int width = lattice.length;
		int height = lattice[0].length;
		for(int[] v : dirs) {
			if(i + v[0] > 0 && i + v[0] < width && j + v[1] > 0 && j + v[1] < height)
				a.add(lattice[i + v[0]][j + v[1]]);
		}
		return a;
	}
	
	/**
 	* Makes a random change to the lattice, mostly used in initial development to
 	* generate random models
 	*
 	* IMPORTANT, DOESN'T UPDATE TREE/FIRE COUNTS
 	*/ 
	public void randomChange() {
		long seed = System.currentTimeMillis();
		Random rand = new Random(seed);
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++)
				lattice[i][j] = rand.nextInt(3);
	}
	
	/**
 	* Sets a particular cell on fire
 	* @param i the x co-ord
 	* @param j the y co-ord
 	* @return true if the cell was previously not on fire and now is
 	*/ 
	public boolean setFire(int i, int j) {
		if(i > 0 && i < width && j  > 0 && j < height)
			if(lattice[i][j] == TREE) {
				lattice[i][j] = ONFIRE;
				numTrees--;
				numFires++;
				return true;
			}
		return false;
	}
	
	/**
 	* Sets a random cell on fire
 	*/	 
	public void setRandomFire() {
		Random rand = new Random(System.currentTimeMillis());
		int x = rand.nextInt(width);
		int y = rand.nextInt(height);
		while(lattice[x][y] != TREE) {
			x = rand.nextInt(width);
			y = rand.nextInt(height);
			
		}
		setFire(x, y);
	}
	
	/**
	 * Sets fire to any trees within a circle around a given point
	 * 
	 * It's kinda inefficient but you only run it once at a time so who cares
	 * @param radius
	 * @param centre
	 */
	public void setFireCircle(int radius, int x, int y) {
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++) {
				if(Math.sqrt((i - x)*(i-x) + (j - y)*(j-y)) < radius)
					setFire(i,j);
			}
	}
	
	/**
 	* Sets fire to a small circle in the middle of the lattice
 	*/ 
	public void setFireCentre() {
		setFireCircle(Math.min(width/50, height/50),width/2,height/2);		
	}
	
	/**
 	* Does a full clone of an existing model
 	*/ 
	public CAModel clone() {
		return new CAModel(lattice, growthrate, lighteningChance);
	}
	
	/**
 	* Array cloning helper function
 	* @param lattice the array to clone
 	* @return a clone of lattice
 	*/ 
	public static int[][] latticeClone(int[][] lattice) {
		int[][] out = new int[lattice.length][lattice[0].length];
		for(int i = 0; i < lattice.length; i++)
			for(int j = 0; j < lattice[0].length; j++)
				out[i][j] = lattice[i][j];
		return out;
	}
	
}
