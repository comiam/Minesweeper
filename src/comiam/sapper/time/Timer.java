package comiam.sapper.time;

import java.util.TimerTask;

public class Timer
{
    private static long seconds = 0;
    private static int milliseconds = 0;
    private static Runnable runnable;
    private static TimerTask task;
    private static java.util.Timer timer;

    private static void makeTask()
    {
        task = new TimerTask()
        {
            @Override
            public void run()
            {
                milliseconds++;
                seconds += milliseconds / 10;
                if(seconds + milliseconds / 10 > seconds)
                    runnable.run();
                milliseconds %= 10;
            }
        };
        timer = new java.util.Timer();
    }

    public static void start(Runnable r)
    {
        milliseconds = 0;
        seconds = 0;
        runnable = r;
        makeTask();
        schedule();
    }

    public static void stop()
    {
        if(timer == null)
            return;
        timer.cancel();
        timer.purge();
    }

    public static void on()
    {
        makeTask();
        schedule();
    }

    public static long getSeconds()
    {
        return seconds;
    }

    private static void schedule()
    {
        timer.schedule(task,0,100);
    }

    public static String makeTimeString(long val)
    {
        String time = Long.toString(val);
        if(time.length() == 1)
            time = "0" + time;

        return time;
    }
}
