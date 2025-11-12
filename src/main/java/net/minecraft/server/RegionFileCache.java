package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegionFileCache {

    private static final Map<File, RegionFile> a = new HashMap<>(); // Tsunami - keep RegionFile references

    private RegionFileCache() {}

    public static synchronized RegionFile a(File file1, int i, int j) {
        File file2 = new File(file1, "region");
        File file3 = new File(file2, "r." + (i >> 5) + "." + (j >> 5) + ".mcr");

        // Tsunami - keep RegionFile references
        RegionFile regionfile = a.get(file3);
        if (regionfile != null) return regionfile;

        if (!file2.exists()) {
            file2.mkdirs();
        }

        regionfile = new RegionFile(file3);
        a.put(file3, regionfile);
        return regionfile;
    }

    public static synchronized void a() {
        // Tsunami - no-op
    }

    // Tsunami start
    static synchronized void closeRegionFiles() {
        for (RegionFile regionfile : a.values()) {
            try {
                regionfile.b();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        a.clear();
    }
    // Tsunami end

    public static int b(File file1, int i, int j) {
        RegionFile regionfile = a(file1, i, j);

        return regionfile.a();
    }

    public static DataInputStream c(File file1, int i, int j) {
        RegionFile regionfile = a(file1, i, j);

        return regionfile.a(i & 31, j & 31);
    }

    public static DataOutputStream d(File file1, int i, int j) {
        RegionFile regionfile = a(file1, i, j);

        return regionfile.b(i & 31, j & 31);
    }
}
