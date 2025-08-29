package phuctnh;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class AttendanceStats {
    private String name;
    private double totalHours;
    private int lateDays;
    private int totalCompleted; // Số lượng task hoàn thành
    private int totalPending;   // Số lượng task đang chờ

    public AttendanceStats(String name, double totalHours, int lateDays, int totalCompleted, int totalPending) {
        this.name = name;
        this.totalHours = totalHours;
        this.lateDays = lateDays;
        this.totalCompleted = totalCompleted;
        this.totalPending = totalPending;
    }

    public String getName() {
        return name;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public int getLateDays() {
        return lateDays;
    }

    public int getTotalCompleted() {
        return totalCompleted;
    }

    public int getTotalPending() {
        return totalPending;
    }
    public static String analyzetodo(String json)
    {
        int pending = 0, completed = 0;
        Gson gson = new Gson();
        ArrayList<Task> tasks = gson.fromJson(json, new TypeToken<ArrayList<Task>>(){}.getType());
        for(Task a : tasks)
        {
            if(a.getStatus().equals("completed"))
            {
                completed++;
            }
            else if(a.getStatus().equals("pending"))
            {
                pending++;
            }
        }
        return pending + "," + completed;
    }
}


