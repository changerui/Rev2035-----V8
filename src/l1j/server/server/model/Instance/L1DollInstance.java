/*
 * This program is free software; you can redistribute it and/or modify
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

package l1j.server.server.model.Instance;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import l1j.server.server.ActionCodes;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.IdFactory;
import l1j.server.server.model.L1World;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_DollPack;
import l1j.server.server.serverpackets.S_MoveCharPacket;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Npc;

public class L1DollInstance extends L1NpcInstance {
	private static final long serialVersionUID = 1L;

	public static final int DOLLTYPE_BUGBEAR = 0;
	public static final int DOLLTYPE_SUCCUBUS = 1;
	public static final int DOLLTYPE_WAREWOLF = 2;
	// 新增3隻魔法娃娃 by 夜小空 1/4
	public static final int DOLLTYPE_BUGELDER = 3;// 長老
	public static final int DOLLTYPE_CRUSTANCE = 4;// 奎斯坦修
	public static final int DOLLTYPE_STONEGOLEM = 5;// 石頭高崙
	public static final int DOLLTYPE_PRINCESS = 6;// 魔法公主 by hot183
	public static final int DOLLTYPE_SEADANCER = 7;// 希爾黛絲 by hot183
	public static final int DOLLTYPE_SERPENTWOMAN = 8;// 蛇女 by ssississi
	public static final int DOLL_TIME = 1800000;

	private static Logger _log = Logger.getLogger(L1DollInstance.class
			.getName());
	private ScheduledFuture<?> _dollFuture;
	private static Random _random = new Random();
	private int _dollType;
	private int _itemObjId;

	// ターゲットがいない場合の処理
	@Override
	public boolean noTarget() { // TODO 魔法娃娃動作Ｃ版本 by 夜小空
		if (_master.isDead()) {
			deleteDoll();
			return true;
		} else if (_master != null && _master.getMapId() == getMapId()) {
			int dir = moveDirection(_master.getX(), _master.getY());
			if (getLocation().getTileLineDistance(_master.getLocation()) < 3) {
				for (int a = 1; a > 0; a--) {
					try {
						Thread.sleep(600);
						switch (new Random().nextInt(20)) {
						case 5:
							broadcastPacket(new S_DoActionGFX(getId(),
									ActionCodes.ACTION_Think));
							Thread.sleep(2000);
							break;
						case 15:
							broadcastPacket(new S_DoActionGFX(getId(),
									ActionCodes.ACTION_Aggress));
							Thread.sleep(2200);
							break;
						}
					} catch (Exception exception) {
						break;
					}
				}
			} else if (getLocation().getTileLineDistance(_master.getLocation()) > 9) {
				setDirectionMove(dir);
			} else {
				setDirectionMove(dir);
				setSleepTime(calcSleepTime(getPassispeed(), dir));
			}
		} else {
			deleteDoll();
			return true;
		}
		return false;
	}

	// ■■■■■■■■■■■■■ 移動関連 ■■■■■■■■■■■
	// 指定された方向に移動させる
	public void setDirectionMoveDoll(int dir) {
		if (dir >= 0) {
			int nx = 0;
			int ny = 0;

			switch (dir) {
			case 1:
				nx = 1;
				ny = -1;
				setHeading(1);
				break;

			case 2:
				nx = 1;
				ny = 0;
				setHeading(2);
				break;

			case 3:
				nx = 1;
				ny = 1;
				setHeading(3);
				break;

			case 4:
				nx = 0;
				ny = 1;
				setHeading(4);
				break;

			case 5:
				nx = -1;
				ny = 1;
				setHeading(5);
				break;

			case 6:
				nx = -1;
				ny = 0;
				setHeading(6);
				break;

			case 7:
				nx = -1;
				ny = -1;
				setHeading(7);
				break;

			case 0:
				nx = 0;
				ny = -1;
				setHeading(0);
				break;

			default:
				break;

			}

			getMap().setPassable(getLocation(), true);

			int nnx = _master.getX() + nx;
			int nny = _master.getY() + ny;
			setX(nnx);
			setY(nny);

			getMap().setPassable(getLocation(), false);

			broadcastPacket(new S_MoveCharPacket(this));
		}
	}

	// 時間計測用
	class DollTimer implements Runnable {
		// @Override
		@Override
		public void run() {
			if (_destroyed) { // 既に破棄されていないかチェック
				return;
			}
			deleteDoll();
		}
	}

	public L1DollInstance(L1Npc template, L1PcInstance master, int dollType,
			int itemObjId) {
		super(template);
		setId(IdFactory.getInstance().nextId());

		setDollType(dollType);
		setItemObjId(itemObjId);
		_dollFuture = GeneralThreadPool.getInstance().schedule(new DollTimer(),
				DOLL_TIME);

		setMaster(master);
		setX(master.getX() + _random.nextInt(5) - 2);
		setY(master.getY() + _random.nextInt(5) - 2);
		setMap(master.getMapId());
		setHeading(5);
		setLightSize(template.getLightSize());

		L1World.getInstance().storeObject(this);
		L1World.getInstance().addVisibleObject(this);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			onPerceive(pc);
		}
		master.addDoll(this);
		if (!isAiRunning()) {
			startAI();
		}
		if (isMpRegeneration()) {
			master.startMpRegenerationByDoll();

		}
		// TODO 魔法娃娃回血功能 by hot183
		if (isHpRegeneration()) {
			master.startHpRegenerationByDoll();
		}
	}

	public void deleteDoll() {
		if (isMpRegeneration()) {
			((L1PcInstance) _master).stopMpRegenerationByDoll();
		}
		// TODO 魔法娃娃回血功能 by hot183
		if (isHpRegeneration()) {
			((L1PcInstance) _master).stopHpRegenerationByDoll();
		}
		_master.getDollList().remove(getId());
		deleteMe();
	}

	@Override
	public void onPerceive(L1PcInstance perceivedFrom) {
		perceivedFrom.addKnownObject(this);
		perceivedFrom.sendPackets(new S_DollPack(this, perceivedFrom));
	}

	@Override
	public void onItemUse() {
		if (!isActived()) {
			// １００％の確率でヘイストポーション使用
			useItem(USEITEM_HASTE, 100);
		}
	}

	@Override
	public void onGetItem(L1ItemInstance item) {
		if (getNpcTemplate().get_digestitem() > 0) {
			setDigestItem(item);
		}
		if (Arrays.binarySearch(haestPotions, item.getItem().getItemId()) >= 0) {
			useItem(USEITEM_HASTE, 100);
		}
	}

	public int getDollType() {
		return _dollType;
	}

	public void setDollType(int i) {
		_dollType = i;
	}

	public int getItemObjId() {
		return _itemObjId;
	}

	public void setItemObjId(int i) {
		_itemObjId = i;
	}

	// TODO 魔法娃娃增加傷害功能
	public int getDamageByDoll() {
		int damage = 0;
		if (getDollType() == DOLLTYPE_WAREWOLF
				|| getDollType() == DOLLTYPE_CRUSTANCE) {// TODO　魔法娃娃 by 夜小空
			int chance = _random.nextInt(100) + 1;
			if (chance <= 3) {
				damage = 15;
				if (_master instanceof L1PcInstance) {
					L1PcInstance pc = (L1PcInstance) _master;
					pc.sendPackets(new S_SkillSound(_master.getId(), 6319));
				}
				_master.broadcastPacket(new S_SkillSound(_master.getId(), 6319));
			}
		}
		return damage;
	}

	// TODO 經驗加倍魔法娃娃 by hot183
	public double getDoubleExpByDoll() {
		double DoubleExp = 1.0;
		if (getDollType() == DOLLTYPE_PRINCESS) {
			int chance = _random.nextInt(100) + 1;
			if (chance <= 30) {
				DoubleExp = 1.5;
				if (_master instanceof L1PcInstance) {
					L1PcInstance pc = (L1PcInstance) _master;
					pc.sendPackets(new S_SystemMessage("經驗值上升 " + DoubleExp
							+ " 倍！"));
					pc.sendPackets(new S_SkillSound(_master.getId(), 6319));
				}
			}
		}
		return DoubleExp;
	}

	// TODO 魔法娃娃減免傷害功能
	public int getDamageReductionByDoll() {
		int DamageReduction = 0;
		if (getDollType() == DOLLTYPE_STONEGOLEM) {
			byte chance = (byte) (_random.nextInt(100) + 1);
			if (chance <= 4) {
				DamageReduction = 15;
				if (_master instanceof L1PcInstance) {
					L1PcInstance pc = (L1PcInstance) _master;
					pc.sendPackets(new S_SkillSound(_master.getId(), 6320));
				}
				_master.broadcastPacket(new S_SkillSound(_master.getId(), 6320));
			}
		}
		return DamageReduction;
	}

	// TODO 魔法娃娃增加回魔功能
	public boolean isMpRegeneration() {
		boolean isMpRegeneration = false;
		if (getDollType() == DOLLTYPE_SUCCUBUS
				|| getDollType() == DOLLTYPE_BUGELDER) {// TODO　魔法娃娃 by 夜小空
			isMpRegeneration = true;
		}
		return isMpRegeneration;
	}

	// TODO　魔法娃娃回血功能 by hot183
	public boolean isHpRegeneration() {
		boolean isHpRegeneration = false;
		if (getDollType() == DOLLTYPE_SEADANCER
				|| getDollType() == DOLLTYPE_SERPENTWOMAN) {
			isHpRegeneration = true;
		}
		return isHpRegeneration;
	}

	// TODO 魔法娃娃減免負重功能
	public int getWeightReductionByDoll() {
		int weightReduction = 0;
		if (getDollType() == DOLLTYPE_BUGBEAR) {
			weightReduction = 10;
		}
		return weightReduction;
	}

}
