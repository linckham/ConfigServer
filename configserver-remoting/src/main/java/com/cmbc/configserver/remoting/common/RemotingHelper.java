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
	public static String exceptionSimpleDesc(final Throwable e) {
		StringBuffer sb = new StringBuffer();
		if (e != null) {
			sb.append(e.toString());

			StackTraceElement[] stackTrace = e.getStackTrace();
			if (stackTrace != null && stackTrace.length > 0) {
				StackTraceElement elment = stackTrace[0];
				sb.append(", ");
				sb.append(elment.toString());
			}
		}

		return sb.toString();
	}

	/**
	 * IP:PORT
	 */
	public static SocketAddress string2SocketAddress(final String addr) {
		String[] s = addr.split(":");
		InetSocketAddress isa = new InetSocketAddress(s[0],
				Integer.valueOf(s[1]));
		return isa;
	}

	public static String parseChannelRemoteAddr(final Channel channel) {
		if (null == channel) {
			return "";
		}
		final SocketAddress remote = channel.remoteAddress();
		final String addr = remote != null ? remote.toString() : "";

		if (addr.length() > 0) {
			int index = addr.lastIndexOf("/");
			if (index >= 0) {
				return addr.substring(index + 1);
			}

			return addr;
		}

		return "";
	}
	
	public static String parseChannelLocalAddr(final Channel channel){
		if (null == channel) {
			return "";
		}
		final SocketAddress local = channel.localAddress();
		final String addr = local != null ? local.toString() : "";
		
		if (addr.length() > 0) {
			int index = addr.lastIndexOf("/");
			if (index >= 0) {
				return addr.substring(index + 1);
			}

			return addr;
		}

		return "";
	}
	
	public static String getChannelId(final Channel channel){
		return parseChannelRemoteAddr(channel) + "-" + parseChannelLocalAddr(channel);
	}

	public static String parseChannelRemoteName(final Channel channel) {
		if (null == channel) {
			return "";
		}
		final InetSocketAddress remote = (InetSocketAddress) channel
				.remoteAddress();
		if (remote != null) {
			return remote.getAddress().getHostName();
		}
		return "";
	}

	public static String parseSocketAddressAddr(SocketAddress socketAddress) {
		if (socketAddress != null) {
			final String addr = socketAddress.toString();

			if (addr.length() > 0) {
				return addr.substring(1);
			}
		}
		return "";
	}

	public static String parseSocketAddressName(SocketAddress socketAddress) {

		final InetSocketAddress addrs = (InetSocketAddress) socketAddress;
		if (addrs != null) {
			return addrs.getAddress().getHostName();
		}
		return "";
	}

}