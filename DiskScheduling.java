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
        StringBuilder result = new StringBuilder("DISK SCHEDULING RESULT\n\n@TABLE|DISK HEAD MOVEMENT|Step|Current|Next cylinder|Seek distance\n");
        StringBuilder calculations = new StringBuilder("CALCULATIONS\n");
        int step = 1;
        for (int cylinder : serviceOrder) {
            int seekDistance = Math.abs(cylinder - currentHeadPosition);
            totalMovement += seekDistance;
            result.append("@ROW|").append(step++).append('|').append(currentHeadPosition).append('|').append(cylinder).append('|').append(seekDistance).append('\n');
            calculations.append("Seek = |Current - Next| -> |").append(currentHeadPosition).append(" - ").append(cylinder).append("| = ").append(seekDistance).append('\n');
            currentHeadPosition = cylinder;
        }
        return result.append("@ENDTABLE\n\nService order: ").append(initialHeadPosition).append(" -> ")
            .append(serviceOrder.toString().replace("[", "").replace("]", ""))
            .append("\n\n").append(calculations).append("Total seek time = sum of seek distances -> ").append(totalMovement)
            .append(" cylinders\n\nTotal head movement = ").append(totalMovement).append(" cylinders").toString();
    }
}
