package com.github.unchama.seichiassist.minestack;

/**
 * @author karayuu
 */
@Deprecated
public class HistoryData {
	public MineStackObj obj;

	HistoryData(MineStackObj obj) {
		this.obj = obj;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof HistoryData)) {
			return false;
		}

		HistoryData data = (HistoryData) object;
		return this.obj.getMineStackObjName().equals(data.obj.getMineStackObjName());
	}
}
