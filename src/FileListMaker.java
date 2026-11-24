import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileListMaker
{
    // program state
    private static Scanner in = new Scanner(System.in);
    private static ArrayList<String> list = new ArrayList<>();
    private static boolean needsToBeSaved = false;   // the "dirty" flag
    private static String currentFileName = "";      // empty until a file is loaded or saved

    public static void main(String[] args)
    {
        boolean done = false;

        do
        {
            displayList();
            String choice = SafeInput.getRegExString(in,
                    "Enter A, D, I, M, V, O, S, C, or Q", "[AaDdIiMmVvOoSsCcQq]").toUpperCase();

            try
            {
                switch (choice)
                {
                    case "A": addItem(); break;
                    case "D": deleteItem(); break;
                    case "I": insertItem(); break;
                    case "M": moveItem(); break;
                    case "V": viewList(); break;
                    case "O": openFile(); break;
                    case "S": saveCurrent(); break;
                    case "C": clearList(); break;
                    case "Q": done = quit(); break;
                }
            }
            catch (NoSuchFileException e)
            {
                System.out.println("That file was not found: " + e.getMessage());
            }
            catch (IOException e)
            {
                System.out.println("File error: " + e.getMessage());
            }
        } while (!done);

        System.out.println("Goodbye!");
    }

    // ---------- display helpers ----------

    private static void displayList()
    {
        System.out.println("\n--- Current List ---");
        if (list.size() == 0)
        {
            System.out.println("(list is empty)");
        }
        else
        {
            for (int i = 0; i < list.size(); i++)
            {
                System.out.println((i + 1) + ". " + list.get(i));
            }
        }
        System.out.println("--------------------");
        String fileInfo = currentFileName.isEmpty() ? "(none)" : currentFileName;
        System.out.println("File: " + fileInfo + (needsToBeSaved ? "   [UNSAVED CHANGES]" : ""));
        System.out.println("A-Add  D-Delete  I-Insert  M-Move  V-View  O-Open  S-Save  C-Clear  Q-Quit");
    }

    private static void viewList()
    {
        System.out.println("\nThe list contains " + list.size() + " item(s):");
        for (String s : list)
        {
            System.out.println(s);
        }
    }

    // ---------- editing commands (these set the dirty flag) ----------

    private static void addItem()
    {
        String item = SafeInput.getNonZeroLenString(in, "Enter the item to add");
        list.add(item);
        needsToBeSaved = true;
    }

    private static void deleteItem()
    {
        if (list.size() == 0)
        {
            System.out.println("The list is empty, nothing to delete.");
            return;
        }
        int num = SafeInput.getRangedInt(in, "Enter the item number to delete", 1, list.size());
        String removed = list.remove(num - 1);
        System.out.println("Deleted: " + removed);
        needsToBeSaved = true;
    }

    private static void insertItem()
    {
        int num = SafeInput.getRangedInt(in, "Enter the position to insert at", 1, list.size() + 1);
        String item = SafeInput.getNonZeroLenString(in, "Enter the item to insert");
        list.add(num - 1, item);
        needsToBeSaved = true;
    }

    private static void moveItem()
    {
        if (list.size() < 2)
        {
            System.out.println("You need at least 2 items to move one.");
            return;
        }
        int from = SafeInput.getRangedInt(in, "Enter the number of the item to move", 1, list.size());
        int to = SafeInput.getRangedInt(in, "Enter the new position for it", 1, list.size());
        String item = list.remove(from - 1);
        list.add(to - 1, item);
        needsToBeSaved = true;
    }

    private static void clearList()
    {
        if (list.size() == 0)
        {
            System.out.println("The list is already empty.");
            return;
        }
        if (SafeInput.getYNConfirm(in, "Clear the entire list?"))
        {
            list.clear();
            needsToBeSaved = true;
        }
    }

    // ---------- file commands (throw IOException up to main) ----------

    private static void openFile() throws IOException
    {
        if (needsToBeSaved)
        {
            System.out.println("\nYou have unsaved changes in the current list.");
            if (SafeInput.getYNConfirm(in, "Save the current list before loading another?"))
            {
                saveCurrent();
            }
            else if (!SafeInput.getYNConfirm(in, "Discard the unsaved changes and continue loading?"))
            {
                System.out.println("Load canceled.");
                return;
            }
        }
        String name = getFileName();
        loadFile(name);
    }

    private static void saveCurrent() throws IOException
    {
        if (currentFileName.isEmpty())
        {
            currentFileName = getFileName();   // new list: ask for a base name
        }
        saveFile(currentFileName);
    }

    private static boolean quit() throws IOException
    {
        if (!SafeInput.getYNConfirm(in, "Are you sure you want to quit?"))
        {
            return false;
        }
        if (needsToBeSaved)
        {
            System.out.println("\nYou have an unsaved list.");
            if (SafeInput.getYNConfirm(in, "Save it before quitting?"))
            {
                saveCurrent();
            }
            else
            {
                System.out.println("Unsaved changes abandoned.");
            }
        }
        return true;
    }

    // ---------- common static file methods ----------

    private static String getFileName()
    {
        String name = SafeInput.getNonZeroLenString(in, "Enter the file base name (no extension)");
        if (!name.toLowerCase().endsWith(".txt"))
        {
            name = name + ".txt";
        }
        return name;
    }

    private static void saveFile(String name) throws IOException
    {
        Path file = Paths.get(System.getProperty("user.dir"), "src", name);
        Files.write(file, list);
        currentFileName = name;
        needsToBeSaved = false;
        System.out.println("Saved " + list.size() + " item(s) to " + name);
    }

    private static void loadFile(String name) throws IOException
    {
        Path file = Paths.get(System.getProperty("user.dir"), "src", name);
        List<String> lines = Files.readAllLines(file);   // throws if the file is missing
        list.clear();
        list.addAll(lines);
        currentFileName = name;
        needsToBeSaved = false;
        System.out.println("Loaded " + list.size() + " item(s) from " + name);
    }
}