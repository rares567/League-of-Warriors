import java.util.ArrayList;
import java.util.Random;

enum CellEntityType {
    PLAYER, VOID, ENEMY, SANCTUARY, PORTAL
}

class ImpossibleMoveException extends Exception {
    public ImpossibleMoveException(String message) {
        super(message);
    }
}

class Cell {
    int x, y;
    CellEntityType type;
    boolean visited;
    public Cell(int x, int y, CellEntityType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        if (type == CellEntityType.PLAYER)
            visited = true;
        else
            visited = false;
    }
    public Cell(int x, int y, CellEntityType type, boolean isVisited) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.visited = isVisited;
    }
    public String toString() {
        switch (type) {
            case PLAYER:
                return "\uD83D\uDE0E";
            case VOID:
                return "";
            case ENEMY:
                return "E";
            case SANCTUARY:
                return "S";
            case PORTAL:
                return "[]";
        }
        return "";
    }
}

class Grid extends ArrayList<ArrayList<Cell>> {
    int length, width;
    Character character = null;
    Cell currentCell = null;
    private Grid(int length, int width) {
        super(width);
        this.length = length;
        this.width = width;
    }
    // minDistance is used to ensure that player and portal are never too close
    public static Grid generateMap(int length, int width, int minDistance) {
        Grid map = new Grid(length, width);
        Random rng = new Random();
        int xPlayer, yPlayer, xPortal, yPortal;
        do {
            xPlayer = rng.nextInt(length);
            yPlayer = rng.nextInt(width);
            xPortal = rng.nextInt(length);
            yPortal = rng.nextInt(width);
        } while ((Math.abs(xPlayer - xPortal) + Math.abs(yPlayer - yPortal)) < minDistance);
        /*
        weight of each cell spawning is:
         - void = 1
         - sanctuary = 2
         - enemy = 4
        total weight is 7 so :
         - 0 is void; 1,2 is sanctuary; 3,4,5,6 is enemy
        */
        int choice, nrSanct, nrEnemy;
        do {
            nrSanct = 0;
            nrEnemy = 0;
            map.clear();
            for (int j = 0; j < width; j++) {
                ArrayList<Cell> line = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    if (i == xPlayer && j == yPlayer) {
                        map.currentCell = new Cell(i, j, CellEntityType.PLAYER);
                        line.add(map.currentCell);
                        continue;
                    }
                    if (i == xPortal && j == yPortal) {
                        line.add(new Cell(i, j, CellEntityType.PORTAL));
                        continue;
                    }
                    choice = rng.nextInt(7);
                    if (choice == 0) {
                        line.add(new Cell(i, j, CellEntityType.VOID));
                    } else if (choice <= 2) {
                        line.add(new Cell(i, j, CellEntityType.SANCTUARY));
                        nrSanct++;
                    } else {
                        line.add(new Cell(i, j, CellEntityType.ENEMY));
                        nrEnemy++;
                    }
                }
                map.add(line);
            }
        } while (nrSanct < 2 || nrEnemy < 4);
        return map;
    }
    public static Grid generateMap(int length, int width) {
        // if not specified, the minDistance is half of longest distance
        return generateMap(length, width, 3 * (length + width - 1) / 5);
    }
    public static Grid generateTestMap() {
        Grid map = new Grid(5, 5);
        Random rng = new Random();
        int choice, nrEnemy;
        do {
            nrEnemy = 0;
            map.clear();
            for (int j = 0; j < 5; j++) {
                ArrayList<Cell> line = new ArrayList<>(5);
                for (int i = 0; i < 5; i++) {
                    // hardcode player
                    if (i == 0 && j == 0) {
                        map.currentCell = new Cell(i, j, CellEntityType.PLAYER);
                        line.add(map.currentCell);
                        continue;
                    }
                    // hardcode portal
                    if (i == 4 && j == 4) {
                        line.add(new Cell(i, j, CellEntityType.PORTAL, true));
                        continue;
                    }
                    if ((i == 3 && j == 0) || (i == 3 && j == 1) || (i == 0 && j == 2) || (i == 3 && j == 4)) {
                        line.add(new Cell(i, j, CellEntityType.SANCTUARY, true));
                        continue;
                    }
                    if (i == 4 && j == 3) {
                        line.add(new Cell(i, j, CellEntityType.ENEMY, true));
                        continue;
                    }
                    choice = rng.nextInt(7);
                    if (choice == 0) {
                        line.add(new Cell(i, j, CellEntityType.VOID));
                    } else if (choice <= 2) {
                        line.add(new Cell(i, j, CellEntityType.SANCTUARY));
                    } else {
                        line.add(new Cell(i, j, CellEntityType.ENEMY));
                        nrEnemy++;
                    }
                }
                map.add(line);
            }
        } while (nrEnemy < 4);
        return map;
    }
    public CellEntityType goNorth() throws ImpossibleMoveException {
        if (currentCell.y == 0)
            throw new ImpossibleMoveException("Player cannot move North");
        int x = currentCell.x;
        int y = currentCell.y;
        currentCell.type = CellEntityType.VOID;
        currentCell = get(y - 1).get(x);
        CellEntityType oldType = currentCell.type;
        currentCell.type = CellEntityType.PLAYER;
        currentCell.visited = true;
        return oldType;
    }
    public CellEntityType goSouth() throws ImpossibleMoveException {
        if (currentCell.y == width - 1)
            throw new ImpossibleMoveException("Player cannot move South");
        int x = currentCell.x;
        int y = currentCell.y;
        currentCell.type = CellEntityType.VOID;
        currentCell = get(y + 1).get(x);
        CellEntityType oldType = currentCell.type;
        currentCell.type = CellEntityType.PLAYER;
        currentCell.visited = true;
        return oldType;
    }
    public CellEntityType goEast() throws ImpossibleMoveException {
        if (currentCell.x == length - 1)
            throw new ImpossibleMoveException("Player cannot move East");
        int x = currentCell.x;
        int y = currentCell.y;
        currentCell.type = CellEntityType.VOID;
        currentCell = get(y).get(x + 1);
        CellEntityType oldType = currentCell.type;
        currentCell.type = CellEntityType.PLAYER;
        currentCell.visited = true;
        return oldType;
    }
    public CellEntityType goWest() throws ImpossibleMoveException {
        if (currentCell.x == 0)
            throw new ImpossibleMoveException("Player cannot move West");
        int x = currentCell.x;
        int y = currentCell.y;
        currentCell.type = CellEntityType.VOID;
        currentCell = get(y).get(x - 1);
        CellEntityType oldType = currentCell.type;
        currentCell.type = CellEntityType.PLAYER;
        currentCell.visited = true;
        return oldType;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n");
        for (int j = 0; j < width; j++) {
            sb.append("\t\t");
            for (int i = 0; i < length; i++) {
                Cell cell = get(j).get(i);
                if (!cell.visited)
                    sb.append("N");
                else
                    sb.append(cell);
                if (cell.type == CellEntityType.PORTAL && cell.visited)
                    sb.append("   ");
                else
                    sb.append("    ");
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }
}
