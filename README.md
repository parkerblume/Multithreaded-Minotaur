# Multi-threaded Minotaur Problems
* To run problem 1: `javac MinotaurParty.java && java MinotaurParty <num> <-p>`
* To run problem 2: `javac MinotaurVaseViewing.java && java MinotaurVaseViewing <num> <-p>`

Where *num* is an optional argument determining the number of guests at the party or wanting to view the Minotaur's vase. *-p* is an optional flag to whether or not print the steps taken in each respective problem.

## Problem 1: Minotaur's Birthday Party
The Minotaur invited N guests to his birthday party. When the guests arrived, he made the following announcement.
The guests may enter his labyrinth, one at a time and only when he invites them to do
so. At the end of the labyrinth, the Minotaur placed a birthday cupcake on a plate. When
a guest finds a way out of the labyrinth, he or she may decide to eat the birthday
cupcake or leave it. If the cupcake is eaten by the previous guest, the next guest will find
the cupcake plate empty and may request another cupcake by asking the Minotaur’s
servants. When the servants bring a new cupcake the guest may decide to eat it or leave
it on the plate.

The Minotaur’s only request for each guest is to not talk to the other guests about her or
his visit to the labyrinth after the game has started. The guests are allowed to come up
with a strategy prior to the beginning of the game. There are many birthday cupcakes, so
the Minotaur may pick the same guests multiple times and ask them to enter the
labyrinth. Before the party is over, the Minotaur wants to know if all of his guests have
had the chance to enter his labyrinth. To do so, the guests must announce that they have
all visited the labyrinth at least once.

Now the guests must come up with a strategy to let the Minotaur know that every guest
entered the Minotaur’s labyrinth. It is known that there is already a birthday cupcake left
at the labyrinth’s exit at the start of the game. How would the guests do this and not
disappoint his generous and a bit temperamental host?
Create a program to simulate the winning strategy (protocol) where each guest is
represented by one running thread. In your program you can choose a concrete number
for N or ask the user to specify N at the start.
### Approach and Proof
At first, I thought I could include an overarching counter that'll just keep track of the number of guests that have entered the labyrinth. However, that is against the rules. It is up to the guests and the guests only to keep track of anything going on based on their "communicated" strategy. The strategy that I've decided is all based on a leader (randomly decided) controlling the replacement of a cupcake. Here are the steps:
1. Have one guest be the leader (through being randomly chosen)
2. This leader is the only one that keeps track of the guests that have entered the labyrinth.
3. If there is a cupcake eaten, the leader increases their count and requests a new cupcake - otherwise, they don't eat the cupcake (they will eat last) and leave the labyrinth.
4. All other guests will only eat a cupcake if it is there on the platter, and they have yet to eat - otherwise, they request a new cupcake nor eat the cupcake if it’s there already and leave the labyrinth.
5. Once the leader determines that all guests have been able to go through the maze (and eat the cupcake) they will announce that all guests have been through.
6. Profits, success, party ends.

Each guest is represented by the `Guest` class and is attached to their own thread.
* `Guest` class has its own methods and attributes that are important to be able to keep track within the defined strategy, like if a `Guest` is a leader.
* In the overwritten `run()` method, guests will continue to wait until they are called into the maze in which the logic of whether they eat or not is implemented inside the Labyrinth's `enterLabyrinth()` method.
* If `Guest` is a leader there are attributes to keep track of the count of empty platters they've come across to determine if everyone has visited the maze.

 The `Labyrinth` is defined with these important attributes:
* `ReentrantLock lock` - This is a synchronized lock where each `Guest` (thread) has access to in order to determine if there is currently another `Guest` inside the Labyrinth. Since there is a possibility of a `Guest` being randomly called in there multiple times in a row, ReentrantLock is important to be used to not cause a deadlock by blocking itself. The `Guest` will be able to reenter, or reacquire, the lock it already holds.
* `AtomicBoolean` type for `cupcakeFilled` and `allGuestsHaveEaten` - AtomicBoolean allows for atomic operations (setting and getting, in this case) that each thread can observe. This will allow a guest to eat a cupcake if the cupcake has been filled, or move on if it hasn't been filled. `allGuestsHaveEaten` is more specifically for the leader, to let the host know that all hosts have been able to visit and eat a cupcake at their party.
The `Labyrinth` class is also its own defined method enterLabyrinth()` (mentioned previously) in which can lock and or unlock the `ReentrantLock` depending on if there is a guest currently in its maze, along with implemented logic for the `Guest` to eat the cupcake, leave, and if it’s the leader to increase the count or not.

With this strategy every guest has been able to always eat a cupcake through every run from 1 to N amount of guests.

### Experimental Evaluation along with Efficiency Analysis
Through the problem, my beginning approach did not work at all. Originally, I had `Guest` extend `Thread` interface and tried to start the party by looping through the list of `Guest` and using the `.start()` method - however, they would not start at all. That's when I realized I needed to declare them as a thread and start it that way. 

On the first *working* run-through, not all threads even got to enter the maze because I didn't have them waiting to be called. They tried to enter the maze, and if they couldn't enter due to it being locked then they just never attempted again. This is where the `AtomicBoolean allGuestsHaveEaten` came into play, as that was the conditional to "end the game" or to keep the threads active and waiting to be able to enter the maze.

I believe this to be the most efficient method that I could think of since the guests cannot communicate when the party starts. One guest needs to keep track of the counts, otherwise no guest will every *truly* know if everyone has been able to enter the labyrinth. If guests were able to freely request new cupcakes then the determined leader would not be able to count accurately either so they have to be the one to control when a cupcake gets replaced so they can definitely know 1 person has or hasn't eaten (or been through the maze) since they last ran through.

## Problem 2: Minotaur's Crystal Vase
The Minotaur decided to show his favorite crystal vase to his guests in a dedicated
showroom with a single door. He did not want many guests to gather around the vase
and accidentally break it. For this reason, he would allow only one guest at a time into
the showroom. He asked his guests to choose from one of three possible strategies for
viewing the Minotaur’s favorite crystal vase:
1) Any guest could stop by and check whether the showroom’s door is open at any time
and try to enter the room. While this would allow the guests to roam around the castle
and enjoy the party, this strategy may also cause large crowds of eager guests to gather
around the door. A particular guest wanting to see the vase would also have no
guarantee that she or he will be able to do so and when.
2) The Minotaur’s second strategy allowed the guests to place a sign on the door
indicating when the showroom is available. The sign would read “AVAILABLE” or
“BUSY.” Every guest is responsible to set the sign to “BUSY” when entering the
showroom and back to “AVAILABLE” upon exit. That way guests would not bother trying
to go to the showroom if it is not available.
3) The third strategy would allow the quests to line in a queue. Every guest exiting the
room was responsible to notify the guest standing in front of the queue that the
showroom is available. Guests were allowed to queue multiple times.
Which of these three strategies should the guests choose? Please discuss the advantages
and disadvantages.
Implement the strategy/protocol of your choice where each guest is represented by 1
running thread. You can choose a concrete number for the number of guests or ask the
user to specify it at the start.

### Approach
I've decided to go with strategy #2 as I believe that would probably be the most efficient way to go about this. If a thread is not able to enter a specific lock, it can go about another process and continually check back until it is allowed in.

For this, I just made a separate class called `Showroom` which is essentially the viewing for the Crystal Vase room. It has a `ReentrantLock` for the locking mechanism along with a Conditional for the lock called `isShowroomAvailable` which will signal for to the guests that the showroom is available. It also keeps track of the number of total guests, and the number of guests that entered to determine if everyone has been able to view the crystal vase.

### Experimental Evaluation along with Efficiency Analysis
There's not much of experimenting to go along with this other than how to implement the code. I roughly did the same thing I did with Problem 1 without the complication of a `Guest` class since I can just have the `Showroom` class keep track if all guests have viewed the vase or not. 

As mentioned previously, this second strategy I believed to be the most efficient out of the 3. It eliminates crowding, but you have to ensure that the guest (thread) will flip the conditional to being busy or available to be entered. The downside to this strategy, however, is it does not allow for the guests to view the vase multiple times and any guest can view it at any time as long as it is available. It is not ordered, if it needed to have a certain order, strategy #3 would be best using a Queue for the threads to enter the room.
