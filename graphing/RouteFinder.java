
//Author: Zachary Lin

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class RouteFinder {

    public static void main (String [] args){

        if (args.length < 1){
            System.out.println("Valid text file required as the first argument.");
            return;
        }

        HashMap<String, Integer> nodeMapping = new HashMap<>();

        int outputNo = 1;

        FileReader fr;
        try{
            fr = new FileReader(args[0]);
        }
        catch (FileNotFoundException e){
            System.out.println("File not found / invalid file name.");
            return;
        }

        try {
            BufferedReader br = new BufferedReader(fr);
            String inputGraph = br.readLine();

            int [][] adjacencyList = createGraph(inputGraph, nodeMapping);

            if (adjacencyList == null){
                System.out.println("Invalid graph input.");
                return;
            }

            String line = br.readLine();
            while (line != null){
                handleCommand(line, outputNo++, adjacencyList, nodeMapping);
                line = br.readLine();
            }

            fr.close();
            br.close();
        }
        catch (IOException e){
            System.out.println("IO exception. Please restart the program.");
            return;
        }


    }

    /*  creates a graph based on the given input
     *
     *  @param input - A string representing the graph
     *  @param nodeMapping - A hashmap used to translate between String inputs and integer indices on the array
     *  @return - A 2D array based off of the input string. If the graph is invalid, returns null
     */
    public static int[][] createGraph(String input, HashMap<String, Integer> nodeMapping){

        if (input == null || input.isEmpty()) return null;

        int nodeCounter = 0;

        String [] inputList = input.split(",");
        ArrayList<ArrayList<Integer>> edgeList = new ArrayList<>();

        //preprocess the input
        for (String rawData : inputList){

            rawData = rawData.trim();

            //check if the input is valid
            if (rawData.length() < 3) return null;

            String rawSrc = rawData.substring(0, 1);
            String rawDest = rawData.substring(1, 2);
            String rawWeight = rawData.substring(2, rawData.length());

            //check if the weight is an integer
            if (!isInteger(rawWeight)){
                System.out.println("One or more edge weights are not valid integers.");
                return null;
            }


            ArrayList<Integer> edge = new ArrayList<Integer>(3);

            //check if we have mapped the nodes before
            int src = nodeMapping.getOrDefault(rawSrc, -1);
            if (src == -1){
                nodeMapping.put(rawSrc, nodeCounter);
                src = nodeCounter++;
            }
            edge.add(src);

            int dest = nodeMapping.getOrDefault(rawDest, -1);
            if (dest == -1){
                nodeMapping.put(rawDest, nodeCounter);
                dest = nodeCounter++;
            }
            edge.add(dest);

            int weight = Integer.parseInt(rawWeight);
            edge.add(weight);

            edgeList.add(edge);
        }

        int [][] adjacencyList = new int[nodeCounter][nodeCounter];

        //initialize all weights to max value
        for (int [] list : adjacencyList) Arrays.fill(list, Integer.MAX_VALUE);

        //fill up the adjacency list with the processed nodes
        for (ArrayList<Integer> edge : edgeList){
            int src = edge.get(0);
            int dest = edge.get(1);
            int weight = edge.get(2);

            adjacencyList[src][dest] = Math.min(adjacencyList[src][dest], weight);
        }

        return adjacencyList;
    }

    /*  calls the appropriate function based on the input
     *
     *  @param input - A string representing the command along with the nodes and other relevant data
     *  @param outputNo - the Output Number represents the current output we are on
     *  @param adjacencyList - A 2d array representing the graph
     *  @param nodeMapping - A hashmap used to translate between String inputs and integer indices on the array
     *  @return - The distance represented by an integer. If no route exists, returns -1
     */
    public static void handleCommand(String input, int outputNo, int [][] adjacencyList,
                                     HashMap<String, Integer> nodeMapping){

        if (input == null || input.isEmpty()){
            System.out.println("Output #" + outputNo + ": Invalid input. Please double check the input.");
            return;
        }

        String [] commandList = input.split(" ");

        if (commandList.length < 2){
            System.out.println("Output #" + outputNo + ": Invalid input. Please double check the input.");
            return;
        }

        int ans = -1;
        int src = 0;
        int dest = 0;
        int stops = 0;

        switch(commandList[0]){
            case "route":
                ans = routeSolver(adjacencyList, nodeMapping, commandList[1]);
                break;
            case "maxStops":
                if (commandList.length < 4){
                    System.out.println("Output #" + outputNo + ": Invalid input. Please double check the input.");
                    return;
                }
                src = nodeMapping.getOrDefault(commandList[1], -1);
                dest = nodeMapping.getOrDefault(commandList[2], -1);

                if (!isInteger(commandList[3])){
                    System.out.println("Output #" + outputNo + ": Invalid input. Please double check the integer.");
                    return;
                }

                stops = Integer.parseInt(commandList[3]);
                ans = routeCounterStops(adjacencyList, src, dest, stops,false);
                break;
            case "exactStops":
                if (commandList.length < 4){
                    System.out.println("Output #" + outputNo + ": Invalid input. Please double check the input.");
                    return;
                }
                src = nodeMapping.getOrDefault(commandList[1], -1);
                dest = nodeMapping.getOrDefault(commandList[2], -1);

                if (!isInteger(commandList[3])){
                    System.out.println("Output #" + outputNo + ": Invalid input. Please double check the integer.");
                    return;
                }

                stops = Integer.parseInt(commandList[3]);
                ans = routeCounterStops(adjacencyList, src, dest, stops, true);
                break;
            case "shortest":
                if (commandList.length < 3){
                    System.out.println("Output #" + outputNo + ": Invalid input. Please double check the input.");
                    return;
                }
                src = nodeMapping.getOrDefault(commandList[1], -1);
                dest = nodeMapping.getOrDefault(commandList[2], -1);
                ans = shortestRoute(adjacencyList, src, dest);
                break;
            case "maxDistance":
                if (commandList.length < 4){
                    System.out.println("Output #" + outputNo + ": Invalid input. Please double check the input.");
                    return;
                }
                src = nodeMapping.getOrDefault(commandList[1], -1);
                dest = nodeMapping.getOrDefault(commandList[2], -1);

                if (!isInteger(commandList[3])){
                    System.out.println("Output #" + outputNo + ": Invalid input. Please double check the integer.");
                    return;
                }

                int maxDist = Integer.parseInt(commandList[3]);

                if (src >= 0 && dest >= 0) {
                    ans = routeCounterDistance(adjacencyList, src, dest, 0,
                            maxDist, new HashSet<>(), false);
                }

                if (ans == 0) ans = -1;

                break;
            default:
                System.out.println("Output #" + outputNo + ": Invalid input. Please double check the input command.");
                return;
        }

        if (ans == -1){
            System.out.println("Output #" + outputNo + ": NO SUCH ROUTE");
        }else {
            System.out.println("Output #" + outputNo + ": " + ans);
        }

    }

    /*  computes the distance of the given route
     *
     *  @param adjacencyList - A 2d array representing the graph
     *  @param nodeMapping - A hashmap used to translate between String inputs and integer indices on the array
     *  @param route - A string representing the route to compute the distance on
     *  @return - The distance represented by an integer. If no route exists, returns -1
     */
    public static int routeSolver(int [][] adjacencyList,
                                  HashMap<String, Integer> nodeMapping, String route){

        if (route == null || route.isEmpty()) return -1;

        int distance = 0;

        String [] nodes = route.split("-");

        int src = -1;
        for (String node : nodes){

            //if src == -1, we simply initialize the node. otherwise, we compute the distance
            if (src == -1) {
                src = nodeMapping.getOrDefault(node, -1);
                if (src == -1) return -1;
            }else{

                int dest = nodeMapping.getOrDefault(node, -1);
                if (dest == -1) return -1;

                int weight = adjacencyList[src][dest];
                if (weight == Integer.MAX_VALUE) return -1;

                distance += weight;

                src = dest;
            }
        }

        return distance;
    }

    /*  counts the number of routes with a restriction on the number of stops
     *
     *  @param adjacencyList - A 2d array representing the graph
     *  @param src - The source node
     *  @param dest - The destination node
     *  @param stops - The maximum number of stops
     *  @param exact - Signifies whether or not routes below the maximum number of stops should be considered
     *  @return - The number of routes represented by an integer.
     */
    public static int routeCounterStops(int [][] adjacencyList,
                                        int src, int dest, int stops, boolean exact){

        if (src == -1 || dest == -1) return -1;

        int routes = 0;
        int distTravelled = 0;

        //traverse the graph with a bfs
        Queue<Integer> queue1 = new LinkedList<>();
        Queue<Integer> queue2 = new LinkedList<>();
        queue1.add(src);

        while(!queue1.isEmpty()){

            while(!queue1.isEmpty()){
                int curNode = queue1.poll();
                int [] neighborList = adjacencyList[curNode];

                //traverse the current node's list of neighbors
                for (int index = 0; index < neighborList.length; index++){
                    int distance = neighborList[index];
                    if (distance == Integer.MAX_VALUE) continue;

                    if (exact){
                        if (distTravelled+1 == stops && index == dest) {
                            routes++;
                        }
                    }else{
                        if (index == dest){
                            routes++;
                        }
                    }
                    queue2.add(index);
                }

            }

            distTravelled++;

            if (distTravelled == stops) break;

            while(!queue2.isEmpty()){
                int curNode = queue2.poll();
                int [] neighborList = adjacencyList[curNode];

                //traverse the current node's list of neighbors
                for (int index = 0; index < neighborList.length; index++){
                    int distance = neighborList[index];
                    if (distance == Integer.MAX_VALUE) continue;

                    if (exact){
                        if (distTravelled+1 == stops && index == dest) {
                            routes++;
                        }
                    }else{
                        if (index == dest){
                            routes++;
                        }
                    }
                    queue1.add(index);
                }
            }

            distTravelled++;

            if (distTravelled == stops) break;

        }

        return routes == 0 ? -1 : routes;
    }

    /*  computes the shortest route between two nodes with dijkstra's algorithm
     *
     *  @param adjacencyList - A 2d array representing the graph
     *  @param src - The source node
     *  @param dest - The destination node
     *  @return - The shortest distance represented by an integer. If no route exists, returns -1
     */
    public static int shortestRoute(int [][] adjacencyList, int src, int dest){

        if (src == -1 || dest == -1) return -1;

        HashSet<Integer> visited = new HashSet<>();

        int nodes = adjacencyList.length;
        int [] distances = new int[nodes];

        Arrays.fill(distances, Integer.MAX_VALUE);

        Queue<Integer> queue = new LinkedList<>();

        //we don't add the source node into the visited hashset nor do we
        //initialize the distances[src] to 0 to make it possible to loop back
        //ex. getting the distance from node B to node B
        queue.add(src);

        while (!queue.isEmpty()){

            int curNode = queue.poll();

            for (int neighbor = 0; neighbor < nodes; neighbor++){

                int edgeWeight = adjacencyList[curNode][neighbor];

                if (edgeWeight == Integer.MAX_VALUE) continue;
                if (!visited.add(neighbor)) continue;

                if (curNode != src) {
                    distances[neighbor] = Math.min(distances[neighbor], distances[curNode] + edgeWeight);
                }else{
                    distances[neighbor] = edgeWeight;
                }
                queue.add(neighbor);

            }

        }

        return distances[dest] == Integer.MAX_VALUE ? -1 : distances[dest];

    }

    /*  uses depth first search to count the number of routes with a restriction on the distance travelled
     *
     *  @param adjacencyList - A 2d array representing the graph
     *  @param curNode - The current node
     *  @param dest - The destination node
     *  @param distTravelled - The amount of distance we travelled
     *  @param maxDist - The maximum distance we can travel
     *  @param visited - The nodes we have already visited
     *  @param destReached - Destination Reached tells us if we have reached the destination before
     *  @return - The number of routes represented by an integer
     */
    public static int routeCounterDistance(int [][] adjacencyList, int curNode, int dest, int distTravelled,
                          int maxDist, HashSet<Integer> visited, boolean destReached){

        if (distTravelled >= maxDist) return 0;

        int [] neighborList = adjacencyList[curNode];
        int routes = 0;

        if (distTravelled > 0 && curNode == dest){
            routes++;
            destReached = true;
        }

        for (int index = 0; index < neighborList.length; index++){
            if (neighborList[index] == Integer.MAX_VALUE) continue;

            //don't continue on a cycle that doesn't contain the destination node
            if (visited.contains(index) && destReached == false) return 0;

            visited.add(index);
            routes += routeCounterDistance(adjacencyList, index, dest,
                    distTravelled + neighborList[index], maxDist, visited, destReached);
            visited.remove(index);

        }

        return routes;
    }

    /*  a helper function to test whether or not a string is an integer
     *
     *  @param input - A string representing the characters we want to test on
     *  @return - A boolean representing whether or not the input is an integer
     */
    public static boolean isInteger(String input){

        if (input == null || input.isEmpty() || input.length() > 10) return false;

        for (int i = 0; i < input.length(); i++){
            char curChar = input.charAt(i);
            if (curChar < '0' || curChar > '9') return false;
        }

        if (Double.parseDouble(input) > Integer.MAX_VALUE) return false;

        return true;

    }
}
