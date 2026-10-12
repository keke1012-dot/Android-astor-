package fr.astor.hades;

/** Plain-Java regression tests: run with javac/java, no Android runtime required. */
public final class LearningIndexRegression {
    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
    }

    public static void main(String[] args) {
        LearningIndex memory = new LearningIndex();
        check(!memory.approve("Combien font 17 x 19 ?", "323", false, 1), "no implicit learning");
        check(memory.size() == 0, "refused correction not stored");
        check(memory.approve("Combien font 17 x 19 ?", "323", true, 1), "explicit confirmation");
        check(memory.retrieve("Combien font 17 x 19 ?", 3).size() == 1, "exact retrieval");
        check(memory.retrieve("Combien font 12 x 12 ?", 3).isEmpty(), "unrelated question");
        check(memory.approve("Combien font 17 x 19 ?", "17 fois 19 = 323", true, 2), "replace");
        check(memory.size() == 1, "no duplicate");
        check(memory.retrieve("Combien font 17 x 19 ?", 3).get(0).correction.contains("323"), "latest");
        check(!memory.approve("question", " ", true, 3), "reject empty");
        check(!memory.approve("question", "correction", true, -1), "reject timestamp");
        check(memory.retrieve("question", 0).isEmpty(), "limit zero");
        memory.clear();
        check(memory.size() == 0, "session clear");
        System.out.println("Learning V1 regression: PASS");
    }
}
