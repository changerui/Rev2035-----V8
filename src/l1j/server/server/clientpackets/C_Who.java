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

import l1j.server.Config;
import l1j.server.server.ClientThread;
import l1j.server.server.GetNowTime;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_WhoAmount;
import l1j.server.server.serverpackets.S_WhoCharinfo;
import l1j.william.L1GameReStart;

public class C_Who extends ClientBasePacket {

	private static final String C_WHO = "[C] C_Who";
	private static Logger _log = Logger.getLogger(C_Who.class.getName());

	public C_Who(byte[] decrypt, ClientThread client) {
		super(decrypt);
		String s = readS();
		L1PcInstance find = L1World.getInstance().getPlayer(s);
		L1PcInstance pc = client.getActiveChar();

		if (find != null) {
			S_WhoCharinfo s_whocharinfo = new S_WhoCharinfo(find);
			pc.sendPackets(s_whocharinfo);
		} else {
			if (Config.ALT_WHO_COMMAND) {
				String amount = String.valueOf(L1World.getInstance()
						.getAllPlayers().size());
				// 刪除S_WhoAmount s_whoamount = new S_WhoAmount(amount);
				// 刪除pc.sendPackets(s_whoamount);
				// 線上資訊
				S_WhoAmount s_whoamount = new S_WhoAmount(amount);
				pc.sendPackets(s_whoamount);
				int i = 1;
				for (L1PcInstance pc1 : L1World.getInstance().getAllPlayers()) {
					if (pc.isGm() == true) {
						pc.sendPackets(new S_SystemMessage(i + "【玩家】【"
								+ pc1.getName() + "】【血盟】【" + pc1.getClanname()
								+ "】【等級【" + pc1.getLevel() + "】"));
					} else {
						pc.sendPackets(new S_SystemMessage(i + "【玩家】【"
								+ pc1.getName() + "】【血盟】【" + pc1.getClanname()
								+ "】"));
					}
					i++;
				}
				pc.sendPackets(new S_SystemMessage("經驗值: " + Config.RATE_XP
						+ " 倍"));
				pc.sendPackets(new S_SystemMessage("掉寶率: "
						+ Config.RATE_DROP_ITEMS + " 倍"));
				pc.sendPackets(new S_SystemMessage("取得金幣: "
						+ Config.RATE_DROP_ADENA + " 倍"));
				pc.sendPackets(new S_SystemMessage("衝裝率: 武器 "
						+ Config.ENCHANT_CHANCE_WEAPON + "%  /  防具 "
						+ Config.ENCHANT_CHANCE_ARMOR + "%"));
				if (Config.REST_TIME != 0) {
					// 今天日期
					int Mon = GetNowTime.GetNowMonth();// 月份錯誤補正
					pc.sendPackets(new S_SystemMessage("\\fU" + "今天是 "
							+ GetNowTime.GetNowYear() + " 年 " + (Mon + 1)
							+ " 月 " + GetNowTime.GetNowDay() + " 日。"));
					// 目前時間
					pc.sendPackets(new S_SystemMessage("\\fU" + "現在時間(24h): "
							+ GetNowTime.GetNowHour() + " 時 "
							+ GetNowTime.GetNowMinute() + " 分 "
							+ GetNowTime.GetNowSecond() + " 秒。"));
					int second = L1GameReStart.getInstance().GetRemnant();
					pc.sendPackets(new S_SystemMessage("\\fU" + "距離伺服器重啟時間還有: "
							+ (second / 60) / 60 + " 小時 " + (second / 60) % 60
							+ " 分 " + second % 60 + " 秒。"));
				}
			} else {
				String amount = String.valueOf(L1World.getInstance()
						.getAllPlayers().size());
				S_WhoAmount s_whoamount = new S_WhoAmount(amount);
				pc.sendPackets(s_whoamount);
			}
			// 對像居場合表示？方修正願。
		}
	}

	@Override
	public String getType() {
		return C_WHO;
	}
}
