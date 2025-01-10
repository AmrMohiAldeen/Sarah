package Sarah;
import java.util.HashSet;
import java.util.HashMap;
import battlecode.common.*;
public abstract class Pathfind {
    public static Direction direction = null;
    private static MapLocation prevDest = null;
    private static HashSet <MapLocation> line = null;
    private static int obstacleStartDist = 0;

    // public static void moveTowards (RobotController rc, MapLocation loc) throws GameActionException {
    //     Direction dir = rc.getLocation().directionTo(loc);
    //     if(rc.canMove(dir)) rc.move(dir);

    //     else {
    //         Direction randomDir = Direction.allDirections()[RobotPlayer.random.nextInt(8)];
    //         if(rc.canMove(randomDir)) rc.move(randomDir);
    //     }
    // }

    private static int bugState = 0; // 0 head to target, 1 circle obstacle
    private static MapLocation closestObstacle = null;
    private static int closestObstacleDist = 1000;
    private static Direction bugDir = null;

    public static void resetBug(){
        bugState = 0; // 0 head to target, 1 circle obstacle
        closestObstacle = null;
        closestObstacleDist = 1000;
        bugDir = null;
    }
    
    public static void bugNavTwo(RobotController rc, MapLocation destination) throws GameActionException {
        if(!destination.equals(prevDest)) {
            prevDest = destination;
            line = createLine(rc.getLocation(), destination);
        }
        // for(MapLocation loc: line) {
        //     rc.setIndicatorDot(loc, 255, 0, 0);
        // }

        if(bugState == 0) {
            bugDir = rc.getLocation().directionTo(destination);
            if(rc.canMove(bugDir)) rc.move(bugDir);
            else{
                bugState = 1;
                obstacleStartDist = rc.getLocation().distanceSquaredTo(destination);
                bugDir = rc.getLocation().directionTo(destination);
            }
        }
        else{
            if(line.contains(rc.getLocation()) && rc.getLocation().distanceSquaredTo(destination) < obstacleStartDist)
                bugState = 0;
            for(int i = 0; i < 9; i++){
                if(rc.canMove(bugDir)){
                    rc.move(bugDir);
                    bugDir = bugDir.rotateRight();
                    bugDir = bugDir.rotateRight();
                    break;
                }
                else {
                    bugDir = bugDir.rotateLeft();
                }
            }
        }
    }

    public static void bugNavOne(RobotController rc, MapLocation destination) throws GameActionException {
        if(bugState == 0){
            bugDir = rc.getLocation().directionTo(destination);
            if(rc.canMove(bugDir)) rc.move(bugDir);
            else{
                bugState = 1;
                closestObstacle = null;
                closestObstacleDist = 1000;
            }
        }
        else{
            if(rc.getLocation().equals(closestObstacle)) bugState = 0;
            if(rc.getLocation().distanceSquaredTo(destination) < closestObstacleDist){
                closestObstacleDist = rc.getLocation().distanceSquaredTo(destination);
                closestObstacle = rc.getLocation();
            }
            for(int i = 0; i < 9; i++){
                if(rc.canMove(bugDir)){
                    rc.move(bugDir);
                    bugDir = bugDir.rotateRight();
                    bugDir = bugDir.rotateRight();
                    break;
                }
                else {
                    bugDir = bugDir.rotateLeft();
                }
            }
        }
    }
    
    
    public static void bugNavZero(RobotController rc, MapLocation destination) throws GameActionException {
        Direction bugDir = rc.getLocation().directionTo(destination);

        if(rc.canMove(bugDir)) rc.move(bugDir);
        else{
            for(int i = 0; i < 8; i++){
                if(rc.canMove(bugDir)){
                    rc.move(bugDir);
                    break;
                }   
                else{
                    bugDir = bugDir.rotateLeft();
                }
            }    
        }
    }


    public static void explore(RobotController rc) throws GameActionException {
        MapInfo [] Tiles= rc.senseNearbyMapInfos(-1);
        MapLocation target = null;
        for(MapInfo tile: Tiles)
        {
            if(tile.getPaint() == PaintType.EMPTY) target = tile.getMapLocation();
        }
        if(rc.isMovementReady()) {
            direction = rc.getLocation().directionTo(target);
            if(rc.canMove(direction)) rc.move(direction);
            else if(target != null) {
                direction = direction.rotateLeft();
                if(rc.canMove(direction)) {
                    rc.move(direction);
                    return;
                }
                direction = direction.rotateRight();
                direction = direction.rotateRight();
                if(rc.canMove(direction)) {
                    rc.move(direction);
                    return;
                }
                direction = direction.rotateLeft();
                direction = direction.rotateLeft();
                direction = direction.rotateLeft();
                if(rc.canMove(direction)) {
                    rc.move(direction);
                    return;
                }

                direction = direction.rotateRight();
                direction = direction.rotateRight();
                direction = direction.rotateRight();
                direction = direction.rotateRight();
                if(rc.canMove(direction)) {
                    rc.move(direction);
                    return;
                }
            }
            else{
                direction = RobotPlayer.directions[RobotPlayer.rng.nextInt(8)];
                for(int i = 0 ; i < 8; i++){
                    if(rc.canMove(direction)) {
                        rc.move(direction);
                        return;
                    }
                    direction = direction.rotateRight();
                }
            }
        }
    }

    private static HashSet<MapLocation> createLine (MapLocation a, MapLocation b) {
        HashSet <MapLocation> locs = new HashSet<>();
        int x = a.x, y = a.y;
        int dx = b.x - a.x;
        int dy = b.y - a.y;
        int sx = (int) Math.signum(dx);
        int sy = (int) Math.signum(dy);
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        int d = Math.max(dx, dy);
        int r = d/2;
        if(dx > dy){
            for(int i = 0; i < d; i++) {
                locs.add(new MapLocation(x, y));
                x += sx;
                r += dy;
                if(r>=dx){
                    locs.add(new MapLocation(x, y));
                    y += sy;
                    r -= dx;
                }
            }
        }
        else{
            for(int i = 0; i < d; i++){
                locs.add(new MapLocation(x, y));
                y += sy;
                r += dx;
                if(r >= dy){
                    locs.add(new MapLocation(x, y));
                    x += sx;
                    r -= dy;
                }
            }
        }
        locs.add(new MapLocation(x, y));
        return locs;
    }

 
}