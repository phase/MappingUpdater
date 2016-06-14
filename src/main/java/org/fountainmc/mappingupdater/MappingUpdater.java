package org.fountainmc.mappingupdater;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.json.JSONArray;
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
        Set<String> newMappings = new HashSet<String>();

        JSONObject oldProfile = new JSONObject(this.oldProfile);
        JSONObject newProfile = new JSONObject(this.newProfile);
        for (String oldClass : oldProfile.keySet()) {
            try {
                JSONObject oldClassObject = oldProfile.getJSONObject(oldClass);
                String oldClassHash = oldClassObject.getString("hash");
                for (String newClass : newProfile.keySet()) {
                    JSONObject newClassObject = newProfile.getJSONObject(newClass);
                    String newClassHash = newClassObject.getString("hash");
                    if (oldClassHash.equals(newClassHash)) {
                        List<String> mappings = getMappings(newClass);
                        for (final ListIterator<String> i = mappings.listIterator(); i.hasNext();) {
                            final String element = i.next();
                            i.set(element.replaceFirst(oldClass, newClass));
                        }
                        newMappings.addAll(mappings);
                        break;
                    }
                    if (oldClassObject.has("string") && newClassObject.has("string")) {
                        JSONArray oldClassStrings = oldClassObject.getJSONArray("string");
                        JSONArray newClassStrings = newClassObject.getJSONArray("string");
                        int sameStrings = 0;
                        for (int i = 0; i < oldClassStrings.length(); i++) {
                            if (newClassStrings.length() > i) {
                                if (oldClassStrings.getString(i).equals(newClassStrings.getString(i))) {
                                    sameStrings++;
                                }
                            }
                        }
                        if (sameStrings >= oldClassStrings.length()) {
                            List<String> mappings = getMappings(newClass);
                            for (final ListIterator<String> i = mappings.listIterator(); i.hasNext();) {
                                final String element = i.next();
                                i.set(element.replaceFirst(oldClass, newClass));
                            }
                            newMappings.addAll(mappings);
                            break;
                        }
                    }
                }

            }
            catch (JSONException ignored) {
                ignored.printStackTrace();
            }
        }

        List<String> sortedMappings = new ArrayList<String>();
        sortedMappings.addAll(newMappings);
        Collections.sort(sortedMappings);
        return sortedMappings;
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
