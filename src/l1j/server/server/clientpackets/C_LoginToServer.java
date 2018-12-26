/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package l1j.server.server.clientpackets;

import static l1j.server.server.model.skill.L1SkillId.COOKING_1_0_N;
import static l1j.server.server.model.skill.L1SkillId.COOKING_1_0_S;
import static l1j.server.server.model.skill.L1SkillId.COOKING_1_6_N;
import static l1j.server.server.model.skill.L1SkillId.COOKING_1_6_S;
import static l1j.server.server.model.skill.L1SkillId.COOKING_2_0_N;
import static l1j.server.server.model.skill.L1SkillId.COOKING_2_0_S;
import static l1j.server.server.model.skill.L1SkillId.COOKING_2_6_N;
import static l1j.server.server.model.skill.L1SkillId.COOKING_2_6_S;
import static l1j.server.server.model.skill.L1SkillId.COOKING_3_0_N;
import static l1j.server.server.model.skill.L1SkillId.COOKING_3_0_S;
import static l1j.server.server.model.skill.L1SkillId.COOKING_3_6_N;
import static l1j.server.server.model.skill.L1SkillId.COOKING_3_6_S;
import static l1j.server.server.model.skill.L1SkillId.SHAPE_CHANGE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_BLUE_POTION;
import static l1j.server.server.model.skill.L1SkillId.STATUS_BRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_CHAT_PROHIBITED;
import static l1j.server.server.model.skill.L1SkillId.STATUS_ELFBRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HASTE;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.ActionCodes;
import l1j.server.server.ClientThread;
import l1j.server.server.WarTimeController;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.GetBackRestartTable;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.model.Getback;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Cooking;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_ActiveSpells;
import l1j.server.server.serverpackets.S_AddSkill;
import l1j.server.server.serverpackets.S_Bookmarks;
import l1j.server.server.serverpackets.S_CharTitle;
import l1j.server.server.serverpackets.S_CharacterConfig;
import l1j.server.server.serverpackets.S_InitialAbilityGrowth;
import l1j.server.server.serverpackets.S_InvList;
import l1j.server.server.serverpackets.S_Karma;
import l1j.server.server.serverpackets.S_MapID;
import l1j.server.server.serverpackets.S_OwnCharPack;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_SPMR;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillBrave;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillIconExp;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SummonPack;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_Unknown1;
import l1j.server.server.serverpackets.S_Unknown2;
import l1j.server.server.serverpackets.S_War;
import l1j.server.server.serverpackets.S_Weather;
import l1j.server.server.serverpackets.S_bonusstats;
import l1j.server.server.templates.L1BookMark;
import l1j.server.server.templates.L1GetBackRestart;
import l1j.server.server.templates.L1Skills;
import l1j.server.server.utils.SQLUtil;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket
//
public class C_LoginToServer extends ClientBasePacket {

	private static final String C_LOGIN_TO_SERVER = "[C] C_LoginToServer";
	private static Logger _log = Logger.getLogger(C_LoginToServer.class
			.getName());

	public C_LoginToServer(byte abyte0[], ClientThread client)
			throws FileNotFoundException, Exception {
		super(abyte0);

		String login = client.getAccountName();

		String charName = readS();

		if (client.getActiveChar() != null) {
			_log.info("角色ID重複登入(" + client.getHostname() + ")已強制中斷連線 。");
			client.close();
			return;
		}

		L1PcInstance pc = L1PcInstance.load(charName);
		if (pc == null || !login.equals(pc.getAccountName())) {
			_log.info("【無法進入】 角色名稱：" + charName + " 玩家帳號：" + login + " 玩家IP："
					+ client.getHostname());
			client.close();
			return;
		}

		if (Config.LEVEL_DOWN_RANGE != 0) {
			if (pc.getHighLevel() - pc.getLevel() >= Config.LEVEL_DOWN_RANGE) {
				_log.info("不容許的等級要求範圍的角色: 角色名稱=" + charName + " 玩家帳號=" + login
						+ " 玩家IP=" + client.getHostname());
				client.kick();
				return;
			}
		}

		// TODO 管理者介面byeric1300460
		if (Config.GUI) {
			l1j.eric.gui.J_Main.getInstance().addPlayerTable(login, charName,
					client.getHostname());
		}

		_log.info("【進入遊戲】 角色名稱：" + charName + " 玩家帳號：" + login + " 玩家IP："
				+ client.getHostname());

		int currentHpAtLoad = pc.getCurrentHp();
		int currentMpAtLoad = pc.getCurrentMp();
		pc.clearSkillMastery();
		pc.setOnlineStatus(1);
		CharacterTable.updateOnlineStatus(pc);
		L1World.getInstance().storeObject(pc);

		pc.setNetConnection(client);
		pc.setPacketOutput(client);
		client.setActiveChar(pc);

		// TODO 初始能力加成
		S_InitialAbilityGrowth AbilityGrowth = new S_InitialAbilityGrowth(pc);
		pc.sendPackets(AbilityGrowth);

		S_Unknown1 s_unknown1 = new S_Unknown1();
		pc.sendPackets(s_unknown1);
		S_Unknown2 s_unknown2 = new S_Unknown2();
		pc.sendPackets(s_unknown2);

		bookmarks(pc);

		// リスタート先がgetback_restartテーブルで指定されていたら移動させる
		GetBackRestartTable gbrTable = GetBackRestartTable.getInstance();
		L1GetBackRestart[] gbrList = gbrTable.getGetBackRestartTableList();
		for (L1GetBackRestart gbr : gbrList) {
			if (pc.getMapId() == gbr.getArea()) {
				pc.setX(gbr.getLocX());
				pc.setY(gbr.getLocY());
				pc.setMap(gbr.getMapId());
				break;
			}
		}

		// altsettings.propertiesでGetBackがtrueなら街に移動させる
		if (Config.GET_BACK) {
			int[] loc = Getback.GetBack_Location(pc, true);
			pc.setX(loc[0]);
			pc.setY(loc[1]);
			pc.setMap((short) loc[2]);
		}

		// 戦争中の旗内に居た場合、城主血盟でない場合は帰還させる。
		int castle_id = L1CastleLocation.getCastleIdByArea(pc);
		if (0 < castle_id) {
			if (WarTimeController.getInstance().isNowWar(castle_id)) {
				L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
				if (clan != null) {
					if (clan.getCastleId() != castle_id) {
						// 城主クランではない
						int[] loc = new int[3];
						loc = L1CastleLocation.getGetBackLoc(castle_id);
						pc.setX(loc[0]);
						pc.setY(loc[1]);
						pc.setMap((short) loc[2]);
					}
				} else {
					// クランに所属して居ない場合は帰還
					int[] loc = new int[3];
					loc = L1CastleLocation.getGetBackLoc(castle_id);
					pc.setX(loc[0]);
					pc.setY(loc[1]);
					pc.setMap((short) loc[2]);
				}
			}
		}

		L1World.getInstance().addVisibleObject(pc);
		S_ActiveSpells s_activespells = new S_ActiveSpells(pc);
		pc.sendPackets(s_activespells);
		pc.sendPackets(new S_Karma(pc)); //TODO カルマ値を表示

		pc.beginGameTimeCarrier();

		S_OwnCharStatus s_owncharstatus = new S_OwnCharStatus(pc);
		pc.sendPackets(s_owncharstatus);

		S_MapID s_mapid = new S_MapID(pc.getMapId(), pc.getMap().isUnderwater());
		pc.sendPackets(s_mapid);

		S_OwnCharPack s_owncharpack = new S_OwnCharPack(pc);
		pc.sendPackets(s_owncharpack);

		pc.sendPackets(new S_SPMR(pc));

		// XXX タイトル情報はS_OwnCharPackに含まれるので多分不要
		S_CharTitle s_charTitle = new S_CharTitle(pc.getId(), pc.getTitle());
		pc.sendPackets(s_charTitle);
		pc.broadcastPacket(s_charTitle);

		pc.sendVisualEffectAtLogin(); // クラウン、毒、水中等の視覚効果を表示

		pc.sendPackets(new S_Weather(L1World.getInstance().getWeather()));

		items(pc);
		skills(pc);
		buff(client, pc);
		pc.turnOnOffLight();

		// TODO 殷海薩的祝福
		int ainOutTime = Config.RATE_AIN_OUTTIME;
		int ainMaxPercent = Config.RATE_MAX_CHARGE_PERCENT;

		if (pc.getLevel() >= 49) { // TODO 49級以上 殷海薩的祝福紀錄
			if (pc.getMap().isSafetyZone(pc.getLocation())) {
				pc.setAinZone(1);
			} else {
				pc.setAinZone(0);
			}

			if (pc.getAinPoint() >= 1) {
				pc.sendPackets(new S_SkillIconExp(pc.getAinPoint()));// TODO 角色
																		// 登入時點數大於1則送出
			}

			if (pc.getAinZone() == 1) {
				Calendar cal = Calendar.getInstance();
				long startTime = (cal.getTimeInMillis() - pc.getLastActive()
						.getTime()) / 60000;

				if (startTime >= ainOutTime) {
					long outTime = startTime / ainOutTime;
					long saveTime = outTime + pc.getAinPoint();
					if (saveTime >= 1 && saveTime <= ainMaxPercent) {
						pc.setAinPoint((int) saveTime);
					} else if (saveTime > ainMaxPercent) {
						pc.setAinPoint(ainMaxPercent);
					}
				}
			}
		}

		if (pc.getCurrentHp() > 0) {
			pc.setDead(false);
			pc.setStatus(0);
		} else {
			pc.setDead(true);
			pc.setStatus(ActionCodes.ACTION_Die);
		}
		// TODO　在線一段時間給物品　by 阿傑
		if (Config.GITorF) {
			if (!pc.hasSkillEffect(1920)) {
				pc.setSkillEffect(1920, Config.GIT * 60000);// 3分鐘
			}
		}
		if (pc.getLevel() >= 51 && pc.getLevel() - 50 > pc.getBonusStats()) {
			if ((pc.getBaseStr() + pc.getBaseDex() + pc.getBaseCon()
					+ pc.getBaseInt() + pc.getBaseWis() + pc.getBaseCha()) < (Config.BONUS_STATS1 * 6)) {// TODO
																											// 調整能力值上限
				pc.sendPackets(new S_bonusstats(pc.getId(), 1));
			}
		}

		if (Config.CHARACTER_CONFIG_IN_SERVER_SIDE) {
			pc.sendPackets(new S_CharacterConfig(pc.getId()));
		}

		serchSummon(pc);

		WarTimeController.getInstance().checkCastleWar(pc);

		// TODO 玩家上線通知GM by xxyyzzxyz & 夜貓子 code
		if (Config.WHO_ONLINE_MSG_ON) {
			Collection<L1PcInstance> allGM = L1World.getInstance()
					.getAllPlayers();
			for (L1PcInstance object : allGM) {
				if (object instanceof L1PcInstance) {
					L1PcInstance GM = object;
					if (pc.getClanid() >= 0
							&& (GM.getAccessLevel() == 100 || GM
									.getAccessLevel() == 200))// xxyyzzxyz & 夜貓子
					{
						String msg = "";
						if (pc.isCrown()) {
							msg = "王子";
						} else if (pc.isKnight()) {
							msg = "騎士";
						} else if (pc.isElf()) {
							msg = "妖精";
						} else if (pc.isWizard()) {
							msg = "法師";
						} else if (pc.isDarkelf()) {
							msg = "黑妖";
						} else if (pc.isDragonKnight()) {
							msg = "龍騎士";
						} else if (pc.isIllusionist()) {
							msg = "幻術士";
						}
						GM.sendPackets(new S_SystemMessage(("(玩家"
								+ pc.getName() + ")(帳號"
								+ client.getAccountName() + ")" + "(IP"
								+ client.getIp() + ")" + "(職業" + msg + ")(上線)")));
					}
				}
			}
		}
		if (pc.getClanid() != 0) { // クラン所属中
			L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
			if (clan != null) {
				if (pc.getClanid() == clan.getClanId() && // クランを解散して、再度、同名のクランが創設された時の対策
						pc.getClanname().toLowerCase()
								.equals(clan.getClanName().toLowerCase())) {
					L1PcInstance[] clanMembers = clan.getOnlineClanMember();
					for (L1PcInstance clanMember : clanMembers) {
						if (clanMember.getId() != pc.getId()) {
							clanMember.sendPackets(new S_ServerMessage(843, pc
									.getName())); // 只今、血盟員の%0%sがゲームに接続しました。
						}
					}

					// 全戦争リストを取得
					for (L1War war : L1World.getInstance().getWarList()) {
						boolean ret = war.CheckClanInWar(pc.getClanname());
						if (ret) { // 戦争に参加中
							String enemy_clan_name = war.GetEnemyClanName(pc
									.getClanname());
							if (enemy_clan_name != null) {
								// あなたの血盟が現在_血盟と交戦中です。
								pc.sendPackets(new S_War(8, pc.getClanname(),
										enemy_clan_name));
							}
							break;
						}
					}
				} else {
					pc.setClanid(0);
					pc.setClanname("");
					pc.setClanRank(0);
					pc.save(); // DBにキャラクター情報を書き込む
				}
			}
		}

		if (pc.getPartnerId() != 0) { // 結婚中
			L1PcInstance partner = (L1PcInstance) L1World.getInstance()
					.findObject(pc.getPartnerId());
			if (partner != null && partner.getPartnerId() != 0) {
				if (pc.getPartnerId() == partner.getId()
						&& partner.getPartnerId() == pc.getId()) {
					pc.sendPackets(new S_ServerMessage(548)); // あなたのパートナーは今ゲーム中です。
					partner.sendPackets(new S_ServerMessage(549)); // あなたのパートナーはたった今ログインしました。
				}
			}
		}

		if (currentHpAtLoad > pc.getCurrentHp()) {
			pc.setCurrentHp(currentHpAtLoad);
		}
		if (currentMpAtLoad > pc.getCurrentMp()) {
			pc.setCurrentMp(currentMpAtLoad);
		}
		pc.startHpRegeneration();
		pc.startMpRegeneration();
		pc.startObjectAutoUpdate();
		client.CharReStart(false);
		pc.beginExpMonitor();
		pc.save(); // DBにキャラクター情報を書き込む

		pc.sendPackets(new S_OwnCharStatus(pc));

		if (pc.getHellTime() > 0) {
			pc.beginHell(false);
		}
	}

	public static void items(L1PcInstance pc) {
		// DBからキャラクターと倉庫のアイテムを読み込む
		CharacterTable.getInstance().restoreInventory(pc);

		pc.sendPackets(new S_InvList(pc.getInventory().getItems()));
	}

	private void bookmarks(L1PcInstance pc) {

		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {

			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("SELECT * FROM character_teleport WHERE char_id=? ORDER BY name ASC");
			pstm.setInt(1, pc.getId());

			rs = pstm.executeQuery();
			while (rs.next()) {
				L1BookMark bookmark = new L1BookMark();
				bookmark.setId(rs.getInt("id"));
				bookmark.setCharId(rs.getInt("char_id"));
				bookmark.setName(rs.getString("name"));
				bookmark.setLocX(rs.getInt("locx"));
				bookmark.setLocY(rs.getInt("locy"));
				bookmark.setMapId(rs.getShort("mapid"));
				S_Bookmarks s_bookmarks = new S_Bookmarks(bookmark.getName(),
						bookmark.getMapId(), bookmark.getId());
				pc.addBookMark(bookmark);
				pc.sendPackets(s_bookmarks);
			}

		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	private void skills(L1PcInstance pc) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {

			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("SELECT * FROM character_skills WHERE char_obj_id=?");
			pstm.setInt(1, pc.getId());
			rs = pstm.executeQuery();
			int i = 0;
			int lv1 = 0;
			int lv2 = 0;
			int lv3 = 0;
			int lv4 = 0;
			int lv5 = 0;
			int lv6 = 0;
			int lv7 = 0;
			int lv8 = 0;
			int lv9 = 0;
			int lv10 = 0;
			int lv11 = 0;
			int lv12 = 0;
			int lv13 = 0;
			int lv14 = 0;
			int lv15 = 0;
			int lv16 = 0;
			int lv17 = 0;
			int lv18 = 0;
			int lv19 = 0;
			int lv20 = 0;
			int lv21 = 0;
			int lv22 = 0;
			int lv23 = 0;
			int lv24 = 0;
			int lv25 = 0;
			int lv26 = 0;
			int lv27 = 0;
			int lv28 = 0;
			while (rs.next()) {
				int skillId = rs.getInt("skill_id");
				L1Skills l1skills = SkillsTable.getInstance().getTemplate(
						skillId);
				if (l1skills.getSkillLevel() == 1) {
					lv1 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 2) {
					lv2 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 3) {
					lv3 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 4) {
					lv4 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 5) {
					lv5 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 6) {
					lv6 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 7) {
					lv7 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 8) {
					lv8 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 9) {
					lv9 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 10) {
					lv10 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 11) {
					lv11 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 12) {
					lv12 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 13) {
					lv13 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 14) {
					lv14 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 15) {
					lv15 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 16) {
					lv16 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 17) {
					lv17 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 18) {
					lv18 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 19) {
					lv19 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 20) {
					lv20 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 21) {
					lv21 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 22) {
					lv22 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 23) {
					lv23 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 24) {
					lv24 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 25) {
					lv25 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 26) {
					lv26 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 27) {
					lv27 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 28) {
					lv28 |= l1skills.getId();
				}
				i = lv1 + lv2 + lv3 + lv4 + lv5 + lv6 + lv7 + lv8 + lv9 + lv10
						+ lv11 + lv12 + lv13 + lv14 + lv15 + lv16 + lv17 + lv18
						+ lv19 + lv20 + lv21 + lv22 + lv23 + lv24 + lv25 + lv26
						+ lv27 + lv28;
				pc.setSkillMastery(skillId);
			}
			if (i > 0) {
				pc.sendPackets(new S_AddSkill(lv1, lv2, lv3, lv4, lv5, lv6,
						lv7, lv8, lv9, lv10, lv11, lv12, lv13, lv14, lv15,
						lv16, lv17, lv18, lv19, lv20, lv21, lv22, lv23, lv24,
						lv25, lv26, lv27, lv28));
				// _log.warning("ここたち来るのね＠直訳");
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	private void serchSummon(L1PcInstance pc) {
		for (L1SummonInstance summon : L1World.getInstance().getAllSummons()) {
			if (summon.getMaster().getId() == pc.getId()) {
				summon.setMaster(pc);
				pc.addPet(summon);
				for (L1PcInstance visiblePc : L1World.getInstance()
						.getVisiblePlayer(summon)) {
					visiblePc.sendPackets(new S_SummonPack(summon, visiblePc));
				}
			}
		}
	}

	private void buff(ClientThread clientthread, L1PcInstance pc) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {

			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("SELECT * FROM character_buff WHERE char_obj_id=?");
			pstm.setInt(1, pc.getId());
			rs = pstm.executeQuery();
			while (rs.next()) {
				int skillid = rs.getInt("skill_id");
				int remaining_time = rs.getInt("remaining_time");
				if (skillid == SHAPE_CHANGE) { // 変身
					int poly_id = rs.getInt("poly_id");
					L1PolyMorph.doPoly(pc, poly_id, remaining_time,
							L1PolyMorph.MORPH_BY_LOGIN);
				} else if (skillid == STATUS_BRAVE) { // ブレイブ ポーション等
					pc.sendPackets(new S_SkillBrave(pc.getId(), 1,
							remaining_time));
					pc.broadcastPacket(new S_SkillBrave(pc.getId(), 1, 0));
					pc.setBraveSpeed(1);
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_ELFBRAVE) { // エルヴンワッフル
					pc.sendPackets(new S_SkillBrave(pc.getId(), 3,
							remaining_time));
					pc.broadcastPacket(new S_SkillBrave(pc.getId(), 3, 0));
					pc.setBraveSpeed(1);
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_HASTE) { // グリーン ポーション
					pc.sendPackets(new S_SkillHaste(pc.getId(), 1,
							remaining_time));
					pc.broadcastPacket(new S_SkillHaste(pc.getId(), 1, 0));
					pc.setMoveSpeed(1);
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_BLUE_POTION) { // ブルーポーション
					pc.sendPackets(new S_SkillIconGFX(34, remaining_time));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid == STATUS_CHAT_PROHIBITED) { // チャット禁止
					pc.sendPackets(new S_SkillIconGFX(36, remaining_time));
					pc.setSkillEffect(skillid, remaining_time * 1000);
				} else if (skillid >= COOKING_1_0_N && skillid <= COOKING_1_6_N
						|| skillid >= COOKING_1_0_S && skillid <= COOKING_1_6_S
						|| skillid >= COOKING_2_0_N && skillid <= COOKING_2_6_N
						|| skillid >= COOKING_2_0_S && skillid <= COOKING_2_6_S
						|| skillid >= COOKING_3_0_N && skillid <= COOKING_3_6_N
						|| skillid >= COOKING_3_0_S && skillid <= COOKING_3_6_S) { // 料理(デザートは除く)
					L1Cooking.eatCooking(pc, skillid, remaining_time);
				} else {
					L1SkillUse l1skilluse = new L1SkillUse();
					l1skilluse.handleCommands(clientthread.getActiveChar(),
							skillid, pc.getId(), pc.getX(), pc.getY(), null,
							remaining_time, L1SkillUse.TYPE_LOGIN);
				}
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	@Override
	public String getType() {
		return C_LOGIN_TO_SERVER;
	}
}
