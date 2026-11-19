import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public final class DiskScheduling {
    private DiskScheduling() {}

    public static String simulate(int algorithmChoice, int directionChoice, int initialHeadPosition, int maximumCylinder, int[] requests) {
        ArrayList<Integer> serviceOrder = new ArrayList<>(), pendingRequests = new ArrayList<>();
        for (int request : requests) pendingRequests.add(request);
        int currentHeadPosition = initialHeadPosition;
        if (algorithmChoice == 0) serviceOrder.addAll(pendingRequests);
        else if (algorithmChoice == 1) while (!pendingRequests.isEmpty()) {
            final int headPosition = currentHeadPosition;
            pendingRequests.sort(Comparator.comparingInt(request -> Math.abs(request - headPosition)));
            currentHeadPosition = pendingRequests.remove(0); serviceOrder.add(currentHeadPosition);
        } else {
            Collections.sort(pendingRequests); ArrayList<Integer> higherRequests = new ArrayList<>(), lowerRequests = new ArrayList<>();
            for (int request : pendingRequests) if (request >= initialHeadPosition) higherRequests.add(request); else lowerRequests.add(request);
            if (directionChoice == 0) { serviceOrder.addAll(higherRequests); serviceOrder.add(maximumCylinder); Collections.reverse(lowerRequests); serviceOrder.addAll(lowerRequests); }
            else { Collections.reverse(lowerRequests); serviceOrder.addAll(lowerRequests); serviceOrder.add(0); serviceOrder.addAll(higherRequests); }
        }
        int totalMovement = 0; currentHeadPosition = initialHeadPosition;
        for (int cylinder : serviceOrder) { totalMovement += Math.abs(cylinder - currentHeadPosition); currentHeadPosition = cylinder; }
        StringBuilder result = new StringBuilder("DISK SCHEDULING RESULT\n\n");
        result.append("CALCULATION WALKTHROUGH\n");
        int previous = initialHeadPosition;
        for (int cylinder : serviceOrder) {
            result.append("Move ").append(Math.abs(cylinder - previous)).append(" cylinders: |").append(cylinder).append(" - ").append(previous).append("|.\n");
            previous = cylinder;
        }
        result.append("\nService order: ").append(initialHeadPosition).append(" -> ")
            .append(serviceOrder.toString().replace("[", "").replace("]", ""))
            .append("\nTotal head movement = ").append(totalMovement).append(" cylinders");
        return result.toString();
    }
}
