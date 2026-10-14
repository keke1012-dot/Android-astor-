package fr.astor.hades;

public final class InternetPolicyTest {
    public static void main(String[] args) {
        for (String invalid : new String[]{"", " ", "x".repeat(301), "test\nsecret", "x\u007f"}) {
            try { InternetPolicy.endpoint(invalid); throw new AssertionError("Accepted invalid query"); }
            catch (IllegalArgumentException expected) { }
        }
        String endpoint = InternetPolicy.endpoint("  été & gsrsearch=secret #?  ");
        if (!endpoint.startsWith("https://fr.wikipedia.org/w/api.php?")) throw new AssertionError();
        if (!endpoint.endsWith("%C3%A9t%C3%A9+%26+gsrsearch%3Dsecret+%23%3F")) throw new AssertionError(endpoint);
        if (endpoint.contains("&gsrsearch=secret")) throw new AssertionError("Parameter injection");
        if (!InternetPolicy.query("a".repeat(300)).equals("a".repeat(300))) throw new AssertionError();
        System.out.println("PASS: Internet policy, fixed HTTPS endpoint, bounds and query encoding.");
    }
}
