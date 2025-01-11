package Sarah;

import java.util.Random;

import battlecode.common.*;




public abstract class Strategy {

    class UnitInfo {
        public static UnitType type;
        public static Team team;
    }
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

        // int towerType = 0;
        // if(UnitType.LEVEL_ONE_PAINT_TOWER.isTowerType() || UnitType.LEVEL_TWO_PAINT_TOWER.isTowerType() || UnitType.LEVEL_THREE_PAINT_TOWER.isTowerType())
        //     towerType = 10000;

        // if(UnitType.LEVEL_ONE_MONEY_TOWER.isTowerType() || UnitType.LEVEL_TWO_MONEY_TOWER.isTowerType() || UnitType.LEVEL_THREE_MONEY_TOWER.isTowerType())
        //     towerType = 20000;

        // if(UnitType.LEVEL_ONE_DEFENSE_TOWER.isTowerType() || UnitType.LEVEL_TWO_DEFENSE_TOWER.isTowerType() || UnitType.LEVEL_THREE_DEFENSE_TOWER.isTowerType())
        //     towerType = 30000;

        // // Read incoming messages
        // Message[] messages = rc.readMessages(-1);
        RobotInfo [] nearbyRobots = rc.senseNearbyRobots(-1);
        MapLocation robotloc;
        for(RobotInfo robot : nearbyRobots){
            robotloc = robot.getLocation();

            if(rc.canAttack(robotloc)) {
            rc.attack(robotloc);
            rc.attack(robotloc, false);
            }
            // if(rc.canSendMessage(robotloc, towerType +  locationToInt(rc, rc.getLocation()))) 
            //     rc.sendMessage(robotloc, towerType + locationToInt(rc, rc.getLocation())); 
            // for(Message m: messages)
            //     rc.sendMessage(robotloc, m.getBytes());
                     
            
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
        
        
        updateTowers(rc);
        int towerDist = Integer.MAX_VALUE;
        MapLocation closestTower = null;
        if(rc.getPaint() < 50)
        {    
            for(int tower: RobotPlayer.knownAllyTowers){
                MapLocation towerLoc = intToLocation(rc, tower);
                int dist = towerLoc.distanceSquaredTo(rc.getLocation());
                    
                    if(dist < towerDist){
                        closestTower = towerLoc;
                        towerDist = dist;
                    }
            }
            Direction towerDir = rc.getLocation().directionTo(closestTower);
            rc.setIndicatorDot(closestTower, 0, 0, 255);
            Pathfind.bugNavTwo(rc, closestTower, towerDir);
            if(rc.canTransferPaint(closestTower, -150)) rc.transferPaint(closestTower, -190);
        }
        else { 

            MapLocation [] nearbyRuins = rc.senseNearbyRuins(-1);
            MapLocation curRuin = null;
            int curDist = Integer.MAX_VALUE;
        
            for (MapLocation ruin: nearbyRuins){              
                if(rc.senseRobotAtLocation(ruin) == null){
                    rc.setIndicatorDot(ruin, 255, 0, 0);
                    int dist = ruin.distanceSquaredTo(rc.getLocation());
                    
                    if(dist < curDist){
                        curRuin = ruin;
                        curDist = dist;
                    }
                }
            }

            if (curRuin != null){
                MapLocation targetLoc = curRuin;
                Direction dir = rc.getLocation().directionTo(targetLoc);
                Pathfind.bugNavTwo(rc, targetLoc, dir);
                // if(rc.canMove(dir)) rc.move(dir);
                // Mark the pattern we need to draw to build a tower here if we haven't already.
                MapLocation shouldBeMarked = curRuin.subtract(dir);
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

            else
                Pathfind.explore(rc);


            // if(RobotPlayer.isMassenger) {
                
            //     checkNearbyRuins(rc);
            // }

            // if(RobotPlayer.isMassenger && isSaving)
            // {
                
            // }


            // Move and attack randomly if no objective.
  
        }

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
    public static void updateTowers (RobotController rc) throws GameActionException {
        RobotInfo [] Robots = rc.senseNearbyRobots(-1);
        for(RobotInfo robot : Robots){
            if(!robot.getType().isTowerType()) continue;

            MapLocation robotLoc = robot.location;
            int intLoc = locationToInt(rc, robotLoc);
            int code = setUnitInfo(rc, robot, robot.getTeam(),intLoc);

            if(rc.getTeam() == robot.getTeam())
            {
                if(RobotPlayer.knownAllyTowers.contains(code))
                    continue;
                RobotPlayer.knownAllyTowers.add(code);
            }
            else{
                if(RobotPlayer.knownEnemyTowers.contains(code))
                    continue;
                RobotPlayer.knownEnemyTowers.add(code);
            }
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

    public static int setUnitInfo (RobotController rc, RobotInfo info, Team team, int location){
        int code = 0;
        if(info.type == UnitType.LEVEL_ONE_PAINT_TOWER && team == rc.getTeam())
            code = 10000;
        if(info.type == UnitType.LEVEL_ONE_MONEY_TOWER && team == rc.getTeam())
            code = 100000;
        if(info.type == UnitType.LEVEL_ONE_DEFENSE_TOWER && team == rc.getTeam())
            code = 110000;
        if(info.type == UnitType.LEVEL_TWO_PAINT_TOWER&& team == rc.getTeam())
            code = 1000000;
        if(info.type == UnitType.LEVEL_TWO_MONEY_TOWER && team == rc.getTeam())
            code = 1010000;
        if(info.type == UnitType.LEVEL_TWO_DEFENSE_TOWER && team == rc.getTeam())
            code = 1100000;
        if(info.type == UnitType.LEVEL_THREE_PAINT_TOWER && team == rc.getTeam())
            code = 1110000;
        if(info.type == UnitType.LEVEL_THREE_MONEY_TOWER && team == rc.getTeam())
            code = 10000000;
        if(info.type == UnitType.LEVEL_THREE_DEFENSE_TOWER && team == rc.getTeam())
            code = 10010000;
        if(info.type == UnitType.LEVEL_ONE_PAINT_TOWER && team == rc.getTeam().opponent())
            code = 10100000;
        if(info.type == UnitType.LEVEL_ONE_MONEY_TOWER && team == rc.getTeam().opponent())
            code = 10110000;
        if(info.type == UnitType.LEVEL_ONE_DEFENSE_TOWER && team == rc.getTeam().opponent())
            code = 11000000;
        if(info.type == UnitType.LEVEL_TWO_PAINT_TOWER&& team == rc.getTeam().opponent())
            code = 11010000;
        if(info.type == UnitType.LEVEL_TWO_MONEY_TOWER && team == rc.getTeam().opponent())
            code = 11100000;
        if(info.type == UnitType.LEVEL_TWO_DEFENSE_TOWER && team == rc.getTeam().opponent())
            code = 11110000;
        if(info.type == UnitType.LEVEL_THREE_PAINT_TOWER && team == rc.getTeam().opponent())
            code = 100000000;
        if(info.type == UnitType.LEVEL_THREE_MONEY_TOWER && team == rc.getTeam().opponent())
            code = 100010000;
        if(info.type == UnitType.LEVEL_THREE_DEFENSE_TOWER && team == rc.getTeam().opponent())
            code = 100100000;

        return code + location;
    }
    public static UnitInfo getUnitInfo( RobotController rc, int message)
    {
      /*
       * 00001 ally paint lvl. 1
       * 00010 ally money lvl. 1
       * 00011 ally defense lvl. 1
       * 00100 ally paint lvl. 2
       * 00101 ally money lvl. 2
       * 00110 ally defense lvl. 2
       * 00111 ally paint lvl. 3
       * 01000 ally money lvl. 3
       * 01001 ally defense lvl. 3
       * 01010 enemy paint lvl. 1
       * 01011 enemy money lvl. 1
       * 01100 enemy defense lvl. 1
       * 01101 enemy paint lvl. 2
       * 01110 enemy money lvl. 2
       * 01111 enemy defense lvl. 2
       * 10000 enemy paint lvl. 3
       * 10001 enemy money lvl. 3
       * 10010 enemy defense lvl. 3
       */
        
        
        int msb5 = (message >> (Integer.SIZE - 5)) & 0b11111;
        UnitInfo info = null;
        switch (msb5) {
            case 0b00001:
                info.type = UnitType.LEVEL_ONE_PAINT_TOWER;
                info.team = rc.getTeam();
                break;
            case 0b00010:
                info.type = UnitType.LEVEL_ONE_MONEY_TOWER;
                info.team = rc.getTeam();
                break;
            case 0b00011:
                info.type = UnitType.LEVEL_ONE_DEFENSE_TOWER;
                info.team = rc.getTeam();
                break;
            case 0b00100:
                info.type = UnitType.LEVEL_TWO_PAINT_TOWER;
                info.team = rc.getTeam();
                break;
            case 0b00101:
                info.type = UnitType.LEVEL_TWO_MONEY_TOWER;
                info.team = rc.getTeam();
                break;
            case 0b00110:
                info.type = UnitType.LEVEL_TWO_DEFENSE_TOWER;
                info.team = rc.getTeam();
                break;
            case 0b00111:
                info.type = UnitType.LEVEL_THREE_PAINT_TOWER;
                info.team = rc.getTeam();
                break;
            case 0b01000:
                info.type = UnitType.LEVEL_THREE_MONEY_TOWER;
                info.team = rc.getTeam();
                break;
            case 0b01001:
                info.type = UnitType.LEVEL_THREE_DEFENSE_TOWER;
                info.team = rc.getTeam();
                break;
            case 0b01010:
            info.type = UnitType.LEVEL_ONE_PAINT_TOWER;
            info.team = rc.getTeam().opponent();
                break;
            case 0b01011:
            info.type = UnitType.LEVEL_ONE_MONEY_TOWER;
            info.team = rc.getTeam().opponent();
                break;
            case 0b01100:
            info.type = UnitType.LEVEL_ONE_DEFENSE_TOWER;
            info.team = rc.getTeam().opponent();
                break;
            case 0b01101:
            info.type = UnitType.LEVEL_TWO_PAINT_TOWER;
            info.team = rc.getTeam().opponent();
                break;
            case 0b01110:
            info.type = UnitType.LEVEL_TWO_MONEY_TOWER;
            info.team = rc.getTeam().opponent();
                break;
            case 0b01111:
            info.type = UnitType.LEVEL_TWO_DEFENSE_TOWER;
            info.team = rc.getTeam().opponent();
                break;
            case 0b10000:
            info.type = UnitType.LEVEL_THREE_PAINT_TOWER;
            info.team = rc.getTeam().opponent();
                break;
            case 0b10001:
            info.type = UnitType.LEVEL_THREE_MONEY_TOWER;
            info.team = rc.getTeam().opponent();
                break;
            case 0b10010:
            info.type = UnitType.LEVEL_THREE_DEFENSE_TOWER;
            info.team = rc.getTeam().opponent();
                break;
            default:
                break;
        }
    
        return info;
    }

    public static MapLocation getMessageLocation (RobotController rc, int message)
    {
        return intToLocation(rc, message % 10000);
    }
}
