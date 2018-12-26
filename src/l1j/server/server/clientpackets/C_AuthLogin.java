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
import l1j.server.server.Account;
import l1j.server.server.AccountAlreadyLoginException;
import l1j.server.server.ClientThread;
import l1j.server.server.GameServerFullException;
import l1j.server.server.LoginController;
import l1j.server.server.serverpackets.S_CommonNews;
import l1j.server.server.serverpackets.S_LoginResult;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_AuthLogin extends ClientBasePacket {

	private static final String C_AUTH_LOGIN = "[C] C_AuthLogin";
	private static Logger _log = Logger.getLogger(C_AuthLogin.class.getName());

	public C_AuthLogin(byte[] decrypt, ClientThread client) {
		super(decrypt);
		String accountName = readS().toLowerCase();
		String password = readS();

		String ip = client.getIp();
		String host = client.getHostname();

		_log.finest("Request AuthLogin from user : " + accountName);

		if (!Config.ALLOW_2PC) {
			for (ClientThread tempClient : LoginController.getInstance()
					.getAllAccounts()) {
				if (ip.equalsIgnoreCase(tempClient.getIp())) {
					_log.info("拒絕了2PC的進入。玩家帳號：" + accountName + " 玩家IP：" + host);
					client.sendPacket(new S_LoginResult(
							S_LoginResult.REASON_USER_OR_PASS_WRONG));
					return;
				}
			}
		}

		Account account = Account.load(accountName);
		if (account == null) {
			if (Config.AUTO_CREATE_ACCOUNTS) {
				account = Account.create(accountName, password, ip, host);
			} else {
				_log.warning("account missing for user " + accountName);
			}
		}
		if (account == null || !account.validatePassword(password)) {
			client.sendPacket(new S_LoginResult(
					S_LoginResult.REASON_USER_OR_PASS_WRONG));
			return;
		}
		if (account.isBanned()) { // BANアカウント
			_log.info("拒絕了被封鎖的帳號進入。玩家帳號：" + accountName + " 玩家IP：" + host);
			client.sendPacket(new S_LoginResult(
					S_LoginResult.REASON_USER_OR_PASS_WRONG));
			return;
		}

		try {
			LoginController.getInstance().login(client, account);
			// TODO 加入登入時更新IP位置，參考 awen0227@99nets 的修改紀錄
			Account.updateLastActive(account, ip); // 最終ログイン日を更新する
			client.setAccount(account);
			client.sendPacket(new S_LoginResult(S_LoginResult.REASON_LOGIN_OK));
			client.sendPacket(new S_CommonNews());
		} catch (GameServerFullException e) {
			client.kick();
			_log.info("連線人數已超過(" + client.getHostname() + ")人，拒絕進入並中斷連線。");
			return;
		} catch (AccountAlreadyLoginException e) {
			client.kick();
			_log.info("同樣的ID因為重複連線(" + client.getHostname() + ")強制中斷連線。");
			return;
		}
	}

	@Override
	public String getType() {
		return C_AUTH_LOGIN;
	}

}