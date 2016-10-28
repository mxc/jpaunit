package za.co.jumpingbean.jpaunit.test;

import org.junit.Assert;
import org.junit.Test;
import za.co.jumpingbean.jpaunit.JpaLoader;
import za.co.jumpingbean.jpaunit.test.model.AnnotatedIdMethodEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

public class AnnotatedMethodTest {

    JpaLoader jpaLoader = new JpaLoader();

    @Test
    public void testSimpleIntegerEntity() {
        Optional<Field> optionalField = jpaLoader.getIdAnnotatedField(AnnotatedIdMethodEntity.class);
        Assert.assertFalse(optionalField.isPresent());
    }

    @Test
    public void testAnnotatedMethod() {
        Optional<Method> optionalMethod = jpaLoader.getIdAnnotatedMethod(AnnotatedIdMethodEntity.class);
        Assert.assertTrue(optionalMethod.isPresent());
    }
}
