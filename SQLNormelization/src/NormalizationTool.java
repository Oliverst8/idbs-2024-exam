import java.util.*;
import java.util.stream.Collectors;

public class NormalizationTool {

    public static void main(String[] args) {
        // Example:
        // Relation: (A, B, C, D, E)
        // FDs:
        // AB -> C
        // C -> D
        // E -> D
        // D -> A

        // In a real application, read from input.
        List<String> attributes = Arrays.asList("W", "X", "Y", "Z", "V");
        List<FunctionalDependency> fds = new ArrayList<>();
        fds.add(new FunctionalDependency(setOf("W","X"), setOf("Y","Z","V")));
        fds.add(new FunctionalDependency(setOf("Z"), setOf("X","Y")));
        fds.add(new FunctionalDependency(setOf("Y"), setOf("V")));

        Set<Set<String>> candidateKeys = findCandidateKeys(attributes, fds);
        String normalForm = determineNormalForm(attributes, fds, candidateKeys);

        System.out.println("Current normal form: " + normalForm);

        if (!normalForm.equals("BCNF") && !normalForm.equals("3NF")) {
            List<Relation> decomposition = decomposeTo3NF(attributes, fds);
            System.out.println("Decomposition to 3NF:");
            for (Relation r : decomposition) {
                System.out.println(r);
            }
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
            return "R(" + atts + ") with FDs: " + fdeps;
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

        // Group by left hand side to form relations
        Map<Set<String>, Set<String>> map = new HashMap<>();
        for (FunctionalDependency fd : minimal) {
            map.putIfAbsent(fd.lhs, new HashSet<>());
            map.get(fd.lhs).addAll(fd.rhs);
        }

        Set<String> used = new HashSet<>();
        List<Relation> relations = new ArrayList<>();

        for (Map.Entry<Set<String>, Set<String>> entry : map.entrySet()) {
            Set<String> relAttrs = new HashSet<>(entry.getKey());
            relAttrs.addAll(entry.getValue());

            // Find FDs that hold fully within this set of attributes
            List<FunctionalDependency> relFDs = minimal.stream()
                    .filter(mfd -> relAttrs.containsAll(mfd.lhs) && relAttrs.containsAll(mfd.rhs))
                    .collect(Collectors.toList());

            relations.add(new Relation(relAttrs, relFDs));
            used.addAll(relAttrs);
        }

        // If there are attributes not covered, add them as a separate relation
        Set<String> leftover = new HashSet<>(attributes);
        leftover.removeAll(used);
        if (!leftover.isEmpty()) {
            relations.add(new Relation(leftover, new ArrayList<>()));
        }

        return relations;
    }

}
