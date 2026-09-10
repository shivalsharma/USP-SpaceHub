package fj.ac.usp.spacehub.service;
import fj.ac.usp.spacehub.model.*;
import java.util.*;

public final class FacilityUtil {
    private FacilityUtil() {}
    private static final List<String> STANDARD_OPTIONS = List.of(
            "COMPUTER", "PROJECTOR", "WHITEBOARD", "AIR_CONDITIONING",
            "SPECIALIZED_SOFTWARE", "SMART_BOARD", "VIDEO_CONFERENCING",
            "AUDIO_SYSTEM", "DOCUMENT_CAMERA"
    );

    public static List<String> standardOptions() { return STANDARD_OPTIONS; }
    public static List<String> additionalOptions() {
        return STANDARD_OPTIONS.stream().filter(x -> !Set.of("COMPUTER", "PROJECTOR", "WHITEBOARD").contains(x)).toList();
    }

    public static String normalize(String s) {
        if (s == null) return "";
        String n = s.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        if (n.equals("COMPUTERS")) n = "COMPUTER";
        if (n.equals("PROJECTORS")) n = "PROJECTOR";
        if (n.equals("WHITEBOARDS")) n = "WHITEBOARD";
        return n;
    }

    public static Set<String> parseCsv(String csv) {
        Set<String> out = new LinkedHashSet<>();
        if (csv == null || csv.isBlank()) return out;
        for (String s : csv.split(",")) {
            String n = normalize(s);
            if (!n.isBlank()) out.add(n);
        }
        return out;
    }

    public static String toCsv(Collection<String> values) { return values == null ? "" : String.join(", ", values); }

    public static Set<String> allFacilities(Room room) {
        Set<String> s = new LinkedHashSet<>();
        if (room.getFacilities() != null) room.getFacilities().forEach(x -> s.add(normalize(x)));
        if (room.getComputerCount() > 0) s.add("COMPUTER");
        if (room.isProjectorAvailable()) s.add("PROJECTOR");
        if (room.isWhiteboardAvailable()) s.add("WHITEBOARD");
        return s;
    }

    public static Set<String> faulty(Room room) {
        Set<String> s = new LinkedHashSet<>();
        if (room.getFaultyFacilities() != null) room.getFaultyFacilities().forEach(x -> s.add(normalize(x)));
        if (room.getComputerCount() > 0 && room.getOperationalComputers() <= 0) s.add("COMPUTER");
        if (room.isProjectorAvailable() && !room.isProjectorOperational()) s.add("PROJECTOR");
        if (room.isWhiteboardAvailable() && !room.isWhiteboardOperational()) s.add("WHITEBOARD");
        return s;
    }
}