package com.bocloud.paas.service.system;
/*
 * The contents of this file are subject to the "END USER LICENSE AGREEMENT FOR F5
 * Software Development Kit for iControl"; you may not use this file except in
 * compliance with the License. The License is included in the iControl
 * Software Development Kit.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is iControl Code and related documentation
 * distributed by F5.
 *
 * The Initial Developer of the Original Code is F5 Networks,
 * Inc. Seattle, WA, USA. Portions created by F5 are Copyright (C) 1996-2004 F5
 * Inc. All Rights Reserved.  iControl (TM) is a registered trademark of F5 Netw
 *
 * Alternatively, the contents of this file may be used under the terms
 * of the GNU General Public License (the "GPL"), in which case the
 * provisions of GPL are applicable instead of those above.  If you wish
 * to allow use of your version of this file only under the terms of the
 * GPL and not to allow others to use your version of this file under the
 * License, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the GPL.
 * If you do not delete the provisions above, a recipient may use your
 * version of this file under either the License or the GPL.
 */

import java.rmi.RemoteException;

import iControl.CommonIPPortDefinition;
import iControl.CommonProtocolType;
import iControl.CommonVirtualServerDefinition;
import iControl.LocalLBAddressType;
import iControl.LocalLBLBMethod;
import iControl.LocalLBMonitorBindingStub;
import iControl.LocalLBMonitorCommonAttributes;
import iControl.LocalLBMonitorIPPort;
import iControl.LocalLBMonitorLocator;
import iControl.LocalLBMonitorMonitorTemplate;
import iControl.LocalLBMonitorRule;
import iControl.LocalLBMonitorRuleType;
import iControl.LocalLBMonitorStrPropertyType;
import iControl.LocalLBMonitorStringValue;
import iControl.LocalLBMonitorTemplateType;
import iControl.LocalLBNodeAddressBindingStub;
import iControl.LocalLBNodeAddressLocator;
import iControl.LocalLBPoolBindingStub;
import iControl.LocalLBPoolLocator;
import iControl.LocalLBPoolMemberBindingStub;
import iControl.LocalLBPoolMemberLocator;
import iControl.LocalLBPoolMonitorAssociation;
import iControl.LocalLBProfileContextType;
import iControl.LocalLBVirtualServerBindingStub;
import iControl.LocalLBVirtualServerLocator;
import iControl.LocalLBVirtualServerVirtualServerPersistence;
import iControl.LocalLBVirtualServerVirtualServerProfile;
import iControl.LocalLBVirtualServerVirtualServerResource;
import iControl.LocalLBVirtualServerVirtualServerStatistics;
import iControl.LocalLBVirtualServerVirtualServerType;
import iControl.SystemConfigSyncBindingStub;
import iControl.SystemConfigSyncLocator;
import iControl.SystemConfigSyncSyncMode;
import iControl.XTrustProvider;


public class F5OperateUtil extends Object {
	// --------------------------------------------------------------------------
	// Member Variables
	// --------------------------------------------------------------------------
	public String m_endpoint;
	public LocalLBVirtualServerBindingStub m_virtualServer;
	public LocalLBPoolBindingStub m_pool;
	public LocalLBPoolMemberBindingStub m_poolMember;
	public LocalLBMonitorBindingStub m_monitor;
	public SystemConfigSyncBindingStub m_systemConfSync;
	public LocalLBNodeAddressBindingStub m_nodeAddress;

	// --------------------------------------------------------------------------
	// Constructor
	// --------------------------------------------------------------------------
	public F5OperateUtil() {
		System.setProperty("javax.net.ssl.trustStore", System.getProperty("user.home") + "/.keystore");
		XTrustProvider.install();
	}

	// --------------------------------------------------------------------------
	// parseArgs
	// --------------------------------------------------------------------------
	public boolean login(String username, String password, String ipaddress, long port) throws Exception {
		boolean bSuccess = false;

		m_endpoint = "https://" + username + ":" + password + "@" + ipaddress + ":" + port
				+ "/iControl/iControlPortal.cgi";

		m_virtualServer =  (LocalLBVirtualServerBindingStub) new LocalLBVirtualServerLocator().getLocalLBVirtualServerPort(new java.net.URL(m_endpoint));

		m_pool = (LocalLBPoolBindingStub) new LocalLBPoolLocator()
				.getLocalLBPoolPort(new java.net.URL(m_endpoint));
		m_poolMember = (LocalLBPoolMemberBindingStub) new LocalLBPoolMemberLocator()
				.getLocalLBPoolMemberPort(new java.net.URL(m_endpoint));

		m_monitor = (LocalLBMonitorBindingStub) new LocalLBMonitorLocator()
				.getLocalLBMonitorPort(new java.net.URL(m_endpoint));

		m_systemConfSync = (SystemConfigSyncBindingStub) new SystemConfigSyncLocator()
				.getSystemConfigSyncPort(new java.net.URL(m_endpoint));
		
		m_nodeAddress = (LocalLBNodeAddressBindingStub) new LocalLBNodeAddressLocator()
				.getLocalLBNodeAddressPort(new java.net.URL(m_endpoint));
		
		bSuccess = true;

		return bSuccess;
	}

	// -------------------------------------------------------------------------
	//
	// --------------------------------------------------------------------------
	public void createVs(String name, String vip, long port, String pool_name) throws Exception {

		CommonVirtualServerDefinition definitions = new CommonVirtualServerDefinition(name, vip, port,
				CommonProtocolType.fromString(CommonProtocolType._PROTOCOL_TCP));

		LocalLBVirtualServerVirtualServerResource resources = new LocalLBVirtualServerVirtualServerResource(
				LocalLBVirtualServerVirtualServerType.RESOURCE_TYPE_POOL, pool_name);
		LocalLBVirtualServerVirtualServerProfile profiles = new LocalLBVirtualServerVirtualServerProfile(
				LocalLBProfileContextType.PROFILE_CONTEXT_TYPE_ALL, "tcp");
		m_virtualServer.create(new CommonVirtualServerDefinition[] { definitions }, new String[] { "255.255.255.255" },
				new LocalLBVirtualServerVirtualServerResource[] { resources },
				new LocalLBVirtualServerVirtualServerProfile[][] {
						new LocalLBVirtualServerVirtualServerProfile[] { profiles } });
		// set AutoMap for SNAT
		m_virtualServer.set_snat_automap(new String[] { name });

		m_virtualServer.add_profile(new String[] { name }, new LocalLBVirtualServerVirtualServerProfile[][] {
				new LocalLBVirtualServerVirtualServerProfile[] {
						new LocalLBVirtualServerVirtualServerProfile(LocalLBProfileContextType.PROFILE_CONTEXT_TYPE_ALL, "http") } });
	
		// persistence cookie
		m_virtualServer.add_persistence_profile(new String[] { name },
				new LocalLBVirtualServerVirtualServerPersistence[][] {
						new LocalLBVirtualServerVirtualServerPersistence[] {
								new LocalLBVirtualServerVirtualServerPersistence("cookie", true) } });
		m_virtualServer.set_fallback_persistence_profile(new String[] { name }, new String[] { "source_addr" });

		// System.out.println("Usage: LocalLBVSStats hostname port username
		// password [vs_address]\n");
	}

	/**
	 * 创建Pool
	 * 
	 * @param name
	 * @param members
	 *            ip端口冒号隔开，多个member用分号隔开 eg: 172.16.0.80:80;172.16.0.4:8080
	 * @param monitorName
	 *            monitor name 如不指定可传入null或空，默认使用tcp
	 * @throws Exception
	 */
	public void createPool(String name, String members, String monitorName) throws Exception {
		name = name.trim();
		if (monitorName == null || monitorName.trim().equals(""))
			monitorName = "tcp";

		String[] ipports = members.trim().split(";");
		CommonIPPortDefinition[] cid = new CommonIPPortDefinition[ipports.length];
		for (int i = 0; i < ipports.length; i++) {
			String[] ipport = ipports[i].split(":");
			String ip = ipport[0];
			long port = Long.parseLong(ipport[1]);
			cid[i] = new CommonIPPortDefinition(ip, port);
		}
		// 给POOL增加member
		CommonIPPortDefinition[][] cids = new CommonIPPortDefinition[][] { cid };

		m_pool.create(new String[] { name }, new LocalLBLBMethod[] { LocalLBLBMethod.LB_METHOD_ROUND_ROBIN }, cids);

		// set monitor
		m_pool.set_monitor_association(
				new LocalLBPoolMonitorAssociation[] { new LocalLBPoolMonitorAssociation(name, new LocalLBMonitorRule(
						LocalLBMonitorRuleType.MONITOR_RULE_TYPE_SINGLE, 1, new String[] { monitorName })) });
	}

	/**
	 * 给pool增加membere
	 * 
	 * @param poolName
	 * @param members
	 * @throws Exception
	 */
	public void addMember(String poolName, String members) throws Exception {
		poolName = poolName.trim();

		String[] ipports = members.trim().split(";");
		CommonIPPortDefinition[] cid = new CommonIPPortDefinition[ipports.length];
		for (int i = 0; i < ipports.length; i++) {
			String[] ipport = ipports[i].split(":");
			String ip = ipport[0];
			long port = Long.parseLong(ipport[1]);
			cid[i] = new CommonIPPortDefinition(ip, port);
		}
		CommonIPPortDefinition[][] cids = new CommonIPPortDefinition[][] { cid };

		// 添加member
		m_pool.add_member(new String[] { poolName }, cids);
	}

	/**
	 * 从pool中移除membere
	 * 
	 * @param poolName
	 * @param members
	 * @throws Exception
	 */
	public void removeMember(String poolName, String members) throws Exception {
		// Get list of pools
		String[] poolList = m_pool.get_list();
		boolean hasPool = false;
		for (int i = 0; i < poolList.length; i++) {
			String pools[] = poolList[i].split("/");
			if (pools[pools.length - 1].equalsIgnoreCase(poolName.trim())) {
				hasPool = true;
			}
		}
		if (hasPool == false)
			throw new Exception("pool not exists!");

		String[] ipports = members.split(";");
		CommonIPPortDefinition[] cid = new CommonIPPortDefinition[ipports.length];
		for (int i = 0; i < ipports.length; i++) {
			String[] ipport = ipports[i].split(":");
			String ip = ipport[0];
			long port = Long.parseLong(ipport[1]);
			cid[i] = new CommonIPPortDefinition(ip, port);
		}
		CommonIPPortDefinition[][] cids = new CommonIPPortDefinition[][] { cid };

		// 移除member
		m_pool.remove_member(new String[] { poolName }, cids);
	}

	/**
	 * 创建monitor，只支持http类型
	 * 
	 * @param name
	 *            monitor name
	 * @param interval
	 *            检测时间间隔，建议5s
	 * @param timeout
	 *            超时时间，建议16s，3*interval+1
	 * @param templateName
	 *            monitor模板名称，默认可使用http
	 * @param uri
	 *            检测的链接，使用相对路径即可，eg： /nesp/app/heartbeat
	 * @param receiveString
	 *            检测页面返回的特殊字符串，不建议使用html等页面常见字符串
	 * @throws Exception
	 */
	public void createMonitor(String name, long interval, long timeout, String templateName, String uri,
			String receiveString) throws Exception {
		name = name.toLowerCase().trim();
		if (templateName == null || templateName.trim().equals(""))
			templateName = "http";
		if (uri == null || uri.trim().equals(""))
			uri = "/";
		uri = "GET " + uri + " HTTP/1.0\\r\\n";
		if (receiveString == null || receiveString.trim().equals(""))
			receiveString = "html";

		LocalLBMonitorMonitorTemplate monitorTemplate = new LocalLBMonitorMonitorTemplate(name,
				LocalLBMonitorTemplateType.TTYPE_HTTP);
		LocalLBMonitorCommonAttributes mca = new LocalLBMonitorCommonAttributes();
		mca.setInterval(interval);
		mca.setTimeout(timeout);
		mca.setParent_template(templateName);
		LocalLBMonitorIPPort lip = new LocalLBMonitorIPPort();
		lip.setAddress_type(LocalLBAddressType.ATYPE_STAR_ADDRESS_STAR_PORT);
		lip.setIpport(new CommonIPPortDefinition("0.0.0.0", 0));
		mca.setDest_ipport(lip);
		m_monitor.create_template(new LocalLBMonitorMonitorTemplate[] { monitorTemplate },
				new LocalLBMonitorCommonAttributes[] { mca });
		m_monitor.set_template_string_property(new String[] { name }, new LocalLBMonitorStringValue[] {
				new LocalLBMonitorStringValue(LocalLBMonitorStrPropertyType.STYPE_RECEIVE, receiveString) });
		m_monitor.set_template_string_property(new String[] { name }, new LocalLBMonitorStringValue[] {
				new LocalLBMonitorStringValue(LocalLBMonitorStrPropertyType.STYPE_SEND, uri) });

	}

	/**
	 * 删除Virtual Server
	 * 
	 * @param name
	 *            VS名称
	 * @throws Exception
	 */
	public void deleteVs(String name) throws Exception {
		name = "/Common/" + name.trim();
		String[] vss = m_virtualServer.get_list();
		boolean existsVs = false;
		for (int i = 0; i < vss.length; i++) {
			if (vss[i].equalsIgnoreCase(name)) {
				existsVs = true;
				break;
			}
		}
		if (existsVs == false)
			throw new Exception("Virtual Server not Exists!");
		m_virtualServer.delete_virtual_server(new String[] { name });

	}

	/**
	 * 删除Pool,删除前应确保不被VS使用
	 * 
	 * @param name
	 *            pool名称
	 * @throws Exception
	 */
	public void deletePool(String name) throws Exception {
		name = name.toUpperCase().trim();
		// Get list of pools
		String[] poolList = m_pool.get_list();
		boolean existsPool = false;
		for (int i = 0; i < poolList.length; i++) {
			if (poolList[i].equalsIgnoreCase(name.trim())) {
				existsPool = true;
				break;
			}
		}
		if (existsPool == false)
			throw new Exception("pool not exists!");
		m_pool.delete_pool(new String[] { name });
	}

	/**
	 * 删除monitor，删除前应确保不被POOL使用
	 * 
	 * @param name
	 * @throws Exception
	 */
	public void deleteMonitor(String name) throws Exception {
		name = name.toLowerCase().trim();
		LocalLBMonitorMonitorTemplate[] monitorTemplateList = m_monitor.get_template_list();
		boolean existsTemplate = false;
		for (int i = 0; i < monitorTemplateList.length; i++) {
			LocalLBMonitorMonitorTemplate monitorTemplate = monitorTemplateList[i];

			if (monitorTemplate.getTemplate_name().equalsIgnoreCase(name)) {
				existsTemplate = true;
			}
		}

		if (existsTemplate == false)
			throw new Exception("monitorTemplate not exists!");
		m_monitor.delete_template(new String[] { name });

	}

	/**
	 * （待测试）同步配置，使用BASIC模式，同步bigip.conf等基本配置。此处同步的参数与已知操作不一致，实际操作时同步到对端或从对端同步到本机
	 * 
	 * @throws Exception
	 * 
	 */
	public void systemConfSync() throws Exception {
		try {
			m_systemConfSync.synchronize_configuration(SystemConfigSyncSyncMode.CONFIGSYNC_BASIC);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new Exception("Configuration Sync failed!");
		}
	}

	// --------------------------------------------------------------------------
	// getAllVSInfo
	// --------------------------------------------------------------------------
	public void getAllVSInfo() throws Exception {
		getVSInfo(m_virtualServer.get_list());
	}

	// --------------------------------------------------------------------------
	// getVSInfo
	// --------------------------------------------------------------------------
	public void getVSInfo(String[] vs_list) throws Exception {
		LocalLBVirtualServerVirtualServerStatistics vs_stats = m_virtualServer
				.get_statistics(vs_list);

		for (int i = 0; i < vs_stats.getStatistics().length; i++) {
			CommonVirtualServerDefinition vs_def = vs_stats.getStatistics()[i]
					.getVirtual_server();

			System.out.println("Virtual Server : " + vs_def.getName() + " ----" + vs_def.getAddress() + ":"
					+ vs_def.getPort() + " Protocol:" + vs_def.getProtocol());

		}
	}

	public void cascaDeleteVs(String vsName) throws Exception {
		String pools[] = m_virtualServer.get_default_pool_name(new String[] { vsName });
		CommonIPPortDefinition[][] ipPortDefinitions = m_pool.get_member(pools);
		m_pool.remove_member(pools, ipPortDefinitions);
		m_virtualServer.delete_virtual_server(new String[] { vsName });
		m_pool.delete_pool(pools);
//		m_nodeAddress.delete_all_node_addresses();
		for(int i=0;i<ipPortDefinitions.length;i++){
			String nodeAddress[]=new String[ipPortDefinitions[i].length];
			for(int j=0;j<nodeAddress.length;j++){
				nodeAddress[j]=ipPortDefinitions[i][j].getAddress();
			}
			m_nodeAddress.delete_node_address(nodeAddress);
		}
		
		
	}

	public void clearPoolMembers(String poolName) throws Exception {
		CommonIPPortDefinition[][] ipPortDefinitions = m_pool.get_member(new String[] { poolName });
		m_pool.remove_member(new String[] { poolName }, ipPortDefinitions);
	}

	public boolean isPoolExisted(String poolName) throws RemoteException {
		// Get list of pools
		poolName = "/Common/" + poolName;
		String[] poolList = m_pool.get_list();
		for (int i = 0; i < poolList.length; i++) {
			if (poolList[i].equalsIgnoreCase(poolName.trim())) {
				return true;
			}
		}
		return false;
	}

	// 判断vs名称、ip、port是否存在
	public boolean isVsExisted(String vsName) throws RemoteException {
		// Get list of pools
		vsName = "/Common/" + vsName;
		String[] vss = m_virtualServer.get_list();
		for (int i = 0; i < vss.length; i++) {
			if (vss[i].equals(vsName)) {
				return true;
			}
		}
		// 检测VIP port,避免已使用，避免使用f5的IP及端口
		// CommonIPPortDefinition[] cipds =
		// m_virtualServer.get_destination(vss);
		// for (int i = 0; i < cipds.length; i++) {
		// CommonIPPortDefinition cipd = cipds[i];
		// if (cipd.getAddress().equals(vsIp) && cipd.getPort() == vsPort)
		// return true;
		// }

		return false;
	}
	

	public static void main(String[] args) throws Exception {
		F5OperateUtil localLBBean = new F5OperateUtil();
		localLBBean.login("admin", "admin", "192.168.1.146", 443);
		
	}

};
