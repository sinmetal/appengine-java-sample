package org.sinmetal.pullqueue.controller;

import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.codec.binary.*;
import org.slim3.controller.*;
import org.slim3.util.*;

import com.google.appengine.api.taskqueue.*;
import com.google.appengine.api.taskqueue.Queue;

public class LeaseController extends SimpleController {

	@Override
	protected Navigation run() throws Exception {
		final Queue q = QueueFactory.getQueue("pull-queue");

		String tag = request.getParameter("tag");
		System.out.println("tag:" + tag);

		List<TaskHandle> tasks;
		if (StringUtil.isEmpty(tag)) {
			tasks = q.leaseTasks(3600, TimeUnit.SECONDS, 100);
		} else {
			tasks = q.leaseTasksByTag(3600, TimeUnit.SECONDS, 100, tag);
		}

		for (TaskHandle task : tasks) {
			System.out.println("task name:" + task.getName());
			System.out.println("task payload:" + new String(task.getPayload()));
			String payload = new String(Base64.decodeBase64(task.getPayload()));
			System.out.println("task decode payload:" + payload);
		}
		q.deleteTask(tasks);
		return null;
	}

}
