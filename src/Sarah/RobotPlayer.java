package Sarah;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;



public class RobotPlayer {
    static boolean isMassenger = false;
    static int turnCount = 0;
    static ArrayList<MapLocation> knownTowers = new ArrayList<>();
    public static Random rng = null;
    static final int r = 9;

    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };
    


    public static void run(RobotController rc) throws GameActionException {

        if(rc.getType() == UnitType.SOLDIER && rc.getID() % 7 == 0)
            isMassenger = true;

        while (true) {

            turnCount += 1;  
            try {

                if(rng == null) rng = new Random(rc.getID());

                switch (rc.getType()){
                    case SOLDIER: Strategy.runSoldier(rc); break; 
                    case MOPPER:  Strategy.runMopper(rc); break;
                    case SPLASHER:  Strategy.runSplasher(rc) ; break;
                    default:  Strategy.runTower(rc); break;
                    }
                }
             catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                Clock.yield();
            }
        }
    }
}
