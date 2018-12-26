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
package l1j.server.server.model.shop;

import java.util.ArrayList;
import java.util.List;

import l1j.server.Config;
import l1j.server.server.model.Instance.L1PcInstance;

class L1ShopSellOrder {
	private final L1AssessedItem _item;
	private final int _count;

	public L1ShopSellOrder(L1AssessedItem item, int count) {
		_item = item;
		_count = count;
	}

	public L1AssessedItem getItem() {
		return _item;
	}

	public int getCount() {
		return _count;
	}

}

public class L1ShopSellOrderList {
	private final L1Shop _shop;
	private final L1PcInstance _pc;
	private final List<L1ShopSellOrder> _list = new ArrayList<L1ShopSellOrder>();

	L1ShopSellOrderList(L1Shop shop, L1PcInstance pc) {
		_shop = shop;
		_pc = pc;
	}

	public void add(int itemObjectId, int count) {
		// TODO 新所有物品販賣 by eric1300460
		L1AssessedItem assessedItem;
		if (Config.AllSell) {
			assessedItem = _shop.allAssessItem(_pc.getInventory().getItem(
					itemObjectId));
		} else {
			assessedItem = _shop.assessItem(_pc.getInventory().getItem(
					itemObjectId));
		}
		// TODO 新所有物品販賣 by eric1300460
		if (assessedItem == null) {
			/*
			 * 買取リストに無いアイテムが指定された。 不正パケの可能性。
			 */
			throw new IllegalArgumentException();
		}

		// TODO 修正新所有東西販賣
		if (assessedItem.getAssessedPrice() > 0) {
			_list.add(new L1ShopSellOrder(assessedItem, count));
		}
		// TODO 修正新所有東西販賣
	}

	L1PcInstance getPc() {
		return _pc;
	}

	List<L1ShopSellOrder> getList() {
		return _list;
	}
}
