package com.bocloud.paas.service.application.model;

/**
 * describe: 容器端口封装对象
 * @author Zaney
 * @data 2017年10月26日
 */
public class Port {
	
	private String targetPort;
	private String port;
	private String protocol;
	private String portType;
	private String nodePort;
	
	public Port(String targetPort, String port, String protocol, String portType, String nodePort) {
		super();
		this.targetPort = targetPort;
		this.port = port;
		this.protocol = protocol;
		this.portType = portType;
		this.nodePort = nodePort;
	}
	
	public Port(String targetPort, String port, String protocol) {
		super();
		this.targetPort = targetPort;
		this.port = port;
		this.protocol = protocol;
	}



	public Port() {
		super();
	}
	
	public String getTargetPort() {
		return targetPort;
	}
	public void setTargetPort(String targetPort) {
		this.targetPort = targetPort;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getPortType() {
		return portType;
	}
	public void setPortType(String portType) {
		this.portType = portType;
	}
	public String getNodePort() {
		return nodePort;
	}
	public void setNodePort(String nodePort) {
		this.nodePort = nodePort;
	}
	
}
