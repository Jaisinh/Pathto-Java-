import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class OptimizedFileSearchUtility {
    private static final Set<String> SKIP_DIRS = Set.of(
        "/proc", "/sys", "/dev", "/tmp", "/var/log", "/var/cache", 
        "/private/var", "/Applications", "/System"
    );
    private static final int MAX_DEPTH = 10;
    private static final int MAX_RESULTS = 1000;

    public static List<String> findFilesByName(String name, String[] searchPaths) {
        ForkJoinPool pool = new ForkJoinPool();
        List<String> matchingItems = new ArrayList<>();

        for (String path : searchPaths) {
            File rootDir = new File(path);
            List<String> pathResults = pool.invoke(
                new FileSearchTask(rootDir, name.toLowerCase(), 0, matchingItems)
            );
            
            if (matchingItems.size() >= MAX_RESULTS) break;
        }

        pool.shutdown();
        return matchingItems;
    }

    private static class FileSearchTask extends RecursiveTask<List<String>> {
        private final File dir;
        private final String searchName;
        private final int depth;
        private final List<String> matchingItems;

        FileSearchTask(File dir, String searchName, int depth, List<String> matchingItems) {
            this.dir = dir;
            this.searchName = searchName;
            this.depth = depth;
            this.matchingItems = matchingItems;
        }

        @Override
        protected List<String> compute() {
            if (depth > MAX_DEPTH || matchingItems.size() >= MAX_RESULTS) 
                return new ArrayList<>();

            if (dir == null || !dir.exists() || !dir.isDirectory()) 
                return new ArrayList<>();

            try {
                File[] files = dir.listFiles();
                if (files == null) return new ArrayList<>();

                List<FileSearchTask> subTasks = new ArrayList<>();

                for (File file : files) {
                    synchronized (matchingItems) {
                        if (matchingItems.size() >= MAX_RESULTS) break;
                    }

                    try {
                        if (file.isDirectory()) {
                            if (file.getName().startsWith(".") || 
                                SKIP_DIRS.contains(file.getAbsolutePath())) 
                                continue;

                            if (file.getName().toLowerCase().contains(searchName)) {
                                synchronized (matchingItems) {
                                    matchingItems.add(file.getAbsolutePath());
                                }
                            }

                            subTasks.add(new FileSearchTask(file, searchName, depth + 1, matchingItems));
                        } else if (file.getName().toLowerCase().contains(searchName)) {
                            synchronized (matchingItems) {
                                matchingItems.add(file.getAbsolutePath());
                            }
                        }
                    } catch (SecurityException e) {
                        // Ignore permission errors
                    }
                }

                for (FileSearchTask task : subTasks) {
                    task.fork();
                }

                for (FileSearchTask task : subTasks) {
                    task.join();
                }
            } catch (SecurityException e) {
                // Ignore directory permission errors
            }

            return new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java OptimizedFileSearchUtility <name>");
            System.exit(1);
        }

        String name = String.join(" ", args);
        String[] searchPaths = {
            System.getProperty("user.home"),
            "/",
            "/Volumes"
        };
        
        System.out.println("Searching for files/folders named '" + name + "'...");

        List<String> matchingItems = findFilesByName(name, searchPaths);

        if (!matchingItems.isEmpty()) {
            System.out.println("\nFound " + matchingItems.size() + " matches:");
            for (String item : matchingItems) {
                File file = new File(item);
                String itemType = file.isDirectory() ? "Directory" : "File";
                System.out.println(itemType + ": " + item);
            }
        } else {
            System.out.println("No items found with the specified name.");
        }
    }
}
