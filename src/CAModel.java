import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;


public class CAModel {

	
	
	public enum States {
		EMPTY (0),
		TREE (1), 
		ONFIRE(2);
	
		private final int index;
		States(int index) {
			this.index = index;
		}
		
		public int index() {
			return index;
		}
	};
	
	int[][] lattice;
	int width, height;
	double growthrate;
	double lighteningChance;
	private final int[][] moore = {{0,0},{0,1},{1,1},{1,0},{0,-1},{-1,-1},{-1,0},{1,-1},{-1,1}};
	
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
				lattice[i][j] = States.EMPTY.index;
		
		long seed = System.currentTimeMillis();
		Random rand = new Random(seed);
		for(int i = 0; i < numtrees; i++) {
			int x = rand.nextInt(width);
			int y = rand.nextInt(height);
			if(lattice[x][y] != States.TREE.index)
				lattice[x][y] = States.TREE.index; //could double up on certain cells but it doesn't really matter
		}
	}
	
	/**
	 * Makes a new model to the spec of the assignment
	 * @param width
	 * @param height
	 * @param q
	 * @param growthrate
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
				if(rand.nextDouble() < q)
					lattice[i][j] = States.TREE.index;
				else
					lattice[i][j] = States.EMPTY.index;
	}
		
		
	
	public CAModel(int[][] lattice, double growthrate) {
		this.lattice = latticeClone(lattice);
		this.width = lattice.length;
		this.height = lattice[0].length;
		this.growthrate = growthrate;
	}
	
	public void step() {
		Random rand = new Random(System.currentTimeMillis());
		int[][] nextLattice = new int[width][height];
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++) {
				if(lattice[i][j] == States.ONFIRE.index) //FIRE -> EMPTY
					nextLattice[i][j] = States.EMPTY.index;
				else if(lattice[i][j] == States.TREE.index){ //TREE -> FIRE if at least one fire in neighbourhood or randomly
					//here I'm assuming Moore neighbourhoods
					boolean onfire = false;
					for(Integer x : getNeighbourhood(lattice, i , j, moore)) {
						if(x == States.ONFIRE.index)
							onfire = true;
					}
					if(rand.nextDouble() < lighteningChance)
						onfire = true;
					if(onfire)
						nextLattice[i][j] = States.ONFIRE.index;
					else
						nextLattice[i][j] = States.TREE.index;
				} else if(lattice[i][j] == States.EMPTY.index) { //EMPTY -> TREE with probability p
					if(rand.nextDouble() < growthrate)
						nextLattice[i][j] = States.TREE.index;
					else
						nextLattice[i][j] = States.EMPTY.index;
				}
			}
		lattice = nextLattice;
	}
	
	/**
	 * Gets the contents of the neighbourhood cells of a point (i,j) from the lattice 
	 * using the neighbour direction vectors dirs
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
	
	public void randomChange() {
		long seed = System.currentTimeMillis();
		Random rand = new Random(seed);
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++)
				lattice[i][j] = rand.nextInt(3);
	}
	
	public boolean setFire(int i, int j) {
		if(i > 0 && i < width && j  > 0 && j < height)
			if(lattice[i][j] == States.TREE.index) {
				lattice[i][j] = States.ONFIRE.index;
				return true;
			}
		return false;
	}
	
	public void setRandomFire() {
		Random rand = new Random(System.currentTimeMillis());
		int x = rand.nextInt(width);
		int y = rand.nextInt(height);
		while(lattice[x][y] != States.TREE.index) {
			x = rand.nextInt(width);
			y = rand.nextInt(height);
			
		}
		setFire(x, y);
	}
	
	/**
	 * Sets fire to any trees within a circle around a given point
	 * 
	 * It's really inefficient but you only run it once at a time so who cares
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
	
	public void setFireCentre() {
		setFireCircle(Math.min(width/50, height/50),width/2,height/2);		
	}
	
	public CAModel clone() {
		return new CAModel(lattice, growthrate);
	}
	
	public static int[][] latticeClone(int[][] lattice) {
		int[][] out = new int[lattice.length][lattice[0].length];
		for(int i = 0; i < lattice.length; i++)
			for(int j = 0; j < lattice[0].length; j++)
				out[i][j] = lattice[i][j];
		return out;
	}
	
}
