package com.cmbc.configserver.remoting.common;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * the helper class for remote communication
 * 
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014/10/17 3:01:22PM
 */
public class RemotingHelper {
	/**
	 * IP:PORT
	 */
	public static SocketAddress string2SocketAddress(final String address) {
		String[] s = address.split(":");
		InetSocketAddress isa = new InetSocketAddress(s[0],
				Integer.valueOf(s[1]));
		return isa;
	}

	public static String parseChannelRemoteAddress(final Channel channel) {
		if (null == channel) {
			return "";
		}
		final SocketAddress remote = channel.remoteAddress();
		final String address = remote != null ? remote.toString() : "";

		if (address.length() > 0) {
			int index = address.lastIndexOf("/");
			if (index >= 0) {
				return address.substring(index + 1);
			}

			return address;
		}

		return "";
	}
	
	public static String parseChannelLocalAddress(final Channel channel){
		if (null == channel) {
			return "";
		}
		final SocketAddress local = channel.localAddress();
		final String address = local != null ? local.toString() : "";
		
		if (address.length() > 0) {
			int index = address.lastIndexOf("/");
			if (index >= 0) {
				return address.substring(index + 1);
			}

			return address;
		}

		return "";
	}
	
	public static String getChannelId(final Channel channel){
		return parseChannelRemoteAddress(channel) + "-" + parseChannelLocalAddress(channel);
	}
}