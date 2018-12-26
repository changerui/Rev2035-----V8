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

import static l1j.server.server.model.Instance.L1PcInstance.REGENSTATE_MOVE;
import static l1j.server.server.model.skill.L1SkillId.ABSOLUTE_BARRIER;
import static l1j.server.server.model.skill.L1SkillId.MEDITATION;

import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.ClientThread;
import l1j.server.server.model.AcceleratorChecker;
import l1j.server.server.model.Dungeon;
import l1j.server.server.model.DungeonRandom;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.trap.L1WorldTraps;
import l1j.server.server.serverpackets.S_MoveCharPacket;
import l1j.server.server.serverpackets.S_SystemMessage;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_MoveChar extends ClientBasePacket {

	private static Logger _log = Logger.getLogger(C_MoveChar.class.getName());

	private static final byte HEADING_TABLE_X[] = { 0, 1, 1, 1, 0, -1, -1, -1 };

	private static final byte HEADING_TABLE_Y[] = { -1, -1, 0, 1, 1, 1, 0, -1 };

	private static final int CLIENT_LANGUAGE = Config.CLIENT_LANGUAGE;

	// マップタイル調査用
	private void sendMapTileLog(L1PcInstance pc) {
		pc.sendPackets(new S_SystemMessage(pc.getMap().toString(
				pc.getLocation())));
	}

	// 移動
	public C_MoveChar(byte decrypt[], ClientThread client) throws Exception {
		super(decrypt);
		int locx = readH();
		int locy = readH();
		int heading = readC();

		L1PcInstance pc = client.getActiveChar();

		if (pc.isTeleport()) { // テレポート処理中
			return;
		}

		// TODO 角色XY軸修正 by linsf260
		if (Config.CLIENT_LANGUAGE == 3) {
			locx = pc.getX(); // 修正X軸
			locy = pc.getY(); // 修正Y軸
		}
		// TODO 角色XY軸修正

		// 移動要求間隔をチェックする
		if (Config.CHECK_MOVE_INTERVAL) {
			int result;
			result = pc.getAcceleratorChecker().checkInterval(
					AcceleratorChecker.ACT_TYPE.MOVE);
			if (result == AcceleratorChecker.R_DISCONNECTED) {
				return;
			}
		}

		pc.killSkillEffectTimer(MEDITATION);
		pc.setCallClanId(0); // コールクランを唱えた後に移動すると召喚無効

		if (!pc.hasSkillEffect(ABSOLUTE_BARRIER)) { // アブソルートバリア中ではない
			pc.setRegenState(REGENSTATE_MOVE);
		}
		pc.getMap().setPassable(pc.getLocation(), true);

		// TODO 角色XY軸修正 by linsf260
		switch (heading) {
		case 1:
		case 72:
			heading = 1;
			locx++;
			locy--;
			break;
		case 0:
		case 73:
			heading = 0;
			locy--;
			break;
		case 3:
		case 74:
			heading = 3;
			locx++;
			locy++;
			break;
		case 2:
		case 75:
			heading = 2;
			locx++;
			break;
		case 5:
		case 76:
			heading = 5;
			locx--;
			locy++;
			break;
		case 4:
		case 77:
			heading = 4;
			locy++;
			break;
		case 7:
		case 78:
			heading = 7;
			locx--;
			locy--;
			break;
		case 6:
		case 79:
			heading = 6;
			locx--;
			break;
		}
		// TODO 角色XY軸修正

		if (Dungeon.getInstance().dg(locx, locy, pc.getMap().getId(), pc)) { // ダンジョンにテレポートした場合
			return;
		}
		if (DungeonRandom.getInstance().dg(locx, locy, pc.getMap().getId(), pc)) { // テレポート先がランダムなテレポート地点
			return;
		}

		pc.getLocation().set(locx, locy);
		pc.setHeading(heading);
		if (pc.isGmInvis() || pc.isGhost()) {
		} else if (pc.isInvisble()) {
			pc.broadcastPacketForFindInvis(new S_MoveCharPacket(pc), true);
		} else {
			pc.broadcastPacket(new S_MoveCharPacket(pc));
		}

		// sendMapTileLog(pc); // 移動先タイルの情報を送る(マップ調査用)

		L1WorldTraps.getInstance().onPlayerMoved(pc);

		pc.getMap().setPassable(pc.getLocation(), false);
		// user.UpdateObject(); // 可視範囲内の全オブジェクト更新
	}
}