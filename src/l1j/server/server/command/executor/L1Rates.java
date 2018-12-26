package l1j.server.server.command.executor;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import l1j.server.Config;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1Rates implements L1CommandExecutor {
	private static Logger _log = Logger.getLogger(L1Rates.class.getName());

	private L1Rates() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1Rates();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		String changed = "Error";
		double data = 0;
		try {
			StringTokenizer stringtokenizer = new StringTokenizer(arg);
			String param = stringtokenizer.nextToken();
			if (param.equalsIgnoreCase("RESET")) {
				Config.reset();
				changed = "RESET";
			} else {
				String value = stringtokenizer.nextToken();
				data = Double.parseDouble(value);
				if (param.equalsIgnoreCase("XP")) {
					Config.setParameterValue("RateXp", ""
							+ (Config.RATE_XP_ORG * data));
					changed = "XP";
				} else if (param.equalsIgnoreCase("ADENA")) {
					Config.setParameterValue("RateDropAdena", ""
							+ (Config.RATE_DROP_ADENA_ORG * data));
					changed = "ADENA";
				} else if (param.equalsIgnoreCase("DROP")) {
					Config.setParameterValue("RateDropItems", ""
							+ (Config.RATE_DROP_ITEMS_ORG * data));
					changed = "DROP";
				} else if (param.equalsIgnoreCase("LAW")) {
					Config.setParameterValue("RateLawful", ""
							+ (Config.RATE_LA_ORG * data));
					changed = "LAW";
				} else if (param.equalsIgnoreCase("KARMA")) {
					Config.setParameterValue(
							"RateKarma",
							""
									+ (Integer.valueOf(value) * Config.RATE_KARMA_ORG));
					changed = "KARMA";
				} else if (param.equalsIgnoreCase("WEIGHT")) {
					int rate = (int) (data * Config.RATE_WEIGHT_LIMIT_ORG);
					Config.setParameterValue("Weightrate", "" + rate);
					changed = "WEIGHT";
				} else if (param.equalsIgnoreCase("ALL")) {
					Config.setParameterValue("RateXp", ""
							+ (Config.RATE_XP_ORG * data));
					Config.setParameterValue("RateDropAdena", ""
							+ (Config.RATE_DROP_ADENA_ORG * data));
					Config.setParameterValue("RateDropItems", ""
							+ (Config.RATE_DROP_ITEMS_ORG * data));
					Config.setParameterValue("RateLawful", ""
							+ (Config.RATE_LA_ORG * data));
					Config.setParameterValue("RateKarma", ""
							+ (Config.RATE_KARMA_ORG * data));
					int rate = (int) (data * Config.RATE_WEIGHT_LIMIT_ORG);
					Config.setParameterValue("Weightrate", "" + rate);
					changed = "ALL";
				} else {
					pc.sendPackets(new S_SystemMessage(
							"請輸入 .rates [XP,ADENA,DROP,LAW,KARMA,WEIGHT,ALL] [數值,RESET]"));
					pc.sendPackets(new S_SystemMessage("經驗值: "
							+ (int) Config.RATE_XP)
							+ " 倍");
					pc.sendPackets(new S_SystemMessage("金幣掉落: "
							+ (int) Config.RATE_DROP_ADENA)
							+ " 倍");
					pc.sendPackets(new S_SystemMessage("物品掉落: "
							+ (int) Config.RATE_DROP_ITEMS)
							+ " 倍");
					pc.sendPackets(new S_SystemMessage("正義值: "
							+ (int) Config.RATE_LA)
							+ " 倍");
					pc.sendPackets(new S_SystemMessage("友好度: "
							+ (int) Config.RATE_KARMA)
							+ " 倍");
					pc.sendPackets(new S_SystemMessage("負重率: "
							+ (int) Config.RATE_WEIGHT_LIMIT)
							+ " 倍");
				}
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(
					"請輸入 .rates [XP,ADENA,DROP,LAW,KARMA,WEIGHT,ALL] [數值,RESET]"));
			pc.sendPackets(new S_SystemMessage("經驗值: " + (int) Config.RATE_XP)
					+ " 倍");
			pc.sendPackets(new S_SystemMessage("金幣掉落: "
					+ (int) Config.RATE_DROP_ADENA)
					+ " 倍");
			pc.sendPackets(new S_SystemMessage("物品掉落: "
					+ (int) Config.RATE_DROP_ITEMS)
					+ " 倍");
			pc.sendPackets(new S_SystemMessage("正義值: " + (int) Config.RATE_LA)
					+ " 倍");
			pc.sendPackets(new S_SystemMessage("友好度: "
					+ (int) Config.RATE_KARMA)
					+ " 倍");
			pc.sendPackets(new S_SystemMessage("負重率: "
					+ (int) Config.RATE_WEIGHT_LIMIT)
					+ " 倍");
		}
		if (!changed.equals("Error")) {
			if (changed.equals("RESET")) {
				broadcastToAll("遊戲管理員將" + changed + "的倍率重新設定為 1.0 倍");
			} else {
				broadcastToAll("遊戲管理員將" + changed + "的倍率更改為 " + data + " 倍");
			}
		}
	}

	private void broadcastToAll(String s) {
		L1World.getInstance().broadcastPacketToAll(new S_SystemMessage(s));
	}
}
