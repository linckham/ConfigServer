package com.cmbc.configserver.domain;

/**
 * the meta information of the heart beat between client and server.
 * 
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014年10月17日 下午4:42:51
 */
public class HeartBeatData {
	/**
	 * the last heart beat timestamp
	 */
	private long lastHeartBeatTimeStamp;
	/**
	 * the client's address. for example 127.0.0.1:8882
	 */
	private String clientAddress;
	/**
	 * the client's group
	 */
	private String group;

	public long getLastHeartBeatTimeStamp() {
		return lastHeartBeatTimeStamp;
	}

	public void setLastHeartBeatTimeStamp(long lastHeartBeatTimeStamp) {
		this.lastHeartBeatTimeStamp = lastHeartBeatTimeStamp;
	}

	public String getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(String clientAddress) {
		this.clientAddress = clientAddress;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clientAddress == null) ? 0 : clientAddress.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime
				* result
				+ (int) (lastHeartBeatTimeStamp ^ (lastHeartBeatTimeStamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HeartBeatData other = (HeartBeatData) obj;
		if (clientAddress == null) {
			if (other.clientAddress != null)
				return false;
		} else if (!clientAddress.equals(other.clientAddress))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (lastHeartBeatTimeStamp != other.lastHeartBeatTimeStamp)
			return false;
		return true;
	}

}
