package com.linkbyte.everscript;

import java.io.*;
import java.util.*;

public class LibraryLoader {
    private final List<String> libraries = new ArrayList<>();

    LibraryLoader() {
        libraries.add("natives/Error.evs");
        libraries.add("natives/List.evs");
        libraries.add("natives/Primitives.evs");
    }

    public void loadClasses() throws IOException {
        int start = Commons.clock();
        System.out.println("Info: Attempting to load " + libraries.size() + " libraries...");
        String line;
        StringBuilder source = new StringBuilder();

        for (String library : libraries) {
            InputStreamReader reader = new InputStreamReader(EverScript.class.getResourceAsStream(library));
            BufferedReader br = new BufferedReader(reader);
            while ((line = br.readLine()) != null) {
                source.append(line).append("\n");
            }
            EverScript.run(library, source.toString());
            source = new StringBuilder();
        }
        int end = Commons.clock() - start;
        System.out.println("Info: Loaded " + libraries.size() + " libraries in " + end + "ms.");
    }

    public int size() {
        return libraries.size();
    }

    public boolean addLibrary(String name) {
        return libraries.add(name);
    }

    public String getLibraryPath(String name) {
        return libraries.get(libraries.indexOf(name));
    }

    public String getLibrarySource(String name) throws IOException {
        String line;
        StringBuilder source = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(EverScript.class.getResourceAsStream(name));
        BufferedReader br = new BufferedReader(reader);
        while ((line = br.readLine()) != null) {
            source.append(line).append("\n");
        }
        return source.toString();
    }
}
