package de.davepe.futorial;

import com.github.amlcurran.showcaseview.targets.Target;

public class Showcase {

    private String title,description;
    private Target target;
    private int id;

    public Showcase(Target t, String title, String desc, int id){
        this.target = t;
        this.title = title;
        this.description = desc;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Target getTarget() {
        return target;
    }

}
