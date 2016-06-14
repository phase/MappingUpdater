package org.fountainmc.mappingupdater;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class MappingUpdater {

    private final String oldProfile;
    private final String newProfile;
    private final List<String> oldMappings;

    public MappingUpdater(String oldProfile, String newProfile, List<String> oldMappings) {
        this.oldProfile = oldProfile;
        this.newProfile = newProfile;
        this.oldMappings = oldMappings;
    }

    public List<String> getMappings(String clazz) {
        List<String> mappings = new LinkedList<String>();

        for (String mapping : oldMappings) {
            String mappingClazz = mapping.split(" ")[1];
            if (mappingClazz.contains("$")) mappingClazz = mappingClazz.split("$")[0];
            if (mappingClazz.contains("/")) mappingClazz = mappingClazz.split("/")[0];
            if (mappingClazz.equals(clazz)) {
                mappings.add(mapping);
                System.out.println(mapping);
            }
        }

        return mappings;
    }

    public List<String> generateMappings() {
        List<String> newMappings = new ArrayList<String>();

        JSONObject oldProfile = new JSONObject(this.oldProfile);
        JSONObject newProfile = new JSONObject(this.newProfile);
        for (String clazz : oldProfile.keySet()) {
            try {
                String oldClassHash = oldProfile.getJSONObject(clazz).getString("hash");
                String newClassHash = newProfile.getJSONObject(clazz).getString("hash");
                if (oldClassHash.equals(newClassHash)) {
                    newMappings.addAll(getMappings(clazz));
                }
            }
            catch (JSONException ignored) {}
        }

        Collections.sort(newMappings);
        return newMappings;
    }

    public void update(String output) {
        List<String> newMappings = generateMappings();
        try {
            Files.write(Paths.get(output), newMappings, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println(
                    "java -jar MappingUpdater.jar <old_profile.json> <new_profile.json> <old_mappings.srg> <output.srg>");
            System.exit(1);
        }

        try {
            String oldProfile = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);
            String newProfile = new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8);
            List<String> oldMappings = Files.readAllLines(Paths.get(args[2]));
            MappingUpdater updater = new MappingUpdater(oldProfile, newProfile, oldMappings);
            updater.update(args[3]);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

}
