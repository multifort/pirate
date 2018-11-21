package com.bocloud.paas.common.util;

import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.ssh.Command;
import com.bocloud.common.ssh.SSH;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class SSHUtil {

	private final static Logger logger = LoggerFactory.getLogger(SSH.class);

	private final static int DEFAULT_PORT = 22;

	/**
	 * The IP address of the agent
	 */
	private String address;

	/**
	 * The user name of the agent
	 */
	private String username;

	/**
	 * The password of the agent, corresponds to the user-name
	 */
	private String password;

	/**
	 * The port
	 */
	private int port;
	/**
	 * SSH connection between the console and agent
	 */
	private Connection conn;

	private Session session;

	public SSHUtil(String hostname, String username, String password) {
		this(hostname, username, password, DEFAULT_PORT);
	}

	public SSHUtil(String address, String username, String password, int port) {
		this.address = address;
		this.username = username;
		this.password = password;
		this.port = port;
	}

	public boolean connect() throws IOException, SocketException {
		boolean isAuthenticated = false;
		conn = new Connection(address, port);
		conn.connect();
		isAuthenticated = conn.authenticateWithPassword(username, password);
		if (isAuthenticated) {
			session = conn.openSession();
		}
		return isAuthenticated;
	}

	public void close() {
		if (session != null) {
			session.close();
		}
		if (conn != null) {
			conn.close();
		}
	}

	public void closeSession() {
		if (session != null) {
			session.close();
		}
	}

	public void openSession() throws IOException {
		if (null != conn) {
			session = conn.openSession();
		}
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}

	/**
	 * 直到指令全部执行完，方能返回结果。 如果执行大文件拷贝指令，则需要等待很久
	 * 
	 * @param command
	 * @return
	 * @throws Exception
	 */
	public boolean execute(String command) throws Exception {
		logger.debug(command);
		return execute(command, 0);
	}

	/**
	 * 如果timeout，则返回false
	 * 
	 * @param commandLine
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public boolean execute(String command, long timeout) throws Exception {
		logger.debug(command);
		if (session == null) {
			logger.error("Session失效！");
			return false;
		}
		if (StringUtils.isEmpty(command)) {
			logger.error("命令行为空！");
			return false;
		}
		session.execCommand(command);
		session.waitForCondition(ChannelCondition.EXIT_STATUS, timeout);
		// 当超时发生时，session.getExitStatus()为null
		if (null == session.getExitStatus()) {
			return false;
		}
		return 0 == session.getExitStatus();
	}

	/**
	 * 直到指令全部执行完，方能返回结果。 如果执行大文件拷贝指令，则需要等待很久
	 * 
	 * @param commandLine
	 * @return
	 * @throws Exception
	 */
	public String executeWithResult(String command) throws Exception {
		logger.debug(command);
		return executeWithResult(command, 0);
	}

	/**
	 * 如果timeout，则返回""
	 * 
	 * @param commandLine
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public String executeWithResult(String command, long timeout) throws Exception {
		logger.debug(command);
		StringBuffer result = new StringBuffer();
		if (session == null) {
			logger.error("Session失效！");
			return null;
		}
		if (StringUtils.isEmpty(command)) {
			logger.error("命令行为空！");
			return null;
		}
		session.execCommand(command);
		int condition = session.waitForCondition(ChannelCondition.EXIT_STATUS, timeout);
		if (timeout(condition)) {
			logger.error("Execute command [{}] is timeout", command);
		} else {
			StreamGobbler is = new StreamGobbler(session.getStderr());
			result.append(IOUtils.toString(new StreamGobbler(session.getStdout()))).append(IOUtils.toString(is));
		}
		return result.toString();
	}

	private boolean timeout(int condition) {
		return ((condition & ChannelCondition.TIMEOUT) == 1) ? true : false;
	}

	public boolean SCPFile(String source, String target) {
		SCPClient cp = new SCPClient(conn);
		try {
			logger.debug("source is [{}];target is [{}]", source, target);
			cp.put(source, target);
			return true;
		} catch (Exception e) {
			logger.error("SCP File exception:", e);
			return false;
		}
	}

	public boolean rmFile(String file) {
		try {
			SFTPv3Client sftpClient = new SFTPv3Client(conn);
			sftpClient.rm(file);
			return true;
		} catch (IOException e) {
			logger.error("RM File [{}] exception:", file, e);
			return false;
		}
	}

	public boolean getFile(String remote, String local) {
		SCPClient sc = new SCPClient(conn);
		try {
			sc.get(remote, this.getClass().getResource("/").getPath());
			return true;
		} catch (IOException e) {
			logger.error("Get File[{}] exception:", remote, e);
			return false;
		}
	}

	public boolean mkdir(String directory) {
		try {
			this.execute(Command.MAKEDIR + directory);
			return true;
		} catch (Exception e) {
			logger.error("Mkdir [{}] exception:", directory, e);
			return false;
		}
	}

	public boolean mv(String source, String target) {
		try {
			this.execute(Command.MOVE + source + " " + target);
			return true;
		} catch (Exception e) {
			logger.error("Move [{}] to [{}] exception:", source, target, e);
			return false;
		}
	}

	public boolean copy(String source, String target) {
		try {
			return execute(Command.COPY + source + " " + target);
		} catch (Exception e) {
			logger.error("Copy [{}] to [{}] exception:", source, target, e);
			return false;
		}
	}
}
