package ai.Fort.MilitaryAI;

import l2open.database.mysql;
import l2open.gameserver.model.L2Player;
import l2open.util.GArray;

import java.util.HashMap;

public abstract class MilitaryDefaultAI {

    protected abstract void move();
    protected abstract void cast();
    protected abstract void attack();


    protected L2Player getUnitFromDB(){

        GArray<HashMap<String, Object>> list = mysql.getAll("SELECT `obj_id`, `value`, (SELECT `account_name` FROM `characters` WHERE `characters`.`obj_Id` = `character_variables`.`obj_id` LIMIT 1) AS `account_name` FROM `character_variables` WHERE name LIKE 'fortressUnit'");

        for
    }



}
