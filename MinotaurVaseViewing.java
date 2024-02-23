import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Showroom {
    private int numGuests = 0;
    private ReentrantLock lock = new ReentrantLock();
    private Condition isShowroomAvailable = lock.newCondition();
    private boolean isShowroomBusy = false;
    private int counter = 0;
    private boolean printViewing = false;

    Showroom(int guests, boolean print)
    {
        numGuests = guests;
        printViewing = print;
    }

    public void viewing(int guestNumber) throws InterruptedException 
    {
        lock.lock();
        try  {
            while (isShowroomBusy) {
                // go do something else, in this case just wait
                isShowroomAvailable.await();
            }
            isShowroomBusy = true;
            if (printViewing) { System.out.println("Guest [" + guestNumber + "] enters showroom flipping the sign to BUSY, and views the vase."); }

            counter++;
        }
        finally 
        {
            if (printViewing) { System.out.println("Guest [" + guestNumber + "] leaves showroom and flips the sign to AVAILABLE."); }
            isShowroomBusy = false;
            isShowroomAvailable.signal();
            lock.unlock();
        }

        if (counter == numGuests)
        {
            System.out.println("All " + numGuests + " guests have had the chance to look at the Minotaur's vase.");
        }
    }
}

public class MinotaurVaseViewing {
    public static boolean printViewing = false;
    public static void main(String[] args) 
    {
        int numGuests = 10;
        // allows for a command line arg from the user to determine number of guests, otherwise defaults to 10 guests, and if they want to print steps.
        if (args.length >= 1)
        {
            try {
                numGuests = Integer.parseInt(args[0]);
                if (numGuests <= 0) 
                {
                    System.out.println("You can't have none (or negative) guests at the viewing! Defaulted to 10.");
                    numGuests = 10;
                }

                printViewing = args.length > 1 ? (args[1].equals("-p") ? true : false) : false;
            } catch (Exception e)
            {
                // if they omit a count, check for p flag otherwise take all defaults.
                printViewing = args[0].equals("-p") ? true : false;
                if (!printViewing)
                {
                    System.err.println("Not a valid value, defaulting to 10 guests and not printing steps.");
                }
            }
        }

        // Create threads for each guest
        Showroom showroom = new Showroom(numGuests, printViewing);
        Thread[] guests = new Thread[numGuests];

        long startTime = System.currentTimeMillis();
        System.out.println("Starting the vase viewing!");
        for (int i = 0; i < numGuests; i++)
        {
            int num = i + 1;
            guests[i] = new Thread( () -> 
            {
                try {
                    showroom.viewing(num);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            guests[i].start();
        }

        for (int i = 0; i < numGuests; i++) {
                try {
                    guests[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("It took " + (endTime - startTime) + "ms.");

    }
}
