package com.bocloud.paas.common.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressConUtil {

	private static Logger logger = LoggerFactory.getLogger(AddressConUtil.class);

	public boolean connect(String server, int servPort) {
		Socket socket = null;
		try {
			InetAddress address = InetAddress.getByName(server);
			boolean con = address.isReachable(3000);
			if (!con) {
				logger.info("IP无法连接");
				return false;
			}

			socket = new Socket();
			socket.setReceiveBufferSize(8192);
			socket.setSoTimeout(3000);//
			SocketAddress socketAddress = new InetSocketAddress(server, servPort);
			socket.connect(socketAddress, 5000);
			if (null != socket) {
				socket.close();
			}
			return true;
		} catch (Exception e) {
			logger.error("端口异常，无法连接");
			return false;
		}

	}
}
