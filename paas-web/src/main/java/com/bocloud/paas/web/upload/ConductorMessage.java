package com.bocloud.paas.web.upload;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ConductorMessage {
	
	private JSONArray tasks = new JSONArray();
	private String context;
	private JSONObject WFjson;

	public ConductorMessage() {
		super();
	}

	public ConductorMessage(String context, JSONArray tasks, JSONObject wFjson) {
		super();
		this.tasks = tasks;
		this.context = context;
		this.WFjson = wFjson;
	}

	public JSONObject getWFjson() {
		return WFjson;
	}

	public void setWFjson(JSONObject wFjson) {
		WFjson = wFjson;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public JSONArray getTasks() {
		return tasks;
	}

	public void setTasks(JSONArray tasks) {
		this.tasks = tasks;
	}

}
