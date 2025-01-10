package Sarah;

import java.util.Random;

import battlecode.common.*;




public abstract class Strategy {

    
    static boolean isSaving = false;
    public static void runTower(RobotController rc) throws GameActionException{

        // if(rc.canUpgradeTower(rc.getLocation())) rc.upgradeTower(rc.getLocation());

        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        // Pick a random robot type to build.
        int robotType = 0;
        //RobotPlayer.rng.nextInt(3);
        if(RobotPlayer.turnCount < 2)
            if (robotType == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)){
                rc.buildRobot(UnitType.SOLDIER, nextLoc);
            }
        // else if (robotType == 1 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)){
        //     rc.buildRobot(UnitType.MOPPER, nextLoc);
        // }
        // else if (robotType == 2 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)){
        //     //rc.buildRobot(UnitType.SPLASHER, nextLoc);
        // }

        int towerType = 0;
        if(UnitType.LEVEL_ONE_PAINT_TOWER.isTowerType() || UnitType.LEVEL_TWO_PAINT_TOWER.isTowerType() || UnitType.LEVEL_THREE_PAINT_TOWER.isTowerType())
            towerType = 10000;

        if(UnitType.LEVEL_ONE_MONEY_TOWER.isTowerType() || UnitType.LEVEL_TWO_MONEY_TOWER.isTowerType() || UnitType.LEVEL_THREE_MONEY_TOWER.isTowerType())
            towerType = 20000;

        if(UnitType.LEVEL_ONE_DEFENSE_TOWER.isTowerType() || UnitType.LEVEL_TWO_DEFENSE_TOWER.isTowerType() || UnitType.LEVEL_THREE_DEFENSE_TOWER.isTowerType())
            towerType = 30000;

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        RobotInfo [] nearbyRobots = rc.senseNearbyRobots(-1);
        MapLocation robotloc;
        for(RobotInfo robot : nearbyRobots){
            robotloc = robot.getLocation();

            if(rc.canAttack(robotloc)) {
            rc.attack(robotloc);
            rc.attack(robotloc, false);

            if(rc.canSendMessage(robotloc, towerType +  locationToInt(rc, rc.getLocation()))) 
                rc.sendMessage(robotloc, towerType + locationToInt(rc, rc.getLocation())); 
            for(Message m: messages)
                rc.sendMessage(robotloc, m.getBytes());
            }          
        }
    }

    public static void runSplasher (RobotController rc) throws GameActionException {

    }
    public static void runSoldier(RobotController rc) throws GameActionException{

        // Message [] messages = rc.readMessages(-1);
        // MapLocation [] locMessages;
        // for(Message m: messages)
        // {
        //     intToLocation(rc, m.getBytes());
        // }

        // if(RobotPlayer.isMassenger){
            
        // }
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        MapInfo curRuin = null;
        int curDist = Integer.MAX_VALUE;
       
        for (MapInfo tile : nearbyTiles){
            if (tile.hasRuin()){
                rc.setIndicatorDot(tile.getMapLocation(), 255, 0, 0);
                
                if(rc.senseRobotAtLocation(tile.getMapLocation()) == null){
                    
                    int dist = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                    
                    if(dist < curDist){
                        curRuin = tile;
                        curDist = dist;
                    }
                }
            }
        }

        if (curRuin != null){
            MapLocation targetLoc = curRuin.getMapLocation();
            Direction dir = rc.getLocation().directionTo(targetLoc);
            Pathfind.bugNavTwo(rc, targetLoc);

            // Mark the pattern we need to draw to build a tower here if we haven't already.
            MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
            if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
                rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
            }
            
            
            // if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.ENEMY_PRIMARY || rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.ENEMY_SECONDARY)
            //     rc.sendMessage(, );


            for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, -1)){
                if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
                    boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;

                    if (rc.canAttack(patternTile.getMapLocation()))
                        rc.attack(patternTile.getMapLocation(), useSecondaryColor);
                }
            }
            // Complete the ruin if we can.
            if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
                rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
            }


        }

        if(RobotPlayer.isMassenger) {
            updateFriendlyTowers(rc);
            checkNearbyRuins(rc);
        }

        if(RobotPlayer.isMassenger && isSaving)
        {
            
        }
        // if(rc.getPaint() < 20)
        // {    
        //     for(MapInfo tile: nearbyTiles)
        //     {
        //         if( )
        //     }
        // }
        // Move and attack randomly if no objective.
        else
            Pathfind.explore(rc);

        MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
        if (currentTile.getPaint() == PaintType.EMPTY && rc.canAttack(rc.getLocation())){
            rc.attack(rc.getLocation());
        }
    }


    /**
     * Run a single turn for a Mopper.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runMopper(RobotController rc) throws GameActionException{
        // Move and attack randomly.
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)){
            rc.move(dir);
        }
        if (rc.canMopSwing(dir)){
            rc.mopSwing(dir);
            System.out.println("Mop Swing! Booyah!");
        }
        else if (rc.canAttack(nextLoc)){
            rc.attack(nextLoc);
        }

        updateEnemyRobots(rc);
    }

    public static void updateEnemyRobots(RobotController rc) throws GameActionException{

        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
            // Occasionally try to tell nearby allies how many enemy robots we see.
            if (rc.getRoundNum() % 20 == 0){
                for (RobotInfo ally : allyRobots){
                    if (rc.canSendMessage(ally.location, enemyRobots.length)){
                        rc.sendMessage(ally.location, enemyRobots.length);
                    }
                }
            }
        }
    }
    public static void updateFriendlyTowers (RobotController rc) throws GameActionException {
        RobotInfo [] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
        for(RobotInfo ally : allyRobots){
            if(!ally.getType().isTowerType()) continue;

            MapLocation allyLoc = ally.location;
            if(RobotPlayer.knownTowers.contains(allyLoc))
                continue;

            RobotPlayer.knownTowers.add(allyLoc);
        }
    }

    public static void checkNearbyRuins (RobotController rc) throws GameActionException {
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();      
        for (MapInfo tile : nearbyTiles){
            if(!tile.hasRuin()) continue;
            if(rc.senseRobotAtLocation(tile.getMapLocation()) != null) continue;
            Direction dir = tile.getMapLocation().directionTo(rc.getLocation());
            MapLocation markTile = tile.getMapLocation().add(dir);
            if(!rc.senseMapInfo(markTile).getMark().isAlly()) continue;
            if (tile.hasRuin()){                
                if(rc.senseRobotAtLocation(tile.getMapLocation()) == null){
                    isSaving = true;
                    return;
                    
                }
            }
        }
    }
    public static int locationToInt(RobotController rc, MapLocation loc){
        if(loc == null)
            return 0;

        return 1 + loc.x + loc.y * rc.getMapWidth();
    }

    public static MapLocation intToLocation(RobotController rc, int m){
        if (m == 0)
            return null;
        
        return new MapLocation((m - 1)% rc.getMapWidth(), (m - 1) / rc.getMapWidth());
    }

    public static int getMessageLocationType(int message)
    {
        //  0 our paint tour
        //  1 our money tour
        //  2 out defence tower
        //  3 enemy paint tower
        //  4 enemy money tower
        //  5 enemy defence tower
        //  6 A ruin with enemy paint arount it
        //  7 Any ruin

        return message/10000;
    }

    public static MapLocation getMessageLocation (RobotController rc, int message)
    {
        return intToLocation(rc, message % 10000);
    }
}
