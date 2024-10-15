package com.clipsoft.cson.serializer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;




import static org.junit.Assert.*;

public class ValidationAttributeTest {

    private CSONValidation mockValidation;

    @Before
    public void setUp() {
        // Mock CSONValidation for testing
        mockValidation = Mockito.mock(CSONValidation.class);
        Mockito.when(mockValidation.min()).thenReturn(-1.0);
        Mockito.when(mockValidation.max()).thenReturn(-1.0);
    }

    @Test
    public void testOfWithNullValidation() {
        assertNull(ValidationAttribute.of(null));
    }

    @Test
    public void testOfWithValidValidation() {
        Mockito.when(mockValidation.required()).thenReturn(true);
        Mockito.when(mockValidation.pattern()).thenReturn("\\d+");
        Mockito.when(mockValidation.message()).thenReturn("Invalid input");
        Mockito.when(mockValidation.notNull()).thenReturn(true);
        Mockito.when(mockValidation.min()).thenReturn(1.0);
        Mockito.when(mockValidation.max()).thenReturn(10.0);
        Mockito.when(mockValidation.typeMatch()).thenReturn(true);

        ValidationAttribute attribute = ValidationAttribute.of(mockValidation);
        assertNotNull(attribute);
        assertEquals("Invalid input", attribute.getInvalidMessage());
    }

    @Test
    public void testIsValidWithNullAttribute() {
        assertTrue(ValidationAttribute.isValid(null, null, Types.String));
    }

    @Test
    public void testIsValidWithNotNullConstraint() {
        Mockito.when(mockValidation.notNull()).thenReturn(true);
        ValidationAttribute attribute = ValidationAttribute.of(mockValidation);

        assertFalse(ValidationAttribute.isValid(attribute, null, Types.String));
        assertTrue(ValidationAttribute.isValid(attribute, "test", Types.String));
    }

    @Test
    public void testIsValidWithPatternMismatch() {
        Mockito.when(mockValidation.pattern()).thenReturn("\\d+");
        ValidationAttribute attribute = ValidationAttribute.of(mockValidation);

        assertFalse(ValidationAttribute.isValid(attribute, "abc", Types.String));
        assertTrue(ValidationAttribute.isValid(attribute, "123", Types.String));
    }

    @Test
    public void testIsValidWithNumberInRange() {
        Mockito.when(mockValidation.min()).thenReturn(1.0);
        Mockito.when(mockValidation.max()).thenReturn(10.0);
        ValidationAttribute attribute = ValidationAttribute.of(mockValidation);

        assertFalse(ValidationAttribute.isValid(attribute, 0, Types.Integer));
        assertTrue(ValidationAttribute.isValid(attribute, 5, Types.Integer));
        assertFalse(ValidationAttribute.isValid(attribute, 11, Types.Integer));
    }

    @Test
    public void testIsValidWithCollectionSize() {
        Mockito.when(mockValidation.min()).thenReturn(1.0);
        Mockito.when(mockValidation.max()).thenReturn(3.0);
        ValidationAttribute attribute = ValidationAttribute.of(mockValidation);

        List<String> testList = Arrays.asList("a", "b");

        assertTrue(ValidationAttribute.isValid(attribute, testList, Types.Collection));
        assertFalse(ValidationAttribute.isValid(attribute, Collections.emptyList(), Types.Collection));
    }

    @Test
    public void testIsValidWithByteArray() {
        Mockito.when(mockValidation.min()).thenReturn(2.0);
        Mockito.when(mockValidation.max()).thenReturn(5.0);
        ValidationAttribute attribute = ValidationAttribute.of(mockValidation);

        byte[] validArray = new byte[]{1, 2};
        byte[] invalidArray = new byte[]{1};

        assertTrue(ValidationAttribute.isValid(attribute, validArray, Types.ByteArray));
        assertFalse(ValidationAttribute.isValid(attribute, invalidArray, Types.ByteArray));
    }

    @Test
    public void testIsValidWithBoolean() {
        Mockito.when(mockValidation.required()).thenReturn(true);
        ValidationAttribute attribute = ValidationAttribute.of(mockValidation);

        assertTrue(ValidationAttribute.isValid(attribute, true, Types.Boolean));
        assertFalse(ValidationAttribute.isValid(attribute, null, Types.Boolean));
    }

    @Test
    public void testIsValidTypeMismatch() {
        Mockito.when(mockValidation.typeMatch()).thenReturn(true);
        ValidationAttribute attribute = ValidationAttribute.of(mockValidation);

        assertFalse(ValidationAttribute.isValid(attribute, "123", Types.Integer));
        assertTrue(ValidationAttribute.isValid(attribute, 123, Types.Integer));
    }

    @Test
    public void testIsValidWithBigNumbers() {
        Mockito.when(mockValidation.min()).thenReturn(100.0);
        Mockito.when(mockValidation.max()).thenReturn(200.0);
        ValidationAttribute attribute = ValidationAttribute.of(mockValidation);

        assertTrue(ValidationAttribute.isValid(attribute, new BigInteger("150"), Types.BigInteger));
        assertFalse(ValidationAttribute.isValid(attribute, new BigDecimal("250.0"), Types.BigDecimal));
    }

    @Test
    public void testIsValidWithEmptyStringRequired() {
        Mockito.when(mockValidation.required()).thenReturn(true);
        ValidationAttribute attribute = ValidationAttribute.of(mockValidation);

        assertFalse(ValidationAttribute.isValid(attribute, "", Types.String));
        assertTrue(ValidationAttribute.isValid(attribute, "non-empty", Types.String));
    }

}
