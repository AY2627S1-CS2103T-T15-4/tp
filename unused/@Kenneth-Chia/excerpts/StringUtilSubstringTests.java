//@@author Kenneth-Chia-unused
// Retired global search in v1.5 in favour of field-based searchc/searchd/searcha.
// Excerpt from StringUtilTest.java at 333692c5.
    //---------------- Tests for containsSubstringIgnoreCase --------------------------------------

    @Test
    public void containsSubstringIgnoreCase_validInputs_correctResult() {
        assertTrue(StringUtil.containsSubstringIgnoreCase("Maxwell", "Max"));
        assertTrue(StringUtil.containsSubstringIgnoreCase("Maxwell", "aXw"));
        assertTrue(StringUtil.containsSubstringIgnoreCase("Maxwell", "well"));
        assertTrue(StringUtil.containsSubstringIgnoreCase("Maxwell", "MAXWELL"));
        assertTrue(StringUtil.containsSubstringIgnoreCase("Mary Maxwell Tan", " max "));
        assertTrue(StringUtil.containsSubstringIgnoreCase("Mary Tan", "mary tan"));

        assertFalse(StringUtil.containsSubstringIgnoreCase("Buddy", "Max"));
        assertFalse(StringUtil.containsSubstringIgnoreCase("Max", "Maxwell"));
        assertFalse(StringUtil.containsSubstringIgnoreCase("", "Max"));
        assertFalse(StringUtil.containsSubstringIgnoreCase("Mary Ann Tan", "Mary Tan"));

        // Query characters are literal, not regular expressions.
        assertFalse(StringUtil.containsSubstringIgnoreCase("Maxwell", ".*"));
        assertTrue(StringUtil.containsSubstringIgnoreCase("Max.*", ".*"));
    }

    @Test
    public void containsSubstringIgnoreCase_queryWithOuterWhitespace_returnsTrue() {
        assertTrue(StringUtil.containsSubstringIgnoreCase("Maxwell", "\t Max \t"));
    }

    @Test
    public void containsSubstringIgnoreCase_internalWhitespace_matchesLiterally() {
        assertTrue(StringUtil.containsSubstringIgnoreCase("Mary  Tan", "mary  tan"));
        assertFalse(StringUtil.containsSubstringIgnoreCase("Mary  Tan", "mary tan"));
        assertFalse(StringUtil.containsSubstringIgnoreCase("Mary Tan", "mary  tan"));
    }

    @Test
    public void containsSubstringIgnoreCase_blankQuery_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                StringUtil.containsSubstringIgnoreCase("Maxwell", ""));
        assertThrows(IllegalArgumentException.class, () ->
                StringUtil.containsSubstringIgnoreCase("Maxwell", " \t\n "));
    }

    @Test
    public void containsSubstringIgnoreCase_nullText_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                StringUtil.containsSubstringIgnoreCase(null, "Max"));
    }

    @Test
    public void containsSubstringIgnoreCase_nullQuery_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                StringUtil.containsSubstringIgnoreCase("Maxwell", null));
    }
