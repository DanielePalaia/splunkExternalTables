package com.example.splunk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import java.io.*;
import java.util.*;



@SpringBootApplication
public class SplunkApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(SplunkApplication.class, args);
	}

	@Override
	public void run(String[] args) throws Exception {
		// Loading ini file
		FileInputStream in;
		Properties SegmentProperties = new Properties();

		try {
			in = new FileInputStream("/home/gpadmin/segment.properties");
			SegmentProperties.load(in);
		}
		catch (Exception e)  {
			e.printStackTrace();
		}


		SplunkEngine mysplunk = new SplunkEngine(args, "search", SegmentProperties.getProperty("earliest"), SegmentProperties.getProperty("latest"));
		mysplunk.connect();
		mysplunk.searchEvents();
	}
}
