package org.samples.cicd.restapp;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Displays the application's meta information like version, hostname, etc.
 * 
 * @author Ricardo Zanini
 * 
 */
@RestController
public class MetainfoController {

	@Value("${app.version}")
	private String applicationVersion;

	@GetMapping("/info")
	Metainfo info() {
		final Metainfo metainfo = new Metainfo();
		metainfo.setVersion(applicationVersion);
		metainfo.setUsername(System.getProperty("user.name"));
		try {
			metainfo.setPodName(
					java.net.InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			metainfo.setPodName(String.format("Unknow: %s", e.getMessage()));
		}
		return metainfo;
	}

}
