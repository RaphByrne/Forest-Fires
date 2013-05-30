import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;


public class CAModel {

	
	
	private final static int EMPTY = 0, TREE = 1, ONFIRE = 2;
	
	int[][] lattice;
	int width, height;
	double q = 0;
	double growthrate; //aka p
	double lighteningChance; //aka f
	double burnResist=-1;
	double windX = 0;
	double windY = 0;
	static double[][] defWindMap = {{1,1},{1,1},{1,1},{1,1},{1,1},{1,1},{1,1},{1,1},{1,1}};
	double[][] windMap = {{1,1},{1,1},{1,1},{1,1},{1,1},{1,1},{1,1},{1,1},{1,1}}; //this is the default for moore neighbourhoods
	private final int[][] moore = {{0,0},{0,1},{1,1},{1,0},{0,-1},{-1,-1},{-1,0},{1,-1},{-1,1}};
	private final int[][] vonNeumann = {{0,0},{0,1},{1,0},{-1,0},{0,-1}};
	int numTrees = 0;
	int numFires = 0;
	int numEmpty = 0;
	int tics = 0;
		
	public int[][] genNewLattice(int width, int height, double q) {	
		lattice = new int[width][height];
		long seed = System.currentTimeMillis();
		Random rand = new Random(seed);
		for(int i = 0; i < width; i++)
			for(int j = 0; j < width; j++)
				if(rand.nextDouble() < q) {
					lattice[i][j] = TREE;
				} else
					lattice[i][j] = EMPTY;
		return lattice;
	}

	public int getNumTrees(int[][] lattice) {
		int trees = 0;
		for(int[] l : lattice) {
			for(int i : l)
				if(i == TREE)
					trees++;
		}
		return trees;
	}

	public int getNumFires(int[][] lattice) {
		int fires = 0;
		for(int[] l : lattice) {
			for(int i : l)
				if(i == ONFIRE)
					fires++;
		}
		return fires;
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
		this.q = q;
		lattice = genNewLattice(width,height,q);
		numTrees = getNumTrees(lattice);
		numEmpty = width*height - numTrees;
	}
		
	/**
	 * Makes a new model to the spec of the assignment, with tree 'resistance to burning'
	 * @param width the width of the lattice
	 * @param height the hieght of the lattice
	 * @param q the probably that a cell will intially contain a tree
	 * @param growthrate the probably that a new tree will grow in an empty cell
	 * @param lighteningChance probably of a tree catching fire
	 */
	public CAModel(int width, int height, double q, double growthrate, double lighteningChance, double burnResist) {
		this(width,height,q,growthrate,lighteningChance);
		this.burnResist = burnResist;
	}	
	
	/**
	 * Makes a new model to the spec of the assignment, with wind
	 * @param width the width of the lattice
	 * @param height the hieght of the lattice
	 * @param q the probably that a cell will intially contain a tree
	 * @param growthrate the probably that a new tree will grow in an empty cell
	 * @param lighteningChance probably of a tree catching fire
	 */
	public CAModel(int width, int height, double q, double growthrate, double lighteningChance, double burnResist, double windX, double windY) {
		this(width,height,q,growthrate,lighteningChance, burnResist);
		this.windX = windX;
		this.windY = windY;
		windMap = getWindMap(moore,windY,windX); //these are reversed because arrays
	}
	
	//consructor for using an existing model
	public CAModel(CAModel model) {
		this.width = model.width;
		this.height = model.height;
		this.lattice = latticeClone(model.lattice);
		this.q = model.q;
		this.growthrate = model.growthrate;
		this.lighteningChance = model.lighteningChance;
		this.burnResist = model.burnResist;
		this.windX = model.windX;
		this.windY = model.windY;
		this.tics = model.tics;
		windMap = model.windMap; 
		numTrees = model.numTrees;
		numFires = model.numFires;
		numEmpty = model.numEmpty;
	}	

	/**
 	* Generates a wind map for use in fire propagation with wind. Takes a neighbourhood and x and y wind strengths.
 	* Wind strength should be at least 1
 	*/	
	public static double[][] getWindMap(int[][] dirs, double windX, double windY) {
		if(windX == 0 && windY == 0)
			return defWindMap; //if they've turned wind off just return the default	
		double[][] map = new double[dirs.length][2];
		for(int i = 0; i < dirs.length; i++) {
			int[] v = dirs[i];
			double x = (double)v[0];
			double y = (double)v[1];
			double[] m = new double[2];
			if(x*windX <= 0) //if they have opposite signs or 0
				m[0] = Math.abs(x*windX); //multiply
			else
				m[0] = Math.abs(1/windX); //otherwise it's the wrong way so less strong
			if(y*windY <= 0)
				m[1] = Math.abs(y*windY);
			else
				m[1] = Math.abs(1/windY);
			map[i] = m;
		}
		return map;
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
					//checks neighbourhood; if a tree is on fire this tree catches fire
					int[] neighbourhood = getNeighbourhood(lattice, i , j, moore);
					for(int n = 0; n < neighbourhood.length; n++) {
						if(neighbourhood[n] == ONFIRE) {
							if(rand.nextDouble()*Math.abs(windX) < windMap[n][0] || rand.nextDouble()*Math.abs(windY) < windMap[n][1])
								if (rand.nextDouble() > burnResist) {
									onfire = true;
								}
						}
					}
					//if this tree is hit by lightning, it catches fire
					if(rand.nextDouble() < lighteningChance)
						onfire = true;
					
					//update next lattice if it is on fire
					if(onfire) {
						nextLattice[i][j] = ONFIRE;
						numFires++;
						numTrees--;
						//otherwise it stays a tree
					} else {
						nextLattice[i][j] = TREE;
					}
					
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
		tics++;
	}
	
	/**
	 * Gets the contents of the neighbourhood cells of a point (i,j) from the lattice 
	 * using the neighbourhood direction vectors dirs
	 * @param dirs
	 * @return
	 */
	public static int[] getNeighbourhood(int[][] lattice, int i, int j, int[][] dirs) {
		int[] a = new int[dirs.length];
		int width = lattice.length;
		int height = lattice[0].length;
		for(int z = 0; z < dirs.length; z++) {
			int[] v = dirs[z];
			int x = i + v[0];
			int y = j + v[1];
			if(i + v[0] < 0)
				x = width + v[0];
			if(j + v[1] < 0)
				y = height + v[1];
			if(i + v[0] >= width)
				x = v[0] - 1;
			if(j + v[1] >= height)
				y = v[1] - 1;
			a[z] = lattice[x][y];
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
	public void setFireCircle(double radius, int x, int y) {
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++) {
				if(Math.sqrt((i - x)*(i-x) + (j - y)*(j-y)) < radius)
					setFire(i,j);
			}
	}
	
	private boolean collinear(int x1, int y1, int x2, int y2, int x3, int y3) {
		double tolerance = width/2;
		double area = ((double)x1*((double)y2-y3) + (double)x2*((double)y3-y1) + (double)x3*((double)y1-y2));
		return Math.abs(area) < tolerance;
	}

	private boolean insideBox(int x1, int y1, int x2, int y2, int x3, int y3) {
		return (x3 >= x1 && x3 <= x2) && (y3 >= y1 && y3 <= y2);
	}

	public void setFireLine(int x1, int y1, int x2, int y2) {
		setFire(x1,y1);
		setFire(x2,y2);
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++) {
				if (collinear(x1,y1,x2,y2,i,j) && insideBox(x1,y1,x2,y2,i,j))
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
		return new CAModel(this);
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
