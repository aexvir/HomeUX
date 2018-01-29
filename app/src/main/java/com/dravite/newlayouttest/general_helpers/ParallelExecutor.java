package com.dravite.newlayouttest.general_helpers;

import android.os.Process;
import android.util.Log;

import com.dravite.newlayouttest.LauncherLog;

import java.util.LinkedList;

/**
 * An AsyncTask-like threaded worker which handles a Runnable queue with a specified amount of threads.
 */
public class ParallelExecutor {
    //A fixed-sized array of workers (threads, to be more exact)
    private ParallelWorker[] mWorkers;

    //A list of Runnables (The queue)
    private final LinkedList<Runnable> mQueue;

    /**
     * This constructor specifies a thread count with which the ParallelExecutor will work.
     * @param numThreads the number of threads.
     */
    public ParallelExecutor(int numThreads){
        mQueue = new LinkedList<>();
        mWorkers = new ParallelWorker[numThreads];

        //Create and start all workers.
        for(int i=0; i<mWorkers.length; i++){
            mWorkers[i] = new ParallelWorker();
            mWorkers[i].start();
        }
    }

    /**
     * Push a Runnable into the Runnable queue and call notify() to let one of the workers work on it.
     * @param r the Runnable to push
     */
    public void enqueue(Runnable r){
        synchronized (mQueue) {
            mQueue.add(r);
            //Let someone work with it!
            mQueue.notify();
        }
    }

    /**
     * Stops all Threads and clears the Queue.
     */
    public void stopAll(){
        synchronized (mQueue){
            for(ParallelWorker worker : mWorkers){
                worker.cancel();
            }
            mQueue.notifyAll();
            mQueue.clear();
        }
    }

    /**
     * A worker thread for the ParallelExecutor. Runs one Runnable at a time parallel to the other threads (including the UI thread).
     */
    public class ParallelWorker extends Thread{

        private boolean isRunning = true;

        public void cancel(){
            isRunning = false;
        }

        public ParallelWorker(){
            setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        }

        @Override
        public void run() {
            Runnable r;
            //Keep it running.
            while(true){
                //Grant synchronized access to the Runnable queue
                synchronized (mQueue){
                    while(mQueue.isEmpty()){
                        try{
                            //Wait for a new Runnable to be enqueued to save runtime cycles
                            mQueue.wait();
                            if(!isRunning)
                                break;
                        } catch (InterruptedException e){
                            //Ignore this
                        }
                    }

                    if(!isRunning)
                        break;

                    //pop first element off the queue
                    r = mQueue.removeFirst();
                }
                try {
                    //Run the latest enqueued runnable which has been popped off of the queue before.
                    r.run();
                } catch (Exception e){
                    //LauncherLog.w("ParallelExecutor", "There was an error happening in a parallel thread execution. Please check for this: " + e.getMessage() + "\nin Class " + e.getStackTrace()[2].getClassName() + " in line " + e.getStackTrace()[2].getLineNumber());
                }
            }
        }
    }
}
