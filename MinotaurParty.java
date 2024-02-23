import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

class Guest extends Thread {
    boolean hasEaten;
    boolean isLeader;
    private Labyrinth labyrinth;
    int counter;

    Guest(Labyrinth labyrinth, boolean isLeader) {
        this.isLeader = isLeader;
        this.labyrinth = labyrinth;
        this.hasEaten = false;
        this.counter = 0;
    }

    @Override
    public void run() {
        // A guest enters labyrinth on start.
        while (!labyrinth.getAllGuestsHaveEaten()) {
            labyrinth.enterLabyrinth(this);
        }
    }

    public void setHasEaten(boolean hasEaten)
    {
        this.hasEaten = hasEaten;
    }

    public boolean getHasEaten()
    {
        return this.hasEaten;
    }

    public boolean getIsLeader()
    {
        return this.isLeader;
    }

    // These are all leader-related functions, a regular guest will not touch these
    public void incrementCounter()
    {
        this.counter++;
    }

    public int getCounter()
    {
        return this.counter;
    }
}

class Labyrinth {
    private AtomicBoolean cupcakeFilled = new AtomicBoolean(true);
    private AtomicBoolean allGuestsHaveEaten = new AtomicBoolean(false);
    private int numGuests;
    private final ReentrantLock lock = new ReentrantLock();
    private boolean printStatements;

    public Labyrinth(int numGuests, boolean printPartySteps) {
        this.numGuests = numGuests;
        this.printStatements = printPartySteps;
    }

    public boolean getAllGuestsHaveEaten()
    {
        return this.allGuestsHaveEaten.get();
    }

    public void enterLabyrinth(Guest guest) {
        lock.lock();
        try {
            // strategy: Leader -> Requests refill cupcake if empty, Regular Guest -> Eats cupcake iff platter is filled.
            if (cupcakeFilled.get()) 
            {
                if (!guest.getIsLeader()) 
                {
                    if (printStatements) { System.out.println("A regular guest has eaten their cupcake."); }
                    guest.setHasEaten(true);
                    cupcakeFilled.set(false);
                } 
                else 
                {
                    if (printStatements) { System.out.println("The leader found a filled cupcake, so they decide to leave the cupcake alone."); }
                }
            }
            else
            {
                if (guest.getIsLeader())
                {
                    if (guest.getCounter() < numGuests - 1)
                    {
                        if (printStatements) { System.out.println("The leader has requested to replace the cupcake."); }
                        cupcakeFilled.set(true);
                        guest.incrementCounter();
                    }
                }
            }

            if (guest.getIsLeader() && guest.getCounter() == numGuests - 1)
            {
                if (printStatements) { System.out.println("The leader is the last to eat therefore, everyone is done!"); }
                cupcakeFilled.set(false);
                guest.setHasEaten(true);
                guest.incrementCounter();
                allGuestsHaveEaten.set(true);
            }

        } finally {
            lock.unlock();
        }
    }
}

public class MinotaurParty {
    private static boolean printPartySteps = false;

    public static int getRandomLeader(int numGuests)
    {
        // Random number interval [0, numGuests]
        return (int) (Math.random() * (numGuests + 1));
    }

    public static void main (String[] args) 
    {
        int numGuests = 25;
        // allows for a command line arg from the user to determine number of guests, otherwise defaults to 25 guests, and if they want to print steps.
        if (args.length >= 1)
        {
            try {
                numGuests = Integer.parseInt(args[0]);
                if (numGuests <= 0) 
                {
                    System.out.println("You can't have none (or negative) guests at a party! Defaulted to 25.");
                    numGuests = 25;
                }

                printPartySteps = args.length > 1 ? (args[1].equals("-p") ? true : false) : false;
            } catch (Exception e)
            {
                // if they omit a count, check for p flag otherwise take all defaults.
                printPartySteps = args[0].equals("-p") ? true : false;
                if (!printPartySteps)
                {
                    System.err.println("Not a valid value, defaulting to 25 guests and not printing steps.");
                }
            }
        }

        // Define our guests (threads) array, and create the labyrinth!
        List<Guest> guests = new ArrayList<>();
        int leader = getRandomLeader(numGuests);
        Labyrinth labyrinth = new Labyrinth(numGuests, printPartySteps);
        Random rand = new Random(System.currentTimeMillis());

        long startTime = System.currentTimeMillis();
        // create our guests, and start the party!
        for (int i = 0; i < numGuests; i++) {
            guests.add(new Guest(labyrinth, i == leader));
        }

        System.out.println("Starting the party with [" + numGuests + "] guests!!");
        List<Thread> threads = new ArrayList<>();

        boolean finish = false;
        while (!finish) {
            // "randomly" select the guests each iteration
            int nextGuest = rand.nextInt(numGuests);

            Thread thread = new Thread(guests.get(nextGuest)); // Create a new thread for each guest
            thread.start();
            threads.add(thread);

            finish = labyrinth.getAllGuestsHaveEaten();
        }

        for (Thread thread : threads) {
            try {
                thread.join(); // Wait for each guest to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();

        System.out.println("All guests have entered the labyrinth! \nIt took " + (endTime - startTime) + "ms.");
    }
}