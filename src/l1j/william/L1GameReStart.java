package l1j.william;

//  elfooxx 重啟系統

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import l1j.server.server.GameServer;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;

//  elfooxx 重啟系統
/*
 *
 * @author Administrator
 */
public class L1GameReStart {
	public int _remnant; /* elfooxx */
	private static L1GameReStart _instance; /* elfooxx */

	private L1GameReStart() {
		GeneralThreadPool.getInstance().execute(new ReStart());
	}

	public class ReStart implements Runnable /* elfooxx */{
		@Override
		public void run() {
			while (true) {
				int remnant = GetRestartTime() * 60;
				System.out.println("正在載入自動重開 設定...完成! " + GetRestartTime()
						+ "分鐘後");
				while (remnant > 0) {
					for (int i = remnant; i >= 0; i--) {
						SetRemnant(i);
						if (i % 60 == 0 && i <= 300 && i != 0) {
							BroadCastToAll("伺服器將於 " + i / 60
									+ " 分鐘後重新啟動，請至安全區域準備登出。");
							System.out.println("伺服器將於 " + i / 60 + " 分鐘後重新啟動");
						} // if (五分鐘內)
						else if (i <= 35 && i != 0) {
							BroadCastToAll("伺服器將於 " + i + "秒後重新啟動，請至安全區域準備登出。");
							System.out.println("伺服器將於 " + i + " 秒後重新啟動");
						} // if (35秒內)
						else if (i == 0) {
							BroadCastToAll("伺服器重新啟動。");
							System.out.println("伺服器重新啟動。");
							GameServer.getInstance().shutdown(); //TODO 修正自動重開角色資料會回溯
						} // if 1秒
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						} // try and catch
					} // for
				} // while ( remnant > 0 )
			} // while true
		} // run()
	} // class ReStart /*elfooxx*/

	private int GetRestartTime() {
		Properties properties = new Properties();
		InputStream input = getClass().getResourceAsStream(
				"/config/altsettings.properties");

		try {
			properties.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		} // try catch
		return Integer.parseInt(properties.getProperty("RestartTime"));
	} // GetRestartTime()

	private void BroadCastToAll(String string) {
		Collection<L1PcInstance> allpc = L1World.getInstance().getAllPlayers();
		for (L1PcInstance pc : allpc) {
			pc.sendPackets(new S_SystemMessage(string));
		}
	} // BroadcastToAll()

	public void SetRemnant(int remnant) {
		_remnant = remnant;
	}

	public int GetRemnant() {
		return _remnant;
	}

	public static void init() {
		_instance = new L1GameReStart();
	}

	public static L1GameReStart getInstance() {
		return _instance;
	}

} // class L1GameReStart() /*elfooxx 重啟系統*/
