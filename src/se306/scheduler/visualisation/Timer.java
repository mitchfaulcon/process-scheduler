package se306.scheduler.visualisation;

import javafx.beans.property.SimpleStringProperty;
import se306.scheduler.logic.Algorithm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.TimerTask;

public class Timer {

    private static Timer instance = new Timer();

    //Time variables
    private SimpleStringProperty sspTime = new SimpleStringProperty("00:00.00");
    private long time;

    //Timer variables
    private java.util.Timer t = new java.util.Timer();
    private TimerTask tt;
    private boolean timing = false;
    private static boolean iVisualised;

    private BigInteger maxSchedules;
    private BigInteger schedulesRemaining;

    public static Timer getInstance(boolean visualise){
        iVisualised = visualise;
        return instance;
    }

    private Timer() {
    }

    public void setMaxSchedules(BigInteger maxSchedules){
        this.maxSchedules = maxSchedules;
    }

    /**
     * Returns a human-readable representation of the number of remaining schedules, which could be a large number.
     */
    public String getSchedulesRemaining() {
        String schedulesString = "";
        BigDecimal bigDecimal = new BigDecimal(schedulesRemaining);

        if (schedulesRemaining.compareTo(BigInteger.valueOf(1000000)) >= 0) { // million
            if (schedulesRemaining.compareTo(BigInteger.valueOf(1000000000)) >= 0) { // billion
                if (schedulesRemaining.compareTo(BigInteger.valueOf(1000000000000L)) >= 0) { // trillion
                    if (schedulesRemaining.compareTo(BigInteger.valueOf(1000000000000000L)) >= 0) {
                        String value = String.valueOf(schedulesRemaining);
                        schedulesString = String.format("%s.%sE%d", value.substring(0, 1), value.substring(1, 3), value.length() - 1);
                    } else {
                        schedulesString = String.format("%.2f trillion",
                                bigDecimal.divide(BigDecimal.valueOf(1000000000000.0), 2, RoundingMode.HALF_DOWN));
                    }
                } else {
                    schedulesString = String.format("%.2f billion",
                            bigDecimal.divide(BigDecimal.valueOf(1000000000.0), 2, RoundingMode.HALF_DOWN));
                }
            } else {
                schedulesString = String.format("%.2f million",
                        bigDecimal.divide(BigDecimal.valueOf(1000000.0), 2, RoundingMode.HALF_DOWN));
            }
        } else {
            schedulesString = String.valueOf(schedulesRemaining);
        }

        return schedulesString;
    }

    public void startTimer(final long time) {
        this.time = time;
        timing = true;

        tt = new TimerTask() {
            @Override
            public void run() {
                if (!timing) {
                    try {
                        tt.cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    updateTime();
                    if(iVisualised) {
                        schedulesRemaining = maxSchedules.subtract(Algorithm.getBigSchedulesChecked());
                    }
                }
            }
        };
        //Timer hits every 10 ms, starting after 10ms
        t.scheduleAtFixedRate(tt, 10,10);
    }

    public void stopTimer() {
        timing = false;
    }

    public void resumeTimer() {
        timing = true;
    }

    private void updateTime() {
        this.time++;
        String[] split = getMSMsTimeFormat();

        //Add extra '0' before number if only one digit
        sspTime.set((split[0].length() == 1 ? "0" + split[0] : split[0].substring(0, 2)) + ":" +
                    (split[1].length() == 1 ? "0" + split[1] : split[1].substring(0, 2)) + "." +
                    (split[2].length() == 1 ? "0" + split[2] : split[2].substring(0, 2)));
    }

    public SimpleStringProperty getSspTime() {
        return sspTime;
    }

    public String getSeconds(){
        String[] split = getMSMsTimeFormat();

        int totalSeconds = Integer.parseInt(split[1]) + Integer.parseInt(split[0])*60;
        String s = Integer.toString(totalSeconds);
        String ms = split[2].length() == 1 ? "0" + split[2] : split[2].substring(0, 2);

        return (s + "." + ms);
    }

    private String[] getMSMsTimeFormat(){
        String[] MSMs = new String[3];

        //Convert elapsed time to minutes, seconds, and milliseconds
        long minutes = (this.time / 6000);
        long seconds = (this.time / 100) % 60;
        long milliseconds = this.time % 100;

        //Put minutes, seconds, milliseconds into string array
        MSMs[0] = Integer.toString((int)minutes);
        MSMs[1] = Integer.toString((int)seconds);
        MSMs[2] = Integer.toString((int)milliseconds);

        return MSMs;
    }
}