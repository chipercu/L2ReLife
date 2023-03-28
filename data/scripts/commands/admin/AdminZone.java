package commands.admin;

import l2open.extensions.scripts.Functions;
import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.handler.AdminCommandHandler;
import l2open.gameserver.handler.IAdminCommandHandler;
import l2open.gameserver.model.*;
import l2open.gameserver.model.items.L2ItemInstance;
import l2open.gameserver.instancemanager.ZoneManager;
import l2open.gameserver.serverpackets.ExShowTrace;
import l2open.gameserver.serverpackets.NpcHtmlMessage;
import l2open.gameserver.tables.SpawnTable;
import l2open.gameserver.tables.TerritoryTable;
import l2open.gameserver.xml.ItemTemplates;
import l2open.util.GArray;
import l2open.util.Location;

import java.util.ArrayList;
import java.util.List;

public class AdminZone extends Functions implements IAdminCommandHandler, ScriptFile {
    private static enum Commands {
        admin_zone_check,
        admin_region,
        admin_loc,
        admin_xloc,
        admin_pos,
        admin_showloc,
        admin_location,
        admin_loc_begin,
        admin_loc_add,
        admin_loc_reset,
        admin_loc_end,
        admin_loc_remove,
        admin_vis_count,
        admin_show_locations,
        admin_zonec,
        admin_zone_tp,
        admin_paint_square,
        admin_paint_line
    }

    private static GArray<int[]> create_loc;
    private static int create_loc_id;
    private static int _loc_id = 900521;

    private static void locationMenu(L2Player activeChar) {
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        StringBuffer replyMSG = new StringBuffer("<html><body><title>Location Create</title>");

        replyMSG.append("<center><table width=260><tr>");
        replyMSG.append("<td width=70>Location:</td>");
        replyMSG.append("<td width=50><edit var=\"loc\" width=50 height=12></td>");
        replyMSG.append("<td width=50><button value=\"Show\" action=\"bypass -h admin_showloc $loc\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
        replyMSG.append("<td width=90><button value=\"New Location\" action=\"bypass -h admin_loc_begin $loc\" width=90 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
        replyMSG.append("<td width=90><button value=\"New Location End\" action=\"bypass -h admin_loc_begin " + _loc_id + "\" width=90 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
        replyMSG.append("</tr></table><br><br></center>");

        if (create_loc != null) {
            replyMSG.append("<center><table width=260><tr>");
            replyMSG.append("<td width=80><button value=\"Add Point\" action=\"bypass -h admin_loc_add menu\" width=80 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
            replyMSG.append("<td width=90><button value=\"Reset Points\" action=\"bypass -h admin_loc_reset menu\" width=90 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
            replyMSG.append("<td width=90><button value=\"End Location\" action=\"bypass -h admin_loc_end menu\" width=90 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
            replyMSG.append("</tr></table></center>");

            replyMSG.append("<center><button value=\"Show\" action=\"bypass -h admin_loc_showloc " + create_loc_id + " menu\" width=80 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");

            replyMSG.append("<br><br>");

            int i = 0;
            for (int[] loc : create_loc) {
                replyMSG.append("<button value=\"Remove\" action=\"bypass -h admin_loc_remove " + i + "\" width=80 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
                replyMSG.append("&nbsp;&nbsp;(" + loc[0] + ", " + loc[1] + ", " + loc[2] + ")<br1>");
                i++;
            }
        }

        replyMSG.append("</body></html>");
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply);
    }

    public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar) {
        Commands command = (Commands) comm;

        if (activeChar == null || !activeChar.getPlayerAccess().CanTeleport)
            return false;

        switch (command) {
            case admin_zone_tp: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("Usage: //zone_tp <zone_id>");
                    return false;
                }

                String zone_id = wordList[1];
                L2Zone zone = ZoneManager.getInstance().getZoneById(Integer.parseInt(zone_id));
                if (zone == null) {
                    activeChar.sendMessage("Zone <" + zone_id + "> undefined.");
                    return false;
                }
                //if(!zone.getLoc().isInside(activeChar.getX(), activeChar.getY()))
                {
                    int[] _loc = zone.getLoc().getRandomPoint();
                    activeChar.teleToLocation(_loc[0], _loc[1], _loc[2]);
                }
                activeChar.sendPacket(Points2Trace(zone.getLoc().getCoords(), 50, true, true));
                break;
            }
            case admin_zonec:
                L2Territory territory = new L2Territory(899999);
                territory.add(70600, 130952, -3696, 3696);
                territory.add(128440, 130616, -2160, 2160);
                territory.add(133560, 98216, -736, 736);
                territory.add(65032, 98760, -3552, 3552);
                territory.validate();
                TerritoryTable.getInstance().getLocations().put(899999, territory);
                L2World.addTerritory(territory);
                for (L2Territory terr : TerritoryTable.getInstance().getLocations().values()) {
                    if (terr == null)
                        continue;
                    if (terr.getId() >= 900000)
                        if (territory.isInside(terr.getXmax(), terr.getYmax())) {
                            boolean isSpawn = true;
                            for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable()) {
                                int location = spawn.getLocation();
                                if (location == 0)
                                    continue;
                                if (location == terr.getId())
                                    isSpawn = false;
                            }
                            if (isSpawn) {
                                activeChar.sendMessage("Territory: " + terr.getId());
                                System.out.println("Territory: " + terr.getId());
                            }
                        }
                }
                break;
            case admin_zone_check: {
                activeChar.sendMessage("===== Active Territories =====");
                GArray<L2Territory> territories = L2World.getTerritories(activeChar.getX(), activeChar.getY(), activeChar.getZ());
                if (territories != null)
                    for (L2Territory terr : territories) {
                        activeChar.sendMessage("Territory: " + terr.getId());
                        if (terr.getZone() != null)
                            activeChar.sendMessage("Zone: " + terr.getZone().getType().toString() + ", id: " + terr.getZone().getId() + ", state: " + (terr.getZone().isActive() ? "active" : "not active"));
                    }
                activeChar.sendMessage("======= Mob Spawns =======");
                for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable()) {
                    int location = spawn.getLocation();
                    if (location == 0)
                        continue;
                    if (location < 0)
                        location *= -1;
                    L2Territory terr = TerritoryTable.getInstance().getLocation(location);
                    if (terr == null)
                        continue;
                    if (terr.isInside(activeChar.getX(), activeChar.getY()))
                        activeChar.sendMessage("Territory: " + terr.getId());
                }
                break;
            }
            case admin_region: {
                activeChar.sendMessage("Current region: " + activeChar.getCurrentRegion().getName());
                activeChar.sendMessage("Objects list:");
                int players = 0;
                int pet = 0;
                int npc = 0;
                for (L2Object o : activeChar.getCurrentRegion().getObjectsList(new GArray<L2Object>(activeChar.getCurrentRegion().getObjectsSize()), 0, activeChar.getReflection()))
                    if (o != null) {
                        activeChar.sendMessage(o.toString());
                        if (o.isPlayable()) {
                            if (o.isPlayer())
                                players++;
                            else
                                pet++;
                        } else
                            npc++;
                    }
                activeChar.sendMessage("Object counts: npc=" + npc + " player=" + players + " pet=" + pet);
                break;
            }
            case admin_vis_count: {
                activeChar.sendMessage("Players count: " + L2World.getAroundPlayers(activeChar).size());
                break;
            }
            /*
             * Пишет в консоль текущую точку для локации, оформляем в виде SQL запроса
             * пример: (8699,'loc_8699',111104,-112528,-1400,-1200),
             * Удобно для рисования локаций под спавн, разброс z +100/-10
             * необязательные параметры: id локации и название локации
             * Бросает бутылку, чтобы не запутаццо :)
             */
            case admin_loc: {
                String loc_id = "0";
                String loc_name;
                if (wordList.length > 1)
                    loc_id = wordList[1];
                if (wordList.length > 2)
                    loc_name = wordList[2];
                else
                    loc_name = "loc_" + loc_id;
                System.out.println("	(" + loc_id + ",'" + loc_name + "'," + activeChar.getX() + "," + activeChar.getY() + "," + activeChar.getZ() + "," + (activeChar.getZ() + 100) + ",0),");
                activeChar.sendMessage("Point saved.");
                L2ItemInstance temp = ItemTemplates.getInstance().createItem(1060);
                temp.dropMe(activeChar, activeChar.getLoc());
                break;
            }
            case admin_xloc: {
                System.out.println("			<coords loc=\"" + activeChar.getX() + " " + activeChar.getY() + " " + activeChar.getZ() + " 0\" />");
                activeChar.sendMessage("Point saved.");
                L2ItemInstance temp = ItemTemplates.getInstance().createItem(1060);
                temp.dropMe(activeChar, activeChar.getLoc());
                break;
            }
            case admin_pos:
                String pos = activeChar.getX() + ", " + activeChar.getY() + ", " + activeChar.getZ() + ", " + activeChar.getHeading() + " Geo [" + (activeChar.getX() - L2World.MAP_MIN_X >> 4) + ", " + (activeChar.getY() - L2World.MAP_MIN_Y >> 4) + "] Ref " + activeChar.getReflection().getId();
                System.out.println(activeChar.getName() + "'s position: " + pos);
                activeChar.sendMessage("Pos: " + pos);
                break;
            case admin_location:
                locationMenu(activeChar);
                break;
            case admin_loc_begin: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("Usage: //loc_begin <location_id>");
                    locationMenu(activeChar);
                    return false;
                }
                try {
                    create_loc_id = Integer.valueOf(wordList[1]);
                    _loc_id = create_loc_id;
                } catch (Exception E) {
                    activeChar.sendMessage("location_id should be integer");
                    create_loc = null;
                    locationMenu(activeChar);
                    return false;
                }

                create_loc = new GArray<int[]>();
                create_loc.add(new int[]{activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getZ() + 100});
                L2ItemInstance temp = ItemTemplates.getInstance().createItem(1060);
                temp.dropMe(activeChar, activeChar.getLoc());
                activeChar.sendMessage("Location(" + _loc_id + "): Now you can add points...");
                activeChar.sendPacket(new ExShowTrace(60000));
                locationMenu(activeChar);
                break;
            }
            case admin_loc_add: {
                if (create_loc == null) {
                    activeChar.sendMessage("Location not started");
                    locationMenu(activeChar);
                    return false;
                }

                create_loc.add(new int[]{activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getZ() + 100});
                L2ItemInstance temp = ItemTemplates.getInstance().createItem(1060);
                temp.dropMe(activeChar, activeChar.getLoc());

                if (create_loc.size() > 1)
                    activeChar.sendPacket(Points2Trace(create_loc, 50, false, false));
                if (wordList.length > 1 && wordList[1].equals("menu"))
                    locationMenu(activeChar);
                break;
            }
            case admin_loc_reset: {
                if (create_loc == null) {
                    activeChar.sendMessage("Location not started");
                    locationMenu(activeChar);
                    return false;
                }

                create_loc.clear();
                activeChar.sendPacket(new ExShowTrace(60000));
                locationMenu(activeChar);
                break;
            }
            case admin_loc_end: {
                if (create_loc == null) {
                    activeChar.sendMessage("Location not started");
                    locationMenu(activeChar);
                    return false;
                }
                if (create_loc.size() < 3) {
                    activeChar.sendMessage("Minimum location size 3 points");
                    locationMenu(activeChar);
                    return false;
                }
                _loc_id++;

                //String prefix = "(" + create_loc_id + ",'loc_" + create_loc_id + "',";
                String prefix = "(" + create_loc_id + ",'dragon_valley',";

                for (int[] _p : create_loc)
                    System.out.println(prefix + _p[0] + "," + _p[1] + "," + _p[2] + "," + _p[3] + ", 0),");
                System.out.println("");

                activeChar.sendPacket(Points2Trace(create_loc, 50, true, false));
                create_loc = null;
                create_loc_id = 0;
                activeChar.sendMessage("Location Created, check stdout");
                if (wordList.length > 1 && wordList[1].equals("menu"))
                    locationMenu(activeChar);
                break;
            }
            case admin_showloc: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("Usage: //showloc <location>");
                    return false;
                }

                String loc_id = wordList[1];
                L2Territory terr = TerritoryTable.getInstance().getLocations().get(Integer.parseInt(loc_id));
                if (terr == null) {
                    activeChar.sendMessage("Location <" + loc_id + "> undefined.");
                    return false;
                }
                if (!terr.isInside(activeChar.getX(), activeChar.getY())) {
                    int[] _loc = terr.getRandomPoint();
                    activeChar.teleToLocation(_loc[0], _loc[1], _loc[2]);
                }
                activeChar.sendPacket(Points2Trace(terr.getCoords(), 50, true, false));

                if (wordList.length > 2 && wordList[2].equals("menu"))
                    locationMenu(activeChar);
                break;
            }
            case admin_loc_remove: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("Usage: //showloc <location>");
                    return false;
                }

                if (create_loc == null) {
                    activeChar.sendMessage("Location not started");
                    locationMenu(activeChar);
                    return false;
                }

                int point_id = Integer.parseInt(wordList[1]);

                create_loc.remove(point_id);

                if (create_loc.size() > 1)
                    activeChar.sendPacket(Points2Trace(create_loc, 50, false, false));

                locationMenu(activeChar);
                break;
            }
            case admin_show_locations: {
                for (L2Territory terr : TerritoryTable.getInstance().getLocations().values())
                    if (activeChar.isInRange(terr.getCenter(), 2000))
                        activeChar.sendPacket(Points2Trace(terr.getCoords(), 50, true, false));
                for (L2Territory terr : TerritoryTable.getInstance().getLocations().values())
                    if (activeChar.isInRange(terr.getCenter(), 2000))
                        activeChar.sendPacket(Points2Trace(terr.getCoords(), 50, true, true));
                break;
            }
            case admin_paint_square: {
                L2Territory terr = new L2Territory(0);
                L2Character target = (L2Character) activeChar.getTarget();
                L2Skill skill = activeChar.getKnownSkill(1239);
                L2Character skillTarget = (L2Character) activeChar.getTarget();


               // ExShowTrace l1 = new ExShowTrace(3000);
                ExShowTrace l2 = new ExShowTrace(3000);
              //  ExShowTrace l3 = new ExShowTrace(3000);
                ExShowTrace l4 = new ExShowTrace(3000);

              //  int radius = Math.max(skill.fan_range_h, skill.fan_range_l)+50;

                int zmin1 = activeChar.getPrevZ() - 50;
                int zmax1 = activeChar.getPrevZ() + 50;
                int zmin2 = target.getZ() - 50;
                int zmax2 = target.getZ() + 50;

                double angle = Location.calculateAngleFrom(activeChar.getPrevX(), activeChar.getPrevY(), target.getX(), target.getY());

                double radian1 = Math.toRadians(angle-90);
                double radian2 = Math.toRadians(angle+90);

//                int dx = target.getX() - activeChar.getPrevX();
//                int dy = target.getY() - activeChar.getPrevY();

                int c_x = (int) (activeChar.getX() - Math.sin(radian1) * activeChar.getRealDistance3D(target));
                int c_y = (int) (activeChar.getY() + Math.cos(radian1) * activeChar.getRealDistance3D(target));

                int width = 25; // _radius

                // fan_range_l - ширина
                // fan_range_h - дистанция до цели
                terr.add(c_x + (int) (Math.cos(radian1) * width), c_y + (int) (Math.sin(radian1) * width), zmin1, zmax1);
                terr.add(c_x + (int) (Math.cos(radian2) * width), c_y + (int) (Math.sin(radian2) * width), zmin1, zmax1);

                terr.add(activeChar.getX() + (int) (Math.cos(radian2) * width), activeChar.getY() + (int) (Math.sin(radian2) * width), zmin2, zmax2);
                terr.add(activeChar.getX() + (int) (Math.cos(radian1) * width), activeChar.getY() + (int) (Math.sin(radian1) * width), zmin2, zmax2);

        //        l1.addLine(c_x + (int) (Math.cos(radian1) * width), (int) (Math.sin(radian1) * width), activeChar.getZ(), c_x + (int) (Math.cos(radian2) * width), c_y + (int) (Math.sin(radian2) * width), activeChar.getZ(), 5 );
                l2.addLine(c_x + (int) (Math.cos(radian2) * width), c_y + (int) (Math.sin(radian2) * width), activeChar.getZ(), activeChar.getX() + (int) (Math.cos(radian2) * width), activeChar.getY() + (int) (Math.sin(radian2) * width), activeChar.getZ(), 20 );
          //      l3.addLine(activeChar.getX() + (int) (Math.cos(radian2) * width), activeChar.getY() + (int) (Math.sin(radian2) * width), activeChar.getZ(), activeChar.getX() + (int) (Math.cos(radian1) * width), activeChar.getY() + (int) (Math.sin(radian1) * width), activeChar.getZ(), 20 );
                l4.addLine(activeChar.getX() + (int) (Math.cos(radian1) * width), activeChar.getY() + (int) (Math.sin(radian1) * width), activeChar.getZ(), c_x + (int) (Math.cos(radian1) * width), c_y + (int) (Math.sin(radian1) * width), activeChar.getZ(), 20 );


//TODO:
//                activeChar.sendPacket(l2);
//                activeChar.sendPacket(l4);


                List<L2Character> _result = new ArrayList<L2Character>(2);
                for (L2Character act : L2World.getAroundCharacters(activeChar, (int) activeChar.getRealDistance3D(target), 256)) {
                    if (act != null && skill.affect_object.validate(activeChar, act) && terr.isInside(act)) {
                        _result.add(act);
                    }
                }
                double dist = skill.getCastRange();
                for (int i = 0; i < _result.size(); i++) {

                    double realDistance3D = activeChar.getRealDistance3D(_result.get(i));
                    activeChar.sendMessage(_result.get(i).getName() + " : dist= " + realDistance3D);
                    if (realDistance3D < dist){
                        dist = realDistance3D;
                        skillTarget = _result.get(i);
                    }
                }
                if (skillTarget == null){
                    skillTarget = (L2Character) activeChar.getTarget();
                }

                activeChar.getAI().Cast(skill, (L2Character) skillTarget , true, false);



                break;
            }

            case admin_paint_line: {
                L2Character target = (L2Character) activeChar.getTarget();
                L2Territory terr = new L2Territory(0);
                L2Character skillTarget = (L2Character) activeChar.getTarget();



                if (activeChar.isPlayer() && ((L2Player) activeChar).isGM()) {
                    ExShowTrace l1 = new ExShowTrace(3000);
                    ExShowTrace l2 = new ExShowTrace(3000);
                    ExShowTrace l3 = new ExShowTrace(3000);
                    ExShowTrace l4 = new ExShowTrace(3000);
                    int[] point2D1 = getPoint2D(activeChar, target, 50);
                    int[] point2D2 = getPoint2D(target, activeChar, 80);
                    int[] point2D3 = new int[2];
                    int[] point2D4 = new int[2];
//
//                    point2D3[0] = (2 * point2D1[0]) - activeChar.getX();
//                    point2D3[1] = (2 * point2D1[1]) - activeChar.getX();
                    point2D3[0] = (2 * activeChar.getX()) -  point2D1[0];
                    point2D3[1] = (2 * activeChar.getX()) - point2D1[1];

//                    point2D4[0] = (2 * point2D2[0]) - target.getX();
//                    point2D4[1] = (2 * point2D2[1]) - target.getX();
                    point2D4[0] = (2 * target.getX() ) - point2D2[0];
                    point2D4[1] = (2 * target.getX() ) - point2D2[1];



//                    double w = 20;
//
//                    double realDistance3D = activeChar.getRealDistance3D(target);
//
//                    double h2 = Math.sqrt((realDistance3D * realDistance3D) + (w * w));
//
//
//                    double u1 = Math.cos(realDistance3D / h2);
//                    int grad = (int) ((180 / Math.PI) * u1);
//
//                    double angle = Location.calculateAngleFrom(activeChar.getPrevX(), activeChar.getPrevY(), target.getX(), target.getY());
//
//                    double x2 = target.getX();
//                    double y2 = target.getY();
//
//                    double x1 = activeChar.getX();
//                    double y1 = activeChar.getY();
////					double x1 = 0;
////					double y1 = 0;
////
////					double x2 = 4;
////					double y2 = 3;
//
//
//                    double powX1 = Math.pow(x1, 2);
//                    double powY1 = Math.pow(y1, 2);
//                    double powX2 = Math.pow(x2, 2);
//                    double powY2 = Math.pow(y2, 2);
//
//                    double pow3X1 = Math.pow(x1, 3);
//                    double pow3Y1 = Math.pow(y1, 3);
//                    double pow3Y2 = Math.pow(y2, 3);
//                    double c = w;
//                    double a = h2;
//
//
//                    double Zx = x1 - x2;
//                    double powZx = Math.pow(Zx, 2);
//                    double Zy = y1 - y2;
//                    double powZy = Math.pow(Zy, 2);
//
//                    double Xf1 = ((-y1 + y2) * Math.sqrt(-(-powX1 + 2 * x2 * x1 - powX2 + (-c + a - y1 + y2) * (-c + a + y1 - y2)) * powZx * (-powX1 + 2 * x2 * x1 - powX2 + (c + a - y1 + y2) * (c + a + y1 - y2))) + (x1 - x2) * (pow3X1 - powX1 * x2 + (powY1 - 2 * y1 * y2 + powY2 + (a * a) - (c * c) - powX2) * x1 - x2 * (-(c * c) - powX2 + (a * a) - powY1 + 2 * y1 * y2 - powY2)));
//                    double Xf2 = ((powX1 - 2 * x2 * x1 + powX2 + powZy) * (x1 - x2));
//
//                    double x = (Xf1 / Xf2) * 0.5;
//                    activeChar.sendMessage("Xf1 = " + Xf1);
//                    activeChar.sendMessage("Xf2 = " + Xf2);
//                    activeChar.sendMessage("X = " + x);
//
//                    double Yf1 = (Math.sqrt(-powZx * (-powX1 + 2 * x2 * x1 - powX2 + (c + a + y1 - y2) * (c + a - y1 + y2)) * (-powX1 + 2 * x2 * x1 - powX2 + (-c + a + y1 - y2) * (-c + a - y1 + y2))) + pow3Y1 - powY1 * y2 + ((a * a) + powX1 - (c * c) + powX2 - 2 * x2 * x1 - powY2) * y1 + pow3Y2 + (powX2 - 2 * x2 * x1 + (c * c) - (a * a) + powX1) * y2);
//                    double Yf2 = (2 * powY1 - 4 * y1 * y2 + 2 * powY2 + 2 * powZx);
//
//                    double y = (Yf1 / Yf2);
//                    activeChar.sendMessage("Yf1 = " + Yf1);
//                    activeChar.sendMessage("Yf2 = " + Yf2);
//                    activeChar.sendMessage("Y = " + y);

//					activeChar.sendMessage(String.valueOf(grad));
//					activeChar.sendMessage("real = " + realDistance3D);
//					activeChar.sendMessage("h2 = " + h2);
//					activeChar.sendMessage("angle = " + angle);
//
//
//					double fx = h2*Math.sin(grad );
//					activeChar.sendMessage("x = " + x);
//					double fy = h2*Math.sin(90-grad*2);
//					activeChar.sendMessage("y = " + y);
//
//
//
//					activeChar.sendMessage("target x= " +target.getX()+ "  y= "+ target.getY());
//					activeChar.sendMessage("self x= " +activeChar.getX()+ "  y= "+ activeChar.getY());
//					activeChar.sendMessage("h2 = " + activeChar.getRealDistance3D(target));
//                    l1.addLine(activeChar.getLoc(), target.getLoc(), 20);
//                    l2.addLine(activeChar.getX(), activeChar.getY(), activeChar.getZ(), point2D1[0], point2D1[1], activeChar.getZ(), 20);
//                    l3.addLine(target.getX(), target.getY(), target.getZ(), point2D2[0], point2D2[1], target.getZ(), 20);
//                    l4.addLine(point2D1[0], point2D1[1], activeChar.getZ(), point2D2[0], point2D2[1], target.getZ(), 20);

                    l1.addLine(point2D1[0], point2D1[1], activeChar.getZ(), point2D2[0], point2D2[1], activeChar.getZ(), 20 );
                    l2.addLine(point2D2[0], point2D2[1], activeChar.getZ(), point2D3[0], point2D3[1], activeChar.getZ(), 20 );
                    l3.addLine(point2D3[0], point2D3[1], activeChar.getZ(), point2D4[0], point2D4[1], activeChar.getZ(), 20 );
                    l4.addLine(point2D4[0], point2D4[1], activeChar.getZ(), point2D1[0], point2D1[1], activeChar.getZ(), 20 );


                    activeChar.sendPacket(l1);
                    activeChar.sendPacket(l2);
                    activeChar.sendPacket(l3);
                    activeChar.sendPacket(l4);

                    terr.add(activeChar.getX(), activeChar.getY(), activeChar.getZ() - 25, activeChar.getZ() + 25);
                    terr.add(target.getX(), target.getY(), target.getZ() - 25, target.getZ() + 25);
                    terr.add(point2D1[0], point2D1[1], activeChar.getZ() - 25, activeChar.getZ() + 25);
                    terr.add(point2D2[0], point2D2[1], target.getZ() - 25, target.getZ() + 25);







                    L2Skill skill = activeChar.getKnownSkill(1239);

                    List<L2Character> _result = new ArrayList<L2Character>(2);
                    for (L2Character act : L2World.getAroundCharacters(target, skill.getCastRange(), 256)) {
                        if (act != null && skill.affect_object.validate(activeChar, act) && terr.isInside(act)) {
                            _result.add(act);
                        }
                    }
                    double dist = skill.getCastRange();
                    for (int i = 0; i < _result.size(); i++) {

                        double realDistance3D = activeChar.getRealDistance3D(_result.get(i));
                        activeChar.sendMessage(_result.get(i).getName() + " : dist= " + realDistance3D);
                        if (realDistance3D < dist){
                            dist = realDistance3D;
                            skillTarget = _result.get(i);
                        }
                    }
                    if (skillTarget == null){
                         skillTarget = (L2Character) activeChar.getTarget();
                    }

                    activeChar.getAI().Cast(skill, (L2Character) skillTarget , true, false);

                    //activeChar.sendPacket(Functions.Points2Trace(terr.getCoords(), 50, true, true));
                }
                break;
            }
        }
        return true;
    }

    public int[] getPoint2D(L2Character p1, L2Character p2, int width) {
        int[] result = new int[2];
        double realDistance3D = p1.getRealDistance3D(p2);

        double h2 = Math.sqrt((realDistance3D * realDistance3D) + (width * width));

        //double c = width;
//        double a = h2;

        double x2 = p2.getX();
        double y2 = p2.getY();

        double x1 = p1.getX();
        double y1 = p1.getY();


        double powX1 = Math.pow(x1, 2);
        double powY1 = Math.pow(y1, 2);
        double powX2 = Math.pow(x2, 2);
        double powY2 = Math.pow(y2, 2);

        double pow3X1 = Math.pow(x1, 3);
        double pow3Y1 = Math.pow(y1, 3);
        double pow3Y2 = Math.pow(y2, 3);

        double Zx = x1 - x2;
        double powZx = Math.pow(Zx, 2);
        double Zy = y1 - y2;
        double powZy = Math.pow(Zy, 2);

        double Xf1 = ((-y1 + y2) * Math.sqrt(-(-powX1 + 2 * x2 * x1 - powX2 + (-width + h2 - y1 + y2) * (-width + h2 + y1 - y2)) * powZx * (-powX1 + 2 * x2 * x1 - powX2 + (width + h2 - y1 + y2) * (width + h2 + y1 - y2))) + (x1 - x2) * (pow3X1 - powX1 * x2 + (powY1 - 2 * y1 * y2 + powY2 + (h2 * h2) - (width * width) - powX2) * x1 - x2 * (-(width * width) - powX2 + (h2 * h2) - powY1 + 2 * y1 * y2 - powY2)));
        double Xf2 = ((powX1 - 2 * x2 * x1 + powX2 + powZy) * (x1 - x2));

        double x = (Xf1 / Xf2) * 0.5;
//        p1.sendMessage("Xf1 = " + Xf1);
//        p1.sendMessage("Xf2 = " + Xf2);
//        p1.sendMessage("X = " + x);

        double Yf1 = (Math.sqrt(-powZx * (-powX1 + 2 * x2 * x1 - powX2 + (width + h2 + y1 - y2) * (width + h2 - y1 + y2)) * (-powX1 + 2 * x2 * x1 - powX2 + (-width + h2 + y1 - y2) * (-width + h2 - y1 + y2))) + pow3Y1 - powY1 * y2 + ((h2 * h2) + powX1 - (width * width) + powX2 - 2 * x2 * x1 - powY2) * y1 + pow3Y2 + (powX2 - 2 * x2 * x1 + (width * width) - (h2 * h2) + powX1) * y2);
        double Yf2 = (2 * powY1 - 4 * y1 * y2 + 2 * powY2 + 2 * powZx);

        double y = Yf1 / Yf2;
//        p1.sendMessage("Yf1 = " + Yf1);
//        p1.sendMessage("Yf2 = " + Yf2);
//        p1.sendMessage("Y = " + y);

        result[0] = (int) x;
        result[1] = (int) y;
        return result;
    }

    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    public void onLoad() {
        AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
    }

    public void onReload() {
    }

    public void onShutdown() {
    }
}