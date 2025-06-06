package gamelogic.level;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import gameengine.PhysicsObject;
import gameengine.graphics.Camera;
import gameengine.loaders.Mapdata;
import gameengine.loaders.Tileset;
import gamelogic.GameResources;
import gamelogic.Main;
import gamelogic.enemies.Enemy;
import gamelogic.player.Player;
import gamelogic.tiledMap.Map;
import gamelogic.tiles.Flag;
import gamelogic.tiles.Flower;
import gamelogic.tiles.Gas;
import gamelogic.tiles.SolidTile;
import gamelogic.tiles.Spikes;
import gamelogic.tiles.Tile;
import gamelogic.tiles.Water;

public class Level {

	private long timerStart = 0;
	private long timerMax = 60;

	private LevelData leveldata;
	private Map map;
	private Enemy[] enemies;
	public static Player player;
	private Camera camera;

	private boolean active;
	private boolean playerDead;
	private boolean playerWin;

	private ArrayList<Enemy> enemiesList = new ArrayList<>();
	private ArrayList<Flower> flowers = new ArrayList<>();
	private ArrayList<Water> waters = new ArrayList<>();
	private ArrayList<Gas> gasses = new ArrayList<>();

	private List<PlayerDieListener> dieListeners = new ArrayList<>();
	private List<PlayerWinListener> winListeners = new ArrayList<>();

	private Mapdata mapdata;
	private int width;
	private int height;
	private int tileSize;
	private Tileset tileset;
	public static float GRAVITY = 70;

	public Level(LevelData leveldata) {
		this.leveldata = leveldata;
		mapdata = leveldata.getMapdata();
		width = mapdata.getWidth();
		height = mapdata.getHeight();
		tileSize = mapdata.getTileSize();
		restartLevel();
		timerStart = System.currentTimeMillis();
	}

	public LevelData getLevelData(){
		return leveldata;
	}

	public void restartLevel() {
		int[][] values = mapdata.getValues();
		Tile[][] tiles = new Tile[width][height];

		resetTimer();

		for (int x = 0; x < width; x++) {
			int xPosition = x;
			for (int y = 0; y < height; y++) {
				int yPosition = y;

				tileset = GameResources.tileset;

				tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this);
				if (values[x][y] == 0)
					tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this); // Air
				else if (values[x][y] == 1)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid"), this);

				else if (values[x][y] == 2)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_DOWNWARDS, this);
				else if (values[x][y] == 3)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_UPWARDS, this);
				else if (values[x][y] == 4)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_LEFTWARDS, this);
				else if (values[x][y] == 5)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_RIGHTWARDS, this);
				else if (values[x][y] == 6)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Dirt"), this);
				else if (values[x][y] == 7)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Grass"), this);
				else if (values[x][y] == 8)
					enemiesList.add(new Enemy(xPosition*tileSize, yPosition*tileSize, this)); // TODO: objects vs tiles
				else if (values[x][y] == 9)
					tiles[x][y] = new Flag(xPosition, yPosition, tileSize, tileset.getImage("Flag"), this);
				else if (values[x][y] == 10) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower1"), this, 1);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 11) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower2"), this, 2);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 12)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_down"), this);
				else if (values[x][y] == 13)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_up"), this);
				else if (values[x][y] == 14)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_middle"), this);
				else if (values[x][y] == 15)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasOne"), this, 1);
				else if (values[x][y] == 16)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasTwo"), this, 2);
				else if (values[x][y] == 17)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasThree"), this, 3);
				else if (values[x][y] == 18)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Falling_water"), this, 0);
				else if (values[x][y] == 19)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Full_water"), this, 3);
				else if (values[x][y] == 20)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Half_water"), this, 2);
				else if (values[x][y] == 21)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Quarter_water"), this, 1);
			}

		}
		enemies = new Enemy[enemiesList.size()];
		map = new Map(width, height, tileSize, tiles);
		camera = new Camera(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, 0, map.getFullWidth(), map.getFullHeight());
		for (int i = 0; i < enemiesList.size(); i++) {
			enemies[i] = new Enemy(enemiesList.get(i).getX(), enemiesList.get(i).getY(), this);
		}
		player = new Player(leveldata.getPlayerX() * map.getTileSize(), leveldata.getPlayerY() * map.getTileSize(),
				this);
		camera.setFocusedObject(player);

		active = true;
		playerDead = false;
		playerWin = false;
	}

	public void onPlayerDeath() {
		active = false;
		playerDead = true;
		throwPlayerDieEvent();
	}

	public void onPlayerWin() {
		active = false;
		playerWin = true;
		throwPlayerWinEvent();
	}

	public void update(float tslf) {
		if (active) {
			// Update the player
			player.update(tslf);

			// Player death
			if (map.getFullHeight() + 100 < player.getY())
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.BOT] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.TOP] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.LEF] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.RIG] instanceof Spikes)
				onPlayerDeath();

			for (int i = 0; i < flowers.size(); i++) {
				if (flowers.get(i).getHitbox().isIntersecting(player.getHitbox())) {
					if(flowers.get(i).getType() == 1)
						water(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 3);
					else
						addGas(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 20, new ArrayList<Gas>());
					flowers.remove(i);
					i--;
				}
			}

			// Update the enemies
			for (int i = 0; i < enemies.length; i++) {
				enemies[i].update(tslf);
				if (player.getHitbox().isIntersecting(enemies[i].getHitbox())) {
					onPlayerDeath();
				}
			}

			//update the water
			for (int i = 0; i < waters.size(); i++){
				waters.get(i).update(tslf);
				if (player.getHitbox().isIntersecting(waters.get(i).getHitbox())){
					player.setWalkSpeed(200);	
				}
				else {
					player.setWalkSpeed(400);
				}
			}

			//update the gas
			for (int i = 0; i < gasses.size(); i++){
				gasses.get(i).update(tslf);
				if (player.getHitbox().isIntersecting(gasses.get(i).getHitbox())){
					player.setJumpPower(2000);
				}
				else{
					player.setJumpPower(1350);
				}
			}

			// Update the map
			map.update(tslf);

			// Update the camera
			camera.update(tslf);
			

			//timer stuff
			if (timerStart != 0){
				long elapsedTimeSeconds =  (System.currentTimeMillis() - timerStart)/1000;
				if (elapsedTimeSeconds >= timerMax && !playerDead){
					this.onPlayerDeath();
					timerStart = 0;
				}
			
			}
		}
	}
	
	
	//#############################################################################################################
	//Your code goes here! 
	//Please make sure you read the rubric/directions carefully and implement the solution recursively!

	//pre condition: col and row are within indexes of map.getTiles(), map has been created, 0<=fullness<=3
	//post condition: recursively calls such that the source block is replaced with a full block of water, then water flows either left, right, or down, depending on conditions
	private void water(int col, int row, Map map, int fullness) {
		Tile[][] tiles = map.getTiles();
    	int maxCols = tiles.length;
    	int maxRows = tiles[0].length;

		//in case input out of bounds, or cant change to water stop
    	if (col < 0 || col >= maxCols || row < 0 || row >= maxRows) return;
    	if (tiles[col][row] instanceof Water || tiles[col][row].isSolid()) return;

		//full block of falls above solid block
		if (fullness == 0 && row + 1 < maxRows && tiles[col][row + 1].isSolid()){
			fullness = 3;
		}

    	String imageName;
    	if (fullness == 3){
			imageName = "Full_water";
		}
    	else if (fullness == 2){
			imageName = "Half_water";
		}
    	else if (fullness == 1){
			imageName = "Quarter_water";
		}
    	else{
			imageName = "Falling_water";
		}

    	Water w = new Water(col, row, tileSize, tileset.getImage(imageName), this, fullness);
    	map.addTile(col, row, w);
		waters.add(w);
		//pours down
    	if (row + 1 < maxRows) {
        	Tile below = tiles[col][row + 1];
        	if (!below.isSolid() && !(below instanceof Water)) {
            	water(col, row + 1, map, 0);
            	return; 
        	}
    	}

		//flows right
    	if (fullness > 0) {
        	if (col + 1 < maxCols) {
            	Tile right = tiles[col + 1][row];
            	if (!right.isSolid() && !(right instanceof Water)) {
                	boolean isAirBelow = (row + 1 < maxRows && !tiles[col + 1][row + 1].isSolid() && !(tiles[col + 1][row + 1] instanceof Water));
					if (isAirBelow){
                    	water(col + 1, row, map, 1); 
						water(col + 1, row + 1, map, 0);
					}
                	 else {
                    	water(col + 1, row, map, Math.max(fullness - 1, 1));
                	}
            	}
        	}

			//flows left
        	if (col - 1 >= 0) {
            	Tile left = tiles[col - 1][row];
            	if (!left.isSolid() && !(left instanceof Water)) {
                	boolean isAirBelow = (row + 1 < maxRows && !tiles[col - 1][row + 1].isSolid() && !(tiles[col - 1][row + 1] instanceof Water));
                    if (isAirBelow){
						water(col - 1, row, map, 1); 
						water(col - 1, row + 1, map, 0);
                	} 
					else {
                    	water(col - 1, row, map, Math.max(fullness - 1, 1));
                	}
            	}
        	}
    	}
	}
	//PRE CONDITION: PLAYER HITS A SPECIAL FLOWER
	//POST CONDITION: GAS IS DRAWN IN RECTANGLE SHAPE AND PLAYER SLOWS DOWN
	private void addGas(int col, int row, Map map, int numSquaresToFill, ArrayList<Gas> placedThisRound) {
		Tile[][] tiles = map.getTiles();
    	int tileSize = this.tileSize;
    	int maxCols = tiles.length;
    	int maxRows = tiles[0].length;

    	//replace flower
    	Gas firstGas = new Gas(col, row, tileSize, tileset.getImage("GasOne"), this, 0);
		gasses.add(firstGas);
    	map.addTile(col, row, firstGas);
    	placedThisRound.add(firstGas);
    	int tilesPlaced = 1;

    	//this is the order in which gas is placed around a central tile
    	int[][] howToExpand = {
        	{0, -1},   // top middle
        	{1, -1},   // top right
        	{-1, -1},  // top left
        	{1, 0},    // middle right
        	{-1, 0},   // middle left
        	{0, 1},    // bottom middle
        	{1, 1},    // bottom right
        	{-1, 1}    // bottom left
    	};

    	int index = 0;
    	while (index < placedThisRound.size() && tilesPlaced < numSquaresToFill) {
        	Gas currentGas = placedThisRound.get(index);
        	int c = currentGas.getCol();
        	int r = currentGas.getRow();

        	//start expanding in the way the rules outline
        	for (int[] direction : howToExpand) {
            	int newCol = c + direction[0];
            	int newRow = r + direction[1];
				
				//check if new tile in bounds
            	if (newCol >= 0 && newCol < maxCols && newRow >= 0 && newRow < maxRows) {

            		Tile nextTile = tiles[newCol][newRow];

            		//place gas if tile is null or not solid and not already gas
            		if ((nextTile == null || !nextTile.isSolid()) && !(nextTile instanceof Gas)) {
                		Gas newGas = new Gas(newCol, newRow, tileSize, tileset.getImage("GasOne"), this, 0);
						gasses.add(newGas);
                		map.addTile(newCol, newRow, newGas);
                		placedThisRound.add(newGas);
                		tilesPlaced++;

						//stop when placed as many tiles as specified
                		if (tilesPlaced >= numSquaresToFill) {
                    		return;  
                		}
            		}
				}
        	}

        	index++; 
    	}
	}

//precondition: player died
//post condition: restarts timer
private void resetTimer() {
    this.timerStart = System.currentTimeMillis();
}

public void draw(Graphics g) {
	   	 g.translate((int) -camera.getX(), (int) -camera.getY());
	   	 // Draw the map
	   	 for (int x = 0; x < map.getWidth(); x++) {
	   		 for (int y = 0; y < map.getHeight(); y++) {
	   			 Tile tile = map.getTiles()[x][y];
	   			 if (tile == null)
	   				 continue;
	   			 if(tile instanceof Gas) {
	   				
	   				 int adjacencyCount =0;
	   				 for(int i=-1; i<2; i++) {
	   					 for(int j =-1; j<2; j++) {
	   						 if(j!=0 || i!=0) {
	   							 if((x+i)>=0 && (x+i)<map.getTiles().length && (y+j)>=0 && (y+j)<map.getTiles()[x].length) {
	   								 if(map.getTiles()[x+i][y+j] instanceof Gas) {
	   									 adjacencyCount++;
	   								 }
	   							 }
	   						 }
	   					 }
	   				 }
	   				 if(adjacencyCount == 8) {
	   					 ((Gas)(tile)).setIntensity(2);
	   					 tile.setImage(tileset.getImage("GasThree"));
	   				 }
	   				 else if(adjacencyCount >5) {
	   					 ((Gas)(tile)).setIntensity(1);
	   					tile.setImage(tileset.getImage("GasTwo"));
	   				 }
	   				 else {
	   					 ((Gas)(tile)).setIntensity(0);
	   					tile.setImage(tileset.getImage("GasOne"));
	   				 }
	   			 }
	   			 if (camera.isVisibleOnCamera(tile.getX(), tile.getY(), tile.getSize(), tile.getSize()))
	   				 tile.draw(g);
	   		 }
	   	 }


	   	 // Draw the enemies
	   	 for (int i = 0; i < enemies.length; i++) {
	   		 enemies[i].draw(g);
	   	 }


	   	 // Draw the player
	   	 player.draw(g);




	   	 // used for debugging
	   	 if (Camera.SHOW_CAMERA)
	   		 camera.draw(g);
	   	 g.translate((int) +camera.getX(), (int) +camera.getY());

		 //timer display
		 if (timerStart != 0) {
    		long remaining = Math.max(0, (timerMax * 1000) - (System.currentTimeMillis() - timerStart));
    		long secondsRemaining = remaining / 1000;
    		g.setColor(java.awt.Color.RED);
			
    		g.drawString("Time: " + secondsRemaining, 600, 40);
		}
	    }

	




	// --------------------------Die-Listener
	public void throwPlayerDieEvent() {
		for (PlayerDieListener playerDieListener : dieListeners) {
			playerDieListener.onPlayerDeath();
		}
	}

	public void addPlayerDieListener(PlayerDieListener listener) {
		dieListeners.add(listener);
	}

	// ------------------------Win-Listener
	public void throwPlayerWinEvent() {
		for (PlayerWinListener playerWinListener : winListeners) {
			playerWinListener.onPlayerWin();
		}
	}

	public void addPlayerWinListener(PlayerWinListener listener) {
		winListeners.add(listener);
	}

	// ---------------------------------------------------------Getters
	public boolean isActive() {
		return active;
	}

	public boolean isPlayerDead() {
		return playerDead;
	}

	public boolean isPlayerWin() {
		return playerWin;
	}

	public Map getMap() {
		return map;
	}

	public Player getPlayer() {
		return player;
	}
}