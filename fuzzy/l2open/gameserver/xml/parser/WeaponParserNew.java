package l2open.gameserver.xml.parser;

import l2open.config.ConfigValue;
import l2open.database.DatabaseUtils;
import l2open.database.FiltredPreparedStatement;
import l2open.database.L2DatabaseFactory;
import l2open.database.ThreadConnection;
import l2open.gameserver.tables.SkillTable;
import l2open.gameserver.templates.L2Item;
import l2open.gameserver.templates.L2Weapon;
import l2open.gameserver.templates.StatsSet;
import l2open.gameserver.xml.XmlUtils;
import l2open.gameserver.xml.loader.XmlWeaponLoader;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author : Ragnarok
 * @date : 09.01.11    12:38
 */
public class WeaponParserNew extends Parser {
    private static WeaponParserNew ourInstance = new WeaponParserNew();
    private static List<Integer> customItem = new ArrayList<Integer>(0);
    private static ConcurrentHashMap<Integer, L2Weapon> weapons = new ConcurrentHashMap<Integer, L2Weapon>();
    private static ConcurrentHashMap<Integer, L2Weapon> weapons_pole = new ConcurrentHashMap<Integer, L2Weapon>();
    private static Logger log = Logger.getLogger(XmlWeaponLoader.class.getName());

    private static int rare = 0;
    private static int pvp = 0;
    private static int sa = 0;
    private static int trig = 0;

    public static void main(String[] args) {
        initialize("./data/stats/items/weapon");
        for(File f : new File(ConfigValue.DatapackRoot + "/data/stats/custom_items").listFiles())
            parseFile(f, true);


        try {
            saveAll("./data/stats/custom_items", "\t", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseFile(File f, boolean custom)
    {
        try
        {
            if(f.getName().endsWith(".xml"))
            {
                Document doc = XmlUtils.readFile(f);
                Element list = doc.getRootElement();
                for(Element weapon : list.elements("weapon"))
                {
                    StatsSet set = new StatsSet();
                    L2Weapon.WeaponType type = L2Weapon.WeaponType.valueOf(weapon.attributeValue("type"));
                    int id = XmlUtils.getSafeInt(weapon, "id", 0);
                    if(id == 0 || !custom && customItem.contains(id))
                        continue;
                    else if(custom)
                        customItem.add(id);
                    set.set("class", "EQUIPMENT");
                    set.set("item_id", id);
                    set.set("name", weapon.attributeValue("name"));
                    for(Iterator<Element> i = weapon.elementIterator("set"); i.hasNext(); )
                    {
                        Element e = i.next();
                        if(e.attributeValue("name").equals("isRare") && e.attributeValue("val").equalsIgnoreCase("true")) // просто счетчик, для интереса
                            rare++;
                        if(e.attributeValue("name").equals("isPvP") && e.attributeValue("val").equalsIgnoreCase("true")) // просто счетчик, для интереса
                            pvp++;
                        if(e.attributeValue("name").equals("isSa") && e.attributeValue("val").equalsIgnoreCase("true")) // просто счетчик, для интереса
                            sa++;
                        set.set(e.attributeValue("name"), e.attributeValue("val"));
                    }
                    if (type == L2Weapon.WeaponType.NONE)
                    { // TODO: избавиться от mask() в типах, и скиллах. Сделать более понятным для конечного пользователя.
                        set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
                        set.set("type2", L2Item.TYPE2_SHIELD_ARMOR);
                    }
                    else
                    {
                        set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
                        set.set("type2", L2Item.TYPE2_WEAPON);
                    }
                    L2Item.Bodypart bodypart = L2Item.Bodypart.NONE;
                    try
                    {
                        bodypart = set.getEnum("bodypart", L2Item.Bodypart.class, L2Item.Bodypart.NONE);
                    }
                    catch (IllegalArgumentException eee)
                    {
                        log.warning(set.getString("item_id") + " " + set.getString("name") + " " + set.getString("bodypart", "!(!I"));
                    }
                    if(type == L2Weapon.WeaponType.PET)
                    {
                        set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
                        if(bodypart == L2Item.Bodypart.WOLF)
                            set.set("type2", L2Item.TYPE2_PET_WOLF);
                        else if(bodypart == L2Item.Bodypart.GWOLF)
                            set.set("type2", L2Item.TYPE2_PET_GWOLF);
                        else if(bodypart == L2Item.Bodypart.HATCHLING)
                            set.set("type2", L2Item.TYPE2_PET_HATCHLING);
                        else
                            set.set("type2", L2Item.TYPE2_PET_STRIDER);
                        set.set("bodypart", "RHAND");
                    }

                    Element triggers = weapon.element("triggers");
                    if(triggers != null)
                    {
                        String sk = "";
                        String lvl = "";
                        String tp = "";
                        String chnc = "";
                        for(Iterator<Element> i = triggers.elementIterator("trigger"); i.hasNext(); )
                        {
                            Element skill = i.next();
                            sk += skill.attributeValue("id");
                            lvl += skill.attributeValue("level");
                            tp += skill.attributeValue("type");
                            chnc += skill.attributeValue("chance");
                        }
                        set.set("triger_id", sk);
                        set.set("triger_level", lvl);
                        set.set("triger_type", tp);
                        set.set("triger_chance", chnc);
                        trig++;
                    }


                    Element skills = weapon.element("skills");
                    if(skills != null)
                    {
                        String sk = "";
                        String lvl = "";
                        for(Iterator<Element> i = skills.elementIterator("skill"); i.hasNext(); )
                        {
                            Element skill = i.next();
                            sk += skill.attributeValue("id") + ";";
                            lvl += skill.attributeValue("lvl") + ";";
                            if(type == L2Weapon.WeaponType.POLE)
                            {
                                //log.info("id: "+skill.attributeValue("id"));

                            }
                        }
                        set.set("skill_id", sk);
                        set.set("skill_level", lvl);
                    }

                    for(int i=1;i<=ConfigValue.EnchantMaxWeapon;i++)
                    {
                        Element enchant4_skill = weapon.element("enchant"+i+"_skill");
                        if(enchant4_skill != null)
                        {
                            set.set("enchant"+i+"_skill_id", enchant4_skill.attributeValue("id"));
                            set.set("enchant"+i+"_skill_lvl", enchant4_skill.attributeValue("lvl"));
                        }
                    }

                    L2Weapon weap = new L2Weapon(type, set);
                    if(weap.isPvP())
                    {
                        switch(type)
                        {
                            case BOW:
                            case CROSSBOW:
                                weap.attachSkill(SkillTable.getInstance().getInfo(3655, 1)); // PvP Weapon - Rapid Fire
                                break;
                            case BIGSWORD:
                            case BIGBLUNT:
                            case ANCIENTSWORD:
                                if(weap.isMageSA())
                                    weap.attachSkill(SkillTable.getInstance().getInfo(3654, 1)); // PvP Weapon - Casting
                                else
                                    weap.attachSkill(SkillTable.getInstance().getInfo(3653, 1)); // PvP Weapon - Attack Chance
                                break;
                            case SWORD:
                            case BLUNT:
                            case RAPIER:
                                if(weap.isMageSA())
                                    weap.attachSkill(SkillTable.getInstance().getInfo(3654, 1)); // PvP Weapon - Casting
                                else
                                    weap.attachSkill(SkillTable.getInstance().getInfo(3650, 1)); // PvP Weapon - CP Drain
                                break;
                            case FIST:
                            case DUALFIST:
                            case DAGGER:
                            case DUALDAGGER:
                                weap.attachSkill(SkillTable.getInstance().getInfo(3651, 1)); // PvP Weapon - Cancel
                                weap.attachSkill(SkillTable.getInstance().getInfo(3652, 1)); // PvP Weapon - Ignore Shield Defense
                                break;
                            case POLE:
                                weap.attachSkill(SkillTable.getInstance().getInfo(3653, 1)); // PvP Weapon - Attack Chance
                                break;
                            case DUAL:
                                weap.attachSkill(SkillTable.getInstance().getInfo(3656, 1)); // PvP Weapon - Decrease Range
                                break;
                        }
                    }
                    if(type == L2Weapon.WeaponType.POLE)
                    {
                        //log.info("---------------------weapon: "+id);
                        weapons_pole.put(id, weap);
                    }
                    weapons.put(id, weap);
                }
            }
        }
        catch (DocumentException e)
        {
            e.printStackTrace();
        }
    }}
