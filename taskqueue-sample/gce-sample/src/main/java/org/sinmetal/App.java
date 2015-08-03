package org.sinmetal;

import java.io.*;
import java.security.*;
import java.util.*;

import com.google.api.client.googleapis.compute.*;
import com.google.api.client.googleapis.javanet.*;
import com.google.api.client.http.javanet.*;
import com.google.api.client.json.*;
import com.google.api.client.json.jackson2.*;
import com.google.api.client.util.*;
import com.google.api.services.taskqueue.*;
import com.google.api.services.taskqueue.model.*;
import com.google.api.services.taskqueue.model.TaskQueue;

/**
 * Hello world!
 */
public class App {

	static String PROJECT_NAME = "s~cp300demo1";

	static String QUEUE_NAME = "pull-queue";

	static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	enum Operation {
		INSERT, LEASE;
	}

	public static void main(String[] args) {
		System.out.println("Hello World!");

		Operation operation = Operation.valueOf(args[0]);

		if (args.length > 1 && args[1] != null) {
			PROJECT_NAME = args[1];
		}
		if (args.length > 2 && args[2] != null) {
			QUEUE_NAME = args[2];
		}

		System.out.println("Project Name:" + PROJECT_NAME);
		System.out.println("Queue Name:" + QUEUE_NAME);

		try {
			if (operation == Operation.INSERT) {
				insert();
			} else if (operation == Operation.LEASE) {
				lease();
			} else {
				System.out.println("no support operation.");
				System.exit(1);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	static void insert() throws GeneralSecurityException, IOException {
		NetHttpTransport httpTransport = GoogleNetHttpTransport
				.newTrustedTransport();

		ComputeCredential credential = new ComputeCredential.Builder(
				httpTransport, JSON_FACTORY).build();

		Taskqueue taskqueue = new Taskqueue.Builder(httpTransport,
				JSON_FACTORY, credential).build();

		TaskQueue queue = getQueue(taskqueue);
		System.out.println(queue);

		Task task = new Task();
		task.setQueueName("");
		task.setPayloadBase64(Base64.encodeBase64String("hoge".getBytes()));
		task.setTag("work");

		Task response = taskqueue.tasks()
				.insert(PROJECT_NAME, QUEUE_NAME, task).execute();
		System.out.println("add task:" + response.getId());
	}

	static void lease() throws GeneralSecurityException, IOException {
		NetHttpTransport httpTransport = GoogleNetHttpTransport
				.newTrustedTransport();

		ComputeCredential credential = new ComputeCredential.Builder(
				httpTransport, JSON_FACTORY).build();

		final Taskqueue taskqueue = new Taskqueue.Builder(httpTransport,
				JSON_FACTORY, credential).build();

		TaskQueue queue = getQueue(taskqueue);
		System.out.println(queue);

		Random random = new Random();

		while (true) {
			Tasks leaseTasks = taskqueue.tasks()
					.lease(PROJECT_NAME, QUEUE_NAME, 3, 60 * 3).execute();
			List<Task> tasks = leaseTasks.getItems();
			if (tasks == null) {
				System.out.println("task nothing.");
				try {
					Thread.sleep(3000 + 1000 * random.nextInt(10));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			for (final Task task : tasks) {
				byte[] decodeBase64 = Base64.decodeBase64(task
						.getPayloadBase64());
				String payload = new String(decodeBase64);
				payload = payload.replaceAll("\n", "");
				System.out.println("{\"__SAMPLE__\":" + payload + "}");

				// task processing

				retry(new Runnable() {

					@Override
					public void run() {
						try {
							taskqueue
									.tasks()
									.delete(PROJECT_NAME, QUEUE_NAME,
											task.getId()).execute();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						System.out.println(task.getId() + " done.");
					}
				}, 10);
			}
		}
	}

	static TaskQueue getQueue(Taskqueue taskqueue) throws IOException {
		Taskqueue.Taskqueues.Get request = taskqueue.taskqueues().get(
				PROJECT_NAME, QUEUE_NAME);
		request.setGetStats(true);
		return request.execute();
	}

	/**
	 * 例外が発生する限り、指定された回数までリトライをする
	 *
	 * @param process
	 *            実行するプロセス
	 * @param tryCount
	 *            実行回数 (=0 の場合は1)
	 * @throws IllegalStateException
	 *             リトライ回数に達した後に発生した例外
	 */
	static void retry(Runnable process, int tryCount)
			throws IllegalStateException {
		if (process == null) {
			return;
		}
		tryCount = tryCount > 0 ? tryCount : 1;

		for (int i = 0; i < tryCount; i++) {
			try {
				process.run();
				break;

			} catch (Throwable e) {
				if (i >= tryCount - 1) {
					throw new IllegalStateException(e);
				}
				try {
					Random random = new Random();
					Thread.sleep(100 + random.nextInt(30) * 100);
				} catch (InterruptedException e1) {
				}
			}
		}
	}
}
