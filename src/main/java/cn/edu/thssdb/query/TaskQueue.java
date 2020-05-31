package cn.edu.thssdb.query;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BooleanSupplier;

/**
 * The singleton task queue manager and executor
 */
public class TaskQueue {

    private static TaskQueue instance = null;
    private ConcurrentLinkedQueue<BooleanSupplier> queue;

    public static TaskQueue get_task_queue() {
        if (instance == null) {
            instance = new TaskQueue();
        }
        return instance;
    }

    private TaskQueue() {
        queue = new ConcurrentLinkedQueue<>();
    }

    // if ready, carry out the task in the current thread
    // else, put it aside, until flush called
    public void add_task(BooleanSupplier task) {
        if (!task.getAsBoolean()) {
            queue.add(task);
        }
    }

    private void clear() {
        while (!queue.isEmpty()) {
            BooleanSupplier task = queue.poll();
            if (!task.getAsBoolean()) {
                break;
            }
        }
    }

    // carry out all the tasks in a working thread
    public void flush() {
        Thread worker = new Thread(this::clear);
        worker.start();
    }
}
