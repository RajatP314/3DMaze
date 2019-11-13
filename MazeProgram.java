import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.event.*;
import java.io.*;
import java.util.*;
public class MazeProgram extends JPanel implements KeyListener,MouseListener
{

	Maze maze;
	double wallSize;
	int moveCount;
	int gameState;
	String filename = "maze1.txt";

	int vw = 800;
	int vh = 800;
	int x=100,y=100;
	int collectibleCount = 0;

	//3D stuff
	int cx = 400;
	int cy = 200;
	double r = 0.33;
	int res = 16;
	String resourcePack = "standard";
	String[] textures = {"wall", "floor", "roof", "finish", "collectible", "paywall"};
	Color[][][] textureMap = new Color[textures.length][res][res];

	int t = 0;

	boolean use3DMode = true;

	JFrame frame;

	public MazeProgram()
	{
		//////
		try {
			for(int k=0;k<textures.length;k++){
				File textureFile = new File("Resources/"+resourcePack+"/"+textures[k]+".png");
				BufferedImage texture = ImageIO.read(textureFile);
				int width = texture.getWidth(null);
				int height = texture.getHeight(null);
				for(int i=0;i<width;i++){
					for(int j=0;j<height;j++){
						textureMap[k][j][i] = new Color(texture.getRGB(j, i));
					}
				}
			}
		}catch (IOException e){
			e.printStackTrace();
		}
		//////
		setBoard();
		moveCount = 0;
		gameState = 0;
		frame=new JFrame();
		frame.add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(vw+10,vh+40);
		frame.setVisible(true);
		frame.addKeyListener(this);
		//this.addMouseListener(this); //in case you need mouse clicking
	}
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		g.setColor(Color.BLACK);	//this will set the background color
		g.fillRect(0,0,vw,vh);  //since the screen size is 1000x800
									//it will fill the whole visible part
									//of the screen with a black rectangle

		//drawBoard here!
		switch (gameState){
			case 0:
				if(use3DMode){
					maze.draw3D(g);
				} else {
					maze.drawBoard(g);
				}
				break;
			case 1:
				g.setColor(Color.ORANGE);
				g.setFont(new Font("Times New Roman", Font.PLAIN,50));
				g.drawString("You win!", 50, 50);
				g.drawString("You took "+moveCount+" moves to finish.", 50, 120);
				g.drawString("You found "+maze.player.points+"/" + collectibleCount + " items", 50, 170);
				break;
			default:
				break;
		}

	}
	public void setBoard()
	{
		//choose your maze design

		//pre-fill maze array here

		File name = new File(filename);
		int r=0;
		try
		{
			BufferedReader input = new BufferedReader(new FileReader(name));
			String text = input.readLine();
			String[] dimensions = text.split(" ");
			int width = Integer.parseInt(dimensions[0]);
			int height = Integer.parseInt(dimensions[1]);
			wallSize = vh/Math.max(width, height);
			Drawable[][] objects = new Drawable[height][width];
			Location startLoc = null, endLoc = null;
			int row = 0;
			while( (text=input.readLine())!= null)
			{
				String[] chars = text.split("");
				for(int i=0;i<chars.length;i++){
					switch(chars[i]){
						case "x":
							objects[row][i] = new Wall(new Location(row, i));
							break;
						case "o":
							objects[row][i] = new Collectible(new Location(row, i));
							collectibleCount++;
							break;
						case "s":
							startLoc = new Location(row, i);
							break;
						case "f":
							endLoc = new Location(row, i);
							break;
						default:
							String numbers = "123456789";
							if(numbers.indexOf(chars[i]) >= 0){
								System.out.println("Location: "+row+" "+i);
								objects[row][i] = new Paywall(new Location(row, i), Integer.parseInt(chars[i]));
							}

							break;
					}
				}
				row++;
			}
			System.out.println(startLoc+"\n"+endLoc);
			maze = new Maze(objects, startLoc, endLoc);
		}
		catch (IOException io)
		{
			System.err.println("File error");
		}

		//setWalls();
	}

	public void keyPressed(KeyEvent e)
	{
		if(gameState == 0){
			if(e.getKeyCode()==37)
				maze.player.direction = mod(maze.player.direction-1,4);
			if(e.getKeyCode()==39)
				maze.player.direction = mod(maze.player.direction+1,4);
			if(e.getKeyCode()==38){
				maze.player.move();
				moveCount++;
			}
			if(e.getKeyCode()==32){
				use3DMode = !use3DMode;
			}
		}
		repaint();

	}
	public void keyReleased(KeyEvent e)
	{
	}
	public void keyTyped(KeyEvent e)
	{
	}
	public void mouseClicked(MouseEvent e)
	{
	}
	public void mousePressed(MouseEvent e)
	{
	}
	public void mouseReleased(MouseEvent e)
	{
	}
	public void mouseEntered(MouseEvent e)
	{
	}
	public void mouseExited(MouseEvent e)
	{
	}
	public static void main(String args[])
	{
		MazeProgram app=new MazeProgram();
	}

	public int[] getXY(Location loc){
		int[] xy = new int[2];
		xy[0] = (int)(loc.getCol()*wallSize);
		xy[1] = (int)(loc.getRow()*wallSize);
		return xy;
	}

	private int mod(int x, int y){
		int result = x%y;
		if(x < 0){
			result += y;
		}
		return result;
	}

	public int[] transform(double x, double y, double z){

		double depth = 0;
		if(z != 0){
			depth = (r-1)*Math.pow(1-r, z-1) + 1;
		}
		double tx = depth*cx + (1-depth)*vw*x;
		double ty = depth*cy + (1-depth)*vh*y;

		int[] coords = {(int)Math.round(tx), (int)Math.round(ty)};
		return coords;
	}

	public int[] getXZLocation(Location playerLoc, int playerDir, Location location){
		int x = 0, z = 0;
		switch(playerDir){
			case 0:
				z = location.getCol() - playerLoc.getCol() - 1;
				x = location.getRow() - playerLoc.getRow();
				break;
			case 1:
				x = playerLoc.getCol() - location.getCol();
				z = location.getRow() - playerLoc.getRow() - 1;
				break;
			case 2:
				z = playerLoc.getCol() - location.getCol() - 1;
				x = playerLoc.getRow() - location.getRow();
				break;
			case 3:
				x = location.getCol() - playerLoc.getCol();;
				z = playerLoc.getRow() - location.getRow() - 1;
				break;
			default: break;
		}
		int[] coords = {x, z};
		return coords;
	}

	public Face createFace(Color color, double[] xc, double[] yc, double[] zc, int[] v){
		double[] x = {xc[v[0]], xc[v[1]], xc[v[2]], xc[v[3]]};
		double[] y = {yc[v[0]], yc[v[1]], yc[v[2]], yc[v[3]]};
		double[] z = {zc[v[0]], zc[v[1]], zc[v[2]], zc[v[3]]};
		return new Face(color, x, y, z);
	}

	public class Location {

		private int row;
		private int col;

		public Location(int r, int c){
			this.row = r;
			this.col = c;
		}

		public int getRow(){
			return row;
		}

		public int getCol(){
			return col;
		}

		public String toString(){
			return this.row+" "+this.col;
		}

		public boolean equals(Location loc){
			return (this.row == loc.getRow()) && (this.col == loc.getCol());
		}

	}

	public class Face implements Comparable<Face>{
		private Color color;
		private double[] xc, yc, zc;

		public Face(Color color, double[] xc, double[] yc, double[] zc){
			this.color = color;
			this.xc = xc;
			this.yc = yc;
			this.zc = zc;
		}

		public Face[] split(int textureNum){
			int n = 16;
			Face[] subFaces = new Face[n*n];
			for(int i=0;i<n;i++){
				for(int j=0;j<n;j++){
					double[] x = new double[4],
							y = new double[4],
							z = new double[4];
					if(zc[0] == zc[2]){
						x[0] = xc[0]+(xc[2]-xc[0])*(j)/n;
						x[1] = xc[0]+(xc[2]-xc[0])*(j+1)/n;
						x[2] = x[1];
						x[3] = x[0];
						y[0] = yc[0]+(yc[2]-yc[0])*(i)/n;
						y[1] = y[0];
						y[2] = yc[0]+(yc[2]-yc[0])*(i+1)/n;
						y[3] = y[2];
						z[3] = z[2] = z[1] = z[0] = zc[0];
					} else if(xc[0] == xc[2]){
						y[0] = yc[0]+(yc[2]-yc[0])*(i)/n;
						y[1] = y[0];
						y[2] = yc[0]+(yc[2]-yc[0])*(i+1)/n;
						y[3] = y[2];
						z[0] = zc[0]+(zc[2]-zc[0])*(j)/n;
						z[1] = zc[0]+(zc[2]-zc[0])*(j+1)/n;
						z[2] = z[1];
						z[3] = z[0];
						x[0] = x[1] = x[2] = x[3] = xc[0];
					} else if(yc[0] == yc[2]){
						x[0] = xc[0]+(xc[2]-xc[0])*(j)/n;
						x[1] = xc[0]+(xc[2]-xc[0])*(j+1)/n;
						x[2] = x[1];
						x[3] = x[0];
						z[0] = zc[0]+(zc[2]-zc[0])*(i)/n;
						z[1] = z[0];
						z[2] = zc[0]+(zc[2]-zc[0])*(i+1)/n;
						z[3] = z[2];
						y[0] = y[1] = y[2] = y[3] = yc[0];
					} else {
						Face[] sf = {this};
						return sf;
					}
					subFaces[n*i + j] = new Face(textureMap[textureNum][j][i], x, y, z);
				}
			}
			return subFaces;
		}

		public void draw(Graphics g, double playerX, double playerY, double playerZ){
			int[][] p = new int[4][2];
			for(int i=0;i<4;i++){
				p[i] = transform(xc[i], yc[i], zc[i]);
			}

			int[] x = new int[4];
			int[] y = new int[4];
			for(int i=0;i<4;i++){
				x[i] = p[i][0];
				y[i] = p[i][1];
			}

			double[] center = getCenter();
			double cx = center[0], cy = center[1], cz = center[2];
			double d = Math.sqrt(this.getDSquared(Maze.playerX, Maze.playerY, Maze.playerZ));
			int red = (int)Math.min(color.getRed()/(d), color.getRed());
			int green = (int)Math.min(color.getGreen()/(d), color.getGreen());
			int blue = (int)Math.min(color.getBlue()/(d), color.getBlue());
			g.setColor(new Color(red, green, blue, color.getAlpha()) );
			g.fillPolygon(x, y, 4);
		}

		public double[] getCenter(){
			double cx = 0, cy = 0, cz = 0;
			for(int i=0;i<4;i++){
				cx += xc[i];
				cy += yc[i];
				cz += zc[i];
			}
			cx /= 4;
			cy /= 4;
			cz /= 4;
			double[] center = {cx, cy, cz};
			return center;
		}

		public double getDSquared(double x, double y, double z){
			double[] center = getCenter();
			double ds = Math.pow(center[0]-x, 2)
						+ Math.pow(center[1]-y, 2)
						+ Math.pow(center[2]-z, 2);
			return ds;
		}

		public void setColor(Color c){
			this.color = c;
		}

		public String toString(){
			String str = "";
			for(int i=0;i<4;i++){
				str += xc[i] + " " + yc[i] + " " + zc[i] + " | ";
			}
			return str;
		}

		public int compareTo(Face f){
			double c = f.getDSquared(Maze.playerX, Maze.playerY, Maze.playerZ) -
					this.getDSquared(Maze.playerX, Maze.playerY, Maze.playerZ);
			if(c > 0){
				return 1;
			} else if(c < 0){
				return -1;
			} else {
				return 0;
			}
		}

	}

	public abstract class Drawable{

		public Face[] faces;
		public Location location;
		public Color color;
		public Color mainColor;
		int x, z;

		public abstract void draw(Graphics g);
		public abstract void draw3D(Graphics g);
		public abstract void setFaces(Location loc, int dir);

	}

	public class Wall extends Drawable{

		public Wall(Location loc){
			this.location = loc;
			faces = new Face[4];
			color = mainColor = new Color(200, 255, 255);
			x = 0;
			z = 0;
		}

		public void draw(Graphics g){
			g.setColor(color);
			int[] xy = getXY(this.location);
			g.fillRect(xy[0],
				xy[1],
				(int)wallSize, (int)wallSize);
		}

		public void draw3D(Graphics g){
			for(Face face : faces){
				face.draw(g, 0.5, 0.5, -0.5);
			}
		}

		public void setFaces(Location playerLoc, int playerDir){
			int[] coords = getXZLocation(playerLoc, playerDir, this.location);
			double x = coords[0], z = coords[1];

			double[] xc = {x, x+1, x+1, x, x, x+1, x+1, x},
					yc = {0, 0, 1, 1, 0, 0, 1, 1},
					zc = {z, z, z, z, z+1, z+1, z+1, z+1};

			int[] bfv = {4, 5, 6, 7};
			Face backFace = createFace(this.mainColor, xc, yc, zc, bfv);
			int[] lfv = {0, 4, 7, 3};
			Face leftFace = createFace(this.mainColor, xc, yc, zc, lfv);
			int[] rfv = {1, 5, 6, 2};
			Face rightFace = createFace(this.mainColor, xc, yc, zc, rfv);
			int[] ffv = {0, 1, 2, 3};
			Face frontFace = createFace(this.mainColor, xc, yc, zc, ffv);

			faces[0] = backFace;
			faces[1] = leftFace;
			faces[2] = rightFace;
			faces[3] = frontFace;

		}

		//public String toString(){
		//	return x+" "+z;
		//}

	}

	public class Paywall extends Wall{

		private int threshold;
		private boolean unlocked;

		public Paywall(Location loc, int amount){
			super(loc);
			threshold = amount;
			unlocked = false;
			color = mainColor = new Color(255, 220*threshold/collectibleCount, 220*threshold/collectibleCount);
		}

		public void unlock(int score){
			if(score >= threshold && !unlocked){
				System.out.println(maze.objects[location.getRow()][location.getCol()]);
				maze.objects[location.getRow()][location.getCol()] = null;
				unlocked = true;
			}
		}

	}

	public class Collectible extends Drawable{

		public Collectible(Location loc){
			this.location = loc;
			faces = new Face[5];
			color = mainColor = new Color(255, 100, 100);
			x = 0;
			z = 0;
		}

		public void draw(Graphics g){
			g.setColor(color);
			int[] xy = getXY(this.location);
			g.fillRect((int)(xy[0]+0.33*wallSize), (int)(xy[1]+0.33*wallSize),
			(int)(wallSize/3), (int)(wallSize/3));
		}

		public void draw3D(Graphics g){
					for(Face face : faces){
						face.draw(g, 0.5, 0.5, -0.5);
					}
				}

		public void setFaces(Location playerLoc, int playerDir){
			int[] coords = getXZLocation(playerLoc, playerDir, this.location);
			double x = coords[0]+0.33, z = coords[1]+0.33;

			double[] xc = {x, x+0.33, x+0.33, x, x, x+0.33, x+0.33, x},
					yc = {1-0.33, 1-0.33, 1, 1, 1-0.33, 1-0.33, 1, 1},
					zc = {z, z, z, z, z+0.33, z+0.33, z+0.33, z+0.33};

			int[] bfv = {4, 5, 6, 7};
			Face backFace = createFace(this.color, xc, yc, zc, bfv);
			int[] lfv = {0, 4, 7, 3};
			Face leftFace = createFace(this.color, xc, yc, zc, lfv);
			int[] rfv = {1, 5, 6, 2};
			Face rightFace = createFace(this.color, xc, yc, zc, rfv);
			int[] ffv = {0, 1, 2, 3};
			Face frontFace = createFace(this.color, xc, yc, zc, ffv);
			int[] tfv = {0, 1, 5, 4};
			Face topFace = createFace(this.mainColor, xc, yc, zc, tfv);

			faces[0] = backFace;
			faces[1] = leftFace;
			faces[2] = rightFace;
			faces[3] = frontFace;
			faces[4] = topFace;

		}

	}

	public class Explorer{

		public Location location;
		public int direction; //0, 1, 2, 3
							  //R, D, L, U
		public int fovX = 3;
		public int fovZ = 8;

		public int points;

		public Explorer(Location loc){
			this.points = 0;
			this.location = loc;
			this.direction = 1;
		}

		public void move(){
			int row = location.getRow();
			int col = location.getCol();
			switch(direction){
				case 0:
					location = new Location(row, col+1);
					break;
				case 1:
					location = new Location(row+1, col);
					break;
				case 2:
					location = new Location(row, col-1);
					break;
				case 3:
					location = new Location(row-1, col);
					break;
				default:
					break;
			}
			if(maze.player.location.equals(maze.endLoc)){
				gameState = 1;
			}


			if(location.getRow() < 0 ||
				location.getRow() >= maze.objects.length ||
				location.getCol() < 0 ||
				location.getCol() >= maze.objects[0].length){
				location = new Location(row, col);
			} else {
				Drawable newLoc = maze.objects[location.getRow()][location.getCol()];
			//	System.out.println(newLoc);
				if( newLoc != null ){
					if( newLoc instanceof Wall ){
						location = new Location(row, col);
					} else if( newLoc instanceof Collectible){
						points++;
						maze.objects[location.getRow()][location.getCol()] = null;
				//		System.out.println("Score: "+points);
					}
				}

			}
			System.out.println(direction);
			System.out.println(location);
			//System.out.println(this.location);
		}

		public void draw(Graphics g){
			int[] xy = getXY(this.location);
			g.setColor(new Color(150, 150, 250));
			int xc = xy[0]+(int)(wallSize/2);
			int yc = xy[1]+(int)(wallSize/2);
			int r = (int)(wallSize/4);
			int headX = (int)(xc+r*Math.cos(direction*Math.PI/2));
			int headY = (int)(yc+r*Math.sin(direction*Math.PI/2));
			g.fillRect( headX - (int)(wallSize/8),
						headY - (int)(wallSize/8),
						(int)(wallSize/4), (int)(wallSize/4));
			g.setColor(new Color(100, 100, 200));
						g.fillRect(xy[0]+(int)(wallSize/4),
							xy[1]+(int)(wallSize/4),
				(int)(wallSize/2), (int)(wallSize/2));
		}
	}

	public class Maze{

		public Drawable[][] objects;
		public Location startLoc, endLoc;
		public Explorer player;

		private ArrayList<Face> faceList;

		public static final double playerX = 0.5;
		public static final double playerY = 0.5;
		public static final double playerZ = -0.5;

		public Maze(Drawable[][] objects, Location start, Location end){
			this.objects = objects;
			startLoc = start;
			endLoc = end;
			player = new Explorer(start);
			faceList = new ArrayList<>();
		}

		public void drawBoard(Graphics g){
			int[] nums = getSlice();
			for(int r=0;r<objects.length;r++){
				for(int c=0;c<objects[0].length;c++){
					if(objects[r][c] instanceof Paywall){
						Paywall p = (Paywall)(objects[r][c]);
						p.unlock(maze.player.points);
					}
					if(objects[r][c] != null){
						if(r <= nums[2] && r >= nums[0] &&
							c <= nums[3] && c >= nums[1]){
							objects[r][c].color = Color.YELLOW;
						} else {
							objects[r][c].color = objects[r][c].mainColor;
						}
						objects[r][c].draw(g);
					}
				}
			}
			g.setColor(Color.GREEN);
			g.fillOval( (int)((startLoc.getCol()+0.5)*wallSize - wallSize/4),
						(int)((startLoc.getRow()+0.5)*wallSize - wallSize/4),
						(int)(wallSize/2), (int)(wallSize/2));
			g.fillOval( (int)((endLoc.getCol()+0.5)*wallSize - wallSize/4),
						(int)((endLoc.getRow()+0.5)*wallSize - wallSize/4),
						(int)(wallSize/2), (int)(wallSize/2));
			player.draw(g);
		}

		public void draw3D(Graphics g){
			int[] nums = getSlice();
			for(int r=nums[0];r<=nums[2];r++){
				for(int c=nums[1];c<=nums[3];c++){
					if(withinBounds(r, c) && objects[r][c] != null){
						objects[r][c].color = objects[r][c].mainColor;
						objects[r][c].setFaces(player.location, player.direction);
					}
				}
			}
			faceList = getFaces();
			for(Face face : faceList){
				face.draw(g, Maze.playerX, Maze.playerY, Maze.playerZ);
			}

		}

		private void addFaces(ArrayList<Face> fl, Face face, int split){
			Face[] subfaces = face.split(split);
				for(Face sf : subfaces){
				fl.add(sf);
			}
		}

		private ArrayList<Face> getFaces(){
			int nums[] = getSlice();
			ArrayList<Face> fl = new ArrayList<>();
			for(int r=nums[0];r<=nums[2];r++){
				for(int c=nums[1];c<=nums[3];c++){
					if(withinBounds(r, c) && objects[r][c] != null){
						for(Face face : objects[r][c].faces){
							if( objects[r][c] instanceof Collectible ){
								addFaces(fl, face, 4);
							} else if( objects[r][c] instanceof Paywall ){
								addFaces(fl, face, 5);
							} else {
								addFaces(fl, face, 0);
							}
						}
					} else if(!withinBounds(r, c)){
						Wall w = new Wall(new Location(r, c));
						w.setFaces(player.location, player.direction);
						for(Face face : w.faces){
							addFaces(fl, face, 0);
						}
					}
					int[] xz = getXZLocation(player.location, player.direction, new Location(r, c));
					double x = xz[0], y = 0, z = xz[1];
					double[] xc = {x, x+1, x+1, x};
					double[] yc = {0, 0, 0, 0};
					double[] floorYC = {1, 1, 1, 1};
					double[] zc = {z, z, z+1, z+1};
					Color color = new Color(180, 200, 250);
					int texture = 1;
					Face ceil = new Face(color, xc, yc, zc);
					addFaces(fl, ceil, 2);
					if(r == endLoc.getRow() && c == endLoc.getCol()){
						color = Color.GREEN;
						texture = 3;
					} else {
						color = new Color(100, 150, 180);
						texture = 1;
					}
					Face floor = new Face(color, xc, floorYC, zc);
					addFaces(fl, floor, texture);
				}
			}
			////

			////
			Collections.sort(fl);
			return fl;
		}

		private boolean withinBounds(int r, int c){
			return r > -1 && c > -1 && r < objects.length && c < objects[0].length;
		}

		public int[] getSlice(){
			int sr = 0, sc = 0, fr = 0, fc = 0;
			int r = player.location.getRow();
			int c = player.location.getCol();
			int dir = -1;
			switch(player.direction){
				case 0:
				case 2:
					sr = r-player.fovX;
					fr = r+player.fovX;
					dir = player.direction == 0 ? 1 : -1;
					sc = c+dir;
					fc = c+player.fovZ*dir;
					break;
				case 1:
				case 3:
					dir = player.direction == 1 ? 1 : -1;
					sc = c-player.fovX;
					fc = c+player.fovX;
					sr = r+dir;
					fr = r+player.fovZ*dir;
					break;

			}
			int[] nums = {sr, sc, fr, fc};
			for(int i : nums){
				System.out.print(i+" ");
			}
			System.out.println();
			if(sc > fc){
				nums[1] = fc;
				nums[3] = sc;
			}
			if(sr > fr){
				nums[0] = fr;
				nums[2] = sr;
			}
			return nums;
		}

	}

}
