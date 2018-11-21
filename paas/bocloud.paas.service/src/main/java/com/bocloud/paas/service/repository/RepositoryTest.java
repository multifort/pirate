package com.bocloud.paas.service.repository;


import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;

public class RepositoryTest {
	public static void main(String[] args) {
		String uri = "http://192.168.56.11:2375";
		DockerClient client = DefaultDockerClient.builder().uri(uri).build();
		RegistryAuth ra = RegistryAuth.builder().serverAddress("139.219.239.226").username("admin").password("123456").build();

		String test = client.getHost();
		System.out.println(test);
		try {
			client.pull("139.219.239.226/library/busybox:latest", ra);
		} catch (DockerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			client.close();
		}
	}
}
