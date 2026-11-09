package ro.cs.tao.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilitiesTest {

    /**
     * Test class for StringUtilities.
     * This class focuses on testing all public static methods in StringUtilities.
     */

    @Test
    public void testIsNullOrEmpty_withNullValue() {
        String value = null;
        boolean result = StringUtilities.isNullOrEmpty(value);
        assertTrue("Expected true for null value", result);
    }

    @Test
    public void testIsNullOrEmpty_withEmptyString() {
        String value = "";
        boolean result = StringUtilities.isNullOrEmpty(value);
        assertTrue("Expected true for empty string", result);
    }

    @Test
    public void testIsNullOrEmpty_withNonEmptyString() {
        String value = "test";
        boolean result = StringUtilities.isNullOrEmpty(value);
        assertFalse("Expected false for non-empty string", result);
    }

    @Test
    public void testToJson_withNullArray() {
        String[] array = null;
        String result = StringUtilities.toJson(array);
        assertNull("Expected null for null array", result);
    }

    @Test
    public void testToJson_withEmptyArray() {
        String[] array = {};
        String result = StringUtilities.toJson(array);
        assertEquals("Expected '[]' for empty array", "[]", result);
    }

    @Test
    public void testToJson_withValidArray() {
        String[] array = {"one", "two", "three"};
        String result = StringUtilities.toJson(array);
        assertEquals("Expected valid JSON representation", "[\"one\",\"two\",\"three\"]", result);
    }

    @Test
    public void testFromJsonArray_withNullJson() {
        String json = null;
        String[] result = StringUtilities.fromJsonArray(json);
        assertNull("Expected null for null JSON", result);
    }

    @Test
    public void testFromJsonArray_withValidJson() {
        String json = "[\"one\",\"two\",\"three\"]";
        String[] result = StringUtilities.fromJsonArray(json);
        assertArrayEquals("Expected correctly parsed array", new String[]{"\"one\"", "\"two\"", "\"three\""}, result);
    }

    @Test
    public void testFromJsonArray_withNonArrayJson() {
        String json = "test";
        String[] result = StringUtilities.fromJsonArray(json);
        assertArrayEquals("Expected single-element array", new String[]{"test"}, result);
    }

    @Test
    public void testPadLeft_withShortString() {
        String value = "test";
        String result = StringUtilities.padLeft(value, 8, " ");
        assertEquals("Expected left-padded string", "    test", result);
    }

    @Test
    public void testPadLeft_withExactLengthString() {
        String value = "test";
        String result = StringUtilities.padLeft(value, 4, " ");
        assertEquals("Expected no padding", "test", result);
    }

    @Test
    public void testPadLeft_withLongString() {
        String value = "test";
        String result = StringUtilities.padLeft(value, 2, " ");
        assertEquals("Expected original string", "test", result);
    }

    @Test
    public void testPadRight_withShortString() {
        String value = "test";
        String result = StringUtilities.padRight(value, 8, " ");
        assertEquals("Expected right-padded string", "test    ", result);
    }

    @Test
    public void testPadRight_withExactLengthString() {
        String value = "test";
        String result = StringUtilities.padRight(value, 4, " ");
        assertEquals("Expected no padding", "test", result);
    }

    @Test
    public void testPadRight_withLongString() {
        String value = "test";
        String result = StringUtilities.padRight(value, 2, " ");
        assertEquals("Expected original string", "test", result);
    }

    @Test
    public void testToFirstCaps() {
        String value = "hello";
        String result = StringUtilities.toFirstCaps(value);
        assertEquals("Expected first letter capitalized", "Hello", result);
    }

    @Test
    public void testToFirstCaps_withMixedCase() {
        String value = "hELLo";
        String result = StringUtilities.toFirstCaps(value);
        assertEquals("Expected first letter capitalized and remaining in lowercase", "Hello", result);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testToFirstCaps_withEmptyString() {
        String value = "";
        StringUtilities.toFirstCaps(value);
    }

    @Test
    public void testByteArrayToHexString_withNullArray() {
        byte[] bytes = null;
        String result = StringUtilities.byteArrayToHexString(bytes);
        assertNull("Expected null for null array", result);
    }

    @Test
    public void testByteArrayToHexString_withEmptyArray() {
        byte[] bytes = {};
        String result = StringUtilities.byteArrayToHexString(bytes);
        assertNull("Expected null for empty array", result);
    }

    @Test
    public void testByteArrayToHexString_withValidArray() {
        byte[] bytes = {10, 20, 30};
        String result = StringUtilities.byteArrayToHexString(bytes);
        assertEquals("Expected valid hex representation", "0a141e", result);
    }

    @Test
    public void testHexStringToByteArray_withValidHexString() {
        String hex = "0a141e";
        byte[] result = StringUtilities.hexStringToByteArray(hex);
        assertArrayEquals("Expected correct byte array", new byte[]{10, 20, 30}, result);
    }

    @Test
    public void testHexStringToByteArray_withOddLengthHexString() {
        String hex = "a141e";
        byte[] result = StringUtilities.hexStringToByteArray(hex);
        assertArrayEquals("Expected correct byte array with padded input", new byte[]{10, 20, 30}, result);
    }

    @Test
    public void testIsGUID_withValidGUID() {
        String guid = "123e4567-e89b-12d3-a456-426614174000";
        boolean result = StringUtilities.isGUID(guid);
        assertTrue("Expected true for valid GUID", result);
    }

    @Test
    public void testIsGUID_withInvalidGUID() {
        String guid = "invalid-guid";
        boolean result = StringUtilities.isGUID(guid);
        assertFalse("Expected false for invalid GUID", result);
    }

    @Test
    public void testCount_withValidInput() {
        String substring = "test";
        String inString = "test this test string test";
        int result = StringUtilities.count(substring, inString);
        assertEquals("Expected correct count of substrings", 3, result);
    }

    @Test
    public void testCount_withNoMatches() {
        String substring = "absent";
        String inString = "test this test string test";
        int result = StringUtilities.count(substring, inString);
        assertEquals("Expected zero count for no matches", 0, result);
    }

    @Test
    public void testCount_withNullSubstring() {
        String substring = null;
        String inString = "test string";
        int result = StringUtilities.count(substring, inString);
        assertEquals("Expected zero count for null substring", 0, result);
    }

    @Test
    public void testCount_withNullInString() {
        String substring = "test";
        String inString = null;
        int result = StringUtilities.count(substring, inString);
        assertEquals("Expected zero count for null inString", 0, result);
    }

    @Test
    public void test_incrementIfDuplicateIdendical() {
        String value = "name_3";
        String reference = "name_3";
        String result = StringUtilities.incrementIfDuplicate(value, reference);
        assertEquals("Expected duplicate value", "name_4", result);
    }

    @Test
    public void test_incrementIfDuplicateSameBase() {
        String value = "name_2";
        String reference = "name_3";
        String result = StringUtilities.incrementIfDuplicate(value, reference);
        assertEquals("Expected duplicate value", "name_4", result);
    }

    @Test
    public void test_incrementIfDuplicateNotNumbered() {
        String value = "name";
        String reference = "name_3";
        String result = StringUtilities.incrementIfDuplicate(value, reference);
        assertEquals("Expected duplicate value", "name_4", result);
    }
}