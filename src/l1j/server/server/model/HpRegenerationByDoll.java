package l1j.server.server.model;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_SkillSound; //娃娃回血效果 by hot183
import l1j.server.server.serverpackets.S_SystemMessage;

public class HpRegenerationByDoll extends TimerTask {
	private static Logger _log = Logger.getLogger(HpRegenerationByDoll.class
			.getName());

	private final L1PcInstance _pc;

	public HpRegenerationByDoll(L1PcInstance pc) {
		_pc = pc;
	}

	@Override
	public void run() {
		try {
			if (_pc.isDead()) {
				return;
			}
			regenHp();
		} catch (Throwable e) {
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	public void regenHp() {
		int mpr = 100;
		if (_pc.get_food() < 3 || isOverWeight(_pc)) {
			mpr = 0;
		}
		int newHp = _pc.getCurrentHp() + mpr;
		if (newHp < 0) {
			newHp = 0;
		}

		// 娃娃回血效果 by hot183
		_pc.sendPackets(new S_SkillSound(_pc.getId(), 6321));// 修正特效編號錯誤問題
																// by0968026609
		_pc.broadcastPacket(new S_SkillSound(_pc.getId(), 6321));// 修正特效編號錯誤問題
																	// by0968026609
		_pc.sendPackets(new S_SystemMessage("突然感到全身充滿力量,體力恢復了許多。"));
		_pc.setCurrentHp(newHp);

	}

	private boolean isOverWeight(L1PcInstance pc) {
		// 態、態、
		// 重量無。
		if (pc.hasSkillEffect(L1SkillId.EXOTIC_VITALIZE)
				|| pc.hasSkillEffect(L1SkillId.ADDITIONAL_FIRE)) {
			return false;
		}

		return (14 < pc.getInventory().getWeight30()) ? true : false;
	}
}
