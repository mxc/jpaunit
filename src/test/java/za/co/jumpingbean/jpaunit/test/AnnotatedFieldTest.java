package za.co.jumpingbean.jpaunit.test;

import org.junit.Assert;
import org.junit.Test;
import za.co.jumpingbean.jpaunit.JpaLoader;
import za.co.jumpingbean.jpaunit.test.model.SimpleIntegerEntity;
import za.co.jumpingbean.jpaunit.test.model.SubClassEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

public class AnnotatedFieldTest {

    JpaLoader jpaLoader = new JpaLoader();

    @Test
    public void testSimpleIntegerEntity() {
        Optional<Field> optionalField = jpaLoader.getIdAnnotatedField(SimpleIntegerEntity.class);
        Assert.assertTrue(optionalField.isPresent());
    }

    @Test
    public void testAnnotatedMethod() {
        Optional<Method> optionalMethod = jpaLoader.getIdAnnotatedMethod(SimpleIntegerEntity.class);
        Assert.assertFalse(optionalMethod.isPresent());
    }

    @Test
    public void testSubClassEntity() {
        Optional<Field> optionalField = jpaLoader.getIdAnnotatedField(SubClassEntity.class);
        Assert.assertTrue(optionalField.isPresent());
    }
}
