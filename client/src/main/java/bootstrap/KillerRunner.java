package bootstrap;

import controller.Killer;

public class KillerRunner {
    public static void main(String[] args) {
        var killer = new Killer();
        killer.askServerToFinish();
    }
}
