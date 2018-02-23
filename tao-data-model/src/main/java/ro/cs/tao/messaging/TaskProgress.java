package ro.cs.tao.messaging;

public class TaskProgress {
    private String name;
    private double progress;
    private SubTaskProgress subTaskProgress;

    public TaskProgress(String name) {
        this.name = name;
    }

    public TaskProgress(String name, double progress, SubTaskProgress subTaskProgress) {
        this.name = name;
        this.progress = progress;
        this.subTaskProgress = subTaskProgress;
    }

    public TaskProgress(String name, double progress, String subTask, double subProgress) {
        this(name, progress, new SubTaskProgress(subTask, subProgress));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public SubTaskProgress getSubTaskProgress() {
        return subTaskProgress;
    }

    public void setSubTaskProgress(SubTaskProgress subTaskProgress) {
        this.subTaskProgress = subTaskProgress;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
