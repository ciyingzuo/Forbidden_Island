import java.util.ArrayList;
import java.util.Arrays;
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;

// represent Cell
class Cell {
    // represents absolute Height of this cell, in feet
    double height;
    // In logical coordinates, with the origin at the top-left corner of the
    // screen
    int x;
    int y;
    // the four adjacent cells to this one
    Cell left;
    Cell top;
    Cell right;
    Cell bottom;
    // reports whether this cell is flooded or not
    boolean isFlooded;
    
    // constructor
    Cell(double height, int x, int y, Cell left, Cell top, Cell right, Cell bottom,
            boolean isFlooded) {
        this.height = height;
        this.x = x;
        this.y = y;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.isFlooded = isFlooded;
    }

    // constructor
    Cell(double height, int x, int y) {
        this.height = height;
        this.x = x;
        this.y = y;
    }

    // constructor
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    // constructor
    public Cell(double height) {
        this.height = height;
    }

    // constructor
    public Cell() {
        // a null cell
    }

    // constructor
    public boolean isOcean() {
        return false;
    }

    // get the color for a cell based on Height
    public Color color(int wh) {
        if (this.isOcean()) {
            return new Color(0, 0, 255);
        }
        else if ((this.height - wh) >= 64) {
            return new Color(255, 255, 255);
        }
        else if (!this.isFlooded && this.height - wh <= 0) {
            return new Color(
                    (int) Math.floor(Math.min(((wh - this.height) /
                            (64 / 2)), 0.5) * 255)
                            + 125,
                    0, 0);
        }
        else if (this.isFlooded) {
            return new Color(0, 0, 255 - (int) (Math.ceil(Math.min(10,
                    (wh - this.height)) * 25)));
        }
        else {
            return new Color(0, 100 + (int) (Math.ceil((this.height - wh) * 2.4)), 0);
        }
    }

    // return the rectangle
    public WorldImage drawRect(Color cl) {
        return new RectangleImage(10, 10, "solid", cl);
    }

    // call the image method
    public WorldImage drawCell(int wh) {
        return drawRect(this.color(wh));
    }

    // determine if the cell can be flood
    public boolean canFlood(int wh) {
        return (this.height < wh && this.top.isFlooded) ||
                (this.height < wh && this.bottom.isFlooded) ||
                (this.height < wh && this.left.isFlooded) ||
                (this.height < wh && this.right.isFlooded);
    }
}

// represent OceanCell
class OceanCell extends Cell {

    // constructor
    OceanCell(int x, int y) {
        super(x, y);
        this.isFlooded = true;
    }

    // constructor
    OceanCell() {
        this.isFlooded = true;
    }

    // return true if cell is oceam=n
    public boolean isOcean() {
        return true;
    }

}

// represent IList
interface IList<T> {
    
    // determine if is empty
    boolean isEmpty();
    
    // return the heights point
    Posn heighest(Cell acc);
    
    // check if the whole island is flooded
    boolean allFlood();
}

class Empty<T> implements IList<T> {
    Empty() {
        // Its a empty list of T
    }

    public boolean isEmpty() {
        return true;
    }

    public boolean allFlood() {
        return true;
    }

    public Posn heighest(Cell acc) {
        return new Posn(acc.x, acc.y);
    }
}

class Cons<T> implements IList<T> {
    // represent a list of T
    T first;
    IList<T> rest;

    Cons(T first, IList<T> rest) {
        this.first = first;
        this.rest = rest;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean allFlood() {
        if (((Cell)(this.first)).isFlooded) {
            return this.rest.allFlood();
        }
        else {
            return false;
        }
    }

    public Posn heighest(Cell acc) {
        if (((Cell) (this.first)).height > acc.height) {
            return this.rest.heighest(((Cell) (this.first)));
        }
        else {
            return this.rest.heighest(acc);
        }
    }
}

// main function
class ForbiddenIslandWorld extends World {
    // All the cells of the game, including the ocean
    IList<Cell> board;
    // the current Height of the ocean
    int waterHeight;
    Player p1;
    Player p2;
    int state;
    Posn target1;
    Posn target2;
    Posn target3;
    Posn heli;
    Posn scuba;
    ArrayList<ArrayList<Cell>> cellCopy;

    // Defines integer constant
    ForbiddenIslandWorld(ArrayList<ArrayList<Cell>> cellCopy, int waterHeight) {
        this.cellCopy = cellCopy;
        this.waterHeight = waterHeight;
        this.board = new Height().arrayToList(cellCopy, 64);
    }

    static final int ISLAND_SIZE = 64;
    static final int CELL_SIZE = 10;
    static int ISLAND_MAX_HEIGHT = ISLAND_SIZE / 2;
    static final int INITIAL_WATER_HEIGHT = 0;
    static int FLOOD_SPEED = 100; // ticks between two floods
    static int GAME_TIME;
    static int GAME_STEP;

    // set location for objects
    public void generatePosition() {
        Posn t1 = new Posn((int) Math.floor(Math.random() * 64), (int)
                Math.floor(Math.random() * 64));
        Posn t2 = new Posn((int) Math.floor(Math.random() * 64), (int)
                Math.floor(Math.random() * 64));
        Posn t3 = new Posn((int) Math.floor(Math.random() * 64), (int)
                Math.floor(Math.random() * 64));
        Posn p1 = new Posn((int) Math.floor(Math.random() * 64), (int)
                Math.floor(Math.random() * 64));
        Posn p2 = new Posn((int) Math.floor(Math.random() * 64), (int)
                Math.floor(Math.random() * 64));
        Posn s = new Posn((int) Math.floor(Math.random() * 64), (int)
                Math.floor(Math.random() * 64));
        if (cellCopy.get(t1.x).get(t1.y).isFlooded | cellCopy.get(t2.x).get(t2.y).isFlooded
                | cellCopy.get(t3.x).get(t3.y).isFlooded | cellCopy.get(s.x).get(s.y).isFlooded
                | cellCopy.get(p1.x).get(p1.y).isFlooded |
                cellCopy.get(p2.x).get(p2.y).isFlooded) {
            this.generatePosition();
        }
        else {
            this.target1 = t1;
            this.target2 = t2;
            this.target3 = t3;
            this.scuba = s;
            this.p1 = new Player(cellCopy.get(p1.x).get(p1.y));
            this.p2 = new Player(cellCopy.get(p2.x).get(p2.y));
            this.heli = this.board.heighest(new Cell(-1.0));
        }
    }

    // generate location for helicopter
    public void generateHeli() {
        Posn heli = new Posn((int) Math.floor(Math.random() * 64), (int)
                Math.floor(Math.random() * 64));
        if (cellCopy.get(heli.x).get(heli.y).isFlooded) {
            this.generateHeli();
        }
        else {
            this.heli = heli;
        }
    }

    // make scene
    public WorldScene makeScene() {
        IList<Cell> boardCopy = this.board;
        WorldScene background = new WorldScene(ForbiddenIslandWorld.CELL_SIZE *
                (ForbiddenIslandWorld.ISLAND_SIZE + 1),
                ForbiddenIslandWorld.CELL_SIZE * (ForbiddenIslandWorld.ISLAND_SIZE + 1));
        for (int i = 0; i < (ForbiddenIslandWorld.ISLAND_SIZE + 1) *
                (ForbiddenIslandWorld.ISLAND_SIZE + 1); i++) {
            background.placeImageXY(((Cons<Cell>) (boardCopy)).first.drawCell(waterHeight),
                    ((Cons<Cell>) (boardCopy)).first.x * 10 + 5, ((Cons<Cell>)
                            (boardCopy)).first.y * 10 + 5);
            boardCopy = ((Cons<Cell>) (boardCopy)).rest;
        }
        if (target1.equals(new Posn(100, 100)) && target2.equals(new Posn(100, 100))
                && target3.equals(new Posn(100, 100))) {
            background.placeImageXY(new FromFileImage("helicopter.png"),
                    this.heli.x * 10 + 10, this.heli.y * 10 + 10);
        }
        else {
            background.placeImageXY(new CircleImage(10, "solid", new Color(255, 255, 255)),
                    target1.x * 10 + 10, target1.y * 10 + 10);
            background.placeImageXY(new CircleImage(10, "solid", new Color(255, 255, 255)),
                    target2.x * 10 + 10, target2.y * 10 + 10);
            background.placeImageXY(new CircleImage(10, "solid", new Color(255, 255, 255)),
                    target3.x * 10 + 10, target3.y * 10 + 10);
            background.placeImageXY(new RectangleImage(20, 20, "solid",
                    new Color(152, 152, 152)), scuba.x * 10 + 10,
                    scuba.y * 10 + 10);
        }
        background.placeImageXY(new FromFileImage("pilot-icon.png"), p1.toPosn().x
                * 10 + 10, p1.toPosn().y * 10 + 10);
        background.placeImageXY(new FromFileImage("pilot-icon.png"), p2.toPosn().x
                * 10 + 10, p2.toPosn().y * 10 + 10);
        if (this.state == 0) {
            background.placeImageXY(new TextImage("Next flood in "
                    + Integer.toString(10 - GAME_TIME % FLOOD_SPEED / 10)
                        + " seconds", Color.red), 80, 10);
        }
        if (this.state == 1) {
            background.placeImageXY(new TextImage("You Win! Press N to start new game, step used:"
                    + Integer.toString(GAME_STEP),
                        Color.red), 320, 320);
        }
        else if (this.state == 2) {

            background.placeImageXY(new TextImage("You Lose! Press N to start new game, step used:"
                    + Integer.toString(GAME_STEP),
                    Color.red), 320, 320);
        }
        return background;
    }

    // on key event
    public void onKeyEvent(String i) {
        if ((this.state == 1 || this.state == 2) && i.equals("n")) {
            ArrayList<ArrayList<Double>> terrain = new Height().terrain();
            ForbiddenIslandWorld w2 = new ForbiddenIslandWorld(
                    new Height().transferHelp(terrain, 64), 0);
            w2.generatePosition();
            this.board = w2.board;
            this.cellCopy = w2.cellCopy;
            this.waterHeight = 0;
            this.p1 = w2.p1;
            this.p2 = w2.p2;
            this.target1 = w2.target1;
            this.target2 = w2.target2;
            this.target3 = w2.target3;
            this.scuba = w2.scuba;
            this.heli = w2.heli;
            this.state = 0;
        }
        if (this.state == 1 || this.state == 2) {
            //do nothing
        }
        else if (i.equals("up")) {
            if (this.p1.p.top.isFlooded && p1.time <= 0) {
              //do nothing
            }
            else {
                GAME_STEP++;
                this.p1.p = this.p1.p.top;
            }
        }
        else if (i.equals("down")) {
            if (this.p1.p.bottom.isFlooded && p1.time <= 0) {
              //do nothing
            }
            else {
                GAME_STEP++;
                this.p1.p = this.p1.p.bottom;
            }
        }
        else if (i.equals("left")) {
            if (this.p1.p.left.isFlooded && p1.time <= 0) {
              //do nothing
            }
            else {
                GAME_STEP++;
                this.p1.p = this.p1.p.left;
            }
        }
        else if (i.equals("right")) {
            if (this.p1.p.right.isFlooded && p1.time <= 0) {
              //do nothing
            }
            else {
                GAME_STEP++;
                this.p1.p = this.p1.p.right;
            }
        }
        else if (i.equals("w")) {
            if (this.p2.p.top.isFlooded && p2.time <= 0) {
              //do nothing
            }
            else {
                GAME_STEP++;
                this.p2.p = this.p2.p.top;
            }
        }
        else if (i.equals("a")) {

            if (this.p2.p.left.isFlooded & p2.time <= 0) {
              //do nothing
            }
            else {
                GAME_STEP++;
                this.p2.p = this.p2.p.left;
            }
        }
        else if (i.equals("s")) {

            if (this.p2.p.bottom.isFlooded && p2.time <= 0) {
              //do nothing
            }
            else {
                GAME_STEP++;
                this.p2.p = this.p2.p.bottom;
            }
        }
        else if (i.equals("d")) {
            if (this.p2.p.right.isFlooded && p2.time <= 0) {
                //do nothing
            }
            else {
                GAME_STEP++;
                this.p2.p = this.p2.p.right;
            }
        }
        else if (i.equals("b")) {
            if (this.p1.e) {
                this.rebuild();
            }
        }
    }

    // rebuild area
    public void rebuild() {
        double g = this.waterHeight;
        if (this.p1.p.height - 5 < g) {
            this.p1.p.height = g + 5.0;
            this.p1.p.isFlooded = false;
        }
        if (this.p1.p.left.left.top.top.height - 5 < g) {
            this.p1.p.left.left.top.top.height = g + 5.0;
            this.p1.p.left.left.top.top.isFlooded = false;
        }
        if (this.p1.p.left.top.top.height - 5 < g) {
            this.p1.p.left.top.top.height = g + 5.0;
            this.p1.p.left.top.top.isFlooded = false;
        }
        if (this.p1.p.top.top.height - 5 < g) {
            this.p1.p.top.top.height = g + 5.0;
            this.p1.p.top.top.isFlooded = false;
        }
        if (this.p1.p.right.top.top.height - 5 < g) {
            this.p1.p.right.top.top.height = g + 5.0;
            this.p1.p.right.top.top.isFlooded = false;
        }
        if (this.p1.p.right.right.top.top.height - 5 < g) {
            this.p1.p.right.right.top.top.height = g + 5.0;
            this.p1.p.right.right.top.top.isFlooded = false;
        }
        if (this.p1.p.left.left.top.height - 5 < g) {
            this.p1.p.left.left.top.height = g + 5.0;
            this.p1.p.left.left.top.isFlooded = false;
        }
        if (this.p1.p.left.top.height - 5 < g) {
            this.p1.p.left.top.height = g + 5.0;
            this.p1.p.left.top.isFlooded = false;
        }
        if (this.p1.p.top.height - 5 < g) {
            this.p1.p.top.height = g + 5.0;
            this.p1.p.top.isFlooded = false;
        }
        if (this.p1.p.right.top.height - 5 < g) {
            this.p1.p.right.top.height = g + 5.0;
            this.p1.p.right.top.isFlooded = false;
        }
        if (this.p1.p.right.right.top.height - 5 < g) {
            this.p1.p.right.right.top.height = g + 5.0;
            this.p1.p.right.right.top.isFlooded = false;
        }
        if (this.p1.p.left.left.height - 5 < g) {
            this.p1.p.left.left.height = g + 5.0;
            this.p1.p.left.left.isFlooded = false;
        }
        if (this.p1.p.left.height - 5 < g) {
            this.p1.p.left.isFlooded = false;
        }
        if (this.p1.p.right.right.height - 5 < g) {
            this.p1.p.right.right.height = g + 5.0;
            this.p1.p.right.right.isFlooded = false;
        }
        if (this.p1.p.right.height - 5 < g) {
            this.p1.p.right.height = g + 5.0;
            this.p1.p.right.isFlooded = false;
        }
        if (this.p1.p.left.left.bottom.height - 5 < g) {
            this.p1.p.left.left.bottom.height = g + 5.0;
            this.p1.p.left.left.bottom.isFlooded = false;
        }
        if (this.p1.p.left.bottom.height - 5 < g) {
            this.p1.p.left.bottom.height = g + 5.0;
            this.p1.p.left.bottom.isFlooded = false;
        }
        if (this.p1.p.bottom.height - 5 < g) {
            this.p1.p.bottom.height = g + 5.0;
            this.p1.p.bottom.isFlooded = false;
        }
        if (this.p1.p.right.right.bottom.height - 5 < g) {
            this.p1.p.right.right.bottom.height = g + 5.0;
            this.p1.p.right.right.bottom.isFlooded = false;
        }
        if (this.p1.p.right.bottom.height - 5 < g) {
            this.p1.p.right.bottom.height = g + 5.0;
            this.p1.p.right.bottom.isFlooded = false;
        }
        if (this.p1.p.left.left.bottom.bottom.height - 5 < g) {
            this.p1.p.left.left.bottom.bottom.height = g + 5.0;
            this.p1.p.left.left.bottom.bottom.isFlooded = false;
        }
        if (this.p1.p.left.bottom.bottom.height - 5 < g) {
            this.p1.p.left.bottom.bottom.height = g + 5.0;
            this.p1.p.left.bottom.bottom.isFlooded = false;
        }
        if (this.p1.p.bottom.bottom.height - 5 < g) {
            this.p1.p.bottom.bottom.height = g + 5.0;
            this.p1.p.bottom.bottom.isFlooded = false;
        }
        if (this.p1.p.right.right.bottom.bottom.height - 5 < g) {
            this.p1.p.right.right.bottom.bottom.height = g + 5.0;
            this.p1.p.right.right.bottom.bottom.isFlooded = false;
        }
        if (this.p1.p.right.bottom.bottom.height - 5 < g) {
            this.p1.p.right.bottom.bottom.height = g + 5.0;
            this.p1.p.right.bottom.bottom.isFlooded = false;
        }
        if (this.p2.p.height - 5 < g) {
            this.p2.p.height = g + 5.0;
            this.p2.p.isFlooded = false;
        }
        if (this.p2.p.left.left.top.top.height - 5 < g) {
            this.p2.p.left.left.top.top.height = g + 5.0;
            this.p2.p.left.left.top.top.isFlooded = false;
        }
        if (this.p2.p.left.top.top.height - 5 < g) {
            this.p2.p.left.top.top.height = g + 5.0;
            this.p2.p.left.top.top.isFlooded = false;
        }
        if (this.p2.p.top.top.height - 5 < g) {
            this.p2.p.top.top.height = g + 5.0;
            this.p2.p.top.top.isFlooded = false;
        }
        if (this.p2.p.right.top.top.height - 5 < g) {
            this.p2.p.right.top.top.height = g + 5.0;
            this.p2.p.right.top.top.isFlooded = false;
        }
        if (this.p2.p.right.right.top.top.height - 5 < g) {
            this.p2.p.right.right.top.top.height = g + 5.0;
            this.p2.p.right.right.top.top.isFlooded = false;
        }
        if (this.p2.p.left.left.top.height - 5 < g) {
            this.p2.p.left.left.top.height = g + 5.0;
            this.p2.p.left.left.top.isFlooded = false;
        }
        if (this.p2.p.left.top.height - 5 < g) {
            this.p2.p.left.top.height = g + 5.0;
            this.p2.p.left.top.isFlooded = false;
        }
        if (this.p2.p.top.height - 5 < g) {
            this.p2.p.top.height = g + 5.0;
            this.p2.p.top.isFlooded = false;
        }
        if (this.p2.p.right.top.height - 5 < g) {
            this.p2.p.right.top.height = g + 5.0;
            this.p2.p.right.top.isFlooded = false;
        }
        if (this.p2.p.right.right.top.height - 5 < g) {
            this.p2.p.right.right.top.height = g + 5.0;
            this.p2.p.right.right.top.isFlooded = false;
        }
        if (this.p2.p.left.left.height - 5 < g) {
            this.p2.p.left.left.height = g + 5.0;
            this.p2.p.left.left.isFlooded = false;
        }
        if (this.p2.p.left.height - 5 < g) {
            this.p2.p.left.height = g + 5.0;
            this.p2.p.left.isFlooded = false;
        }
        if (this.p2.p.right.right.height - 5 < g) {
            this.p2.p.right.right.height = g + 5.0;
            this.p2.p.right.right.isFlooded = false;
        }
        if (this.p2.p.right.height - 5 < g) {
            this.p2.p.right.height = g + 5.0;
            this.p2.p.right.isFlooded = false;
        }
        if (this.p2.p.left.left.bottom.height - 5 < g) {
            this.p2.p.left.left.bottom.height = g + 5.0;
            this.p2.p.left.left.bottom.isFlooded = false;
        }
        if (this.p2.p.left.bottom.height - 5 < g) {
            this.p2.p.left.bottom.height = g + 5.0;
            this.p2.p.left.bottom.isFlooded = false;
        }
        if (this.p2.p.bottom.height - 5 < g) {
            this.p2.p.bottom.height = g + 5.0;
            this.p2.p.bottom.isFlooded = false;
        }
        if (this.p2.p.right.right.bottom.height - 5 < g) {
            this.p2.p.right.right.bottom.height = g + 5.0;
            this.p2.p.right.right.bottom.isFlooded = false;
        }
        if (this.p2.p.right.bottom.height - 5 < g) {
            this.p2.p.right.bottom.height = g + 5.0;
            this.p2.p.right.bottom.isFlooded = false;
        }
        if (this.p2.p.left.left.bottom.bottom.height - 5 < g) {
            this.p2.p.left.left.bottom.bottom.height = g + 5.0;
            this.p2.p.left.left.bottom.bottom.isFlooded = false;
        }
        if (this.p2.p.left.bottom.bottom.height - 5 < g) {
            this.p2.p.left.bottom.bottom.height = g + 5.0;
            this.p2.p.left.bottom.bottom.isFlooded = false;
        }
        if (this.p2.p.bottom.bottom.height - 5 < g) {
            this.p2.p.bottom.bottom.height = g + 5.0;
            this.p2.p.bottom.bottom.isFlooded = false;
        }
        if (this.p2.p.right.right.bottom.bottom.height - 5 < g) {
            this.p2.p.right.right.bottom.bottom.height = g + 5.0;
            this.p2.p.right.right.bottom.bottom.isFlooded = false;
        }
        if (this.p2.p.right.bottom.bottom.height - 5 < g) {
            this.p2.p.right.bottom.bottom.height = g + 5.0;
            this.p2.p.right.bottom.bottom.isFlooded = false;
        }
    }

    // process game
    public void onTick() {
        GAME_TIME++;
        // gaming
        if (this.state == 0) {
            // flood every x tick
            if (GAME_TIME % FLOOD_SPEED == 0) {
                this.waterHeight++;
                new Height().flood(this.board, this.waterHeight);
            }
        }
        // collect target
        if (this.p1.toPosn().equals(target1) || this.p2.toPosn().equals(target1)) {
            this.target1 = new Posn(100, 100);
        }
        if (this.p1.toPosn().equals(target2) || this.p2.toPosn().equals(target2)) {
            this.target2 = new Posn(100, 100);
        }
        if (this.p1.toPosn().equals(target3) || this.p2.toPosn().equals(target3)) {
            this.target3 = new Posn(100, 100);
        }
        if (this.p1.toPosn().equals(scuba) || this.p2.toPosn().equals(scuba)) {
            this.p1.time = 10000;
            this.p2.time = 10000;
            this.scuba = new Posn(100, 100);
        }
        if (this.p1.p.isFlooded || this.p1.time > 0) {
            this.p1.time = this.p1.time - 1;
        }
        if (this.p2.p.isFlooded || this.p2.time > 0) {
            this.p2.time = this.p2.time - 1;
        }
        if (this.p1.toPosn().equals(heli) && this.p2.toPosn().equals(heli)) {
            this.state = 1;
        }
        if (this.board.allFlood() || (this.p1.p.isFlooded && this.p1.time <= 0)
                || (this.p2.p.isFlooded && this.p2.time <= 0)) {
            this.state = 2;
        }
        
        new Height().flood(this.board, this.waterHeight);
    }
}

// represent player
class Player {
    Cell p;
    int time;
    boolean e;
    int count;

    // constructor
    Player(Cell p) {
        this.p = p;
        this.time = 0;
        this.e = true;
        this.count = 0;
    }

    // transfer to posn
    public Posn toPosn() {
        return new Posn(this.p.x, this.p.y);
    }
}

// this class has the method to generate a Array that contains Height data
class Height {

    // flood the area
    public void flood(IList<Cell> l, int wh) {
        if (l.isEmpty()) {
          //do nothing
        }
        else if (((Cons<Cell>) l).first.canFlood(wh)) {
            ((Cons<Cell>) l).first.isFlooded = true;
            flood(((Cons<Cell>) l).rest, wh);
        }
        else {
            flood(((Cons<Cell>) l).rest, wh);
        }
    }

    // transfer Array to list
    ArrayList<ArrayList<Cell>> transferHelp(ArrayList<ArrayList<Double>> l, int size) {
        return new Height().fixNear((new Height().doubleToCell(l, size)), size);
    }

    // transfer double to cell
    ArrayList<ArrayList<Cell>> doubleToCell(ArrayList<ArrayList<Double>> l, int size) {
        ArrayList<ArrayList<Cell>> c = new ArrayList<ArrayList<Cell>>();
        for (int i = 0; i <= size; i++) {
            ArrayList<Cell> a = new ArrayList<Cell>();
            for (int n = 0; n <= size; n++) {
                Cell cell = new Cell(l.get(i).get(n), i, n);
                if (l.get(i).get(n) <= 0) {
                    cell = new OceanCell(i, n);
                }
                else {
                    cell.isFlooded = false;
                }
                a.add(cell);
            }
            c.add(a);
        }
        return c;
    }

    // set near cell
    ArrayList<ArrayList<Cell>> fixNear(ArrayList<ArrayList<Cell>> l, int size) {
        for (int i = 0; i <= size; i++) {
            for (int n = 0; n <= size; n++) {
                Cell c = l.get(i).get(n);
                if (i == 0 && n == 0) {
                    c.left = c;
                    c.top = c;
                    c.right = l.get(i + 1).get(n);
                    c.bottom = l.get(i).get(n + 1);
                }
                else if (i == size && n == 0) {
                    c.right = c;
                    c.top = c;
                    c.left = l.get(i - 1).get(n);
                    c.bottom = l.get(i).get(n + 1);
                }
                else if (i == 0 && n == size) {
                    c.left = c;
                    c.top = l.get(i).get(n - 1);
                    c.bottom = c;
                    c.right = l.get(i + 1).get(n);
                }
                else if (i == size && n == size) {
                    c.right = c;
                    c.bottom = c;
                    c.top = l.get(i).get(n - 1);
                    c.left = l.get(i - 1).get(n);
                }
                else if (i == 0) {
                    c.left = c;
                    c.top = l.get(i).get(n - 1);
                    c.right = l.get(i + 1).get(n);
                    c.bottom = l.get(i).get(n + 1);
                }
                else if (n == 0) {
                    c.top = c;
                    c.left = l.get(i - 1).get(n);
                    c.right = l.get(i + 1).get(n);
                    c.bottom = l.get(i).get(n + 1);
                }
                else if (i == size) {
                    c.right = c;
                    c.top = l.get(i).get(n - 1);
                    c.left = l.get(i - 1).get(n);
                    c.bottom = l.get(i).get(n + 1);
                }
                else if (n == size) {
                    c.bottom = c;
                    c.top = l.get(i).get(n - 1);
                    c.left = l.get(i - 1).get(n);
                    c.right = l.get(i + 1).get(n);
                }
                else {
                    c.top = l.get(i).get(n - 1);
                    c.left = l.get(i - 1).get(n);
                    c.right = l.get(i + 1).get(n);
                    c.bottom = l.get(i).get(n + 1);
                }
            }
        }
        return l;
    }

    // transfer array to IList
    IList<Cell> arrayToList(ArrayList<ArrayList<Cell>> l, int size) {
        IList<Cell> c = new Empty<Cell>();
        for (int i = 0; i <= size; i++) {
            for (int n = 0; n <= size; n++) {
                c = new Cons<Cell>(l.get(i).get(n), c);
            }
        }
        return c;
    }

    // initialize a empty Height map
    ArrayList<ArrayList<Double>> init(int size) {
        ArrayList<ArrayList<Double>> l = new ArrayList<ArrayList<Double>>(size + 1);
        // initialize the array list with 0s
        for (int i = 0; i <= size; i++) {
            ArrayList<Double> line = new ArrayList<Double>(size + 1);
            for (int n = 0; n <= size; n++) {
                line.add(0.0);
            }
            l.add(line);
        }
        return l;
    }

    // initialize points
    ArrayList<ArrayList<Double>> iniPoint(ArrayList<ArrayList<Double>> l, int size) {
        ArrayList<Double> left = l.get(0);
        ArrayList<Double> middle = l.get(size / 2);
        ArrayList<Double> right = l.get(size);
        left.set(0, -32.0);
        left.set(size / 2, -1.0);
        left.set(64, -32.0);
        right.set(size / 2, -1.0);
        right.set(64, -32.0);
        right.set(0, -32.0);
        middle.set(0, -1.0);
        middle.set(size / 2, (double) size);
        middle.set(size, -1.0);
        return l;
    }

    // calculate the height for cells
    void caclHeight(ArrayList<ArrayList<Double>> l, Posn tl, Posn tr, Posn bl, Posn br) {
        if (tl.x + 1 == tr.x) {
            // dont do anything
        }
        else {
            double scale = (tr.x - tl.x) / 2 / 64;
            ArrayList<Double> left = l.get(tl.x);
            ArrayList<Double> middle = l.get((tr.x + tl.x) / 2);
            ArrayList<Double> right = l.get(tr.x);
            if (Math.random() > 0.1) {
                left.set((bl.y + tl.y) / 2, ((left.get(tl.y) + left.get(bl.y))
                        / 2) * (Math.random() + scale));
            }
            else {
                left.set((bl.y + tl.y) / 2, ((left.get(tl.y) + left.get(bl.y))
                        / 2) * (Math.random() - scale * 2.0));
            }
            if (Math.random() > 0.1) {
                right.set((br.y + tr.y) / 2, ((right.get(tr.y) + right.get(br.y))
                        / 2) * (Math.random() + scale));
            }
            else {
                right.set((br.y + tr.y) / 2, ((right.get(tr.y) + right.get(br.y))
                        / 2) * (Math.random() - scale * 2.0));
            }
            if (Math.random() > 0.1) {
                middle.set(tl.y, ((left.get(tl.y) + right.get(tr.y)) / 2) *
                        (Math.random() + scale));
            }
            else {
                middle.set(tl.y, ((left.get(tl.y) + right.get(tr.y)) / 2) *
                        (Math.random() - scale * 2.0));
            }
            if (Math.random() > 0.1) {
                middle.set(bl.y, ((left.get(bl.y) + right.get(br.y)) / 2) *
                        (Math.random() + scale));
            }
            else {
                middle.set(bl.y, ((left.get(bl.y) + right.get(br.y)) / 2) *
                        (Math.random() - scale * 2.0));
            }
            if (Math.random() > 0.1) {
                middle.set((bl.y + tl.y) / 2,
                        ((left.get(tl.y) + left.get(bl.y) + right.get(tr.y) + right.get(br.y)) / 4)
                                * (Math.random() + scale));
            }

            else {
                middle.set((bl.y + tl.y) / 2,
                        ((left.get(tl.y) + left.get(bl.y) + right.get(tr.y) + right.get(br.y)) / 4)
                                * (Math.random() - scale * 2.0));
            }

            // recall the method to keep generate
            caclHeight(l, tl, new Posn((tr.x + tl.x) / 2, tr.y),
                    new Posn(bl.x, (bl.y + tl.y) / 2),
                    new Posn((tr.x + tl.x) / 2, (bl.y + tl.y) / 2));

            caclHeight(l, new Posn((tr.x + tl.x) / 2, tr.y), tr,
                    new Posn((tr.x + tl.x) / 2, (bl.y + tl.y) / 2),
                    new Posn(br.x, (br.y + tr.y) / 2));

            caclHeight(l, new Posn(bl.x, (bl.y + tl.y) / 2),
                    new Posn((tr.x + tl.x) / 2, (bl.y + tl.y) / 2), bl,
                    new Posn((br.x + bl.x) / 2, br.y));

            caclHeight(l, new Posn((tr.x + tl.x) / 2, (bl.y + tl.y) / 2),
                    new Posn(br.x, (br.y + tr.y) / 2),
                    new Posn((br.x + bl.x) / 2, br.y), br);
        }

    }

    // generate island
    ArrayList<ArrayList<Double>> terrain() {
        ArrayList<ArrayList<Double>> l = iniPoint((init(64)), 64);
        caclHeight(l, new Posn(0, 0), new Posn(32, 0), new Posn(0, 32), new Posn(32, 32));
        caclHeight(l, new Posn(32, 0), new Posn(64, 0), new Posn(32, 32), new Posn(64, 32));
        caclHeight(l, new Posn(0, 32), new Posn(32, 32), new Posn(0, 64), new Posn(32, 64));
        caclHeight(l, new Posn(32, 32), new Posn(64, 32), new Posn(32, 64), new Posn(64, 64));
        return l;
    }
}

class Example {
    ArrayList<Double> l1 = new ArrayList<Double>(Arrays.asList(0.0, 0.0, 1.0, 0.0, 0.0));
    ArrayList<Double> l2 = new ArrayList<Double>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0));
    ArrayList<Double> l3 = new ArrayList<Double>(Arrays.asList(1.0, 0.0, 5.0, 0.0, 1.0));
    ArrayList<Double> l4 = new ArrayList<Double>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0));
    ArrayList<Double> l5 = new ArrayList<Double>(Arrays.asList(0.0, 0.0, 1.0, 0.0, 0.0));
    ArrayList<ArrayList<Double>> l6 = new ArrayList<ArrayList<Double>>(5);

    // for test only
    void method1() {
        l6.add(l1);
        l6.add(l2);
        l6.add(l3);
        l6.add(l4);
        l6.add(l5);
    }

    ArrayList<Double> l11 = new ArrayList<Double>(Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0));
    ArrayList<Double> l22 = new ArrayList<Double>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0));
    ArrayList<Double> l33 = new ArrayList<Double>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0));
    ArrayList<Double> l44 = new ArrayList<Double>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0));
    ArrayList<Double> l55 = new ArrayList<Double>(Arrays.asList(1.0, 0.0, 0.0, 0.0, 1.0));
    ArrayList<Double> l111 = new ArrayList<Double>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0));
    ArrayList<Double> l222 = new ArrayList<Double>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0));
    ArrayList<Double> l333 = new ArrayList<Double>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0));
    ArrayList<Double> l444 = new ArrayList<Double>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0));
    ArrayList<Double> l555 = new ArrayList<Double>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0));
    ArrayList<ArrayList<Double>> l66 = new ArrayList<ArrayList<Double>>(5);

    // for test only
    void method2() {
        l66.add(l11);
        l66.add(l22);
        l66.add(l33);
        l66.add(l44);
        l66.add(l55);
    }

    ArrayList<ArrayList<Double>> l666 = new ArrayList<ArrayList<Double>>(5);

    // for test only
    void method3() {
        l666.add(l111);
        l666.add(l222);
        l666.add(l333);
        l666.add(l444);
        l666.add(l555);
    }

    ArrayList<Double> l56 = new ArrayList<Double>(Arrays.asList(0.0, 0.0, 0.0));

    // test cell related method
    boolean test2(Tester t) {
        ArrayList<ArrayList<Double>> ll = new Height().iniPoint(new Height().init(64), 64);
        return t.checkExpect(ll.get(0).get(0), -32.0) && t.checkExpect(ll.get(64).get(64), -32.0)
                && t.checkExpect(ll.get(0).get(64), -32.0)
                && t.checkExpect(ll.get(64).get(0), -32.0);
    }

    // test generate related method
    boolean test3(Tester t) {
        ArrayList<ArrayList<Cell>> terrain = new Height().transferHelp(new Height().terrain(), 64);
        WorldImage i = new RectangleImage(10, 10, "solid", new Color(0, 0, 255));
        return t.checkExpect(terrain.get(0).get(0).isFlooded, true)
                && t.checkExpect(terrain.get(32).get(32).height, 64.0)
                && t.checkExpect(terrain.get(3).get(3).isFlooded, true)
                && t.checkExpect(terrain.get(8).get(63).isFlooded, true)
                && t.checkExpect(terrain.get(60).get(2).isFlooded, true)
                && t.checkExpect(terrain.get(62).get(0).isFlooded, true)
                && t.checkExpect(terrain.get(62).get(0).y, 0)
                && t.checkExpect(terrain.get(3).get(5).x, 3)
                && t.checkExpect(terrain.get(32).get(0).y, 0)
                && t.checkExpect(terrain.get(45).get(45).x, 45)
                && t.checkExpect(terrain.get(62).get(30).y, 30)
                && t.checkExpect(terrain.get(12).get(5).x, 12)
                && t.checkExpect(terrain.get(31).get(32).right.height, 64.0)
                && t.checkExpect(terrain.get(33).get(32).left.height, 64.0)
                && t.checkExpect(terrain.get(32).get(31).bottom.height, 64.0)
                && t.checkExpect(terrain.get(32).get(33).top.height, 64.0)
                && t.checkExpect(terrain.get(0).get(0).isOcean(), true)
                && t.checkExpect(terrain.get(63).get(63).isOcean(), true)
                && t.checkExpect(terrain.get(0).get(0).drawCell(0), i)
                && t.checkExpect(terrain.get(32).get(32).drawCell(32),
                        new RectangleImage(10, 10, "solid", new Color(0, 177, 0)));
    }

    // launch the game
    void testDiamond(Tester t) {
        ArrayList<ArrayList<Double>> terrain = new Height().terrain();
        ForbiddenIslandWorld w2 =
                new ForbiddenIslandWorld(new Height().transferHelp(terrain, 64), 0);
        w2.generatePosition();
        w2.bigBang(650, 650, 0.1);
    }
}
