package commands.admin;

import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.handler.AdminCommandHandler;
import l2open.gameserver.handler.IAdminCommandHandler;
import l2open.gameserver.model.EffectList;
import l2open.gameserver.model.L2Effect;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.items.Inventory;
import l2open.gameserver.model.items.L2ItemInstance;
import l2open.gameserver.templates.L2Item;

import java.io.FileWriter;
import java.io.IOException;

public class AdminItems implements IAdminCommandHandler, ScriptFile {
    private static enum Commands {
        admin_item_info,
        admin_buff_info,
        admin_inventory_info,
        admin_delete_all_armor,
        admin_time_plus,
    }

    public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar) {
        Commands command = (Commands) comm;

        if (!activeChar.getPlayerAccess().CanEditNPC)
            return false;

        switch (command) {
            case admin_item_info:
                final L2Item.Bodypart[] values = L2Item.Bodypart.values();
                for (int i = 0; i < Inventory.PAPERDOLL_MAX; i++) {
                    final L2ItemInstance paperdollItem = activeChar.getInventory().getPaperdollItem(i);
                    if (paperdollItem != null) {
                        activeChar.sendMessage(paperdollItem.getName() + " - " + paperdollItem.getItemId());
                    }
                }
                break;
            case admin_buff_info:
                final EffectList effectList = activeChar.getEffectList();
                for (L2Effect effect : effectList.getAllEffects()) {
                    final L2Skill skill = effect.getSkill();
                    FileWriter file = null;
                    StringBuilder sb = new StringBuilder();
                    try {
                        file = new FileWriter("out/buff.txt", true);
                        sb.append(skill.getId()).append(", ").append(skill.getLevel()).append(",		//").append(skill.getName()).append("\n");
                        file.write(sb.toString());
                        file.flush();
                        file.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            assert file != null;
                            file.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
                break;
            case admin_time_plus:
                for (int i = 0; i < 24; i++) {
                    for (int j = 0; j < 65; j += 5) {
                        FileWriter file = null;
                        StringBuilder sb = new StringBuilder();
                        try {
                            file = new FileWriter("event_start_time.txt", true);
                            sb.append(i).append(", ").append(j).append(",");
                            file.write(sb.toString());
                            file.flush();
                            file.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                assert file != null;
                                file.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
                break;

            case admin_delete_all_armor:
                for (L2ItemInstance item : activeChar.getInventory().getItems()) {
                    if (item.isArmor()) {
                        activeChar.getInventory().destroyItem(item, item.getCount(), false);
                    }
                }
                break;
            case admin_inventory_info:
                for (L2ItemInstance item : activeChar.getInventory().getItems()) {
                    if (item.isWeapon()) {
                        FileWriter file = null;
                        StringBuilder sb = new StringBuilder();
                        try {
                            file = new FileWriter("inventory_items_weapons.txt", true);
                            sb.append(item.getItemId()).append(", ").append("		//").append(item.getName()).append("  -  ").append(item.getItemType()).append(" ").append(item.getCrystalType()).append("\n");
                            file.write(sb.toString());
                            file.flush();
                            file.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                assert file != null;
                                file.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (item.isArmor()) {
                        FileWriter file = null;
                        StringBuilder sb = new StringBuilder();
                        try {
                            file = new FileWriter("inventory_items_armors.txt", true);
                            sb.append(item.getItemId()).append(", ").append("		//").append(item.getName()).append("  -  ").append(item.getItemType()).append(" ").append(item.getCrystalType()).append("\n");
                            file.write(sb.toString());
                            file.flush();
                            file.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                assert file != null;
                                file.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        FileWriter file = null;
                        StringBuilder sb = new StringBuilder();
                        try {
                            file = new FileWriter("inventory_items_etc.txt", true);
                            sb.append(item.getItemId()).append(", ").append("		//").append(item.getName()).append("  -  ").append(item.getItemType()).append(" ").append(item.getCrystalType()).append("\n");
                            file.write(sb.toString());
                            file.flush();
                            file.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                assert file != null;
                                file.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
                break;
        }

        return true;
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