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
package l1j.server.server.serverpackets;

import java.util.logging.Logger;

import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.Config; //TODO GM使用公頻(&)顯示方式設定 4/5
//Referenced classes of package l1j.server.server.serverpackets:
//ServerBasePacket

public class S_ChatPacket extends ServerBasePacket {

	private static Logger _log = Logger.getLogger(S_ChatPacket.class.getName());
	private static final String _S__1F_NORMALCHATPACK = "[S] S_ChatPacket";
	private byte[] _byte = null;

	public S_ChatPacket(L1PcInstance pc, String chat, int opcode, int type) {

		if (type == 0) { // 通常チャット
			writeC(opcode);
			writeC(type);
			if (pc.isInvisble()) {
				writeD(0);
			} else {
				writeD(pc.getId());
			}
			writeS(pc.getName() + ": " + chat);
		} else if (type == 2) { // 叫び
			writeC(opcode);
			writeC(type);
			if (pc.isInvisble()) {
				writeD(0);
			} else {
				writeD(pc.getId());
			}
			writeS("<" + pc.getName() + "> " + chat);
			writeH(pc.getX());
			writeH(pc.getY());
			// TODO 說話時加上頭銜 byttt999
		} else if (type == 3) // 全體
		{
			String clan_name = pc.getClanname();
			L1Clan clan = L1World.getInstance().getClan(clan_name);
			writeC(opcode);
			writeC(type);
			if (pc.isGm() == true) { // GM
				// TODO change GM使用公頻(&)顯示方式設定 5/5
				if (Config.GMTalkShowName) {
					writeS("[*" + pc.getName() + "*] " + chat);
				} else {
					writeS("[******] " + chat);
				}
				writeS("\\fY" + "[******] " + "\\fW" + chat);
			} else if (pc.getClanid() != 0) { // 有血盟
				if (pc.getId() == clan.getLeaderId() && clan.getCastleId() == 1) { // TODO
																					// 肯特城
					writeS("\\fR" + "肯特城主[" + pc.getName() + "] " + chat);
				} else if (pc.getId() == clan.getLeaderId()
						&& clan.getCastleId() == 2) { // TODO 妖堡
					writeS("\\fB" + "妖魔城主[" + pc.getName() + "] " + chat);
				} else if (pc.getId() == clan.getLeaderId()
						&& clan.getCastleId() == 3) { // TODO 風木
					writeS("\\fU" + "風木城主[" + pc.getName() + "] " + chat);
				} else if (pc.getId() == clan.getLeaderId()
						&& clan.getCastleId() == 4) { // TODO 奇岩
					writeS("\\fT" + "奇岩城主[" + pc.getName() + "] " + chat);
				} else if (pc.getId() == clan.getLeaderId()
						&& clan.getCastleId() == 5) { // TODO 海音
					writeS("\\fW" + "海音城主[" + pc.getName() + "] " + chat);
				} else if (pc.getId() == clan.getLeaderId()
						&& clan.getCastleId() == 6) { // TODO 侏儒
					writeS("\\fJ" + "侏儒城主[" + pc.getName() + "] " + chat);
				} else if (pc.getId() == clan.getLeaderId()
						&& clan.getCastleId() == 7) { // TODO 亞丁
					writeS("\\fG" + "亞丁城主[" + pc.getName() + "] " + chat);
				} else if (pc.getId() == clan.getLeaderId()
						&& clan.getCastleId() == 8) { // TODO 狄亞得要塞
					writeS("\\fO" + "狄亞得要塞城主[" + pc.getName() + "] " + chat);
				} else if (pc.getId() == clan.getLeaderId()
						&& clan.getCastleId() == 0) { // TODO 沒城堡,有血盟
					writeS("\\fS" + pc.getClanname() + "盟主[" + pc.getName()
							+ "] " + chat);
				} else { // TODO 血盟成員
					writeS(pc.getClanname() + "盟[" + pc.getName() + "] " + chat);
				}
			} else { // TODO 不是GM,沒有血盟
				writeS("[" + pc.getName() + "] " + chat);
			}
		} else if (type == 4) { // 血盟チャット
			writeC(opcode);
			writeC(type);
			writeS("{" + pc.getName() + "} " + chat);
		} else if (type == 9) { // ウィスパー
			writeC(opcode);
			writeC(type);
			writeS("-> (" + pc.getName() + ") " + chat);
		} else if (type == 11) { // パーティーチャット
			writeC(opcode);
			writeC(type);
			writeS("(" + pc.getName() + ") " + chat);
		} else if (type == 12) { // トレードチャット
			writeC(opcode);
			writeC(type);
			writeS("[" + pc.getName() + "] " + chat);
		} else if (type == 13) { // 連合チャット
			writeC(opcode);
			writeC(type);
			writeS("{{" + pc.getName() + "}} " + chat);
		} else if (type == 14) { // チャットパーティー
			writeC(opcode);
			writeC(type);
			if (pc.isInvisble()) {
				writeD(0);
			} else {
				writeD(pc.getId());
			}
			writeS("(" + pc.getName() + ") " + chat);
		} else if (type == 16) { // ウィスパー
			writeC(opcode);
			writeS(pc.getName());
			writeS(chat);
		}
	}

	@Override
	public byte[] getContent() {
		if (null == _byte) {
			_byte = _bao.toByteArray();
		}
		return _byte;
	}

	@Override
	public String getType() {
		return _S__1F_NORMALCHATPACK;
	}

}