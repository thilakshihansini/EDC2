//package org.example;
import java.util.*;

class InterlockingImpl implements Interlocking {
    private final HashMap<Integer, String> trackSections = new HashMap<>(); // track number and train name
    private final HashMap<String, Integer> trainCurrentTrack = new HashMap<>();
    private final HashMap<String, Integer> nextTrainPosition = new HashMap<>(); // train name and next position
    private final HashMap<String, Integer> destinationTrainPosition = new HashMap<>();
    private final HashMap<String, Integer> trackToken = new HashMap<>();
    private final HashMap<String, Integer> trainToken = new HashMap<>();
    private final HashMap<String, Integer> stratTrack = new HashMap<>();
    private final HashMap<String, Integer> movedTrainsToDestination = new HashMap<>();

    private static final Map<String, List<Integer>> trainPaths = new HashMap<>();

    static {
        trainPaths.put("1,9", List.of(1, 5, 9));
        trainPaths.put("1,8", List.of(1, 5, 8));
        trainPaths.put("3,11", List.of(3, 7, 11));
        trainPaths.put("3,4", List.of(3, 4));
        trainPaths.put("4,3", List.of(4, 3));
        trainPaths.put("9,2", List.of(9, 6, 2));
        trainPaths.put("10,2", List.of(10, 6, 2));
        trainPaths.put("11,3", List.of(11, 7, 3));
    }


    @Override
    public int moveTrains(String[] trainNames) throws IllegalArgumentException { //override move train method
        int movedTrainsCount = 0;
        List<String> trainList = new ArrayList<>(Arrays.asList(trainNames));
        System.out.println("\n----- Trains to move :" + Arrays.toString(trainNames) +" -----");
        for (String trainName : trainNames) {
            if (!trainCurrentTrack.containsKey(trainName)) {
                System.out.println("There is no available train named " + trainName +" to move ");
            }else {
                System.out.println("Moving train :" + trainName +" ");
                trainToken.put(trainName,1);
                if (!isDeadLock()) {
                    movedTrainsCount += move(trainName);
                }
            }
        }
        return movedTrainsCount;
    }

    @Override
    public void addTrain(String trainName, int entryTrackSection, int destinationTrackSection) //override addTrain method
            throws IllegalArgumentException, IllegalStateException {


        if (trackSections.containsValue(trainName)) { // Check for duplicate train names
            throw new IllegalArgumentException("Train name already exists: " + trainName);
        }

        if (entryTrackSection != destinationTrackSection) {
            if (trainPaths.get(entryTrackSection + "," + destinationTrackSection) == null) {
                throw new IllegalArgumentException("No path available between " + entryTrackSection + " and " + destinationTrackSection);
            }
        }

        if (trackSections.containsKey(entryTrackSection)) { // Check if the entry section is already occupied
            throw new IllegalStateException("Entry track section is already occupied: " + entryTrackSection);
        }


        System.out.println("Adding " + trainName + " start section " + entryTrackSection + " exit section " + destinationTrackSection);


        trackSections.put(entryTrackSection, trainName); // Add the train to the entry track section and save its destination
        trainCurrentTrack.put(trainName, entryTrackSection);
        nextTrainPosition.put(trainName, getNextHop(entryTrackSection,entryTrackSection,destinationTrackSection));
        destinationTrainPosition.put(trainName, destinationTrackSection); // Store the destination for the train
        trainToken.put(trainName, 0);
        stratTrack.put(trainName, entryTrackSection);

    }

    @Override
    public String getSection(int trackSection) throws IllegalArgumentException {
        if (!trackSections.containsKey(trackSection)) {
            return null;
        }
        return trackSections.get(trackSection);
    }

    @Override
    public int getTrain(String trainName) throws IllegalArgumentException {
        if (trainCurrentTrack.get(trainName) != null) {
            return trainCurrentTrack.get(trainName);
        }
        return -1; // Return -1 if the train is no longer in the rail corridor
    }

    public static Integer getNextHop(int current, int source, int destination) {
        String key = source + "," + destination;
        List<Integer> path = trainPaths.get(key);

        if (source != destination) {
            if (path != null) {
                int index = path.indexOf(current);
                if (index != -1 && index < path.size() - 1) {
                    return path.get(index + 1); // Return the next hop
                } else if (current == destination) {
                    return destination;
                }
            } else {
                System.out.println("No path available between " + source + " and " + destination);
            }
        } else {
            return destination;
        }

        return null;
    }

    public boolean isDeadLock(){

        if (trackSections.containsKey(3) && trackSections.containsKey(4)){
            if (nextTrainPosition.get(trackSections.get(3))==4 && nextTrainPosition.get(trackSections.get(4))==3){
                trainToken.put(trackSections.get(3),0);
                trainToken.put(trackSections.get(4),0);
                System.out.println(trackSections.get(4)+ " and "+trackSections.get(3)+" not moving because of dead lock." );
                return true;
            }
        }

        if (trackSections.containsKey(3) && trackSections.containsKey(7)){
            if (nextTrainPosition.get(trackSections.get(3))==7 && nextTrainPosition.get(trackSections.get(7))==3){
                trainToken.put(trackSections.get(3),0);
                trainToken.put(trackSections.get(7),0);
                System.out.println(trackSections.get(3)+ " and "+trackSections.get(7)+" not moving because of dead lock." );
                return true;
            }
        }

        if (trackSections.containsKey(3) && trackSections.containsKey(11)){
            if (nextTrainPosition.get(trackSections.get(3))==7 && nextTrainPosition.get(trackSections.get(11))==7){
                trainToken.put(trackSections.get(3),0);
                trainToken.put(trackSections.get(11),0);
                System.out.println(trackSections.get(3)+ " and "+trackSections.get(11)+" not moving because of dead lock." );
                return true;
            }
        }

        if (trackSections.containsKey(7) && trackSections.containsKey(11)){
            if (nextTrainPosition.get(trackSections.get(7))==11 && nextTrainPosition.get(trackSections.get(11))==7){
                trainToken.put(trackSections.get(7),0);
                trainToken.put(trackSections.get(11),0);
                System.out.println(trackSections.get(7)+ " and "+trackSections.get(11)+" not moving because of dead lock." );
                return true;
            }
        }

        return false;
    }

    public int transition(String transition,String train) { //create transition and return transition fired or not

        if (transition == "T1") { //fire transition T1
            if (trackSections.containsKey(1)){
                if (trainToken.get(trackSections.get(1)) == 1){
                    if (nextTrainPosition.get(trackSections.get(1))==5){
                        if (!trackSections.containsKey(5)){
                            updateTranTrack(transition, trackSections.get(1), 1,5);
                            return 1;
                        }else {
                            System.out.println("Transition T1 cannot fired. There is a train on section 5. "+trackSections.get(5)+" remain in section 5");
                        }
                    }
                }
            }
        }

        if (transition == "T2") { //fire transition T2
            if (trackSections.containsKey(5)){
                if (trainToken.get(trackSections.get(5)) == 1){
                    if (nextTrainPosition.get(trackSections.get(5))==9){
                        if (!trackSections.containsKey(9)){
                            updateTranTrack(transition, trackSections.get(5), 5,9);
                            return 1;
                        }else {
                            System.out.println("Transition T2 cannot fired. There is a train on section 9. "+trackSections.get(5)+" remain in section 5");
                        }
                    }
                }
            }
        }

        if (transition == "T3") { //fire transition T3

            if (trackSections.containsKey(5)){
                if (trainToken.get(trackSections.get(5)) == 1){
                    if (nextTrainPosition.get(trackSections.get(5))==8){
                        if (!trackSections.containsKey(8)){
                            updateTranTrack(transition, trackSections.get(5), 5,8);
                            return 1;
                        }else {
                            System.out.println("Transition T3 cannot fired. There is a train on section 8. "+trackSections.get(5)+" remain in section 5");
                        }
                    }
                }
            }
        }

        if (transition == "T4") { //fire transition T4
            if (trackSections.containsKey(3)){
                if (trainToken.get(trackSections.get(3)) == 1){
                    if (nextTrainPosition.get(trackSections.get(3))==7){
                        if (!trackSections.containsKey(7)){
                            updateTranTrack(transition, trackSections.get(3), 3,7);
                            return 1;
                        }else {
                            System.out.println("Transition T4 cannot fired. There is a train on section 7. "+trackSections.get(5)+" remain in section 3");
                        }
                    }
                }
            }
        }

        if (transition == "T5") { //fire transition T5

            if (trackSections.containsKey(7)){
                if (trainToken.get(trackSections.get(7)) == 1){
                    if (nextTrainPosition.get(trackSections.get(7))==11){
                        if (!trackSections.containsKey(11)){
                            updateTranTrack(transition, trackSections.get(7), 7,11);
                            return 1;
                        }else {
                            System.out.println("Transition T5 cannot fired. There is a train on section 11. "+trackSections.get(5)+" remain in section 7");
                        }
                    }
                }
            }
        }

        if (transition == "T6") { //fire transition T6
            if (trackSections.containsKey(3)){
                if (trainToken.get(trackSections.get(3)) == 1){
                    if (nextTrainPosition.get(trackSections.get(3))==4){
                        if (!trackSections.containsKey(4)){
                            if (trackSections.containsKey(1)){
                                if (trainToken.get(trackSections.get(1)) == 1){
                                    if (nextTrainPosition.get(trackSections.get(1))!=5){
                                        updateTranTrack(transition, trackSections.get(3), 3,4);
                                        return 1;
                                    }else {
                                        System.out.println("Transition T6 cannot fired. There is a train on section 1 to move to section 5. "+trackSections.get(5)+" remain in section 4");
                                    }
                                }else {
                                    updateTranTrack(transition, trackSections.get(3), 3,4);
                                    return 1;
                                }
                            }else if (trackSections.containsKey(6)){
                                if (trainToken.get(trackSections.get(6)) == 1){
                                    if (nextTrainPosition.get(trackSections.get(6))!=2){
                                        updateTranTrack(transition, trackSections.get(3), 3,4);
                                        return 1;
                                    }else {
                                        System.out.println("Transition T6 cannot fired. There is a train on section 6 to move to section 2. "+trackSections.get(5)+" remain in section 4");
                                    }
                                }else {
                                    updateTranTrack(transition, trackSections.get(3), 3,4);
                                    return 1;
                                }
                            }else {
                                updateTranTrack(transition, trackSections.get(3), 3,4);
                            }
                        }else {
                            System.out.println("Transition T6 cannot fired. There is a train on section 4. "+trackSections.get(5)+" remain in section 3");
                        }
                    }
                }
            }
        }

        if (transition == "T7") { //fire transition T7
            if (trackSections.containsKey(9)){
                if (trainToken.get(trackSections.get(9)) == 1){
                    if (nextTrainPosition.get(trackSections.get(9))==6){
                        if (!trackSections.containsKey(6)){
                            updateTranTrack(transition, trackSections.get(9), 9,6);
                            return 1;
                        }else {
                            System.out.println("Transition T7 cannot fired. There is a train on section 6. "+trackSections.get(5)+" remain in section 9");
                        }
                    }
                }
            }
        }
        if (transition == "T8") { //fire transition T8

            if (trackSections.containsKey(6)){
                if (trainToken.get(trackSections.get(6)) == 1){
                    if (nextTrainPosition.get(trackSections.get(6))==2){
                        if (!trackSections.containsKey(2)){
                            updateTranTrack(transition, trackSections.get(6), 6,2);
                            return 1;
                        }else {
                            System.out.println("Transition T8 cannot fired. There is a train on section 2. "+trackSections.get(5)+" remain in section 6");
                        }
                    }
                }
            }
        }
        if (transition == "T9") { //fire transition T9
            if (trackSections.containsKey(10)){
                if (trainToken.get(trackSections.get(10)) == 1){
                    if (nextTrainPosition.get(trackSections.get(10))==6){
                        if (!trackSections.containsKey(6)){
                            updateTranTrack(transition, trackSections.get(10), 10,6);
                            return 1;
                        }else {
                            System.out.println("Transition T9 cannot fired. There is a train on section 6. "+trackSections.get(5)+" remain in section 10");
                        }
                    }
                }
            }
        }
        if (transition == "T10") { //fire transition T10
            if (trackSections.containsKey(4)){
                if (trainToken.get(trackSections.get(4)) == 1){
                    if (nextTrainPosition.get(trackSections.get(4))==3){
                        if (!trackSections.containsKey(3)){
                            if (trackSections.containsKey(1)){
                                if (trainToken.get(trackSections.get(1)) == 1){
                                    if (nextTrainPosition.get(trackSections.get(1))!=5){
                                        updateTranTrack(transition, trackSections.get(4), 4,3);
                                        return 1;
                                    }else {
                                        System.out.println("Transition T10 cannot fired. There is a train on section 1 to move to section 5. "+trackSections.get(5)+" remain in section 4");
                                    }
                                }else {
                                    updateTranTrack(transition, trackSections.get(4), 4,3);
                                    return 1;
                                }
                            }else if (trackSections.containsKey(6)){
                                if (trainToken.get(trackSections.get(6)) == 1){
                                    if (nextTrainPosition.get(trackSections.get(6))!=2){
                                        updateTranTrack(transition, trackSections.get(4), 4,3);
                                        return 1;
                                    }else {
                                        System.out.println("Transition T10 cannot fired. There is a train on section 6 to move to section 2. "+trackSections.get(5)+" remain in section 4");
                                    }
                                }else {
                                    updateTranTrack(transition, trackSections.get(4), 4,3);
                                    return 1;
                                }
                            }else {
                                updateTranTrack(transition, trackSections.get(4), 4,3);
                                return 1;
                            }
                        }else {
                            System.out.println("Transition T10 cannot fired. There is a train on section 3. "+trackSections.get(5)+" remain in section 4");
                        }
                    }
                }
            }
        }
        if (transition == "T11") { //fire transition T11
            if (trackSections.containsKey(11)){
                if (trainToken.get(trackSections.get(11)) == 1){
                    if (nextTrainPosition.get(trackSections.get(11))==7){
                        if (!trackSections.containsKey(7)){
                            updateTranTrack(transition, trackSections.get(11), 11,7);
                            return 1;
                        }else {
                            System.out.println("Transition T11 cannot fired. There is a train on section 7. "+trackSections.get(5)+" remain in section 11");
                        }
                    }
                }
            }
        }
        if (transition == "T12") { //fire transition T12
            if (trackSections.containsKey(7)){
                if (trainToken.get(trackSections.get(7)) == 1){
                    if (nextTrainPosition.get(trackSections.get(7))==3){
                        if (!trackSections.containsKey(3)){
                            updateTranTrack(transition, trackSections.get(7), 7,3);
                            return 1;
                        }else {
                            System.out.println("Transition T12 cannot fired. There is a train on section 3. "+trackSections.get(5)+" remain in section 7");
                        }
                    }
                }
            }
        }
        if (transition == "T13") { //fire transition T13
            if (Objects.equals(trainCurrentTrack.get(train), destinationTrainPosition.get(train))){
                exitTrain(train);
                System.out.println("Transition T13 fired. "+train +" exit from section "+ destinationTrainPosition.get(train));
                return 1;
            }
        }
        return 0;
    }

    public void updateTranTrack(String transition,String train, int start, int end){
        trainToken.remove(train);
        trainToken.put(train,0);

        trackSections.remove(start);
        trackSections.put(end, train);

        trainCurrentTrack.remove(train);
        trainCurrentTrack.put(train, end);

        nextTrainPosition.remove(train);
        nextTrainPosition.put(train, getNextHop(trainCurrentTrack.get(train), stratTrack.get(train), destinationTrainPosition.get(train)));

        if (trainCurrentTrack.get(train) == destinationTrainPosition.get(train)){
            movedTrainsToDestination.put(train, 1);
        }
//        exitTrain(train);
        System.out.println("Transition "+ transition +" fired. " + train + " moved from section " + start + " to section " +end);
    }

    public int move(String train) {

        int movetrains = 0;

        movetrains += transition("T13",train);

        movetrains += transition("T1",train);
        movetrains += transition("T2",train);
        movetrains += transition("T3",train);
        movetrains += transition("T4",train);
        movetrains += transition("T5",train);
        movetrains += transition("T6",train);
        movetrains += transition("T7",train);
        movetrains += transition("T8",train);
        movetrains += transition("T9",train);
        movetrains += transition("T10",train);
        movetrains += transition("T11",train);
        movetrains += transition("T12",train);


        return movetrains;
    }

    public void exitTrain(String train) {
        if (movedTrainsToDestination.containsKey(train)){
            trackSections.remove(trainCurrentTrack.get(train));
            trainCurrentTrack.remove(train);
            nextTrainPosition.remove(train);
        }else if (trainCurrentTrack.get(train) == destinationTrainPosition.get(train)) {
            movedTrainsToDestination.put(train, 1);
        }
    }

    public static void main(String[] args) {

        InterlockingImpl interlocking = new InterlockingImpl(); // Sample testing

        interlocking.addTrain("Train-A", 1, 9);
        interlocking.addTrain("Train-B", 4, 3);
        interlocking.addTrain("Train-C", 9, 2);

        System.out.println("Moved Train Count: "+ interlocking.moveTrains(new String[]{"Train-B"}));
        System.out.println("Moved Train Count: "+ interlocking.moveTrains(new String[]{"Train-A", "Train-C"}));
        System.out.println("Moved Train Count: "+ interlocking.moveTrains(new String[]{"Train-B", "Train-A"}));

        for (int i = 1; i <= 11; i++) {
            System.out.println("Track " + i + ": " + interlocking.getSection(i));
        }
    }
}