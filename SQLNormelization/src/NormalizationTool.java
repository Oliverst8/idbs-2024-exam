import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class NormalizationTool {

    public static void main(String[] args) throws IOException, UnsupportedFlavorException {
        try {

            // Example:
            // Relation: (A, B, C, D, E)
            // FDs:
            // AB -> C
            // C -> D
            // E -> D
            // D -> A

            // In a real application, read from input.
//        List<String> attributes = Arrays.asList("W", "X", "Y", "Z", "V");
//        List<FunctionalDependency> fds = new ArrayList<>();
//        fds.add(new FunctionalDependency(setOf("W","X"), setOf("Y","Z","V")));
//        fds.add(new FunctionalDependency(setOf("Z"), setOf("X","Y")));
//        fds.add(new FunctionalDependency(setOf("Y"), setOf("V")));

            List<String> attributes = new ArrayList<>();
            List<FunctionalDependency> fds = new ArrayList<>();
            StringBuilder output = new StringBuilder();

            // Get the clipboard
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            // Get the clipboard content
            Transferable content = clipboard.getContents(null);

            // Check if the clipboard contains a string
            if (content != null && content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String clipboardText = (String) content.getTransferData(DataFlavor.stringFlavor);
                List<String> lines = new ArrayList<>(Arrays.asList(clipboardText.split("\n")));

                Collections.addAll(attributes, lines.get(0).split(","));
                lines.remove(0);

                for (var line : lines) {
                    var parts = line.split("→");
                    var lhs = parts[0].trim().split(",");
                    var rhs = parts[1].trim().split(",");
                    if (lhs.length == 1 && !attributes.contains(lhs[0])) {
                        lhs = parts[0].trim().split("");
                    }
                    if (rhs.length == 1 && !attributes.contains(rhs[0])) {
                        rhs = parts[1].trim().split("");
                    }
                    fds.add(new FunctionalDependency(new HashSet<>(Arrays.asList(lhs)), new HashSet<>(Arrays.asList(rhs))));
                }
            } else {
                System.exit(0);
            }


            Set<Set<String>> candidateKeys = findCandidateKeys(attributes, fds);
            String normalForm = determineNormalForm(attributes, fds, candidateKeys);

//        System.out.println("Current normal form: " + normalForm);
//        System.out.println("Candidate keys: ");
            output.append("Was: ").append(normalForm).append("\n");
            output.append("Candidate keys: \n");
            for (Set<String> ck : candidateKeys) {
//            System.out.print(ck);
                output.append(ck);
            }
//        System.out.println();
            output.append("\n");

            if (!normalForm.equals("BCNF") && !normalForm.equals("3NF")) {
                List<Relation> decomposition = decomposeTo3NF(attributes, fds);
//            System.out.println("Decomposition to 3NF:");
                output.append("To 3NF:\n");
                for (Relation r : decomposition) {
//                System.out.println(r);
                    output.append(r).append("\n");
                }
            }
            // Set the clipboard content
            StringSelection stringSelection = new StringSelection(output.toString());
            clipboard.setContents(stringSelection, null);
        } catch (Exception e) {
            System.exit(0);
        }
    }

    // Helper to create sets
    private static Set<String> setOf(String... elems) {
        return new HashSet<>(Arrays.asList(elems));
    }

    // Data Structures
    static class FunctionalDependency {
        Set<String> lhs;
        Set<String> rhs;
        FunctionalDependency(Set<String> lhs, Set<String> rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }
        @Override
        public String toString() {
            return lhs + " -> " + rhs;
        }
    }

    static class Relation {
        Set<String> attributes;
        List<FunctionalDependency> fds;

        Relation(Set<String> attributes, List<FunctionalDependency> fds) {
            this.attributes = attributes;
            this.fds = fds;
        }

        @Override
        public String toString() {
            String atts = attributes.stream().sorted().collect(Collectors.joining(", "));
            String fdeps = fds.stream().map(FunctionalDependency::toString).collect(Collectors.joining("; "));
            return "R(" + atts + ") FDs: " + fdeps;
        }
    }

    // Compute closure of a set of attributes under FDs
    static Set<String> closure(Set<String> X, List<FunctionalDependency> fds) {
        Set<String> closure = new HashSet<>(X);
        boolean changed = true;
        while(changed) {
            changed = false;
            for(FunctionalDependency fd : fds) {
                if (closure.containsAll(fd.lhs) && !closure.containsAll(fd.rhs)) {
                    closure.addAll(fd.rhs);
                    changed = true;
                }
            }
        }
        return closure;
    }

    // Find candidate keys
    static Set<Set<String>> findCandidateKeys(List<String> attributes, List<FunctionalDependency> fds) {
        Set<Set<String>> candidateKeys = new HashSet<>();
        List<String> attrs = new ArrayList<>(attributes);
        for (int r = 1; r <= attrs.size(); r++) {
            List<List<String>> combos = combinations(attrs, r);
            for (List<String> combo : combos) {
                Set<String> subset = new HashSet<>(combo);
                Set<String> c = closure(subset, fds);
                if(c.containsAll(attributes)) {
                    // Check minimality
                    boolean minimal = true;
                    for (String att : subset) {
                        Set<String> subMinus = new HashSet<>(subset);
                        subMinus.remove(att);
                        if(!subMinus.isEmpty() && closure(subMinus, fds).containsAll(attributes)) {
                            minimal = false;
                            break;
                        }
                    }
                    if(minimal) {
                        candidateKeys.add(subset);
                    }
                }
            }
        }
        return candidateKeys;
    }

    // Generate combinations
    static List<List<String>> combinations(List<String> list, int r) {
        List<List<String>> result = new ArrayList<>();
        combinationsHelper(list, r, 0, new ArrayList<>(), result);
        return result;
    }

    static void combinationsHelper(List<String> list, int r, int start, List<String> current, List<List<String>> result) {
        if(current.size() == r) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            current.add(list.get(i));
            combinationsHelper(list, r, i + 1, current, result);
            current.remove(current.size()-1);
        }
    }

    static boolean isSuperkey(Set<String> X, List<String> attributes, List<FunctionalDependency> fds) {
        return closure(X, fds).containsAll(attributes);
    }

    static Set<String> primeAttributes(Set<Set<String>> candidateKeys) {
        Set<String> prime = new HashSet<>();
        for (Set<String> ck : candidateKeys) {
            prime.addAll(ck);
        }
        return prime;
    }

    // Check normal forms
    static boolean checkBCNF(List<String> attributes, List<FunctionalDependency> fds, Set<Set<String>> candidateKeys) {
        for (FunctionalDependency fd : fds) {
            if(!isSuperkey(fd.lhs, attributes, fds)) {
                return false;
            }
        }
        return true;
    }

    static boolean check3NF(List<String> attributes, List<FunctionalDependency> fds, Set<Set<String>> candidateKeys) {
        Set<String> prime = primeAttributes(candidateKeys);
        for (FunctionalDependency fd : fds) {
            if(!isSuperkey(fd.lhs, attributes, fds)) {
                for(String att : fd.rhs) {
                    if(!prime.contains(att)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static boolean check2NF(List<String> attributes, List<FunctionalDependency> fds, Set<Set<String>> candidateKeys) {
        Set<String> prime = primeAttributes(candidateKeys);
        for (FunctionalDependency fd : fds) {
            for (Set<String> ck : candidateKeys) {
                if(ck.size() > 1 && ck.containsAll(fd.lhs) && !fd.lhs.containsAll(ck)) {
                    // LHS is a proper subset of a candidate key
                    for (String att : fd.rhs) {
                        if(!prime.contains(att)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    static String determineNormalForm(List<String> attributes, List<FunctionalDependency> fds, Set<Set<String>> candidateKeys) {
        if (checkBCNF(attributes, fds, candidateKeys)) {
            return "BCNF";
        }
        if (check3NF(attributes, fds, candidateKeys)) {
            return "3NF";
        }
        if (check2NF(attributes, fds, candidateKeys)) {
            return "2NF";
        }
        return "1NF";
    }

    // Minimal cover
    static List<FunctionalDependency> minimalCover(List<FunctionalDependency> fds) {
        List<FunctionalDependency> decomposed = decomposeRHS(fds);
        List<FunctionalDependency> noExtraneous = removeExtraneousLeftAttributes(decomposed);
        List<FunctionalDependency> minimal = removeRedundantFDs(noExtraneous);
        return minimal;
    }

    static List<FunctionalDependency> decomposeRHS(List<FunctionalDependency> fds) {
        List<FunctionalDependency> result = new ArrayList<>();
        for (FunctionalDependency fd : fds) {
            for (String r : fd.rhs) {
                result.add(new FunctionalDependency(new HashSet<>(fd.lhs), setOf(r)));
            }
        }
        return result;
    }

    static List<FunctionalDependency> removeExtraneousLeftAttributes(List<FunctionalDependency> fds) {
        List<FunctionalDependency> result = new ArrayList<>(fds);
        boolean changed = true;
        while(changed) {
            changed = false;
            List<FunctionalDependency> newFDs = new ArrayList<>();
            for(FunctionalDependency fd : result) {
                Set<String> X = new HashSet<>(fd.lhs);
                for (String attr : new HashSet<>(X)) {
                    Set<String> Xminus = new HashSet<>(X);
                    Xminus.remove(attr);
                    if(!Xminus.isEmpty()) {
                        if(closure(Xminus, resultWithoutFD(result, fd)).containsAll(fd.rhs)) {
                            X.remove(attr);
                            changed = true;
                        }
                    }
                }
                newFDs.add(new FunctionalDependency(X, fd.rhs));
            }
            result = newFDs;
        }
        return result;
    }

    static List<FunctionalDependency> removeRedundantFDs(List<FunctionalDependency> fds) {
        List<FunctionalDependency> result = new ArrayList<>(fds);
        boolean changed = true;
        while(changed) {
            changed = false;
            for (int i = 0; i < result.size(); i++) {
                FunctionalDependency fd = result.get(i);
                List<FunctionalDependency> testSet = resultWithoutFD(result, fd);
                if(closure(fd.lhs, testSet).containsAll(fd.rhs)) {
                    result.remove(i);
                    changed = true;
                    break;
                }
            }
        }
        return result;
    }

    static List<FunctionalDependency> resultWithoutFD(List<FunctionalDependency> fds, FunctionalDependency fd) {
        List<FunctionalDependency> res = new ArrayList<>(fds);
        res.remove(fd);
        return res;
    }

    static List<Relation> decomposeTo3NF(List<String> attributes, List<FunctionalDependency> fds) {
        List<FunctionalDependency> minimal = minimalCover(fds);

        // Start by creating one relation per FD
        List<Relation> relations = new ArrayList<>();
        for (FunctionalDependency fd : minimal) {
            Set<String> relAttrs = new HashSet<>(fd.lhs);
            relAttrs.addAll(fd.rhs);
            relations.add(new Relation(relAttrs, new ArrayList<>(Arrays.asList(fd))));
        }

        // Remove redundant relations if any
        removeRedundantRelations(relations, minimal);

        // Now attempt merging to achieve desired minimal forms
        // We'll repeatedly try to merge pairs of relations if it doesn't break 3NF or lose dependencies.
        mergeRelationsToReduce(relations, minimal, attributes);

        return relations;
    }

    static void removeRedundantRelations(List<Relation> relations, List<FunctionalDependency> globalFDs) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < relations.size(); i++) {
                Relation r1 = relations.get(i);
                for (int j = 0; j < relations.size(); j++) {
                    if (i == j) continue;
                    Relation r2 = relations.get(j);
                    if (r2.attributes.containsAll(r1.attributes)) {
                        if (fdsPreserved(r1.fds, r2.attributes, globalFDs)) {
                            relations.remove(i);
                            changed = true;
                            break;
                        }
                    }
                }
                if (changed) break;
            }
        }
    }

    static boolean fdsPreserved(List<FunctionalDependency> fdsToCheck, Set<String> relationAttrs, List<FunctionalDependency> globalFDs) {
        List<FunctionalDependency> restrictedFDs = globalFDs.stream()
                .filter(fd -> relationAttrs.containsAll(fd.lhs) && relationAttrs.containsAll(fd.rhs))
                .collect(Collectors.toList());

        for (FunctionalDependency fd : fdsToCheck) {
            Set<String> c = closure(fd.lhs, restrictedFDs);
            if (!c.containsAll(fd.rhs)) return false;
        }
        return true;
    }

    // Attempt merging relations:
    // We try pairwise merges if:
    // - The merged set of FDs still forms a 3NF relation.
    // - No dependencies are lost.
    // This allows creating larger relations, closer to your desired output.
    static void mergeRelationsToReduce(List<Relation> relations, List<FunctionalDependency> globalFDs, List<String> allAttributes) {
        boolean changed = true;
        while (changed) {
            changed = false;
            outer:
            for (int i = 0; i < relations.size(); i++) {
                for (int j = i+1; j < relations.size(); j++) {
                    Relation r1 = relations.get(i);
                    Relation r2 = relations.get(j);
                    Relation merged = attemptMerge(r1, r2, globalFDs, allAttributes);
                    if (merged != null) {
                        // Merge successful
                        relations.remove(j);
                        relations.remove(i);
                        relations.add(merged);
                        changed = true;
                        break outer;
                    }
                }
            }
        }
    }

    // Attempt to merge two relations r1 and r2
    static Relation attemptMerge(Relation r1, Relation r2, List<FunctionalDependency> globalFDs, List<String> allAttributes) {
        Set<String> mergedAttrs = new HashSet<>(r1.attributes);
        mergedAttrs.addAll(r2.attributes);

        List<FunctionalDependency> mergedFDs = new ArrayList<>(r1.fds);
        mergedFDs.addAll(r2.fds);

        // Check if merging preserves dependencies of these two sets and remains in 3NF
        // Since we want to ensure no FD lost: all FDs in r1 and r2 must be preserved by merged
        if (!fdsPreserved(r1.fds, mergedAttrs, globalFDs)) return null;
        if (!fdsPreserved(r2.fds, mergedAttrs, globalFDs)) return null;

        // Check 3NF for the merged relation
        // We only need to check 3NF of the mergedFDs within mergedAttrs
        if (!is3NFRelation(mergedAttrs, mergedFDs, globalFDs, allAttributes)) return null;

        return new Relation(mergedAttrs, mergedFDs);
    }

    static boolean is3NFRelation(Set<String> attrs, List<FunctionalDependency> fds, List<FunctionalDependency> globalFDs, List<String> allAttributes) {
        // Candidate keys for the sub-relation might differ, but let's approximate:
        // For checking 3NF: For each FD in fds, either LHS is a superkey of attrs or RHS are prime attributes.
        // Compute candidate keys restricted to these attributes and FDs:
        List<FunctionalDependency> restrictedFDs = fds.stream()
                .filter(fd -> attrs.containsAll(fd.lhs) && attrs.containsAll(fd.rhs))
                .collect(Collectors.toList());

        Set<Set<String>> candidateKeysSub = findCandidateKeys(new ArrayList<>(attrs), restrictedFDs);
        if (candidateKeysSub.isEmpty()) {
            // If no candidate key found (odd case), treat as not 3NF
            return false;
        }

        Set<String> prime = primeAttributes(candidateKeysSub);
        for (FunctionalDependency fd : fds) {
            if (!isSuperkey(fd.lhs, new ArrayList<>(attrs), restrictedFDs)) {
                for (String att : fd.rhs) {
                    if (!prime.contains(att)) return false;
                }
            }
        }
        return true;
    }
    
}
