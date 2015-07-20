package org.sinmetal;

import java.io.*;
import java.security.*;

import com.google.api.client.googleapis.compute.*;
import com.google.api.client.googleapis.javanet.*;
import com.google.api.client.http.javanet.*;
import com.google.api.client.json.*;
import com.google.api.client.json.jackson2.*;
import com.google.api.client.util.*;
import com.google.api.services.taskqueue.*;
import com.google.api.services.taskqueue.model.*;

/**
 * Hello world!
 *
 */
public class App {

	static String PROJECT_NAME = "s~cp300demo1";

	static String QUEUE_NAME = "sample";

	static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	public static void main(String[] args) {
		System.out.println("Hello World!");

		if (args.length > 0 && args[0] != null) {
			PROJECT_NAME = args[0];
		}
		if (args.length > 1 && args[1] != null) {
			QUEUE_NAME = args[1];
		}

		System.out.println("Project Name:" + PROJECT_NAME);
		System.out.println("Queue Name:" + QUEUE_NAME);

		try {
			run();
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	static void run() throws GeneralSecurityException, IOException {
		NetHttpTransport httpTransport = GoogleNetHttpTransport
				.newTrustedTransport();

		ComputeCredential credential = new ComputeCredential.Builder(
				httpTransport, JSON_FACTORY).build();

		Taskqueue taskqueue = new Taskqueue.Builder(httpTransport,
				JSON_FACTORY, credential).build();

		com.google.api.services.taskqueue.model.TaskQueue queue = getQueue(taskqueue);
		System.out.println(queue);

		Task task = new Task();
		task.setQueueName("");
		task.setPayloadBase64(Base64.encodeBase64String("hoge".getBytes()));
		task.setTag("work");

		Task response = taskqueue.tasks()
				.insert(PROJECT_NAME, QUEUE_NAME, task).execute();
		System.out.println("add task:" + response.getId());
	}

	static TaskQueue getQueue(Taskqueue taskqueue) throws IOException {
		Taskqueue.Taskqueues.Get request = taskqueue.taskqueues().get(
				PROJECT_NAME, QUEUE_NAME);
		request.setGetStats(true);
		return request.execute();
	}
}
