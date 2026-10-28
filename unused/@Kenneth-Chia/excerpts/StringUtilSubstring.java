//@@author Kenneth-Chia-unused
// Retired global search in v1.5 in favour of field-based searchc/searchd/searcha.
// Excerpt from StringUtil.java at 333692c5.
    /**
     * Returns true if {@code text} contains {@code query}, ignoring case using locale-independent lowercasing.
     * Leading and trailing whitespace in the query is ignored; internal whitespace is matched literally.
     *
     * @throws NullPointerException if either argument is null.
     * @throws IllegalArgumentException if the query is blank.
     */
    public static boolean containsSubstringIgnoreCase(String text, String query) {
        requireNonNull(text);
        requireNonNull(query);

        String trimmedQuery = query.strip();
        checkArgument(!trimmedQuery.isEmpty(), "Query cannot be blank");

        return text.toLowerCase(Locale.ROOT)
                .contains(trimmedQuery.toLowerCase(Locale.ROOT));
    }
