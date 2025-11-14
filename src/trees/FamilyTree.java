package trees;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class FamilyTree {

    private static class TreeNode {
        private String name;
        private TreeNode parent;
        private ArrayList<TreeNode> children;

        TreeNode(String name) {
            this.name = name;
            children = new ArrayList<>();
        }

        String getName() {
            return name;
        }

        void addChild(TreeNode childNode) {
            children.add(childNode);
            childNode.parent = this;
        }

        TreeNode getNodeWithName(String targetName) {
            if (name.equals(targetName))
                return this;
            for (TreeNode child : children) {
                TreeNode found = child.getNodeWithName(targetName);
                if (found != null)
                    return found;
            }
            return null;
        }

        ArrayList<TreeNode> collectAncestorsToList() {
            ArrayList<TreeNode> ancestors = new ArrayList<>();
            TreeNode current = this.parent;
            while (current != null) {
                ancestors.add(current);
                current = current.parent;
            }
            return ancestors;
        }

        public String toString() {
            return toStringWithIndent("");
        }

        private String toStringWithIndent(String indent) {
            String s = indent + name + "\n";
            indent += "  ";
            for (TreeNode childNode : children)
                s += childNode.toStringWithIndent(indent);
            return s;
        }
    }

    private TreeNode root;

    public FamilyTree() throws IOException, TreeException {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Family tree text files", "txt");
        File dirf = new File("data");
        if (!dirf.exists()) dirf = new File(".");

        JFileChooser chooser = new JFileChooser(dirf);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) System.exit(1);
        File treeFile = chooser.getSelectedFile();

        FileReader fr = new FileReader(treeFile);
        BufferedReader br = new BufferedReader(fr);
        
        String line;
        while ((line = br.readLine()) != null)
            addLine(line);
        br.close();
        fr.close();
    }

    private void addLine(String line) throws TreeException {
        int colonIndex = line.indexOf(':');
        if (colonIndex < 0)
            throw new TreeException("Missing colon in line: " + line);
        String parent = line.substring(0, colonIndex).trim();
        String childrenString = line.substring(colonIndex + 1).trim();
        String[] childrenArray = childrenString.split(",");

        TreeNode parentNode;
        if (root == null)
            parentNode = root = new TreeNode(parent);
        else {
            parentNode = root.getNodeWithName(parent);
            if (parentNode == null)
                throw new TreeException("Parent not found: " + parent);
        }

        for (String childName : childrenArray) {
            childName = childName.trim();
            if (!childName.isEmpty())
                parentNode.addChild(new TreeNode(childName));
        }
    }

    TreeNode getMostRecentCommonAncestor(String name1, String name2) throws TreeException {
        TreeNode node1 = root.getNodeWithName(name1);
        if (node1 == null)
            throw new TreeException("Node not found: " + name1);
        TreeNode node2 = root.getNodeWithName(name2);
        if (node2 == null)
            throw new TreeException("Node not found: " + name2);

        ArrayList<TreeNode> ancestorsOf1 = node1.collectAncestorsToList();
        ArrayList<TreeNode> ancestorsOf2 = node2.collectAncestorsToList();

        for (TreeNode n1 : ancestorsOf1)
            if (ancestorsOf2.contains(n1))
                return n1;

        return null;
    }

    public String toString() {
        return "Family Tree:\n\n" + root;
    }

    public static void main(String[] args) {
        try {
            FamilyTree tree = new FamilyTree();
            System.out.println("Tree:\n" + tree + "\n**************\n");
            TreeNode ancestor = tree.getMostRecentCommonAncestor("Bilbo", "Frodo");
            System.out.println("Most recent common ancestor of Bilbo and Frodo is " + ancestor.getName());
        } catch (IOException x) {
            System.out.println("IO trouble: " + x.getMessage());
        } catch (TreeException x) {
            System.out.println("Input file trouble: " + x.getMessage());
        }
    }
}