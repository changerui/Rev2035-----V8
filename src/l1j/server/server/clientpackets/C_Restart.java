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

package l1j.server.server.clientpackets;

import java.util.logging.Logger;

import l1j.server.server.ClientThread;
import l1j.server.server.model.Getback;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_MapID;
import l1j.server.server.serverpackets.S_OtherCharPacks;
import l1j.server.server.serverpackets.S_OwnCharPack;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_Weather;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_Restart extends ClientBasePacket {

	private static Logger _log = Logger.getLogger(C_Restart.class.getName());

	private static final String C_RESTART = "[C] C_Restart";

	public C_Restart(byte[] data, ClientThread client) throws Exception {
		super(data);

		L1PcInstance pc = client.getActiveChar();

		if (pc.getLevel() >= 49) { // 49級以上 殷海薩的祝福安全區域登出紀錄
			if (pc.getMap().isSafetyZone(pc.getLocation())) {
				pc.setAinZone(1);
			} else {
				pc.setAinZone(0);
			}
		}

		int[] loc = null;

		if (pc.getHellTime() > 0) {
			loc = new int[] { 32701, 32777, 666 };
		} else {
			loc = Getback.GetBack_Location(pc, true);
		}

		pc.removeAllKnownObjects();
		pc.broadcastPacket(new S_RemoveObject(pc));

		pc.setCurrentHp(pc.getLevel());
		pc.set_food(40);
		pc.setDead(false);
		pc.setStatus(0);
		L1World.getInstance().moveVisibleObject(pc, loc[2]);
		pc.setX(loc[0]);
		pc.setY(loc[1]);
		pc.setMap((short) loc[2]);
		pc.sendPackets(new S_MapID(pc.getMapId(), pc.getMap().isUnderwater()));
		pc.broadcastPacket(new S_OtherCharPacks(pc));
		pc.sendPackets(new S_OwnCharPack(pc));
		pc.sendPackets(new S_CharVisualUpdate(pc));
		pc.startHpRegeneration();
		pc.startMpRegeneration();
		pc.sendPackets(new S_Weather(L1World.getInstance().getWeather()));
		if (pc.getHellTime() > 0) {
			pc.beginHell(false);
			pc.stopPcDeleteTimer();
		}
	}

	@Override
	public String getType() {
		return C_RESTART;
	}
}