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
        while (changed) {
            changed = false;
            for (FunctionalDependency fd : fds) {
                if (closure.containsAll(fd.lhs) && !closure.containsAll(fd.rhs)) {
                    closure.addAll(fd.rhs);
                    changed = true;
                }
            }
        }
        return closure;
    }

    // Find candidate keys by checking closures of all subsets
    static Set<Set<String>> findCandidateKeys(List<String> attributes, List<FunctionalDependency> fds) {
        Set<Set<String>> candidateKeys = new HashSet<>();
        List<String> attrs = new ArrayList<>(attributes);
        // Check subsets of increasing size
        for (int r = 1; r <= attrs.size(); r++) {
            List<List<String>> combos = combinations(attrs, r);
            for (List<String> combo : combos) {
                Set<String> subset = new HashSet<>(combo);
                Set<String> c = closure(subset, fds);
                if (c.containsAll(attributes)) {
                    // Check minimality
                    boolean minimal = true;
                    for (String att : subset) {
                        Set<String> subMinus = new HashSet<>(subset);
                        subMinus.remove(att);
                        if (!subMinus.isEmpty() && closure(subMinus, fds).containsAll(attributes)) {
                            minimal = false;
                            break;
                        }
                    }
                    if (minimal) {
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
        if (current.size() == r) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            current.add(list.get(i));
            combinationsHelper(list, r, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    static boolean isSuperkey(Set<String> X, List<String> attributes, List<FunctionalDependency> fds) {
        Set<String> c = closure(X, fds);
        return c.containsAll(attributes);
    }

    static Set<String> primeAttributes(Set<Set<String>> candidateKeys) {
        Set<String> prime = new HashSet<>();
        for (Set<String> ck : candidateKeys) {
            prime.addAll(ck);
        }
        return prime;
    }

    // Check normal forms
    // BCNF: For every FD X->Y, X must be a superkey.
    static boolean checkBCNF(List<String> attributes, List<FunctionalDependency> fds, Set<Set<String>> candidateKeys) {
        for (FunctionalDependency fd : fds) {
            if (!isSuperkey(fd.lhs, attributes, fds)) {
                return false;
            }
        }
        return true;
    }

    // 3NF: For every FD X->Y, either X is a superkey or every attribute in Y is prime
    static boolean check3NF(List<String> attributes, List<FunctionalDependency> fds, Set<Set<String>> candidateKeys) {
        Set<String> prime = primeAttributes(candidateKeys);
        for (FunctionalDependency fd : fds) {
            if (!isSuperkey(fd.lhs, attributes, fds)) {
                // Not a superkey, so check if all RHS are prime
                for (String att : fd.rhs) {
                    if (!prime.contains(att)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // 2NF: No partial dependency of a non-prime attribute on a part of any candidate key.
    // For each FD X->Y, if X is a proper subset of a candidate key and Y contains a non-prime attribute, it's a violation.
    static boolean check2NF(List<String> attributes, List<FunctionalDependency> fds, Set<Set<String>> candidateKeys) {
        Set<String> prime = primeAttributes(candidateKeys);

        for (FunctionalDependency fd : fds) {
            for (Set<String> ck : candidateKeys) {
                if (ck.size() > 1 && ck.containsAll(fd.lhs) && !fd.lhs.containsAll(ck)) {
                    // LHS is a proper subset of a candidate key.
                    // Check if RHS has non-prime attributes:
                    for (String att : fd.rhs) {
                        if (!prime.contains(att)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    static String determineNormalForm(List<String> attributes, List<FunctionalDependency> fds, Set<Set<String>> candidateKeys) {
        // Already in 1NF by default assumption
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

    // Minimal cover:
    // Steps:
    // 1. Make sure each FD has a single attribute on RHS.
    // 2. Remove extraneous attributes from LHS.
    // 3. Remove redundant FDs.
    static List<FunctionalDependency> minimalCover(List<FunctionalDependency> fds) {
        // Step 1: Decompose RHS so that each FD has a single attribute on RHS
        List<FunctionalDependency> decomposed = new ArrayList<>();
        for (FunctionalDependency fd : fds) {
            for (String rhsAttr : fd.rhs) {
                decomposed.add(new FunctionalDependency(new HashSet<>(fd.lhs), setOf(rhsAttr)));
            }
        }

        // Step 2: Remove extraneous attributes from LHS
        List<FunctionalDependency> afterExtraneousRemoval = removeExtraneousLeftAttributes(decomposed);

        // Step 3: Remove redundant FDs
        List<FunctionalDependency> minimal = removeRedundantFDs(afterExtraneousRemoval);

        return minimal;
    }

    private static List<FunctionalDependency> removeExtraneousLeftAttributes(List<FunctionalDependency> fds) {
        List<FunctionalDependency> result = new ArrayList<>(fds);
        // For each FD X -> A, if there is an attribute 'b' in X such that (X - b)+ still determines A, remove b.
        boolean changed = true;
        while (changed) {
            changed = false;
            List<FunctionalDependency> newFDs = new ArrayList<>();
            for (FunctionalDependency fd : result) {
                Set<String> X = new HashSet<>(fd.lhs);
                for (String attr : new HashSet<>(X)) {
                    Set<String> Xminus = new HashSet<>(X);
                    Xminus.remove(attr);
                    if (!Xminus.isEmpty()) {
                        // Check closure of Xminus w.r.t current FDs (excluding fd itself to avoid trivial inclusion)
                        if (closure(Xminus, result).containsAll(fd.rhs)) {
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

    private static List<FunctionalDependency> removeRedundantFDs(List<FunctionalDependency> fds) {
        List<FunctionalDependency> result = new ArrayList<>(fds);
        boolean changed = true;
        while (changed) {
            changed = false;
            // Try removing each FD and see if the closure still determines that FD's RHS
            for (int i = 0; i < result.size(); i++) {
                FunctionalDependency fd = result.get(i);
                List<FunctionalDependency> testSet = new ArrayList<>(result);
                testSet.remove(i);
                if (closure(fd.lhs, testSet).containsAll(fd.rhs)) {
                    // Redundant FD
                    result.remove(i);
                    changed = true;
                    break;
                }
            }
        }
        return result;
    }

    // Decompose to 3NF using the minimal cover:
    // 1. Compute minimal cover.
    // 2. For each FD in minimal cover X->A, create a relation with attributes X ∪ {A}.
    // 3. If any attribute not included, add it as well.
    static List<Relation> decomposeTo3NF(List<String> attributes, List<FunctionalDependency> fds) {
        List<FunctionalDependency> minimal = minimalCover(fds);

        // Initially, create one relation per FD: R(X∪Y)
        List<Relation> initialRelations = new ArrayList<>();
        for (FunctionalDependency fd : minimal) {
            Set<String> relAttrs = new HashSet<>(fd.lhs);
            relAttrs.addAll(fd.rhs);
            List<FunctionalDependency> singleFDList = new ArrayList<>();
            singleFDList.add(fd);
            initialRelations.add(new Relation(relAttrs, singleFDList));
        }

        // Try to merge relations if they share the same LHS or if one is a subset of another.
        // The goal: minimal number of relations without unnecessary attributes.
        boolean changed = true;
        while (changed) {
            changed = false;

            // Attempt merges of relations with identical LHS sets:
            // This is a more conservative merge than before. We'll merge only if
            // it doesn't introduce unnecessary attributes.
            // Instead of grouping by LHS blindly, we check if merging reduces redundancy.
            Map<Set<String>, List<Relation>> byLHS = new HashMap<>();
            for (Relation r : initialRelations) {
                // Compute minimal key for each relation or just store LHS sets of all FDs in it
                Set<String> lhsSet = new HashSet<>();
                for (FunctionalDependency fd : r.fds) {
                    lhsSet.addAll(fd.lhs);
                }
                byLHS.computeIfAbsent(lhsSet, k -> new ArrayList<>()).add(r);
            }

            // Merge only if beneficial
            for (Map.Entry<Set<String>, List<Relation>> entry : byLHS.entrySet()) {
                List<Relation> group = entry.getValue();
                if (group.size() > 1) {
                    // Merge all these relations into one
                    Set<String> mergedAttrs = new HashSet<>();
                    List<FunctionalDependency> mergedFDs = new ArrayList<>();
                    for (Relation r : group) {
                        mergedAttrs.addAll(r.attributes);
                        mergedFDs.addAll(r.fds);
                    }

                    // Replace them with a single merged relation
                    initialRelations.removeAll(group);
                    initialRelations.add(new Relation(mergedAttrs, mergedFDs));
                    changed = true;
                    break;
                }
            }

            if (!changed) {
                // Check if any relation is a subset of another and can be removed
                outer:
                for (int i = 0; i < initialRelations.size(); i++) {
                    Relation r1 = initialRelations.get(i);
                    for (int j = 0; j < initialRelations.size(); j++) {
                        if (i == j) continue;
                        Relation r2 = initialRelations.get(j);
                        // If r1 is a subset of r2 (attributes of r1 included in r2)
                        // and r2's FDs include r1's FDs, we can remove r1
                        if (r2.attributes.containsAll(r1.attributes)) {
                            // Check if all FDs in r1 are preserved by r2's attributes:
                            if (fdsPreserved(r1.fds, r2.attributes, minimal)) {
                                initialRelations.remove(i);
                                changed = true;
                                break outer;
                            }
                        }
                    }
                }
            }
        }

        return initialRelations;
    }

    // Check if all given FDs are preserved by the attributes of a given relation
    static boolean fdsPreserved(List<FunctionalDependency> fdsToCheck, Set<String> relationAttrs, List<FunctionalDependency> globalFDs) {
        // For FD X->Y to hold in relationAttrs, (X)+ within the restricted FDs that apply to relationAttrs should include Y.
        // Restrict globalFDs to those whose attributes are subset of relationAttrs:
        List<FunctionalDependency> restrictedFDs = globalFDs.stream()
                .filter(fd -> relationAttrs.containsAll(fd.lhs) && relationAttrs.containsAll(fd.rhs))
                .collect(Collectors.toList());

        for (FunctionalDependency fd : fdsToCheck) {
            Set<String> closureSet = closure(fd.lhs, restrictedFDs);
            if (!closureSet.containsAll(fd.rhs)) return false;
        }
        return true;
    }

}
